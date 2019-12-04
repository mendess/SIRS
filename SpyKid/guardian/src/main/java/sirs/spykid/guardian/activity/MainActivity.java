package sirs.spykid.guardian.activity;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import sirs.spykid.guardian.R;
import sirs.spykid.util.GuardianToken;
import sirs.spykid.util.ServerApiKt;


public class MainActivity extends AppCompatActivity {

    private List<AuthUI.IdpConfig> providers;
    private static final int MY_REQUEST_CODE = 7117;
    private Button buttonSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initialize providers
        providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());

        buttonSignIn = (Button) findViewById(R.id.btn_sign_in);
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignInOptions();
            }
        });
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
                if(user != null)
                    login(user);

            } else {
                Toast.makeText(this, "" + idpResponse.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startActivityAfterLogin(FirebaseUser user, GuardianToken token) {
        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
        intent.putExtra("User", user);
        //TODO
        intent.putExtra("Token", token.toString());
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
    private void login(FirebaseUser user) {
        ServerApiKt.registerGuardian(r -> r.match(
                ok -> startActivityAfterLogin(user, ok.getGuardianToken()),
                err -> Toast.makeText(this, "Error connecting to server..." , Toast.LENGTH_SHORT).show()
        ), user.getEmail(), user.getUid());
    }


}


