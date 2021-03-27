package com.va.android.task.implementation.java.engine;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.va.android.task.implementation.java.engine.data.model.MathAnswer;
import com.va.android.task.implementation.java.engine.data.model.MathQuestion;
import com.va.android.task.implementation.java.engine.data.model.Operation;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import static com.va.android.task.implementation.java.util.Preconditions.checkNotNull;

/**
 * The entry point for the background service, call {@link #start()} method to start the service,
 * if the service is already running calling {@link #start()} will has no effect.
 * Use {@link #calculate(MathQuestion)} to evaluate simple math equations.
 */
public final class MathEngine implements LifecycleObserver {
    private final Context mContext;
    private final Lifecycle mLifecycle;
    private final MathEngine.Listener mListener;

    private MathEngineService mService;
    private boolean mIsBound;

    /**
     * Notifies clients about the operations.
     */
    public interface Listener {
        /**
         * Invoked when connected to the service.
         *
         * @param pending The pending operations
         * @param results The operations results
         */
        default void onConnected(@NonNull List<Operation> pending,
                                 @NonNull List<MathAnswer> results) {
        }

        /**
         * Invoked when the pending operations have changed.
         *
         * @param pending The pending operations
         */
        default void onPendingOperationsChanged(@NonNull List<Operation> pending) { }

        /**
         * Invoked when the operations results have changed.
         *
         * @param results The operations results
         */
        default void onResultsChanged(@NonNull List<MathAnswer> results) { }

        /**
         * Invoked when the user has cancelled all the pending operations by clicking on the
         * cancelAll action button shown in the notification.
         */
        default void onNotificationActionCancelAllClick() { }
    }

    /**
     * Constructor for the MathEngine component.
     *
     * @param context The context
     * @param lifecycle The lifecycle
     * @param listener The listener used to Notify clients about the operations
     */
    public MathEngine(@NonNull Context context, @NonNull Lifecycle lifecycle,
                      @NonNull Listener listener) {
        mContext = checkNotNull(context);
        mLifecycle = checkNotNull(lifecycle);
        mListener = checkNotNull(listener);
        lifecycle.addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void bindToService() {
        if (!mIsBound) {
            Intent bindIntent = new Intent(mContext, MathEngineService.class);
            mIsBound = mContext.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void unbindFromService() {
        if (mIsBound) {
            if (mService != null) {
                mService.removeListener(mServiceListener);
            }
            mContext.unbindService(mServiceConnection);
            mIsBound = false;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void removeObserver() {
        mLifecycle.removeObserver(this);
    }

    @VisibleForTesting
    boolean isBound() {
        return mIsBound;
    }

    /**
     * Starts the engine and wait for math questions.
     */
    public void start() {
        MathEngineService.start(mContext);
    }

    /**
     * Evaluates the math equation and deliver the result after the specified delay time.
     *
     * @param mathQuestion The mathQuestion
     */
    public void calculate(@NonNull MathQuestion mathQuestion) {
        MathEngineService.calculate(mContext, checkNotNull(mathQuestion));
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((MathEngineService.LocalBinder) service).getService();
            mService.addListener(mServiceListener);
            mListener.onConnected(mService.getPendingOperations(), mService.getOperationsResults());
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mIsBound = false;
        }
    };

    private final MathEngineService.Listener mServiceListener = new MathEngineService.Listener() {
        @Override
        public void onPendingOperationsChanged() {
            mListener.onPendingOperationsChanged(mService.getPendingOperations());
        }

        @Override
        public void onResultsChanged() {
            mListener.onResultsChanged(mService.getOperationsResults());
        }

        @Override
        public void onNotificationActionCancelAllClick() {
            mListener.onNotificationActionCancelAllClick();
        }
    };
}
