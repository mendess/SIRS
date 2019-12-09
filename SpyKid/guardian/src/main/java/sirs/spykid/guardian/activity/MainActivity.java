package sirs.spykid.guardian.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.List;

import sirs.spykid.guardian.R;
import sirs.spykid.util.ServerApiKt;


@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity {

    private List<AuthUI.IdpConfig> providers;
    private static final int MY_REQUEST_CODE = 7117;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initialize providers
        providers = Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build());

        findViewById(R.id.firebase_signin).setOnClickListener(v -> showSignInOptions());
        findViewById(R.id.signin).setOnClickListener(v -> normalSignIn(
                this.<EditText>findViewById(R.id.username).getText().toString(),
                this.<EditText>findViewById(R.id.password).getText().toString()
        ));
        findViewById(R.id.signup).setOnClickListener(v -> normalSignUp(
                this.<EditText>findViewById(R.id.username).getText().toString(),
                this.<EditText>findViewById(R.id.password).getText().toString()
        ));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            IdpResponse idpResponse = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                //Get User
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    login(user);
                } else {
                    Toast.makeText(this, "Failed to authenticate", Toast.LENGTH_SHORT).show();
                }

            } else {
                if (idpResponse != null && idpResponse.getError() != null) {
                    Toast.makeText(this, idpResponse.getError().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "idpResponse is null", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void startActivityAfterLogin(FirebaseUser user) {
        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    private void startActivityAfterLogin() {
        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
        startActivity(intent);
    }

    private void showSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.MyTheme)
                        .build(), MY_REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void login(@NonNull FirebaseUser user) {
        //noinspection ConstantConditions
        ServerApiKt.registerGuardian(user.getEmail(), user.getUid(), r -> r.match(
                ok -> startActivityAfterLogin(user),
                err -> Toast.makeText(this, "Error connecting to server...", Toast.LENGTH_SHORT).show()
        ));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void normalSignUp(@NonNull String user, String password) {
        Toast.makeText(this, "User" + user + " Password " + password, Toast.LENGTH_SHORT).show();
        ServerApiKt.registerGuardian(user, password, r -> r.match(
                ok -> startActivityAfterLogin(),
                err -> Toast.makeText(this, "Error connecting to server...", Toast.LENGTH_SHORT).show()
        ));
    }

    private void normalSignIn(String user, String password) {
        ServerApiKt.loginGuardian(user, password, r -> r.match(
                ok -> startActivityAfterLogin(),
                err -> Toast.makeText(this, "Error connecting to server...", Toast.LENGTH_SHORT).show()
        ));
    }
}


