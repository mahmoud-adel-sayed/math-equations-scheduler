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

import com.va.android.task.R;
import com.va.android.task.implementation.java.data.model.MathAnswer;
import com.va.android.task.implementation.java.data.model.MathQuestion;
import com.va.android.task.implementation.java.data.model.Operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MathEngineService extends Service {
    private static final String PACKAGE_NAME = "com.va.android.task";
    private static final int NOTIFICATION_ID = 1;

    public static final String ACTION_CALCULATE = PACKAGE_NAME + ".engine.action.CALCULATE";
    public static final String ACTION_CANCEL_ALL = PACKAGE_NAME + ".engine.action.CANCEL_ALL";
    public static final String KEY_MATH_QUESTION = "KEY_MATH_QUESTION";

    private final IBinder mBinder = new LocalBinder();
    private final List<Listener> mListeners = new ArrayList<>();

    private ScheduledExecutorService mScheduler;
    private Handler mMainThreadHandler;
    private List<MathQuestion> mPendingTasks;
    private List<MathAnswer> mResults;

    private NotificationActionsReceiver mNotificationActionsReceiver;
    private NotificationManagerCompat mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;

    public interface Listener {
        void onResultsChanged();
        void onPendingOperationsChanged();
        default void onNotificationActionCancelAllClick() { }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mScheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        mMainThreadHandler = new Handler(Looper.getMainLooper());
        mPendingTasks = new CopyOnWriteArrayList<>();
        mResults = new CopyOnWriteArrayList<>();

        mNotificationActionsReceiver = new NotificationActionsReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CANCEL_ALL);
        registerReceiver(mNotificationActionsReceiver, filter);

        mNotificationManager = NotificationManagerCompat.from(this);
        String channelId = getString(R.string.channel_engine_id);
        mNotificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(getString(R.string.label_math_engine_service))
                .setContentText(getString(R.string.engine_waiting))
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
            calculate(mathQuestion);
        }
        // If killed, restart
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mNotificationActionsReceiver);
        mScheduler.shutdownNow();
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
    public class LocalBinder extends Binder {
        public MathEngineService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MathEngineService.this;
        }
    }

    public void addListener(@NonNull Listener listener) {
        mListeners.add(listener);
    }

    public void removeListener(@NonNull Listener listener) {
        mListeners.remove(listener);
    }

    @NonNull
    public List<MathQuestion> getPendingOperations() {
        return mPendingTasks;
    }

    @NonNull
    public List<MathAnswer> getOperationsResults() {
        return mResults;
    }

    private void calculate(@NonNull MathQuestion mathQuestion) {
        mPendingTasks.add(mathQuestion);
        updateNotificationContent();
        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
        for (Listener listener : mListeners) {
            listener.onPendingOperationsChanged();
        }
        mScheduler.schedule(new Task(mathQuestion), mathQuestion.getDelayTime(), TimeUnit.SECONDS);
    }

    private void updateNotificationContent() {
        String content = getString(
                R.string.format_pending_finished_operations, mPendingTasks.size(), mResults.size()
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
            mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
        });
    }

    private final class Task implements Runnable {
        private final MathQuestion mathQuestion;

        private Task(MathQuestion mathQuestion) {
            this.mathQuestion = mathQuestion;
        }

        @Override
        public void run() {
            double first = mathQuestion.getFirstOperand();
            double second = mathQuestion.getSecondOperand();
            Operator operator = mathQuestion.getOperator();
            String result = String.format(Locale.US, "%.2f %s %.2f = %.2f",
                    first, operator.symbol(), second, operator.compute(first, second)
            );
            mResults.add(new MathAnswer(result));
            mPendingTasks.remove(mathQuestion);
            notifyAndUpdateNotification();
        }
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
                stopSelf();
            }
        }
    }
}
