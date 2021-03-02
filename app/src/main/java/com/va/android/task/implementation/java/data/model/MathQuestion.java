package com.va.android.task.implementation.java.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;
import java.util.UUID;

import androidx.annotation.NonNull;

/**
 * An Immutable model that represents a basic mathematical question.
 */
public final class MathQuestion implements Parcelable {

    @NonNull
    private final String operationId;
    private final double firstOperand;
    private final double secondOperand;
    private final Operator operator;
    private final long delayTime;

    /**
     * Constructor for MathQuestion.
     *
     * @param firstOperand The first operand of the math equation.
     * @param secondOperand The second operand of the math equation.
     * @param operator The operator.
     * @param delayTime The delay time (in seconds).
     */
    public MathQuestion(double firstOperand, double secondOperand, Operator operator,
                        long delayTime) {
        operationId = UUID.randomUUID().toString();
        this.firstOperand = firstOperand;
        this.secondOperand = secondOperand;
        this.operator = operator;
        this.delayTime = delayTime;
    }

    private MathQuestion(Parcel in) {
        operationId = in.readString();
        firstOperand = in.readDouble();
        secondOperand = in.readDouble();
        operator = Operator.values()[in.readInt()];
        delayTime = in.readLong();
    }

    /**
     * ID that uniquely identifies the operation (Generated automatically at construction time).
     *
     * @return The operation id.
     */
    @NonNull
    public String getOperationId() {
        return operationId;
    }

    /**
     * The first operand of the math equation.
     *
     * @return The first operand.
     */
    public double getFirstOperand() {
        return firstOperand;
    }

    /**
     * The second operand of the math equation.
     *
     * @return The second operand.
     */
    public double getSecondOperand() {
        return secondOperand;
    }

    /**
     * The operator of the math equation.
     *
     * @return The operator.
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * The delay time (in seconds) before receiving the result of the operation.
     *
     * @return The delay time (in seconds).
     */
    public long getDelayTime() {
        return delayTime;
    }

    @NonNull
    @Override
    public String toString() {
        return "MathQuestion(" +
                "operationId=" + operationId + ", " +
                "firstOperand=" + firstOperand + ", " +
                "secondOperand=" + secondOperand + ", " +
                "operator=" + operator.name() + ", " +
                "delayTime=" + delayTime +
                ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != this.getClass()) return false;

        MathQuestion other = (MathQuestion) o;
        return (other.operationId.equals(operationId) &&
                other.firstOperand == firstOperand &&
                other.secondOperand == secondOperand &&
                other.operator == operator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationId, firstOperand, secondOperand, operator);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(operationId);
        dest.writeDouble(firstOperand);
        dest.writeDouble(secondOperand);
        dest.writeInt(operator.ordinal());
        dest.writeLong(delayTime);
    }

    public static final Parcelable.Creator<MathQuestion> CREATOR = new Parcelable.Creator<MathQuestion>() {
        @Override
        public MathQuestion createFromParcel(Parcel source) {
            return new MathQuestion(source);
        }

        @Override
        public MathQuestion[] newArray(int size) {
            return new MathQuestion[size];
        }
    };
}
