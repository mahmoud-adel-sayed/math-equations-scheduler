package com.example.android.scheduler.implementation.kotlin.engine.data

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * A model that represents an operation.
 *
 * @property id The ID that uniquely identifies the operation.
 * @property startTime The operation's start time in milliseconds.
 * @property endTime The operation's end time in milliseconds.
 * @property mathQuestion The associated [MathQuestion].
 */
data class Operation(
    val id: String = UUID.randomUUID().toString(),
    val startTime: Long,
    val endTime: Long,
    val mathQuestion: MathQuestion
) {
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Operation) return false

        return other.id == id
    }

    override fun hashCode() = 31 * id.hashCode()
}

/**
 * An Immutable model that represents a basic mathematical question.
 *
 * @property firstOperand The first operand of the math equation.
 * @property secondOperand The second operand of the math equation.
 * @property operator The operator of the math equation.
 * @property delayTime The delay time (in seconds) before receiving the result of the operation.
 */
data class MathQuestion(
    val firstOperand: Double,
    val secondOperand: Double,
    val operator: Operator,
    val delayTime: Long
) : Parcelable {

    private constructor(parcel: Parcel) : this(
        firstOperand = parcel.readDouble(),
        secondOperand = parcel.readDouble(),
        operator = Operator.values()[parcel.readInt()],
        delayTime = parcel.readLong()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeDouble(firstOperand)
        dest.writeDouble(secondOperand)
        dest.writeInt(operator.ordinal)
        dest.writeLong(delayTime)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<MathQuestion> {
        override fun createFromParcel(source: Parcel): MathQuestion = MathQuestion(source)

        override fun newArray(size: Int): Array<MathQuestion?> = arrayOfNulls(size)
    }
}

/**
 * An Immutable model that represents an arithmetic result.
 */
class MathAnswer(val result: String)

enum class Operator(val symbol: String) {
    ADD("+") {
        override fun compute(firstOperand: Double, secondOperand: Double): Double {
            return firstOperand + secondOperand
        }
    },
    SUBTRACT("-") {
        override fun compute(firstOperand: Double, secondOperand: Double): Double {
            return firstOperand - secondOperand
        }
    },
    MULTIPLY("*") {
        override fun compute(firstOperand: Double, secondOperand: Double): Double {
            return firstOperand * secondOperand
        }
    },
    DIVIDE("/") {
        override fun compute(firstOperand: Double, secondOperand: Double): Double {
            return firstOperand / secondOperand
        }
    };

    abstract fun compute(firstOperand: Double, secondOperand: Double): Double

    override fun toString() = name.toLowerCase(Locale.US)
}