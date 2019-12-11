package sirs.spykid.child.activity

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import sirs.spykid.child.R
import sirs.spykid.util.EncryptionAlgorithm
import sirs.spykid.util.loginChild
import java.util.function.Consumer

@RequiresApi(api = Build.VERSION_CODES.N)
@TargetApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {

    private lateinit var error: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Initialize EncryptionAlgorithm
        findViewById<Button>(R.id.signIn).setOnClickListener {
            normalSignIn(
                this.findViewById<EditText>(R.id.username).text.toString().trim(),
                this.findViewById<EditText>(R.id.password).text.toString().trim()
            )
        }
        this.error = findViewById(R.id.main_error)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startActivityAfterLogin(user: String) {
        EncryptionAlgorithm.get(this)
        val intent = Intent(applicationContext, MenuActivity::class.java).putExtra("user", user)
        startActivity(intent)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun normalSignIn(user: String, password: String) {
        loginChild(user, password, Consumer { response ->
            response.match(
                Consumer { startActivityAfterLogin(user) },
                Consumer { err -> runOnUiThread { error.text = "Error logging in: $err" } })
        })
    }
}


