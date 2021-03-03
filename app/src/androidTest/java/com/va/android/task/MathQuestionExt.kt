package com.va.android.task

import com.va.android.task.implementation.java.engine.data.model.MathQuestion
import java.util.*

fun MathQuestion.answer() = String.format(
        Locale.US,
        "%.2f %s %.2f = %.2f",
        firstOperand,
        operator.symbol(),
        secondOperand,
        operator.compute(firstOperand, secondOperand)
)