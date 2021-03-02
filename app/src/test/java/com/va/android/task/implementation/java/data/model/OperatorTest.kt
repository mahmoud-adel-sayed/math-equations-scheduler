package com.va.android.task.implementation.java.data.model

import androidx.test.filters.SmallTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

/**
 * Unit tests for the operator logic.
 */
@SmallTest
class OperatorTest {

    @Test
    fun addition() {
        val result = Operator.ADD.compute(1.0, 1.0)
        assertThat(result, `is`(equalTo(2.0)))
    }

    @Test
    fun subtractionWithPositiveResult() {
        val result = Operator.SUBTRACT.compute(10.0, 5.0)
        assertThat(result, `is`(equalTo(5.0)))
    }

    @Test
    fun subtractionWithNegativeResult() {
        val result = Operator.SUBTRACT.compute(5.0, 10.0)
        assertThat(result, `is`(equalTo(-5.0)))
    }

    @Test
    fun multiplication() {
        val result = Operator.MULTIPLY.compute(2.0, 2.0)
        assertThat(result, `is`(equalTo(4.0)))
    }

    @Test
    fun division() {
        val result = Operator.DIVIDE.compute(8.0, 2.0)
        assertThat(result, `is`(equalTo(4.0)))
    }
}