package sirs.spykid.child.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import sirs.spykid.child.R
import sirs.spykid.util.EncryptionAlgorithm
import sirs.spykid.util.Location
import sirs.spykid.util.updateChildLocation
import java.time.LocalDateTime
import java.util.*
import java.util.function.Consumer

@RequiresApi(Build.VERSION_CODES.O)
class BeaconActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beacon)
        findViewById<Button>(R.id.beacon_delete_key).setOnClickListener {
            EncryptionAlgorithm.deleteKey(EncryptionAlgorithm.SHARED_SECRET_NAME)
            finish()
        }
        startLocationBroadcaster()
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
