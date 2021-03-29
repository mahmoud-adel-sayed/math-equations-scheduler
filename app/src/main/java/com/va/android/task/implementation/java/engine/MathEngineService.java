package com.va.android.task.implementation.java.engine;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.va.android.task.BuildConfig;
import com.va.android.task.R;
import com.va.android.task.implementation.java.App;
import com.va.android.task.implementation.java.engine.data.model.MathAnswer;
import com.va.android.task.implementation.java.engine.data.model.MathQuestion;
import com.va.android.task.implementation.java.engine.data.model.Operation;
import com.va.android.task.implementation.java.util.SimpleCountingIdlingResource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

/**
 * A background service that schedules tasks to answer math questions.
 */
public class MathEngineService extends Service {
    private static final String ARITHMETIC_WORK_TAG = "ARITHMETIC_WORK_TAG";
    private static final int NOTIFICATION_ID = 1;

    private static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    private static final String ACTION_CALCULATE = PACKAGE_NAME + ".engine.action.CALCULATE";
    private static final String ACTION_RESULT = PACKAGE_NAME + ".engine.action.RESULT";

    @VisibleForTesting
    static final String ACTION_CANCEL_ALL = PACKAGE_NAME + ".engine.action.CANCEL_ALL";

    private static final String KEY_MATH_QUESTION = "KEY_MATH_QUESTION";
    private static final String KEY_OPERATION_ID = "KEY_OPERATION_ID";
    private static final String KEY_RESULT = "KEY_RESULT";

    private final IBinder mBinder = new LocalBinder();
    private final List<Listener> mListeners = new ArrayList<>();

    private WorkManager mWorkManager;
    private Handler mMainThreadHandler;
    private List<Operation> mPendingOperations;
    private List<MathAnswer> mResults;

    private NotificationActionsReceiver mNotificationActionsReceiver;
    private NotificationCompat.Builder mNotificationBuilder;

    @Nullable
    private SimpleCountingIdlingResource mIdlingResource;

    interface Listener {
        void onResultsChanged();
        void onPendingOperationsChanged();
        default void onNotificationActionCancelAllClick() { }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mWorkManager = WorkManager.getInstance(getApplicationContext());
        mMainThreadHandler = new Handler(Looper.getMainLooper());
        mPendingOperations = new ArrayList<>();
        mResults = new ArrayList<>();
        mIdlingResource = ((App)getApplication()).getIdlingResource();

        mNotificationActionsReceiver = new NotificationActionsReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CANCEL_ALL);
        registerReceiver(mNotificationActionsReceiver, filter);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                (int) System.currentTimeMillis(),
                new Intent(getApplicationContext(), ((App)getApplication()).getMainEntryPoint()),
                0
        );

        String channelId = getString(R.string.channel_engine_id);
        mNotificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(getString(R.string.label_math_engine_service))
                .setContentText(getString(R.string.engine_waiting))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_va)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(createCancelAllAction());

        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_CALCULATE.equals(intent.getAction())) {
            MathQuestion mathQuestion = intent.getParcelableExtra(KEY_MATH_QUESTION);
            handleMathQuestion(mathQuestion);
        }
        else if (intent != null && ACTION_RESULT.equals(intent.getAction())) {
            String operationId = intent.getStringExtra(KEY_OPERATION_ID);
            String result = intent.getStringExtra(KEY_RESULT);
            handleResult(operationId, result);
        }
        else {
            startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
        }
        // If killed, restart
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mNotificationActionsReceiver);
        mWorkManager.cancelAllWorkByTag(ARITHMETIC_WORK_TAG);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    class LocalBinder extends Binder {
        MathEngineService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MathEngineService.this;
        }
    }

    static void start(@NonNull Context c) {
        ContextCompat.startForegroundService(c, new Intent(c, MathEngineService.class));
    }

    static void calculate(@NonNull Context c, @NonNull MathQuestion mathQuestion) {
        ContextCompat.startForegroundService(c, createIntent(c, mathQuestion));
    }

    @VisibleForTesting
    static Intent createIntent(@NonNull Context c, @NonNull MathQuestion mathQuestion) {
        Intent intent = new Intent(c, MathEngineService.class);
        intent.setAction(ACTION_CALCULATE);
        intent.putExtra(KEY_MATH_QUESTION, mathQuestion);
        return intent;
    }

    static void showResult(@NonNull Context c, String operationId, String result) {
        Intent intent = new Intent(c, MathEngineService.class);
        intent.setAction(ACTION_RESULT);
        intent.putExtra(KEY_OPERATION_ID, operationId);
        intent.putExtra(KEY_RESULT, result);
        ContextCompat.startForegroundService(c, intent);
    }

    void addListener(@NonNull Listener listener) {
        mListeners.add(listener);
    }

    void removeListener(@NonNull Listener listener) {
        mListeners.remove(listener);
    }

    @NonNull
    List<Operation> getPendingOperations() {
        return new ArrayList<>(mPendingOperations);
    }

    @NonNull
    List<MathAnswer> getOperationsResults() {
        return new ArrayList<>(mResults);
    }

    @VisibleForTesting
    List<Operation> getPending() {
        return mPendingOperations;
    }

    @VisibleForTesting
    List<MathAnswer> getResults() {
        return mResults;
    }

    private void handleMathQuestion(@NonNull MathQuestion mathQuestion) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (mathQuestion.getDelayTime() * 1000);
        Operation operation = new Operation(startTime, endTime, mathQuestion);

        mPendingOperations.add(operation);
        updateNotificationContent();
        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
        for (Listener listener : mListeners) {
            listener.onPendingOperationsChanged();
        }
        if (mIdlingResource != null) {
            mIdlingResource.increment();
        }
        // Enqueue work
        WorkRequest workRequest = new OneTimeWorkRequest.Builder(ArithmeticWorker.class)
                .setInputData(ArithmeticWorker.getWorkInputData(operation.getId(), mathQuestion))
                .setInitialDelay(mathQuestion.getDelayTime(), TimeUnit.SECONDS)
                .addTag(ARITHMETIC_WORK_TAG)
                .build();
        mWorkManager.enqueue(workRequest);
    }

    private void handleResult(String operationId, String result) {
        Operation operation = findOperation(operationId);
        if (operation != null) {
            mResults.add(new MathAnswer(result));
            mPendingOperations.remove(operation);
            notifyAndUpdateNotification();
        }
    }

    @Nullable
    private Operation findOperation(String operationId) {
        for (Operation operation : mPendingOperations) {
            if (operation.getId().equals(operationId))
                return operation;
        }
        return null;
    }

    private void updateNotificationContent() {
        String content = getString(
                R.string.format_pending_finished_operations, mPendingOperations.size(), mResults.size()
        );
        mNotificationBuilder
                .setContentText(content)
                .setTicker(getString(R.string.label_scheduling));
    }

    private NotificationCompat.Action createCancelAllAction() {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_ID,
                new Intent(ACTION_CANCEL_ALL), PendingIntent.FLAG_UPDATE_CURRENT);

        String actionLabel = getString(R.string.cancel_all);
        return new NotificationCompat.Action.Builder(0, actionLabel, pendingIntent).build();
    }

    private void notifyAndUpdateNotification() {
        mMainThreadHandler.post(() -> {
            for (Listener listener : mListeners) {
                listener.onPendingOperationsChanged();
                listener.onResultsChanged();
            }
            updateNotificationContent();
            startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
            if (mIdlingResource != null) {
                mIdlingResource.decrement();
            }
        });
    }

    private final class NotificationActionsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_CANCEL_ALL.equals(action)) {
                for (Listener listener : mListeners) {
                    listener.onNotificationActionCancelAllClick();
                }
                stopForeground(true);
                mWorkManager.cancelAllWorkByTag(ARITHMETIC_WORK_TAG);
                mPendingOperations.clear();
                stopSelf();
            }
        }
    }
}
