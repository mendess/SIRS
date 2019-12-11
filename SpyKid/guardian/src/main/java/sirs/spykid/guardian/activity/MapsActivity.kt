package sirs.spykid.guardian.activity

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import sirs.spykid.guardian.R
import sirs.spykid.util.Child
import sirs.spykid.util.Location
import sirs.spykid.util.childLocation
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import java.util.function.Consumer

@RequiresApi(api = Build.VERSION_CODES.O)
class MapsActivity : FragmentActivity() {

    private var missingChild: Notification? = null
    private var sos: Notification? = null
    private val seenLocations = TreeSet<Location>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        val intent = intent
        val child = intent.getParcelableExtra<Child>("child")
        mapFragment?.getMapAsync { map -> startLocationListener(map, child) }
        createNotificationChannel()
        this.missingChild = NotificationCompat.Builder(this, NOTIFY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Oh no 😱")
            .setContentText("We can't find your child!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()
        this.sos = NotificationCompat.Builder(this, NOTIFY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("️⚠️ Your child has sent an sos! ⚠️")
            .setContentText("Go save them!!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()
    }

    private fun updateLocation(map: GoogleMap, child: Child?, locations: List<Location>) {
        for (l in locations) {
            if (seenLocations.contains(l)) continue
            if (l.sos) {
                NotificationManagerCompat.from(this).notify(1, sos!!)
            }
            Log.d("INFO", "Location$l")
            synchronized(seenLocations) {
                seenLocations.add(l)
            }
            val position = LatLng(l.x, l.y)
            this.runOnUiThread {
                map.addMarker(MarkerOptions().position(position).title(child!!.username))
                map.moveCamera(CameraUpdateFactory.newLatLng(position))
            }
        }
        if (!seenLocations.isEmpty() && seenLocations.last().timestamp.isBefore(
                LocalDateTime.now().minus(
                    Duration.ofMinutes(5)
                )
            )
        ) {
            NotificationManagerCompat.from(this).notify(0, missingChild!!)
        }
    }

    private fun startLocationListener(map: GoogleMap, child: Child?) {
        Thread {
            try {
                while (true) {
                    childLocation(child!!.id, child.username, Consumer { r ->
                        r.match(
                            Consumer { ok -> updateLocation(map, child, ok.locations) },
                            Consumer { error ->
                                runOnUiThread {
                                    Toast.makeText(
                                        this,
                                        "Error fetching location: $error", Toast.LENGTH_SHORT
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFY_CHANNEL_ID, name, importance)
            channel.description = descriptionText
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel)
        }
    }

    companion object {
        private const val NOTIFY_CHANNEL_ID = "Alerts"
    }
}
