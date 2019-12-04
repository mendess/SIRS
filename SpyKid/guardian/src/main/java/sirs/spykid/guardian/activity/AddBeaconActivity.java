package sirs.spykid.guardian.activity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.sirs.guardianapp.service.EncryptionService;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import sirs.spykid.guardian.R;

public class AddBeaconActivity extends AppCompatActivity {

    //Add Button
    //Remove?

    private static final int keySize = 128;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_beacon);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Add beacon");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Toast.makeText(this, "CREATE BEACON ACTIVITY", Toast.LENGTH_SHORT).show();

        SecretKey key = null;
        try {
            key = new EncryptionService().generateSecretKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if(key != null) {
            String qrString = key.getEncoded().toString();
        }


    }

  /*  @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

}
