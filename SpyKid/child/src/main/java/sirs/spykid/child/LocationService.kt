package sirs.spykid.child

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.IntentSender
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import android.os.Binder


@SuppressLint("Registered")
class LocationService : Service() {

    val PACKAGE_NAME =
        "com.google.android.gms.location.sample.locationupdatesforegroundservice"

    /**
     * The name of the channel for notifications.
     */
    private val CHANNEL_ID = "channel_01"

    val ACTION_BROADCAST = "$PACKAGE_NAME.broadcast"

    val EXTRA_LOCATION = "$PACKAGE_NAME.location"
    private val EXTRA_STARTED_FROM_NOTIFICATION = "$PACKAGE_NAME.started_from_notification"

    private val mBinder = LocalBinder()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var mCurrentLocation : Location
    private lateinit var locationRequest : LocationRequest
    var requestingLocationUpdates : Boolean = false
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

    override fun onCreate() {
        //Retrieve last known location of the client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { mCurrentLocation : Location? ->}

        setupLocationRequest()
        defineLocationCallback()
    }

    override fun onBind(intent: Intent?): IBinder? {
        stopForeground(true)
        return mBinder
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        internal val service: LocationService
            get() = this@LocationService
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
                print("Loacation settings are not satisfied")
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

    fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper())
    }

    private fun defineLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations)
                    onNewLocation(location)
            }
        }

    }

    private fun onNewLocation(location: Location) {
        mCurrentLocation = location

        // Notify anyone listening for broadcasts about the new location.
        val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_LOCATION, location)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    fun getLocationText(location: Location): String {
        return "The current location of your child is " + location.longitude + " longuitude " + location.latitude + " latitude."
    }


}