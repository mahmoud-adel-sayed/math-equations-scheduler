package com.va.android.task.implementation.java.engine;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.va.android.task.implementation.java.engine.data.model.MathAnswer;
import com.va.android.task.implementation.java.engine.data.model.MathQuestion;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public final class MathEngine implements LifecycleObserver {
    private final Context mContext;
    private final Lifecycle mLifecycle;
    private final MathEngine.Listener mListener;

    private MathEngineService mService;
    private boolean mIsBound;

    public interface Listener {
        void onConnected(@NonNull List<MathQuestion> pending, @NonNull List<MathAnswer> results);

        void onPendingOperationsChanged(@NonNull List<MathQuestion> pending);

        void onResultsChanged(@NonNull List<MathAnswer> results);

        default void onNotificationActionCancelAllClick() { }
    }

    public MathEngine(@NonNull Context context, @NonNull Lifecycle lifecycle,
                      @NonNull Listener listener) {
        mContext = context;
        mLifecycle = lifecycle;
        mListener = listener;
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
            mService.removeListener(mServiceListener);
            mContext.unbindService(mServiceConnection);
            mIsBound = false;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void removeObserver() {
        mLifecycle.removeObserver(this);
    }

    public void start() {
        MathEngineService.start(mContext);
    }

    public void calculate(@NonNull MathQuestion mathQuestion) {
        MathEngineService.calculate(mContext, mathQuestion);
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
            if (mIsBound) mListener.onPendingOperationsChanged(mService.getPendingOperations());
        }

        @Override
        public void onResultsChanged() {
            if (mIsBound) mListener.onResultsChanged(mService.getOperationsResults());
        }

        @Override
        public void onNotificationActionCancelAllClick() {
            mListener.onNotificationActionCancelAllClick();
        }
    };
}
