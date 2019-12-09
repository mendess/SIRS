package sirs.spykid.child.activity

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import sirs.spykid.child.R
import sirs.spykid.util.EncryptionAlgorithm
import sirs.spykid.util.SharedKey

@RequiresApi(Build.VERSION_CODES.O)
class QRActivity : AppCompatActivity() {

    private var qrScanner: ImageView? = null
    private var buttonScan: Button? = null
    private var imageUri: Uri? = null
    private var scanResults: TextView? = null
    private var detector: BarcodeDetector? = null
    private val IMAGE_CAPTURE_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)
        qrScanner = findViewById(R.id.qr_scan)

        buttonScan = findViewById(R.id.scan)
        buttonScan!!.setOnClickListener { openCamera() }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New QR-code")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    @ExperimentalStdlibApi
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {
            detector = BarcodeDetector.Builder(applicationContext)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build()
            if (!detector!!.isOperational) {
                scanResults!!.text = "Could not set up the detector!"
                return
            }
            val input = imageUri?.let(this.contentResolver::openInputStream)
            val bitmap = BitmapFactory.decodeStream(input)
            if (detector!!.isOperational && bitmap != null) {
                val frame = Frame.Builder().setBitmap(bitmap).build()
                val barcode = detector!!.detect(frame)
                val encodedKey = StringBuilder()
                for (index in 0 until barcode.size()) {
                    val code = barcode.valueAt(index).displayValue
                    encodedKey.append(code)
                }
                val key = SharedKey.decode(encodedKey.toString())
                val encryptionAlgorithm = EncryptionAlgorithm(this)
                encryptionAlgorithm.storeSecretKey("SharedSecret", key)
            }
        }
    }
}
