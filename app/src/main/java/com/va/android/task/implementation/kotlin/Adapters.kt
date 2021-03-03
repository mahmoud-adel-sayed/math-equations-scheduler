package com.va.android.task.implementation.kotlin

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.va.android.task.R
import com.va.android.task.implementation.kotlin.engine.data.MathAnswer
import com.va.android.task.implementation.kotlin.engine.data.MathQuestion
import java.util.*
import kotlin.collections.ArrayList

class OperationsResultsAdapter(
        private var mathAnswers: List<MathAnswer> = ArrayList(initialCapacity = 0)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.operation_result_list_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ItemViewHolder).result.text = mathAnswers[position].result
    }

    override fun getItemCount(): Int = mathAnswers.size

    fun replaceData(mathAnswers: List<MathAnswer>) {
        this.mathAnswers = mathAnswers
        notifyDataSetChanged()
    }

    internal class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.result)
        lateinit var result: TextView

        init {
            ButterKnife.bind(this, v)
        }
    }
}

class PendingOperationsAdapter(
        private var mathQuestions: MutableList<MathQuestion> = ArrayList(initialCapacity = 0)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.pending_operation_list_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = mathQuestions[position]
        (holder as ItemViewHolder).equation.text = String.format(
                Locale.US, "Equation: %.2f %s %.2f",
                item.firstOperand, item.operator.symbol, item.secondOperand
        )
    }

    override fun getItemCount(): Int = mathQuestions.size

    fun replaceData(mathQuestions: List<MathQuestion>) {
        this.mathQuestions = mathQuestions as MutableList<MathQuestion>
        notifyDataSetChanged()
    }

    fun clearData() {
        mathQuestions.clear()
        notifyDataSetChanged()
    }

    internal class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.equation)
        lateinit var equation: TextView

        init {
            ButterKnife.bind(this, v)
        }
    }
}