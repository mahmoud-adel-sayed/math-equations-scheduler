package com.va.android.task.implementation.kotlin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnItemSelected
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.va.android.task.R
import com.va.android.task.implementation.kotlin.engine.MathEngine
import com.va.android.task.implementation.kotlin.engine.data.MathAnswer
import com.va.android.task.implementation.kotlin.engine.data.MathQuestion
import com.va.android.task.implementation.kotlin.engine.data.Operator
import com.va.android.task.implementation.kotlin.location.LocationManager
import com.va.android.task.implementation.kotlin.util.getAppSettingsIntent
import com.va.android.task.implementation.kotlin.util.showSnackBar
import com.va.android.task.implementation.kotlin.util.showToast
import com.va.android.task.implementation.kotlin.util.toDouble

@SuppressLint("NonConstantResourceId")
class MainActivity : AppCompatActivity() {

    @BindView(R.id.root)
    lateinit var root: View

    @BindView(R.id.et_first_operand)
    lateinit var firstOperandEditText: EditText

    @BindView(R.id.et_second_operand)
    lateinit var secondOperandEditText: EditText

    @BindView(R.id.et_delay_time)
    lateinit var delayTimeEditText: EditText

    @BindView(R.id.spinner_operators)
    lateinit var operatorsSpinner: Spinner

    @BindView(R.id.current_location_container)
    lateinit var currentLocationContainer: View

    @BindView(R.id.cb_my_current_location)
    lateinit var currentLocationCB: CheckBox

    @BindView(R.id.progress_location)
    lateinit var currentLocationProgress: View

    @BindView(R.id.lat_long_container)
    lateinit var latLongContainer: View

    @BindView(R.id.latitude)
    lateinit var latTextView: TextView

    @BindView(R.id.longitude)
    lateinit var longTextView: TextView

    @BindView(R.id.rv_pending_operations)
    lateinit var pendingOperationsRV: RecyclerView

    @BindView(R.id.rv_operations_results)
    lateinit var operationsResultsRV: RecyclerView

    private lateinit var pendingOperationsAdapter: PendingOperationsAdapter
    private lateinit var operationsResultsAdapter: OperationsResultsAdapter
    private var selectedOperator: Operator = Operator.ADD

    private lateinit var mathEngine: MathEngine
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        title = getString(R.string.app_name)
        ButterKnife.bind(this)

        setupOperatorsSpinner()
        setupTabs()
        setupPendingOperations()
        setupOperationsResults()

        locationManager = LocationManager(
                activity = this,
                savedInstanceState = savedInstanceState,
                listener = locationListener
        )

        mathEngine = MathEngine(
                context = this,
                lifecycle = lifecycle,
                listener = mathEngineListener
        )
        mathEngine.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        locationManager.onActivityResult(requestCode, resultCode)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        locationManager.onSaveInstanceState(savedInstanceState)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) = locationManager.onRequestPermissionsResult(requestCode, grantResults)

    override fun onResume() {
        super.onResume()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    @OnClick(R.id.current_location_container)
    fun onCurrentLocationContainerClick() {
        if (currentLocationCB.isChecked) {
            currentLocationCB.isChecked = false
            latLongContainer.visibility = View.GONE
            locationManager.setEnabled(false)
        } else {
            locationManager.setEnabled(true)
        }
    }

    private fun setLocationLoading(loading: Boolean) {
        if (loading) {
            currentLocationProgress.visibility = View.VISIBLE
            with(currentLocationContainer) {
                isEnabled = false
                alpha = 0.5f
            }
        } else {
            currentLocationProgress.visibility = View.GONE
            with(currentLocationContainer) {
                isEnabled = true
                alpha = 1.0f
            }
        }
    }

    private fun setupOperatorsSpinner() {
        operatorsSpinner.adapter = ArrayAdapter<Operator>(
                this, android.R.layout.simple_list_item_1, Operator.values()
        )
    }

    // For simplicity sake, I do not use viewPager with the tabLayout.
    // We could also use multiple fragments here.
    private fun setupTabs() {
        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    if (it.position == 0) {
                        pendingOperationsRV.visibility = View.VISIBLE
                        operationsResultsRV.visibility = View.GONE
                    } else {
                        operationsResultsRV.visibility = View.VISIBLE
                        pendingOperationsRV.visibility = View.GONE
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) { }

            override fun onTabUnselected(tab: TabLayout.Tab?) { }
        })
        tabLayout.addTab(tabLayout.newTab().setText(R.string.pending))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.results))
    }

    private fun setupPendingOperations() {
        pendingOperationsAdapter = PendingOperationsAdapter()
        with(pendingOperationsRV) {
            layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
            adapter = pendingOperationsAdapter
        }
    }

    private fun setupOperationsResults() {
        operationsResultsAdapter = OperationsResultsAdapter()
        with(operationsResultsRV) {
            layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
            adapter = operationsResultsAdapter
        }
    }

    @OnItemSelected(R.id.spinner_operators)
    fun onOperatorSelected(position: Int) {
        selectedOperator = Operator.values()[position]
    }

    @OnClick(R.id.btn_calculate)
    fun onCalculate() {
        val firstOperand: Double
        val secondOperand: Double
        try {
            firstOperand = firstOperandEditText.toDouble()
            secondOperand = secondOperandEditText.toDouble()
        } catch (e: NumberFormatException) {
            showSnackBar(root, R.string.err_operand, Snackbar.LENGTH_SHORT)
            return
        }

        if (selectedOperator == Operator.DIVIDE && secondOperand == 0.0) {
            showSnackBar(root, R.string.err_division_by_zero, Snackbar.LENGTH_SHORT)
            return
        }

        val delayTime: Long
        try {
            delayTime = delayTimeEditText.text.toString().toLong()
        } catch (e: NumberFormatException) {
            showSnackBar(root, R.string.err_invalid_delay_time, Snackbar.LENGTH_SHORT)
            return
        }

        val mathQuestion = MathQuestion(
                firstOperand = firstOperand,
                secondOperand = secondOperand,
                operator = selectedOperator,
                delayTime = delayTime
        )
        mathEngine.calculate(mathQuestion)

        clearInputs()
    }

    private fun clearInputs() {
        firstOperandEditText.text = null
        secondOperandEditText.text = null
        delayTimeEditText.text = null
    }

    private val mathEngineListener: MathEngine.Listener = object : MathEngine.Listener {
        override fun onConnected(pending: List<MathQuestion>, results: List<MathAnswer>) {
            pendingOperationsAdapter.replaceData(pending)
            operationsResultsAdapter.replaceData(results)
        }

        override fun onPendingOperationsChanged(pending: List<MathQuestion>) {
            pendingOperationsAdapter.replaceData(pending)
        }

        override fun onResultsChanged(results: List<MathAnswer>) {
            operationsResultsAdapter.replaceData(results)
        }

        override fun onNotificationActionCancelAllClick() = pendingOperationsAdapter.clearData()
    }

    private val locationListener: LocationManager.Listener = object : LocationManager.Listener {
        override fun onProvideLocationPermissionRationale() {
            showSnackBar(
                    root = root,
                    message = R.string.location_permission_rationale,
                    duration = Snackbar.LENGTH_INDEFINITE,
                    actionLabel = getString(R.string.ok),
                    action = { locationManager.requestLocationPermission() }
            )
        }

        override fun onLocationPermissionDenied() {
            showSnackBar(
                    root = root,
                    message = R.string.permission_denied_explanation,
                    duration = Snackbar.LENGTH_INDEFINITE,
                    actionLabel = getString(R.string.settings),
                    action = { startActivity(getAppSettingsIntent()) }
            )
        }

        override fun shouldFetchLocationInfo(): Boolean = currentLocationCB.isChecked

        override fun onStartLocationListening() = setLocationLoading(true)

        override fun onLocationSettingsSuccess() {
            currentLocationCB.isChecked = true
        }

        override fun onLocationSettingsFailure(error: String?) {
            error?.let { showToast(it, Toast.LENGTH_LONG) }
            setLocationLoading(false)
        }

        override fun onLocationResult(latitude: Double, longitude: Double) {
            setLocationLoading(false)
            latLongContainer.visibility = View.VISIBLE
            latTextView.text = getString(R.string.format_lat, latitude)
            longTextView.text = getString(R.string.format_long, longitude)
        }
    }
}