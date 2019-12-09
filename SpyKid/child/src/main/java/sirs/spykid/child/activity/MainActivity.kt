package sirs.spykid.child.activity

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import sirs.spykid.child.R
import sirs.spykid.util.loginChild
import java.util.function.Consumer

@RequiresApi(api = Build.VERSION_CODES.N)
@TargetApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Initialize providers

        findViewById<Button>(R.id.signIn).setOnClickListener {
            normalSignIn(
                this.findViewById<EditText>(R.id.username).text.toString(),
                this.findViewById<EditText>(R.id.password).text.toString()
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startActivityAfterLogin(user: String) {
        val intent = Intent(applicationContext, QRActivity::class.java)
        startActivity(intent)
/*
        val key = EncryptionAlgorithm(this).generateSecretKey("SharedSecret")
        val intent2 = Intent(applicationContext, BeaconActivity::class.java).putExtra("key", key)
        startActivity(intent2)
*/
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun normalSignIn(user: String, password: String) {
        loginChild(user, password, Consumer { response ->
            response.match(
                Consumer { run { startActivityAfterLogin(user) } },
                Consumer { err ->
                    run { Toast.makeText(this, err.name, Toast.LENGTH_SHORT).show() }
                })
        })
    }
}

