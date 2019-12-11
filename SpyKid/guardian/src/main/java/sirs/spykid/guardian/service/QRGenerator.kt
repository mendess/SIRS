package sirs.spykid.guardian.service

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

object QRGenerator {

    fun qrFromString(input: String): Bitmap? {
        val writer = QRCodeWriter()
        Log.d("INFO", "\"" + input + "\"")
        return try {
            val bitMatrix = writer.encode(input, BarcodeFormat.QR_CODE, 350, 350)
            val height = bitMatrix.height
            val width = bitMatrix.width
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }

    }
}
