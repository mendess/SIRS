package sirs.spykid.guardian.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import sirs.spykid.guardian.R;
import sirs.spykid.util.EncryptionAlgorithm;
import sirs.spykid.util.ServerApiKt;


@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    private TextView error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EncryptionAlgorithm.Companion.get(this);

        error = findViewById(R.id.main_error);
        findViewById(R.id.signin).setOnClickListener(v -> normalSignIn(
                this.<EditText>findViewById(R.id.username).getText().toString().trim(),
                this.<EditText>findViewById(R.id.password).getText().toString().trim()
        ));
        findViewById(R.id.signup).setOnClickListener(v -> normalSignUp(
                this.<EditText>findViewById(R.id.username).getText().toString().trim(),
                this.<EditText>findViewById(R.id.password).getText().toString().trim()
        ));
    }

    private void startActivityAfterLogin() {
        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
        startActivity(intent);
    }

    @SuppressLint("SetTextI18n")
    private void normalSignUp(@NonNull String user, String password) {
        ServerApiKt.registerGuardian(user, password, r -> r.match(
                ok -> startActivityAfterLogin(),
                err -> runOnUiThread(() -> error.setText("Error registering: " + err))
        ));
    }

    @SuppressLint("SetTextI18n")
    private void normalSignIn(String user, String password) {
        ServerApiKt.loginGuardian(user, password, r -> r.match(
                ok -> startActivityAfterLogin(),
                err -> runOnUiThread(() -> error.setText("Error logging in: " + err))
        ));
    }
}


