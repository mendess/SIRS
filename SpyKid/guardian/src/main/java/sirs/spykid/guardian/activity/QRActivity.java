package sirs.spykid.guardian.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import sirs.spykid.guardian.R;
import sirs.spykid.guardian.service.QRGenerator;
import sirs.spykid.util.SharedKey;

@RequiresApi(api = Build.VERSION_CODES.O)
public class QRActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        ImageView qrImage = findViewById(R.id.qr_image);
        Intent intent = getIntent();
        SharedKey key = intent.getParcelableExtra("key");
        if(key != null) {
            Bitmap bmp = QRGenerator.qrFromString(key.getEncoded());
            qrImage.setImageBitmap(bmp);
            qrImage.setVisibility(View.VISIBLE);
        }
    }
}
