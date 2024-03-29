package com.example.android.scheduler.implementation.kotlin.engine

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.android.scheduler.BuildConfig
import com.example.android.scheduler.R
import com.example.android.scheduler.implementation.kotlin.App
import com.example.android.scheduler.implementation.kotlin.MainActivity
import com.example.android.scheduler.implementation.kotlin.engine.ArithmeticWorker.Companion.getWorkInputData
import com.example.android.scheduler.implementation.kotlin.engine.data.MathAnswer
import com.example.android.scheduler.implementation.kotlin.engine.data.MathQuestion
import com.example.android.scheduler.implementation.kotlin.engine.data.Operation
import com.example.android.scheduler.implementation.kotlin.util.SimpleCountingIdlingResource
import java.util.concurrent.TimeUnit

/**
 * A background service that schedules tasks to answer math questions.
 */
class MathEngineService : Service() {

    private val binder: IBinder = LocalBinder()
    private val listeners: MutableList<Listener> = ArrayList()

    private lateinit var workManager: WorkManager
    private lateinit var mainThreadHandler: Handler

    @VisibleForTesting
    internal lateinit var pending: MutableList<Operation>

    @VisibleForTesting
    internal lateinit var results: MutableList<MathAnswer>

    private lateinit var notificationActionsReceiver: NotificationActionsReceiver
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private var idlingResource: SimpleCountingIdlingResource? = null

    internal interface Listener {
        fun onResultsChanged()
        fun onPendingOperationsChanged()
        fun onNotificationActionCancelAllClick() { }
    }

    override fun onCreate() {
        super.onCreate()

        workManager = WorkManager.getInstance(applicationContext)
        mainThreadHandler = Handler(Looper.getMainLooper())
        pending = ArrayList()
        results = ArrayList()
        idlingResource = (application as App).getIdlingResource()

        notificationActionsReceiver = NotificationActionsReceiver()
        val filter = IntentFilter().apply { addAction(ACTION_CANCEL_ALL) }
        registerReceiver(notificationActionsReceiver, filter)

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            System.currentTimeMillis().toInt(),
            Intent(applicationContext, MainActivity::class.java),
            0
        )

        val channelId = getString(R.string.channel_engine_id)
        notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.label_math_engine_service))
            .setContentText(getString(R.string.engine_waiting))
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_schedule)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(createCancelAllAction())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && ACTION_CALCULATE == intent.action) {
            val mathQuestion: MathQuestion? = intent.getParcelableExtra(KEY_MATH_QUESTION)
            handleMathQuestion(mathQuestion!!)
        }
        else if (intent != null && ACTION_RESULT == intent.action) {
            val operationId = intent.getStringExtra(KEY_OPERATION_ID)
            val result = intent.getStringExtra(KEY_RESULT)
            handleResult(operationId!!, result!!)
        }
        else {
            startForeground(NOTIFICATION_ID, notificationBuilder.build())
        }
        // If killed, restart
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(notificationActionsReceiver)
        workManager.cancelAllWorkByTag(ARITHMETIC_WORK_TAG)
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder = binder

    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    internal inner class LocalBinder : Binder() {
        internal fun getService(): MathEngineService {
            // Return this instance of LocalService so clients can call public methods
            return this@MathEngineService
        }
    }

    internal fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    internal fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    internal val pendingOperations: List<Operation> get() = ArrayList(pending)

    internal val operationsResults: List<MathAnswer> get() = ArrayList(results)

    private fun handleMathQuestion(mathQuestion: MathQuestion) {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + (mathQuestion.delayTime * 1000)
        val operation = Operation(
            startTime = startTime,
            endTime = endTime,
            mathQuestion = mathQuestion
        )

        pending.add(operation)
        updateNotificationContent()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        for (listener in listeners) {
            listener.onPendingOperationsChanged()
        }
        idlingResource?.increment()
        // Enqueue work
        val workRequest = OneTimeWorkRequest.Builder(ArithmeticWorker::class.java)
            .setInputData(operation.getWorkInputData())
            .setInitialDelay(mathQuestion.delayTime, TimeUnit.SECONDS)
            .addTag(ARITHMETIC_WORK_TAG)
            .build()
        workManager.enqueue(workRequest)
    }

    private fun handleResult(operationId: String, result: String) {
        val operation = pending.find { it.id == operationId }
        if (operation != null) {
            results.add(MathAnswer(result))
            pending.remove(operation)
            notifyAndUpdateNotification()
        }
    }

    private fun updateNotificationContent() {
        val content = getString(
            R.string.format_pending_finished_operations, pending.size, results.size
        )
        notificationBuilder
            .setContentText(content)
            .setTicker(getString(R.string.label_scheduling))
    }

    private fun createCancelAllAction(): NotificationCompat.Action {
        val pendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_ID,
            Intent(ACTION_CANCEL_ALL), PendingIntent.FLAG_UPDATE_CURRENT)

        val actionLabel = getString(R.string.cancel_all)
        return NotificationCompat.Action.Builder(0, actionLabel, pendingIntent).build()
    }

    private fun notifyAndUpdateNotification() {
        mainThreadHandler.post {
            for (listener in listeners) {
                listener.onPendingOperationsChanged()
                listener.onResultsChanged()
            }
            updateNotificationContent()
            startForeground(NOTIFICATION_ID, notificationBuilder.build())
            idlingResource?.decrement()
        }
    }

    inner class NotificationActionsReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (ACTION_CANCEL_ALL == intent?.action) {
                for (listener in listeners) {
                    listener.onNotificationActionCancelAllClick()
                }
                stopForeground(true)
                workManager.cancelAllWorkByTag(ARITHMETIC_WORK_TAG)
                pending.clear()
                stopSelf()
            }
        }
    }

    companion object {
        private const val ARITHMETIC_WORK_TAG = "ARITHMETIC_WORK_TAG"
        private const val NOTIFICATION_ID = 1

        private const val PACKAGE_NAME = BuildConfig.APPLICATION_ID
        private const val ACTION_CALCULATE = "$PACKAGE_NAME.engine.action.CALCULATE"
        private const val ACTION_RESULT = "$PACKAGE_NAME.engine.action.RESULT"
        @VisibleForTesting internal const val ACTION_CANCEL_ALL = "$PACKAGE_NAME.engine.action.CANCEL_ALL"

        private const val KEY_MATH_QUESTION = "KEY_MATH_QUESTION"
        private const val KEY_OPERATION_ID = "KEY_OPERATION_ID"
        private const val KEY_RESULT = "KEY_RESULT"

        @JvmStatic
        internal fun start(c: Context) =
            ContextCompat.startForegroundService(c, Intent(c, MathEngineService::class.java))

        @JvmStatic
        internal fun calculate(c: Context, mathQuestion: MathQuestion) =
            ContextCompat.startForegroundService(c, createIntent(c, mathQuestion))

        @VisibleForTesting
        internal fun createIntent(c: Context, mathQuestion: MathQuestion): Intent =
            Intent(c, MathEngineService::class.java).apply {
                action = ACTION_CALCULATE
                putExtra(KEY_MATH_QUESTION, mathQuestion)
            }

        internal fun showResult(c: Context, operationId: String, result: String) {
            val intent = Intent(c, MathEngineService::class.java).apply {
                action = ACTION_RESULT
                putExtra(KEY_OPERATION_ID, operationId)
                putExtra(KEY_RESULT, result)
            }
            ContextCompat.startForegroundService(c, intent)
        }
    }
}