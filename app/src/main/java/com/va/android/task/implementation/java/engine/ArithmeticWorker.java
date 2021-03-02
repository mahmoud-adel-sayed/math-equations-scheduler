package com.va.android.task.implementation.java.engine;

import android.content.Context;

import com.va.android.task.implementation.java.data.model.Operator;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ArithmeticWorker extends Worker {
    static final String KEY_OPERATION_ID = "KEY_OPERATION_ID";
    static final String KEY_FIRST_OPERAND = "KEY_FIRST_OPERAND";
    static final String KEY_SECOND_OPERAND = "KEY_SECOND_OPERAND";
    static final String KEY_OPERATOR_ORDINAL = "KEY_OPERATOR_ORDINAL";

    public ArithmeticWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Data data = getInputData();
            double first = data.getDouble(KEY_FIRST_OPERAND, 0);
            double second = data.getDouble(KEY_SECOND_OPERAND, 0);
            int operatorOrdinal = data.getInt(KEY_OPERATOR_ORDINAL, 0);
            Operator operator = Operator.values()[operatorOrdinal];

            String operationId = data.getString(KEY_OPERATION_ID);
            String result = String.format(Locale.US, "%.2f %s %.2f = %.2f",
                    first, operator.symbol(), second, operator.compute(first, second)
            );

            MathEngineService.showResult(getApplicationContext(), operationId, result);
            return Result.success();
        }
        catch (Exception e) {
            return Result.failure();
        }
    }
}