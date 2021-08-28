package com.example.android.scheduler

import com.example.android.scheduler.implementation.java.engine.data.model.MathQuestion
import java.util.*

fun MathQuestion.answer() = String.format(
    Locale.US,
    "%.2f %s %.2f = %.2f",
    firstOperand,
    operator.symbol(),
    secondOperand,
    operator.compute(firstOperand, secondOperand)
)