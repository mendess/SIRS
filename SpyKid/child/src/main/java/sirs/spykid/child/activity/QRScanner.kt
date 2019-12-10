package sirs.spykid.child.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import sirs.spykid.util.EncryptionAlgorithm
import sirs.spykid.util.SharedKey


@RequiresApi(Build.VERSION_CODES.O)
class QRScanner : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val intent = Intent("com.google.zxing.client.android.SCAN")
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE")
            startActivityForResult(intent, 0)
        } catch (e: Exception) {
            val marketUri = Uri.parse("market://details?id=com.google.zxing.client.android")
            val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
            startActivity(marketIntent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                if (null != data?.getStringExtra("SCAN_RESULT")
                        ?.takeIf { it.isNotEmpty() }
                        ?.let {
                            EncryptionAlgorithm.get(this)
                                .storeSecretKey(
                                    EncryptionAlgorithm.KeyStores.SharedSecret,
                                    SharedKey.decode(it)
                                )
                        }
                ) {
                    Log.d("INFO", "Got a key")
                } else {
                    Log.d("INFO", "Invalid key: " + data?.getStringExtra("SCAN_RESULT"))
                }
            } else {
                Log.d("INFO", "SCAN CANCELED")
            }
        }
    }
}