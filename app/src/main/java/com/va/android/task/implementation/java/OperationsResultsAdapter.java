package com.va.android.task.implementation.java;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.va.android.task.R;
import com.va.android.task.implementation.java.data.model.MathAnswer;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class OperationsResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<MathAnswer> mMathAnswers;

    public OperationsResultsAdapter(@NonNull List<MathAnswer> mathAnswers) {
        super();
        mMathAnswers = mathAnswers;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.operation_result_list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        MathAnswer mathAnswer = mMathAnswers.get(position);
        ((ItemViewHolder)holder).result.setText(mathAnswer.getResult());
    }

    @Override
    public int getItemCount() {
        return mMathAnswers.size();
    }

    public void replaceData(@NonNull List<MathAnswer> mathAnswers) {
        mMathAnswers = mathAnswers;
        notifyDataSetChanged();
    }

    static final class ItemViewHolder extends RecyclerView.ViewHolder {
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.result)
        TextView result;

        ItemViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
