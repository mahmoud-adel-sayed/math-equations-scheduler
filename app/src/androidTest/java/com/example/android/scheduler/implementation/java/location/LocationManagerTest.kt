package com.example.android.scheduler.implementation.java.location

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.lifecycle.Lifecycle.State
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.rule.GrantPermissionRule
import com.example.android.scheduler.TestActivity
import com.example.android.scheduler.implementation.java.location.LocationManager.*
import com.example.android.scheduler.mock
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Tests the LocationManager.
 *
 * <p>Location Settings is not tested here, so <b>enable it on the test device</b> before running
 * the tests.</p>
 */
@MediumTest
@RunWith(AndroidJUnit4::class)
class LocationManagerTest {

    @get:Rule
    var permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)

    private lateinit var activityScenario: ActivityScenario<TestActivity>
    private lateinit var testActivity: TestActivity

    @Before
    fun setup() {
        activityScenario = launch(TestActivity::class.java)
        activityScenario.onActivity { testActivity = it }
    }

    @After
    fun tearDown() {
        activityScenario.close()
    }

    @Test
    fun locationOptions_onCreate_readProperly() {
        val options = optionsMock()
        activityScenario.moveToState(State.CREATED)
        runOnUiThread {
            LocationManager(testActivity, null, options, mock())
        }
        verify(options).priority
        verify(options).interval
        verify(options).fastestInterval
    }

    @Test
    fun readsFromBundle_writesToBundle() {
        val location = mock<Location>()
        `when`(location.latitude).thenReturn(30.091375)
        `when`(location.longitude).thenReturn(31.216454)

        val bundle = Bundle()
        bundle.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, true)
        bundle.putBoolean(KEY_ENABLED, true)
        bundle.putParcelable(KEY_LOCATION, location)

        activityScenario.moveToState(State.CREATED)
        var manager: LocationManager? = null
        runOnUiThread {
            manager = LocationManager(testActivity, bundle, optionsMock(), mock())
        }
        val locationManager = manager!!

        assertTrue(locationManager.isRequestingLocationUpdates)
        assertTrue(locationManager.mEnabled)
        assertEquals(location.latitude, locationManager.location?.latitude)
        assertEquals(location.longitude, locationManager.location?.longitude)

        bundle.remove(KEY_REQUESTING_LOCATION_UPDATES)
        bundle.remove(KEY_ENABLED)
        bundle.remove(KEY_LOCATION)
        locationManager.onSaveInstanceState(bundle)

        assertTrue(bundle.containsKey(KEY_REQUESTING_LOCATION_UPDATES))
        assertThat(bundle.getBoolean(KEY_REQUESTING_LOCATION_UPDATES), `is`(true))
        assertTrue(bundle.containsKey(KEY_ENABLED))
        assertThat(bundle.getBoolean(KEY_ENABLED), `is`(true))

        assertTrue(bundle.containsKey(KEY_LOCATION))
        val locationFromBundle: Location = bundle.getParcelable(KEY_LOCATION)!!
        assertEquals(locationFromBundle.latitude, locationManager.location?.latitude)
        assertEquals(locationFromBundle.longitude, locationManager.location?.longitude)
    }

    @Test
    fun locationSettings_onActivityResult() {
        val listener = mock<Listener>()
        activityScenario.moveToState(State.CREATED)
        var manager: LocationManager? = null
        runOnUiThread {
            manager = LocationManager(testActivity, null, optionsMock(), listener)
        }
        val locationManager = manager!!

        locationManager.onActivityResult(REQUEST_CHECK_SETTINGS, Activity.RESULT_CANCELED)
        assertFalse(locationManager.isRequestingLocationUpdates)
        assertFalse(locationManager.mEnabled)
        verify(listener).onLocationSettingsFailure(null)

        locationManager.onActivityResult(REQUEST_CHECK_SETTINGS, Activity.RESULT_OK)
        assertTrue(locationManager.isRequestingLocationUpdates)
        verify(listener).onStartLocationListening()
    }

    @Test
    fun locationPermission_onRequestPermissionsResult() {
        val listener = mock<Listener>()
        activityScenario.moveToState(State.CREATED)
        var manager: LocationManager? = null
        runOnUiThread {
            manager = LocationManager(testActivity, null, optionsMock(), listener)
        }
        val locationManager = manager!!

        val grantResults = IntArray(1) { PackageManager.PERMISSION_DENIED }
        locationManager.onRequestPermissionsResult(REQUEST_LOCATION_PERMISSION, grantResults)
        verify(listener).onLocationPermissionDenied()

        grantResults[0] = PackageManager.PERMISSION_GRANTED
        locationManager.onRequestPermissionsResult(REQUEST_LOCATION_PERMISSION, grantResults)
        assertTrue(locationManager.isRequestingLocationUpdates)
        verify(listener).onStartLocationListening()
    }

    @Test
    fun enablingAndDisabling_locationManager() {
        val listener = mock<Listener>()
        activityScenario.moveToState(State.CREATED)
        var manager: LocationManager? = null
        runOnUiThread {
            manager = LocationManager(testActivity, null, optionsMock(), listener)
        }
        val locationManager = manager!!

        locationManager.setEnabled(true)
        assertTrue(locationManager.isRequestingLocationUpdates)
        verify(listener).onStartLocationListening()

        locationManager.setEnabled(false)
        assertFalse(locationManager.mEnabled)
        assertNull(locationManager.location)
        verifyNoMoreInteractions(listener)
        Thread.sleep(200)
        assertFalse(locationManager.isRequestingLocationUpdates)
    }

    @Test
    fun locationUpdates_onLocationResultCalled() {
        val interval = 4000L
        val fastestInterval = 2000L
        val options = LocationOptions.Builder()
            .setPriority(LocationOptions.Priority.PRIORITY_HIGH_ACCURACY)
            .setInterval(interval)
            .setFastestInterval(fastestInterval)
            .build()

        var latitude: Double? = null
        var longitude: Double? = null
        val latch = CountDownLatch(1)
        val listener = object : Listener {
            override fun onProvideLocationPermissionRationale() { }

            override fun onLocationPermissionDenied() { }

            override fun onLocationResult(lat: Double, long: Double) {
                latitude = lat
                longitude = long
                latch.countDown()
            }
        }

        activityScenario.moveToState(State.CREATED)
        var manager: LocationManager? = null
        runOnUiThread {
            manager = LocationManager(testActivity, null, options, listener)
        }
        val locationManager = manager!!
        locationManager.setEnabled(true)

        assertTrue(latch.await(interval + 4000, TimeUnit.MILLISECONDS))
        assertTrue(locationManager.mEnabled)
        assertNotNull(locationManager.location)
        assertNotNull(latitude)
        assertNotNull(longitude)
        assertEquals(locationManager.location!!.latitude, latitude!!, 0.0)
        assertEquals(locationManager.location!!.longitude, longitude!!, 0.0)
    }

    @Test
    fun locationFetchingNotEnabled_onResume_shouldNotFetchLocationInfo() {
        val listener = mock<Listener>()

        activityScenario.moveToState(State.CREATED)
        var manager: LocationManager? = null
        runOnUiThread {
            manager = LocationManager(testActivity, null, optionsMock(), listener)
        }
        manager!!.mEnabled = false

        activityScenario.moveToState(State.RESUMED)
        Thread.sleep(100)

        assertFalse(manager!!.isRequestingLocationUpdates)
        verify(listener, never()).onStartLocationListening()
        verifyNoMoreInteractions(listener)
    }

    @Test
    fun locationFetchingEnabled_onResume_shouldFetchLocationInfo() {
        val listener = mock<Listener>()

        activityScenario.moveToState(State.CREATED)
        var manager: LocationManager? = null
        runOnUiThread {
            manager = LocationManager(testActivity, null, optionsMock(), listener)
        }
        manager!!.mEnabled = true

        activityScenario.moveToState(State.RESUMED)
        Thread.sleep(100)

        verify(listener).onStartLocationListening()
        assertTrue(manager!!.isRequestingLocationUpdates)
    }

    @Test
    fun locationFetchingEnabled_onResume_thenOnPause_shouldNotFetchLocationInfo() {
        val listener = mock<Listener>()

        activityScenario.moveToState(State.CREATED)
        var manager: LocationManager? = null
        runOnUiThread {
            manager = LocationManager(testActivity, null, optionsMock(), listener)
        }
        manager!!.mEnabled = true

        activityScenario.moveToState(State.RESUMED)
        Thread.sleep(100)
        assertTrue(manager!!.isRequestingLocationUpdates)

        // This will call onPause() & onStop() methods
        activityScenario.moveToState(State.CREATED)
        assertFalse(manager!!.isRequestingLocationUpdates)
    }

    private fun optionsMock(): LocationOptions {
        val options = mock<LocationOptions>()
        `when`(options.priority).thenReturn(LocationOptions.Priority.PRIORITY_HIGH_ACCURACY)
        `when`(options.interval).thenReturn(10_000)
        `when`(options.fastestInterval).thenReturn(5000)
        return options
    }
}