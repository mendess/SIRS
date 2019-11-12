package sirs.spykid.child

import android.content.*
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import sirs.spykid.child.Beacon as ChildBeacon

class Child : AppCompatActivity() {

    // Beacon of the child
    private lateinit var mBeacon: ChildBeacon

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private var myReceiver: MyReceiver? = null

    // A reference to the service used to get location updates.
    private var mService: LocationService? = null

    // Tracks the bound state of the service.
    private var mBound = false

    // Monitors the state of the connection to the service.
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationService.LocalBinder
            mService = binder.service
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myReceiver = MyReceiver()
        setContentView(R.layout.activity_child)
        // Create beacon
        mBeacon = ChildBeacon()
        mBeacon.register()
    }

    override fun onResume() {
        super.onResume()
        if (mService != null) {
            if (mService?.requestingLocationUpdates!!) {
                mService?.startLocationUpdates()
            }
        }
    }

    /**
     * Receiver for broadcasts sent by [LocationService].
     */
    private inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(mService?.EXTRA_LOCATION)
            if (location != null) {
                mBeacon.updateLocation(location)
            }
        }
    }

}
