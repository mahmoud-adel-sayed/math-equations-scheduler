package com.va.android.task

import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.va.android.task.implementation.java.data.model.MathQuestion
import com.va.android.task.implementation.java.data.model.Operator
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests that the parcelable interface is implemented correctly.
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class MathQuestionTest {

    companion object {
        private const val FIRST_OPERAND = 1.0
        private const val SECOND_OPERAND = 3.0
        private const val DELAY_TIME = 10L
        private val OPERATOR = Operator.ADD
    }

    private lateinit var mathQuestion: MathQuestion

    @Before
    fun initMathQuestion() {
        mathQuestion = MathQuestion(FIRST_OPERAND, SECOND_OPERAND, OPERATOR, DELAY_TIME)
    }

    @Test
    fun mathQuestion_ReadWriteParcelable() {
        // Write
        val parcel = Parcel.obtain()
        mathQuestion.writeToParcel(parcel, mathQuestion.describeContents())
        // Reset for reading
        parcel.setDataPosition(0)
        // Read
        val mathQuestionFromParcel = MathQuestion.CREATOR.createFromParcel(parcel)
        // Verify
        assertThat(mathQuestionFromParcel.operationId).isEqualTo(mathQuestion.operationId)
        assertThat(mathQuestionFromParcel.firstOperand).isEqualTo(FIRST_OPERAND)
        assertThat(mathQuestionFromParcel.secondOperand).isEqualTo(SECOND_OPERAND)
        assertThat(mathQuestionFromParcel.operator).isEqualTo(OPERATOR)
        assertThat(mathQuestionFromParcel.delayTime).isEqualTo(DELAY_TIME)
    }
}