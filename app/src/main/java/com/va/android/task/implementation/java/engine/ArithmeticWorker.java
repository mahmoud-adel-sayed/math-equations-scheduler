package com.va.android.task.implementation.java.engine;

import android.content.Context;

import com.va.android.task.implementation.java.data.model.MathQuestion;
import com.va.android.task.implementation.java.data.model.Operator;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ArithmeticWorker extends Worker {
    private static final String KEY_OPERATION_ID = "KEY_OPERATION_ID";
    private static final String KEY_FIRST_OPERAND = "KEY_FIRST_OPERAND";
    private static final String KEY_SECOND_OPERAND = "KEY_SECOND_OPERAND";
    private static final String KEY_OPERATOR_ORDINAL = "KEY_OPERATOR_ORDINAL";

    static final String KEY_RESULT = "key_result";

    public ArithmeticWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Data data = getInputData();
            if (!validInputData(data)) {
                return Result.failure();
            }
            double first = data.getDouble(KEY_FIRST_OPERAND, 0);
            double second = data.getDouble(KEY_SECOND_OPERAND, 0);
            int operatorOrdinal = data.getInt(KEY_OPERATOR_ORDINAL, 0);
            Operator operator = Operator.values()[operatorOrdinal];

            String operationId = data.getString(KEY_OPERATION_ID);
            String result = String.format(Locale.US, "%.2f %s %.2f = %.2f",
                    first, operator.symbol(), second, operator.compute(first, second)
            );

            MathEngineService.showResult(getApplicationContext(), operationId, result);
            return Result.success(
                    new Data.Builder().putString(KEY_RESULT, result).build()
            );
        }
        catch (Exception e) {
            return Result.failure();
        }
    }

    @NonNull
    static Data getWorkInputData(MathQuestion mathQuestion) {
        return new Data.Builder()
                .putString(KEY_OPERATION_ID, mathQuestion.getOperationId())
                .putDouble(KEY_FIRST_OPERAND, mathQuestion.getFirstOperand())
                .putDouble(KEY_SECOND_OPERAND, mathQuestion.getSecondOperand())
                .putInt(KEY_OPERATOR_ORDINAL, mathQuestion.getOperator().ordinal())
                .build();
    }

    private static boolean validInputData(Data data) {
        return data.hasKeyWithValueOfType(KEY_FIRST_OPERAND, Double.class) &&
               data.hasKeyWithValueOfType(KEY_SECOND_OPERAND, Double.class) &&
               data.hasKeyWithValueOfType(KEY_OPERATOR_ORDINAL, Integer.class) &&
               data.getString(KEY_OPERATION_ID) != null;
    }
}