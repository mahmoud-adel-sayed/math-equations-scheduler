package com.example.android.scheduler.implementation.kotlin.engine

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.android.scheduler.implementation.kotlin.engine.data.Operation
import com.example.android.scheduler.implementation.kotlin.engine.data.Operator
import java.util.*

class ArithmeticWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val data = inputData
            if (!data.valid()) {
                Result.failure()
            }
            val first = data.getDouble(KEY_FIRST_OPERAND, 0.0)
            val second = data.getDouble(KEY_SECOND_OPERAND, 0.0)
            val operatorOrdinal = data.getInt(KEY_OPERATOR_ORDINAL, 0)
            val operator = Operator.values()[operatorOrdinal]

            val operationId = data.getString(KEY_OPERATION_ID)
            val result = String.format(Locale.US, "%.2f %s %.2f = %.2f",
                first, operator.symbol, second, operator.compute(first, second)
            )

            MathEngineService.showResult(applicationContext, operationId!!, result)
            Result.success(Data.Builder().putString(KEY_RESULT, result).build())
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun Data.valid(): Boolean {
        return hasKeyWithValueOfType(KEY_FIRST_OPERAND, Double::class.java) &&
               hasKeyWithValueOfType(KEY_SECOND_OPERAND, Double::class.java) &&
               hasKeyWithValueOfType(KEY_OPERATOR_ORDINAL, Integer::class.java) &&
               getString(KEY_OPERATION_ID) != null
    }

    companion object {
        private const val KEY_OPERATION_ID = "KEY_OPERATION_ID"
        private const val KEY_FIRST_OPERAND = "KEY_FIRST_OPERAND"
        private const val KEY_SECOND_OPERAND = "KEY_SECOND_OPERAND"
        private const val KEY_OPERATOR_ORDINAL = "KEY_OPERATOR_ORDINAL"

        internal const val KEY_RESULT = "key_result"

        internal fun Operation.getWorkInputData(): Data = Data.Builder()
            .putString(KEY_OPERATION_ID, id)
            .putDouble(KEY_FIRST_OPERAND, mathQuestion.firstOperand)
            .putDouble(KEY_SECOND_OPERAND, mathQuestion.secondOperand)
            .putInt(KEY_OPERATOR_ORDINAL, mathQuestion.operator.ordinal)
            .build()
    }
}