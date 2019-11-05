package sirs.spykid.child

import android.content.IntentSender
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task


class Child : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var mCurrentLocation : Location
    private lateinit var locationRequest : LocationRequest
    private var requestingLocationUpdates : Boolean = false
    private lateinit var locationCallback: LocationCallback
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private val UPDATE_INTERVAL: Long = 60000 // Every 60 seconds.

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    private val FASTEST_UPDATE_INTERVAL: Long = 30000 // Every 30 seconds

    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    private val MAX_WAIT_TIME = UPDATE_INTERVAL * 5 // Every 5 minutes.

    /**
     * The set priority when obtaining the location of the client. Could focus more on accuracy
     * or more on low battery usage
     */
    private val LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY // High accuracy preferred

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child)

        //Retrieve last known location of the client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { mCurrentLocation : Location? ->}

        setupLocationRequest()
        defineLocationCallback()
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
    }

    private fun setupLocationRequest() {
        //Setup location request
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(createLocationRequest())

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            requestingLocationUpdates = true
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    //exception.startResolutionForResult(this@Child, REQUEST_CHECK_SETTINGS) //TODO: write resolution (pop up)
                } catch (sendEx: IntentSender.SendIntentException) {
                    requestingLocationUpdates = false
                }
            }
        }
    }

    private fun createLocationRequest(): LocationRequest {
        locationRequest = LocationRequest.create().apply {
            interval = UPDATE_INTERVAL
            fastestInterval = FASTEST_UPDATE_INTERVAL
            maxWaitTime = MAX_WAIT_TIME
            priority = LOCATION_PRIORITY
        }
        return locationRequest
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }

    private fun defineLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations)
                    mCurrentLocation = location
            }
        }
    }
}
