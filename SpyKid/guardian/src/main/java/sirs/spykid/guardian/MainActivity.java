package sirs.spykid.guardian;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
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
import java.util.concurrent.ExecutionException;

import kotlin.Pair;
import kotlin.Unit;
import sirs.spykid.util.Responses;
import sirs.spykid.util.Result;
import sirs.spykid.util.ServerApiKt;


public class MainActivity extends AppCompatActivity {

    private List<AuthUI.IdpConfig> providers;
    private static final int MY_REQUEST_CODE = 7117;
    Button buttonSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize providers
        providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());

        //Sign In Button
        buttonSignIn = (Button) findViewById(R.id.btn_sign_in);
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignInOptions();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            IdpResponse idpResponse = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                //Get User
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Toast.makeText(this, "" + user.getEmail(), Toast.LENGTH_SHORT).show();

                //Switch to Menu Activity
                Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                intent.putExtra("User", user);
                startActivity(intent);

            } else {
                Toast.makeText(this, "" + idpResponse.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
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
    private void login(String user, String pass) {
        ServerApiKt.registerGuardian(r -> r.match(
                ok -> System.out.println("Logged in! " + ok.getGuardianToken()),
                err -> System.out.println("Error! " + err.toString())
        ), user, pass);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void listChildren() {
        ServerApiKt.listChildren(r -> r.match(
                ok -> System.out.println(ok.getChildren()),
                System.out::println
        ), null);
    }
}


