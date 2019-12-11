package sirs.spykid.guardian.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

import sirs.spykid.guardian.R
import sirs.spykid.util.EncryptionAlgorithm
import sirs.spykid.util.*
import java.util.function.Consumer


@RequiresApi(api = Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {

    private var error: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EncryptionAlgorithm.get(this)

        error = findViewById(R.id.main_error)
        findViewById<Button>(R.id.signin).setOnClickListener {
            normalSignIn(
                this.findViewById<EditText>(R.id.username).text.toString().trim { it <= ' ' },
                this.findViewById<EditText>(R.id.password).text.toString().trim { it <= ' ' }
            )
        }
        findViewById<Button>(R.id.signup).setOnClickListener {
            normalSignUp(
                this.findViewById<EditText>(R.id.username).text.toString().trim { it <= ' ' },
                this.findViewById<EditText>(R.id.password).text.toString().trim { it <= ' ' }
            )
        }
    }

    private fun startActivityAfterLogin() {
        val intent = Intent(applicationContext, MenuActivity::class.java)
        startActivity(intent)
    }

    @SuppressLint("SetTextI18n")
    private fun normalSignUp(user: String, password: String) {
        registerGuardian(user, password, Consumer { r ->
            r.match(
                Consumer { startActivityAfterLogin() },
                Consumer { err -> runOnUiThread { error!!.text = "Error registering: $err" } }
            )
        })
    }

    @SuppressLint("SetTextI18n")
    private fun normalSignIn(user: String, password: String) {
        loginGuardian(user, password, Consumer { r ->
            r.match(
                Consumer { startActivityAfterLogin() },
                Consumer { err -> runOnUiThread { error!!.text = "Error logging in: $err" } }
            )
        })
    }
}


