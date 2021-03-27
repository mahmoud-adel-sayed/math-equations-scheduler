package com.va.android.task.implementation.java.engine.data.model;

import java.util.UUID;

import androidx.annotation.NonNull;

/**
 * A model that represents an operation.
 */
public final class Operation {
    @NonNull
    private final String id;

    private final long startTime;
    private final long endTime;

    @NonNull
    private final MathQuestion mathQuestion;

    /**
     * Constructor for the Operation.
     *
     * @param startTime The operation's start time in milliseconds
     * @param endTime The operation's end time in milliseconds
     * @param mathQuestion The math question
     */
    public Operation(long startTime, long endTime, @NonNull MathQuestion mathQuestion) {
        id = UUID.randomUUID().toString();
        this.startTime = startTime;
        this.endTime = endTime;
        this.mathQuestion = mathQuestion;
    }

    /**
     * ID that uniquely identifies the operation (Generated automatically at construction time).
     *
     * @return The operation id.
     */
    @NonNull
    public String getId() {
        return id;
    }

    /**
     * Returns the operation start time in milliseconds.
     *
     * @return The start time.
     *
     * @see #getEndTime()
     */
    @SuppressWarnings("unused")
    public long getStartTime() {
        return startTime;
    }

    /**
     * Returns the operation end time in milliseconds.
     *
     * @return The end time.
     *
     * @see #getStartTime()
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Returns the associated MathQuestion.
     *
     * @return The math question.
     */
    @NonNull
    public MathQuestion getMathQuestion() {
        return mathQuestion;
    }

    @NonNull
    @Override
    public String toString() {
        return "Operation(" +
                "id=" + id + ", " +
                "startTime=" + startTime + ", " +
                "endTime=" + endTime + ", " +
                "mathQuestion={" + mathQuestion + "}" +
                ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != this.getClass()) return false;

        Operation other = (Operation) o;
        return other.id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
