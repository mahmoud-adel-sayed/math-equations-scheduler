package com.example.android.scheduler.implementation.kotlin

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.example.android.scheduler.R
import com.example.android.scheduler.implementation.kotlin.engine.data.MathAnswer
import com.example.android.scheduler.implementation.kotlin.engine.data.Operation
import java.util.*
import java.util.concurrent.TimeUnit

class OperationsResultsAdapter(
    private var mathAnswers: List<MathAnswer> = arrayListOf()
) : RecyclerView.Adapter<OperationsResultsAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.operation_result_list_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.result.text = mathAnswers[position].result
    }

    override fun getItemCount(): Int = mathAnswers.size

    fun replaceData(mathAnswers: List<MathAnswer>) {
        this.mathAnswers = mathAnswers
        notifyDataSetChanged()
    }

    class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.result)
        lateinit var result: TextView

        init {
            ButterKnife.bind(this, v)
        }
    }
}

class PendingOperationsAdapter(
    lifecycle: Lifecycle,
    private var operations: MutableList<Operation> = arrayListOf()
) : RecyclerView.Adapter<PendingOperationsAdapter.ItemViewHolder>(), LifecycleObserver {

    private val timers = HashMap<String, CountDownTimer>()

    init {
        lifecycle.addObserver(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pending_operation_list_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val operation = operations[position]
        with(holder) {
            if (timer != null) {
                timer?.cancel()
                timer = null
                timers.remove(operation.id)
            }

            val question = operation.mathQuestion
            equation.text = String.format(
                Locale.US, "Equation: %.2f %s %.2f",
                question.firstOperand, question.operator.symbol, question.secondOperand
            )

            setupTimer(this, operation)
        }
    }

    override fun getItemCount(): Int = operations.size

    fun replaceData(operations: List<Operation>) {
        this.operations = operations.toMutableList()
        cancelTimers()
        timers.clear()
        notifyDataSetChanged()
    }

    fun clearData() {
        operations.clear()
        cancelTimers()
        timers.clear()
        notifyDataSetChanged()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    internal fun cancelTimers() = timers.values.forEach { it.cancel() }

    private fun setupTimer(holder: ItemViewHolder, operation: Operation) {
        val totalMillis = operation.endTime - System.currentTimeMillis()
        if (totalMillis <= 0) {
            holder.remainingTime.text = null
            return
        }
        holder.timer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(untilFinished: Long) {
                var millisUntilFinished = untilFinished

                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                millisUntilFinished -= TimeUnit.HOURS.toMillis(hours)

                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                millisUntilFinished -= TimeUnit.MINUTES.toMillis(minutes)

                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)

                holder.remainingTime.text = getTimeFormatted(
                    hours = hours, minutes = minutes, seconds = seconds
                )
            }

            override fun onFinish() {
                holder.remainingTime.text = null
            }
        }.start()
        timers[operation.id] = holder.timer!!
    }

    @SuppressLint("NonConstantResourceId")
    class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        @BindView(R.id.equation)
        lateinit var equation: TextView

        @BindView(R.id.remaining_time)
        lateinit var remainingTime: TextView

        var timer: CountDownTimer? = null

        init {
            ButterKnife.bind(this, v)
        }
    }

    companion object {
        private fun getTimeFormatted(hours: Long, minutes: Long, seconds: Long) =
            "Remaining Time: ${hours.digits}:${minutes.digits}:${seconds.digits}"

        private val Long.digits get() = if (this < 10) "0$this" else toString()
    }
}