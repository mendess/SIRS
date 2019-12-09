package sirs.spykid.child.activity

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import sirs.spykid.child.R
import sirs.spykid.util.EncryptionAlgorithm
import sirs.spykid.util.loginChild
import java.util.function.Consumer

@RequiresApi(api = Build.VERSION_CODES.N)
@TargetApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {

    private var providers: List<AuthUI.IdpConfig>? = null
    private var buttonSignIn: Button? = null
    private val MY_REQUEST_CODE = 7117

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child)
        //Initialize providers
        providers = listOf(AuthUI.IdpConfig.GoogleBuilder().build())

        buttonSignIn = findViewById<View>(R.id.button_sign_in) as Button
        findViewById<View>(R.id.signin).setOnClickListener { v ->
            normalSignIn(
                this.findViewById<EditText>(R.id.username).text.toString(),
                this.findViewById<EditText>(R.id.password).text.toString()
            )
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            val idpResponse = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                //Get User
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    login(user)
                }

            } else {
                Toast.makeText(this, "" + idpResponse!!.error!!.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startActivityAfterLogin(user: String) {
        val intent = Intent(applicationContext, QRActivity::class.java)
        startActivity(intent)
        finish()
        val key = EncryptionAlgorithm(this).generateSecretKey("SharedSecret")
        val intent2 = Intent(applicationContext, BeaconActivity::class.java).putExtra("key", key)
        startActivity(intent2)
/*
        val intent = Intent(applicationContext, MenuActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
 */
    }

    private fun showSignInOptions() {
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers!!)
                .setTheme(R.style.AppTheme)
                .build(), MY_REQUEST_CODE
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun login(user: FirebaseUser) {
        loginChild(user.email!!, user.uid, Consumer { response ->
            response.match(
                Consumer { ok -> run{startActivityAfterLogin(user.email!!)} },
                Consumer { err ->
                    run { Toast.makeText(this, err.name, Toast.LENGTH_SHORT).show() }
                })
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun normalSignIn(user: String, password: String) {
        loginChild(user, password, Consumer { response ->
            response.match(
                Consumer { ok -> run{startActivityAfterLogin(user)} },
                Consumer { err ->
                    run { Toast.makeText(this, err.name, Toast.LENGTH_SHORT).show() }
                })
        })
    }


}


