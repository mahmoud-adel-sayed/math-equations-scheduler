package com.va.android.task.implementation.java.engine.data.model

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class OperatorAddParameterizedTest(
        private val firstOperand: Double,
        private val secondOperand: Double,
        private val expectedResult: Double
) {
    companion object {
        @JvmStatic
        @Parameters(name= "{index}: Operator.ADD.compute({0}, {1}) = {2}")
        fun data(): Iterable<Array<Any>> = listOf(
                arrayOf<Any>(0, 0, 0),
                arrayOf<Any>(0, -1, -1),
                arrayOf<Any>(2, 2, 4),
                arrayOf<Any>(2, 0, 2)
        )
    }

    @Test
    fun addTwoNumbers() {
        val result = Operator.ADD.compute(firstOperand, secondOperand)
        assertThat(result, `is`(equalTo(expectedResult)))
    }
}