package com.va.android.task.implementation.kotlin.engine

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.va.android.task.implementation.kotlin.engine.data.MathAnswer
import com.va.android.task.implementation.kotlin.engine.data.MathQuestion
import com.va.android.task.implementation.kotlin.engine.data.Operation

/**
 * The entry point for the background service, call [start] method to start the service,
 * if the service is already running calling [start] will has no effect.
 * Use [calculate] to evaluate simple math equations.
 */
@Suppress("unused")
class MathEngine(
        private val context: Context,
        private val lifecycle: Lifecycle,
        private val listener: Listener
) : LifecycleObserver {

    private var service: MathEngineService? = null
    private var isBound = false

    init {
        lifecycle.addObserver(this)
    }

    /**
     * Notifies clients about the operations.
     */
    interface Listener {
        /**
         * Invoked when connected to the service and provides the [pending] operations & the [results].
         */
        fun onConnected(pending: List<Operation>, results: List<MathAnswer>) { }

        /**
         * Invoked when the [pending] operations have changed.
         */
        fun onPendingOperationsChanged(pending: List<Operation>) { }

        /**
         * Invoked when the operations [results] have changed.
         */
        fun onResultsChanged(results: List<MathAnswer>) { }

        /**
         * Invoked when the user has cancelled all the pending operations by clicking on the
         * cancelAll action button shown in the notification.
         */
        fun onNotificationActionCancelAllClick() { }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    internal fun bindToService() {
        if (!isBound) {
            val bindIntent = Intent(context, MathEngineService::class.java)
            isBound = context.bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    internal fun unbindFromService() {
        if (isBound) {
            service?.removeListener(serviceListener)
            context.unbindService(serviceConnection)
            isBound = false
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    internal fun removeObserver() = lifecycle.removeObserver(this)

    @VisibleForTesting
    internal fun isBound(): Boolean = isBound

    /**
     * Starts the engine and wait for math questions.
     */
    fun start() = MathEngineService.start(context)

    /**
     * Evaluates the [mathQuestion] and delivers the result after the specified delay time.
     */
    fun calculate(mathQuestion: MathQuestion) = MathEngineService.calculate(context, mathQuestion)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as MathEngineService.LocalBinder).getService()
            service!!.addListener(serviceListener)
            listener.onConnected(service!!.pendingOperations, service!!.operationsResults)
            isBound = true
        }

        override fun onServiceDisconnected(binder: ComponentName?) {
            service = null
            isBound = false
        }
    }

    private val serviceListener = object : MathEngineService.Listener {
        override fun onPendingOperationsChanged() {
            listener.onPendingOperationsChanged(service!!.pendingOperations)
        }

        override fun onResultsChanged() {
            listener.onResultsChanged(service!!.operationsResults)
        }

        override fun onNotificationActionCancelAllClick() {
            listener.onNotificationActionCancelAllClick()
        }
    }
}