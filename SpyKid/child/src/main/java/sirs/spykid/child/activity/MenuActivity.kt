package sirs.spykid.child.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import sirs.spykid.child.R
import sirs.spykid.util.EncryptionAlgorithm

@RequiresApi(api = Build.VERSION_CODES.O)
class MenuActivity : AppCompatActivity() {

    private lateinit var user: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        user = intent.getStringExtra("user")!!

        findViewById<Button>(R.id.go_to_scanner).setOnClickListener {
            val intent = Intent(applicationContext, QRScanner::class.java)
                .putExtra("user", user)
            startActivityForResult(intent, 0)
        }
        initBroadcastButton()
    }

    private fun initBroadcastButton() {
        if (EncryptionAlgorithm.get(this).getKey(user) != null) {
            findViewById<Button>(R.id.start_beacon).setOnClickListener {
                val intent = Intent(applicationContext, BeaconActivity::class.java)
                    .putExtra("user", user)
                startActivity(intent)
            }
        } else {
            findViewById<Button>(R.id.start_beacon).setOnClickListener {
                Toast.makeText(this, "Please scan a key first", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        initBroadcastButton()
    }

}