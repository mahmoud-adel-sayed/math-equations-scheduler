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

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

/**
 * Fetches location info and notifies the registered listener.
 *
 * <p>
 *     The {@link AppCompatActivity} passed to the constructor must override & delegate its callbacks
 *     to these methods {@link #onSaveInstanceState(Bundle)}, {@link #onActivityResult(int, int)},
 *     {@link #onRequestPermissionsResult(int, int[])}.
 * </p>
 */
public final class LocationManager implements LifecycleObserver {
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private static final int REQUEST_LOCATION_PERMISSION = 34;
    private static final int REQUEST_CHECK_SETTINGS = 9000;

    private final static String KEY_REQUESTING_LOCATION_UPDATES = "KEY_REQUESTING_LOCATION_UPDATES";
    private final static String KEY_LOCATION = "KEY_LOCATION";

    private final WeakReference<AppCompatActivity> mActivity;
    private final Listener mListener;

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
                           @NonNull Listener listener) {
        mActivity = new WeakReference<>(activity);
        activity.getLifecycle().addObserver(this);
        updateValuesFromBundle(savedInstanceState);
        mListener = listener;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void initialize() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity.get());
        mSettingsClient = LocationServices.getSettingsClient(mActivity.get());
        mLocationCallback = createLocationCallback();
        mLocationRequest = createLocationRequest();
        mLocationSettingsRequest = buildLocationSettingsRequest();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void startListening() {
        if (mListener.shouldFetchLocationInfo()) startLocationUpdates();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void stopListening() {
        stopLocationUpdates();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void removeObserver() {
        if (mActivity.get() != null) {
            mActivity.get().getLifecycle().removeObserver(this);
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mLocation);
    }

    public void onActivityResult(int requestCode, int resultCode) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    startLocationUpdates();
                    break;
                case Activity.RESULT_CANCELED:
                    mRequestingLocationUpdates = false;
                    mListener.onLocationSettingsFailure(null);
                    break;
            }
        }
    }

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
                mListener.onLocationPermissionDenied();
            }
        }
    }

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

    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                mActivity.get(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION
        );
    }

    private boolean isLocationPermissionGranted() {
        int permissionState = ActivityCompat.checkSelfPermission(mActivity.get(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void grantLocationPermission() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(mActivity.get(),
                        Manifest.permission.ACCESS_FINE_LOCATION);
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            mListener.onProvideLocationPermissionRationale();
        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            requestLocationPermission();
        }
    }

    private LocationRequest createLocationRequest() {
        LocationRequest lr = LocationRequest.create();
        lr.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        lr.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return lr;
    }

    private LocationCallback createLocationCallback() {
        return new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLocation = locationResult.getLastLocation();
                mListener.onLocationResult(mLocation.getLatitude(), mLocation.getLongitude());
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
        mListener.onStartLocationListening();
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(mActivity.get(), response -> {
                    mFusedLocationClient.requestLocationUpdates(
                            mLocationRequest,
                            mLocationCallback,
                            Looper.getMainLooper()
                    );
                    mListener.onLocationSettingsSuccess();
                })
                .addOnFailureListener(mActivity.get(), e -> {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException rae = (ResolvableApiException) e;
                                rae.startResolutionForResult(mActivity.get(), REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException sie) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            mRequestingLocationUpdates = false;
                            String error = "Location settings are inadequate. Fix in Settings.";
                            mListener.onLocationSettingsFailure(error);
                    }
                });
    }

    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            return;
        }
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(mActivity.get(), task -> mRequestingLocationUpdates = false);
    }
}
