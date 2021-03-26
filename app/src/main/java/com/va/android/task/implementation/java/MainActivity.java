package com.va.android.task.implementation.java;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.va.android.task.R;
import com.va.android.task.implementation.java.engine.MathEngine;
import com.va.android.task.implementation.java.engine.data.model.MathAnswer;
import com.va.android.task.implementation.java.engine.data.model.MathQuestion;
import com.va.android.task.implementation.java.engine.data.model.Operation;
import com.va.android.task.implementation.java.engine.data.model.Operator;
import com.va.android.task.implementation.java.location.LocationManager;
import com.va.android.task.implementation.java.location.LocationOptions;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

import static com.va.android.task.implementation.java.util.ViewUtil.getAppSettingsIntent;
import static com.va.android.task.implementation.java.util.ViewUtil.getOperand;
import static com.va.android.task.implementation.java.util.ViewUtil.showSnackBar;

@SuppressLint("NonConstantResourceId")
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.root)
    View mRoot;

    @BindView(R.id.et_first_operand)
    EditText mFirstOperandEditText;

    @BindView(R.id.et_second_operand)
    EditText mSecondOperandEditText;

    @BindView(R.id.et_delay_time)
    EditText mDelayTimeEditText;

    @BindView(R.id.spinner_operators)
    Spinner mOperatorsSpinner;

    @BindView(R.id.current_location_container)
    View mCurrentLocationContainer;

    @BindView(R.id.cb_my_current_location)
    CheckBox mCurrentLocationCB;

    @BindView(R.id.progress_location)
    View mCurrentLocationProgress;

    @BindView(R.id.lat_long_container)
    View mLatLongContainer;

    @BindView(R.id.latitude)
    TextView mLatTextView;

    @BindView(R.id.longitude)
    TextView mLongTextView;

    @BindView(R.id.rv_pending_operations)
    RecyclerView mPendingOperationsRV;

    @BindView(R.id.rv_operations_results)
    RecyclerView mOperationsResultsRV;

    private PendingOperationsAdapter mPendingOperationsAdapter;
    private OperationsResultsAdapter mOperationsResultsAdapter;
    private Operator mSelectedOperator = Operator.ADD;

    private MathEngine mMathEngine;
    private LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.app_name));
        ButterKnife.bind(this);

        setupOperatorsSpinner();
        setupTabs();
        setupPendingOperations();
        setupOperationsResults();

        LocationOptions options = new LocationOptions.Builder()
                .setPriority(LocationOptions.Priority.PRIORITY_HIGH_ACCURACY)
                .build();
        mLocationManager =
                new LocationManager(this, savedInstanceState, options, mLocationListener);

        mMathEngine = new MathEngine(this, getLifecycle(), mMathEngineListener);
        mMathEngine.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLocationManager.onActivityResult(requestCode, resultCode);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        mLocationManager.onSaveInstanceState(savedInstanceState);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationManager.onRequestPermissionsResult(requestCode, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @OnClick(R.id.current_location_container)
    void onCurrentLocationContainerClick() {
        if (mCurrentLocationCB.isChecked()) {
            mCurrentLocationCB.setChecked(false);
            mLatLongContainer.setVisibility(View.GONE);
            mLocationManager.setEnabled(false);
        }
        else {
            mLocationManager.setEnabled(true);
        }
    }

    private void setLocationLoading(boolean loading) {
        if (loading) {
            mCurrentLocationProgress.setVisibility(View.VISIBLE);
            mCurrentLocationContainer.setEnabled(false);
            mCurrentLocationContainer.setAlpha(0.5f);
        } else {
            mCurrentLocationProgress.setVisibility(View.GONE);
            mCurrentLocationContainer.setEnabled(true);
            mCurrentLocationContainer.setAlpha(1.0f);
        }
    }

    private void setupOperatorsSpinner() {
        ArrayAdapter<Operator> operatorsAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, Operator.values()
        );
        mOperatorsSpinner.setAdapter(operatorsAdapter);
    }

    // For simplicity sake, I do not use viewPager with the tabLayout.
    // We could also use multiple fragments here.
    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    mPendingOperationsRV.setVisibility(View.VISIBLE);
                    mOperationsResultsRV.setVisibility(View.GONE);
                } else {
                    mOperationsResultsRV.setVisibility(View.VISIBLE);
                    mPendingOperationsRV.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        tabLayout.addTab(tabLayout.newTab().setText(R.string.pending));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.results));
    }

    private void setupPendingOperations() {
        mPendingOperationsAdapter =
                new PendingOperationsAdapter(getLifecycle(), new ArrayList<>(0));
        mPendingOperationsRV.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mPendingOperationsRV.setAdapter(mPendingOperationsAdapter);
    }

    private void setupOperationsResults() {
        mOperationsResultsAdapter = new OperationsResultsAdapter(new ArrayList<>(0));
        mOperationsResultsRV.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mOperationsResultsRV.setAdapter(mOperationsResultsAdapter);
    }

    @OnItemSelected(R.id.spinner_operators)
    void onOperatorSelected(int position) {
        mSelectedOperator = Operator.values()[position];
    }

    @OnClick(R.id.btn_calculate)
    void onCalculate() {
        double firstOperand;
        double secondOperand;
        try {
            firstOperand = getOperand(mFirstOperandEditText);
            secondOperand = getOperand(mSecondOperandEditText);
        } catch (NumberFormatException e) {
            showSnackBar(mRoot, R.string.err_operand, Snackbar.LENGTH_SHORT);
            return;
        }

        if (mSelectedOperator == Operator.DIVIDE && secondOperand == 0) {
            showSnackBar(mRoot, R.string.err_division_by_zero, Snackbar.LENGTH_SHORT);
            return;
        }

        long delayTime;
        try {
            delayTime = Long.parseLong(mDelayTimeEditText.getText().toString());
        } catch (NumberFormatException e) {
            showSnackBar(mRoot, R.string.err_invalid_delay_time, Snackbar.LENGTH_SHORT);
            return;
        }

        MathQuestion mathQuestion =
                new MathQuestion(firstOperand, secondOperand, mSelectedOperator, delayTime);
        mMathEngine.calculate(mathQuestion);

        clearInputs();
    }

    private void clearInputs() {
        mFirstOperandEditText.setText(null);
        mSecondOperandEditText.setText(null);
        mDelayTimeEditText.setText(null);
    }

    private final MathEngine.Listener mMathEngineListener = new MathEngine.Listener() {
        @Override
        public void onConnected(@NonNull List<Operation> pending, @NonNull List<MathAnswer> results) {
            mPendingOperationsAdapter.replaceData(pending);
            mOperationsResultsAdapter.replaceData(results);
        }

        @Override
        public void onPendingOperationsChanged(@NonNull List<Operation> pending) {
            mPendingOperationsAdapter.replaceData(pending);
        }

        @Override
        public void onResultsChanged(@NonNull List<MathAnswer> results) {
            mOperationsResultsAdapter.replaceData(results);
        }

        @Override
        public void onNotificationActionCancelAllClick() {
            mPendingOperationsAdapter.clearData();
        }
    };

    private final LocationManager.Listener mLocationListener = new LocationManager.Listener() {
        @Override
        public void onProvideLocationPermissionRationale() {
            showSnackBar(
                    mRoot,
                    R.string.location_permission_rationale,
                    getString(R.string.ok),
                    Snackbar.LENGTH_INDEFINITE,
                    view -> mLocationManager.requestLocationPermission()
            );
        }

        @Override
        public void onLocationPermissionDenied() {
            showSnackBar(
                    mRoot,
                    R.string.permission_denied_explanation,
                    getString(R.string.settings),
                    Snackbar.LENGTH_INDEFINITE,
                    view -> startActivity(getAppSettingsIntent())
            );
        }

        @Override
        public void onStartLocationListening() {
            setLocationLoading(true);
        }

        @Override
        public void onLocationSettingsSuccess() {
            mCurrentLocationCB.setChecked(true);
        }

        @Override
        public void onLocationSettingsFailure(@Nullable String error) {
            if (error != null) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
            }
            setLocationLoading(false);
        }

        @Override
        public void onLocationResult(double latitude, double longitude) {
            setLocationLoading(false);
            mLatLongContainer.setVisibility(View.VISIBLE);
            mLatTextView.setText(getString(R.string.format_lat, latitude));
            mLongTextView.setText(getString(R.string.format_long, longitude));
        }
    };
}