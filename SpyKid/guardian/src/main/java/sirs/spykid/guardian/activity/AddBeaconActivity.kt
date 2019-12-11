package sirs.spykid.guardian.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import sirs.spykid.guardian.R
import sirs.spykid.util.EncryptionAlgorithm
import sirs.spykid.util.registerChild
import java.util.function.Consumer

@RequiresApi(api = Build.VERSION_CODES.O)
class AddBeaconActivity : AppCompatActivity() {

    private var userInput: EditText? = null
    private var passInput: EditText? = null
    private var error: TextView? = null
    private var crypto: EncryptionAlgorithm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_beacon)
        userInput = findViewById(R.id.user_input)
        passInput = findViewById(R.id.password_input)
        error = findViewById(R.id.add_beacon_error)
        crypto = EncryptionAlgorithm.get(this)
        findViewById<Button>(R.id.submit_bttn).setOnClickListener { createUser() }
    }

    @SuppressLint("SetTextI18n")
    private fun createUser() {
        val username = userInput!!.text.toString()
        val password = passInput!!.text.toString()
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Invalid input, try again...", Toast.LENGTH_SHORT).show()
        } else {
            registerChild(username, password, Consumer { r ->
                r.match(
                    Consumer { showQRCode(username) },
                    Consumer { err -> runOnUiThread { error!!.text = "Error registering child: $err" } }
                )
            })
        }
    }

    private fun showQRCode(username: String) {
        val key = crypto!!.generateSecretKey(username)
        val intent = Intent(applicationContext, QRActivity::class.java)
        intent.putExtra("key", key)
        startActivity(intent)
        finish()
    }
}
