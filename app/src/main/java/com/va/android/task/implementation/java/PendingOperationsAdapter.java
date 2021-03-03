package com.va.android.task.implementation.java;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.va.android.task.R;
import com.va.android.task.implementation.java.engine.data.model.MathQuestion;

import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PendingOperationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<MathQuestion> mMathQuestions;

    public PendingOperationsAdapter(@NonNull List<MathQuestion> mathQuestions) {
        super();
        mMathQuestions = mathQuestions;
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
        MathQuestion item = mMathQuestions.get(position);
        ((ItemViewHolder)holder).equation.setText(String.format(Locale.US, "Equation: %.2f %s %.2f",
                item.getFirstOperand(), item.getOperator().symbol(), item.getSecondOperand()));
    }

    @Override
    public int getItemCount() {
        return mMathQuestions.size();
    }

    public void replaceData(@NonNull List<MathQuestion> mathQuestions) {
        mMathQuestions = mathQuestions;
        notifyDataSetChanged();
    }

    public void clearData() {
        mMathQuestions.clear();
        notifyDataSetChanged();
    }

    static final class ItemViewHolder extends RecyclerView.ViewHolder {
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.equation)
        TextView equation;

        ItemViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
