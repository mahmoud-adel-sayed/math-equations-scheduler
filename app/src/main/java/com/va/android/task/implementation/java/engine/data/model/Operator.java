package com.va.android.task.implementation.java.engine.data.model;

import androidx.annotation.NonNull;

public enum Operator {
    ADD("+") {
        @Override
        public double compute(double firstOperand, double secondOperand) {
            return firstOperand + secondOperand;
        }
    },
    SUBTRACT("-") {
        @Override
        public double compute(double firstOperand, double secondOperand) {
            return firstOperand - secondOperand;
        }
    },
    MULTIPLY("*") {
        @Override
        public double compute(double firstOperand, double secondOperand) {
            return firstOperand * secondOperand;
        }
    },
    DIVIDE("/") {
        @Override
        public double compute(double firstOperand, double secondOperand) {
            return firstOperand / secondOperand;
        }
    };

    private final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public abstract double compute(double firstOperand, double secondOperand);

    @NonNull
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
