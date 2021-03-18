package com.va.android.task.implementation.java.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import static com.va.android.task.implementation.java.util.Preconditions.checkNotNull;

/**
 * Fetches location info and notifies the registered listener.
 *
 * <p>The implementation replies on Google location services and handles the following functionality:
 * <ul>
 *     <li>Requesting the location permission if not granted.</li>
 *     <li>Checking if the device has the necessary location settings.</li>
 *     <li>Automatically start/stop listening for location updates when activity is resumed/paused.</li>
 * </ul>
 * </p>
 *
 * <p>The {@link AppCompatActivity} passed to the constructor <strong>must override some callbacks</strong>
 * & delegate them to these methods {@link #onSaveInstanceState(Bundle)},
 * {@link #onActivityResult(int, int)}, {@link #onRequestPermissionsResult(int, int[])}.</p>
 *
 * <p><strong>Do not use {@code 2000} or {@code 2001} as request codes</strong>,
 * they are used internally.</p>
 */
public final class LocationManager implements LifecycleObserver {

    @VisibleForTesting
    static final int REQUEST_LOCATION_PERMISSION = 2000;

    @VisibleForTesting
    static final int REQUEST_CHECK_SETTINGS = 2001;

    @VisibleForTesting
    final static String KEY_REQUESTING_LOCATION_UPDATES = "KEY_REQUESTING_LOCATION_UPDATES";

    @VisibleForTesting
    final static String KEY_LOCATION = "KEY_LOCATION";

    private AppCompatActivity mActivity;
    private final LocationOptions mOptions;
    private Listener mListener;

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mLocation;
    private boolean mRequestingLocationUpdates;

    public interface Listener {
        void onProvideLocationPermissionRationale();

        void onLocationPermissionDenied();

        boolean shouldFetchLocationInfo();

        default void onStartLocationListening() { }

        default void onLocationSettingsSuccess() { }

        default void onLocationSettingsFailure(@Nullable String error) { }

        void onLocationResult(double latitude, double longitude);
    }

    public LocationManager(@NonNull AppCompatActivity activity, @Nullable Bundle savedInstanceState,
                           @NonNull LocationOptions options, @Nullable Listener listener) {
        mActivity = checkNotNull(activity);
        updateValuesFromBundle(savedInstanceState);
        mOptions = checkNotNull(options);
        mListener = listener;
        activity.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void initialize() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity);
        mSettingsClient = LocationServices.getSettingsClient(mActivity);
        mLocationCallback = createLocationCallback();
        mLocationRequest = createLocationRequest();
        mLocationSettingsRequest = buildLocationSettingsRequest();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void startListening() {
        if (mListener != null && mListener.shouldFetchLocationInfo()) startLocationUpdates();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void stopListening() {
        stopLocationUpdates();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void removeObserver() {
        mListener = null;
        mActivity.getLifecycle().removeObserver(this);
        mActivity = null;
    }

    @VisibleForTesting
    boolean isRequestingLocationUpdates() {
        return mRequestingLocationUpdates;
    }

    @Nullable
    @VisibleForTesting
    Location getLocation() {
        return mLocation;
    }

    /**
     * The activity must delegate the call to this method from the its similar callback.
     *
     * @param savedInstanceState The savedInstanceState
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mLocation);
    }

    /**
     * The activity must delegate the call to this method from the its similar callback.
     *
     * @param requestCode The requestCode
     * @param resultCode The resultCode
     */
    public void onActivityResult(int requestCode, int resultCode) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    startLocationUpdates();
                    break;
                case Activity.RESULT_CANCELED:
                    mRequestingLocationUpdates = false;
                    if (mListener != null) {
                        mListener.onLocationSettingsFailure(null);
                    }
                    break;
            }
        }
    }

    /**
     * The activity must override {@link AppCompatActivity#onRequestPermissionsResult(int, String[], int[])}
     * callback and delegate the call to this method.
     *
     * @param requestCode The requestCode
     * @param grantResults The grantResults
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                // Permission denied.
                // It is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                if (mListener != null) {
                    mListener.onLocationPermissionDenied();
                }
            }
        }
    }

    /**
     * If enabled is {@code true}, it checks permission & location settings then starts listening
     * for location updates, otherwise it stops listening.
     *
     * @param enabled The flag indicating that location fetching should be enabled or not.
     */
    public void setEnabled(boolean enabled) {
        if (enabled) {
            if (!isLocationPermissionGranted()) {
                grantLocationPermission();
                return;
            }
            if (!mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        } else {
            stopLocationUpdates();
            mLocation = null;
        }
    }

    /**
     * Requests the {@code Manifest.permission.ACCESS_FINE_LOCATION} permission.
     */
    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                mActivity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION
        );
    }

    private boolean isLocationPermissionGranted() {
        int permissionState = ActivityCompat.checkSelfPermission(mActivity,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void grantLocationPermission() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION);
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            if (mListener != null) {
                mListener.onProvideLocationPermissionRationale();
            }
        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            requestLocationPermission();
        }
    }

    private LocationRequest createLocationRequest() {
        LocationRequest lr = LocationRequest.create();
        lr.setInterval(mOptions.getInterval());
        lr.setFastestInterval(mOptions.getFastestInterval());
        lr.setPriority(mOptions.getPriority());
        return lr;
    }

    private LocationCallback createLocationCallback() {
        return new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLocation = locationResult.getLastLocation();
                if (mListener != null) {
                    mListener.onLocationResult(mLocation.getLatitude(), mLocation.getLongitude());
                }
            }
        };
    }

    private LocationSettingsRequest buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        return builder.build();
    }

    private void updateValuesFromBundle(Bundle bundle) {
        if (bundle == null) {
            return;
        }

        if (bundle.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
            mRequestingLocationUpdates = bundle.getBoolean(KEY_REQUESTING_LOCATION_UPDATES);
        }
        if (bundle.keySet().contains(KEY_LOCATION)) {
            mLocation = bundle.getParcelable(KEY_LOCATION);
        }
    }

    // Suppressing the location permission here is safe because this method will never be called
    // without the permission being granted.
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mRequestingLocationUpdates = true;
        if (mListener != null) {
            mListener.onStartLocationListening();
        }
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(mActivity, response -> {
                    mFusedLocationClient.requestLocationUpdates(
                            mLocationRequest,
                            mLocationCallback,
                            Looper.getMainLooper()
                    );
                    if (mListener != null) {
                        mListener.onLocationSettingsSuccess();
                    }
                })
                .addOnFailureListener(mActivity, e -> {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException rae = (ResolvableApiException) e;
                                rae.startResolutionForResult(mActivity, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException sie) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            mRequestingLocationUpdates = false;
                            String error = "Location settings are inadequate. Fix in Settings.";
                            if (mListener != null) {
                                mListener.onLocationSettingsFailure(error);
                            }
                    }
                });
    }

    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            return;
        }
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(mActivity, task -> mRequestingLocationUpdates = false);
    }
}
