package sirs.spykid.guardian.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import sirs.spykid.guardian.R;
import sirs.spykid.util.EncryptionAlgorithm;
import sirs.spykid.util.ServerApiKt;
import sirs.spykid.util.SharedKey;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AddBeaconActivity extends AppCompatActivity {

    private EditText userInput;
    private EditText passInput;
    private TextView error;
    private EncryptionAlgorithm crypto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_beacon);
        userInput = findViewById(R.id.user_input);
        passInput = findViewById(R.id.password_input);
        error = findViewById(R.id.add_becon_error);
        crypto = new EncryptionAlgorithm(this);
        findViewById(R.id.submit_bttn).setOnClickListener(v -> createUser());
    }

    @SuppressLint("SetTextI18n")
    private void createUser() {
        String username = userInput.getText().toString();
        String password = passInput.getText().toString();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Invalid input, try again...", Toast.LENGTH_SHORT).show();
        } else {
            ServerApiKt.registerChild(username, password, r -> r.match(
                    ok -> showQRCode(),
                    err -> {
                        if (error == null) {
                            Log.d("INFO", "Error is null");
                        } else {
                            error.setText("Error registering child: " + err);
                        }
                    }
                    //err -> Toast.makeText(this, "Error registering child, please try again...", Toast.LENGTH_SHORT).show()
            ));
        }
    }

    private void showQRCode() {
        SharedKey key = crypto.generateSecretKey("SharedSecret");
        Intent intent = new Intent(getApplicationContext(), QRActivity.class);
        intent.putExtra("key", key);
        startActivity(intent);
        finish();
    }
}
