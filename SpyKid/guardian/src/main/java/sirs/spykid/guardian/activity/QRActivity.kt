package sirs.spykid.guardian.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import sirs.spykid.guardian.R
import sirs.spykid.guardian.service.QRGenerator
import sirs.spykid.util.SharedKey

@RequiresApi(api = Build.VERSION_CODES.O)
class QRActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)
        val qrImage = findViewById<ImageView>(R.id.qr_image)
        val key = intent.getParcelableExtra<SharedKey>("key")
        if (key != null) {
            val bmp = QRGenerator.qrFromString(key.encoded)
            qrImage.setImageBitmap(bmp)
            qrImage.visibility = View.VISIBLE
        }
    }
}
