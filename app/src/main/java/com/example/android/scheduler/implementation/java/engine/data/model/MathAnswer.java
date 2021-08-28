package com.example.android.scheduler.implementation.java.engine.data.model;

/**
 * An Immutable model that represents an arithmetic result.
 */
public final class MathAnswer {
    private final String result;

    public MathAnswer(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }
}
