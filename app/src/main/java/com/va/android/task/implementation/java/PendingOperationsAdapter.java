package com.va.android.task.implementation.java;

import android.annotation.SuppressLint;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.va.android.task.R;
import com.va.android.task.implementation.java.engine.data.model.MathQuestion;
import com.va.android.task.implementation.java.engine.data.model.Operation;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.va.android.task.implementation.java.util.TimeUtil.getTimeFormatted;

public class PendingOperationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements LifecycleObserver {

    private final Map<String, CountDownTimer> mTimers;
    private List<Operation> mOperations;

    public PendingOperationsAdapter(Lifecycle lifecycle, @NonNull List<Operation> operations) {
        super();
        mTimers = new HashMap<>();
        mOperations = operations;
        lifecycle.addObserver(this);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pending_operation_list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        ItemViewHolder itemViewHolder = ((ItemViewHolder)holder);
        Operation operation = mOperations.get(position);

        if (itemViewHolder.timer != null) {
            itemViewHolder.timer.cancel();
            itemViewHolder.timer = null;
            mTimers.remove(operation.getId());
        }

        MathQuestion question = operation.getMathQuestion();
        itemViewHolder.equation.setText(String.format(Locale.US, "Equation: %.2f %s %.2f",
                question.getFirstOperand(), question.getOperator().symbol(), question.getSecondOperand())
        );

        setupTimer(itemViewHolder, operation);
    }

    @Override
    public int getItemCount() {
        return mOperations.size();
    }

    public void replaceData(@NonNull List<Operation> operations) {
        mOperations = operations;
        cancelTimers();
        mTimers.clear();
        notifyDataSetChanged();
    }

    public void clearData() {
        mOperations.clear();
        cancelTimers();
        mTimers.clear();
        notifyDataSetChanged();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void cancelTimers() {
        for (CountDownTimer timer : mTimers.values())
            timer.cancel();
    }

    private void setupTimer(ItemViewHolder holder, Operation operation) {
        long totalMillis = operation.getEndTime() - System.currentTimeMillis();
        if (totalMillis <= 0) {
            holder.remainingTime.setText(null);
            return;
        }
        holder.timer = new CountDownTimer(totalMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                millisUntilFinished -= TimeUnit.HOURS.toMillis(hours);

                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                millisUntilFinished -= TimeUnit.MINUTES.toMillis(minutes);

                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);

                // We could also use SimpleDateFormat class to parse time here
                String remainingTime = "Remaining Time: " + getTimeFormatted(hours, minutes, seconds);
                holder.remainingTime.setText(remainingTime);
            }

            @Override
            public void onFinish() {
                holder.remainingTime.setText(null);
            }
        }.start();
        mTimers.put(operation.getId(), holder.timer);
    }

    @SuppressLint("NonConstantResourceId")
    static final class ItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.equation)
        TextView equation;

        @BindView(R.id.remaining_time)
        TextView remainingTime;

        CountDownTimer timer;

        ItemViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
