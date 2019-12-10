package sirs.spykid.child.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import sirs.spykid.child.R

class MenuActivity : AppCompatActivity() {

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        findViewById<Button>(R.id.button_done).setOnClickListener {
            //Logout
            AuthUI.getInstance()
                .signOut(this@MenuActivity)
                .addOnCompleteListener {
                    val intent = Intent(
                        this@MenuActivity,
                        MainActivity::class.java
                    )
                    startActivity(intent)
                }.addOnFailureListener { e: Exception ->
                    Toast.makeText(
                        this@MenuActivity,
                        "" + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        findViewById<Button>(R.id.go_to_scanner).setOnClickListener {
            val intent = Intent(
                applicationContext,
                QRScanner::class.java
            )
            startActivity(intent)
        }

        findViewById<Button>(R.id.start_beacon).setOnClickListener {
            val intent = Intent(
                applicationContext,
                BeaconActivity::class.java
            )
            startActivity(intent)
        }

    }

}