package com.va.android.task.implementation.kotlin.engine.data

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * An Immutable model that represents a basic mathematical question.
 */
data class MathQuestion(
        val operationId: String = UUID.randomUUID().toString(),
        val firstOperand: Double,
        val secondOperand: Double,
        val operator: Operator,
        val delayTime: Long
) : Parcelable {

    private constructor(parcel: Parcel) : this(
            operationId = parcel.readString() ?: "",
            firstOperand = parcel.readDouble(),
            secondOperand = parcel.readDouble(),
            operator = Operator.values()[parcel.readInt()],
            delayTime = parcel.readLong()
    )

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is MathQuestion) return false

        return other.operationId == operationId
    }

    override fun hashCode() = 31 * operationId.hashCode()

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(operationId)
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