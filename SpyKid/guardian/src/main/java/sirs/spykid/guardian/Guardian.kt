package sirs.spykid.guardian

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import sirs.spykid.util.registerGuardian

@RequiresApi(Build.VERSION_CODES.N)
class Guardian : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Example usage or ServerApi
        val id = registerGuardian("user", "pass")
    }
}
