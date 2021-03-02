package com.va.android.task.implementation.java;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.va.android.task.BuildConfig;
import com.va.android.task.R;
import com.va.android.task.implementation.java.data.model.MathQuestion;
import com.va.android.task.implementation.java.data.model.Operator;
import com.va.android.task.implementation.java.engine.MathEngineService;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

@SuppressLint("NonConstantResourceId")
public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final int REQUEST_CHECK_SETTINGS = 9000;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";

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

    // Service
    private MathEngineService mServiceReference;
    private boolean mIsBound, mIsDataPopulated;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceReference = ((MathEngineService.LocalBinder) service).getService();
            mServiceReference.addListener(mServiceListener);
            if (!mIsDataPopulated) {
                mPendingOperationsAdapter.replaceData(mServiceReference.getPendingOperations());
                mOperationsResultsAdapter.replaceData(mServiceReference.getOperationsResults());
                mIsDataPopulated = true;
            }
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceReference = null;
            mIsBound = false;
        }
    };

    // Location
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private boolean mRequestingLocationUpdates;

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
        mIsDataPopulated = false;

        mRequestingLocationUpdates = false;
        updateValuesFromBundle(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        MathEngineService.start(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mIsBound) {
            Intent bindIntent = new Intent(this, MathEngineService.class);
            mIsBound = bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mIsBound) {
            mServiceReference.removeListener(mServiceListener);
            unbindService(mServiceConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCurrentLocationCB.isChecked()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    startLocationUpdates();
                    break;
                case Activity.RESULT_CANCELED:
                    mRequestingLocationUpdates = false;
                    setButtonsEnabled(true);
                    break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                // Permission denied.
                // It is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackBar(R.string.permission_denied_explanation, R.string.settings, view -> {
                    // Build intent that displays the App settings screen.
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                });
            }
        }
    }

    private boolean isLocationPermissionGranted() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            showSnackBar(R.string.location_permission_rationale, R.string.ok, view ->
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_PERMISSIONS_REQUEST_CODE
                    )
            );
        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE
            );
        }
    }

    @OnClick(R.id.current_location_container)
    void onCurrentLocationContainerClick() {
        if (mCurrentLocationCB.isChecked()) {
            stopLocationUpdates();
            mCurrentLocationCB.setChecked(false);
            mLatLongContainer.setVisibility(View.GONE);
            mCurrentLocation = null;
        } else {
            if (!isLocationPermissionGranted()) {
                requestLocationPermission();
                return;
            }
            if (!mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(KEY_REQUESTING_LOCATION_UPDATES);
            }
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }
        }
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                setButtonsEnabled(true);
                mLatLongContainer.setVisibility(View.VISIBLE);
                mLatTextView.setText(getString(R.string.format_lat, mCurrentLocation.getLatitude()));
                mLongTextView.setText(getString(R.string.format_long, mCurrentLocation.getLongitude()));
            }
        };
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    // Suppressing the location permission here is safe because this method will never be called
    // without the permission being granted.
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mRequestingLocationUpdates = true;
        setButtonsEnabled(false);
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, response -> {
                    mFusedLocationClient.requestLocationUpdates(
                            mLocationRequest,
                            mLocationCallback,
                            Looper.getMainLooper()
                    );
                    mCurrentLocationCB.setChecked(true);
                })
                .addOnFailureListener(this, e -> {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException rae = (ResolvableApiException) e;
                                rae.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException sie) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            String errorMessage = "Location settings are inadequate, and cannot be " +
                                    "fixed here. Fix in Settings.";
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                            mRequestingLocationUpdates = false;
                            setButtonsEnabled(true);
                    }
                });
    }

    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            return;
        }
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, task -> mRequestingLocationUpdates = false);
    }

    private void setButtonsEnabled(boolean enabled) {
        if (enabled) {
            mCurrentLocationProgress.setVisibility(View.GONE);
            mCurrentLocationContainer.setEnabled(true);
            mCurrentLocationContainer.setAlpha(1.0f);
        } else {
            mCurrentLocationProgress.setVisibility(View.VISIBLE);
            mCurrentLocationContainer.setEnabled(false);
            mCurrentLocationContainer.setAlpha(0.5f);
        }
    }

    private void showSnackBar(@StringRes int message, @StringRes int actionLabel,
                              View.OnClickListener listener) {
        Snackbar snackbar = Snackbar.make(mRoot, getString(message), Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(getString(actionLabel), listener);
        snackbar.show();
        View view = snackbar.getView();
        TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
        if (tv != null) tv.setGravity(Gravity.CENTER_HORIZONTAL);
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
        mPendingOperationsAdapter = new PendingOperationsAdapter(new ArrayList<>(0));
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
            showToast(R.string.err_operand);
            return;
        }

        if (mSelectedOperator == Operator.DIVIDE && secondOperand == 0) {
            showToast(R.string.err_division_by_zero);
            return;
        }

        long delayTime;
        try {
            delayTime = Long.parseLong(mDelayTimeEditText.getText().toString());
        } catch (NumberFormatException e) {
            showToast(R.string.err_invalid_delay_time);
            return;
        }

        MathQuestion mathQuestion =
                new MathQuestion(firstOperand, secondOperand, mSelectedOperator, delayTime);
        MathEngineService.calculate(this, mathQuestion);

        clearInputs();
    }

    private static double getOperand(EditText editText) {
        String operandText = editText.getText().toString();
        if (TextUtils.isEmpty(operandText)) {
            throw new NumberFormatException("operand cannot be empty!");
        }
        return Double.parseDouble(operandText);
    }

    private void showToast(@StringRes int stringResId) {
        Toast.makeText(this, stringResId, Toast.LENGTH_SHORT).show();
    }

    private void clearInputs() {
        mFirstOperandEditText.setText(null);
        mSecondOperandEditText.setText(null);
        mDelayTimeEditText.setText(null);
    }

    private final MathEngineService.Listener mServiceListener = new MathEngineService.Listener() {
        @Override
        public void onResultsChanged() {
            if (mIsBound) {
                mOperationsResultsAdapter.replaceData(mServiceReference.getOperationsResults());
            }
        }

        @Override
        public void onPendingOperationsChanged() {
            if (mIsBound) {
                mPendingOperationsAdapter.replaceData(mServiceReference.getPendingOperations());
            }
        }

        @Override
        public void onNotificationActionCancelAllClick() {
            mPendingOperationsAdapter.clearData();
        }
    };
}