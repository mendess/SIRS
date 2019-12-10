package sirs.spykid.child.activity

import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import sirs.spykid.child.R
import sirs.spykid.util.EncryptionAlgorithm
import sirs.spykid.util.Location
import sirs.spykid.util.updateChildLocation
import java.time.LocalDateTime
import java.util.*
import java.util.function.Consumer

@RequiresApi(Build.VERSION_CODES.O)
class BeaconActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: android.location.Location
    private lateinit var locationRequest : LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beacon)
        findViewById<Button>(R.id.beacon_delete_key).setOnClickListener {
            EncryptionAlgorithm.deleteKey(EncryptionAlgorithm.SHARED_SECRET_NAME)
            finish()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }!!

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){

                    //TODO send locations to server (they come with lat, long and timestamp)

                }
            }
        }
        startLocationUpdates()


      /*  fusedLocationClient.lastLocation.addOnSuccessListener { location : android.location.Location? ->
            if (location != null) {
                lastLocation = location
            }
        }*/
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper())
    }




    private fun startLocationBroadcaster() {
        // Calling an async task inside another async task is a deadlock, so we use a thread
        Thread {
            try {
                while (true) {
                    updateChildLocation(getRandomLocation(), Consumer { r ->
                        r.match(
                            Consumer { },
                            Consumer {
                                runOnUiThread {
                                    Toast.makeText(
                                        this, "Error sending location",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    })
                    Thread.sleep(3000)
                }
            } catch (e: InterruptedException) {
                Toast.makeText(this, "Location loop stopped", Toast.LENGTH_SHORT).show()
                Log.d("ERROR", "Location loop stopped")
            }
        }.start()
    }

    private fun getRandomLocation(): Location {
        val random = Random()
        return Location(random.nextDouble() * 100, random.nextDouble() * 100, LocalDateTime.now())
    }




}
