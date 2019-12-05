package sirs.spykid.guardian.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

import java.security.Key;

import javax.crypto.SecretKey;

import sirs.spykid.guardian.R;
import sirs.spykid.guardian.model.BeaconUser;
import sirs.spykid.util.Child;
import sirs.spykid.util.ChildId;
import sirs.spykid.util.EncryptionAlgorithm;
import sirs.spykid.util.GuardianToken;
import sirs.spykid.util.ServerApiKt;

public class AddBeaconActivity extends AppCompatActivity {

    private Button createUserButton;
    private EditText userInput;
    private EditText passInput;
    private EncryptionAlgorithm ea;
    private GuardianToken guardianToken;
    private FirebaseDatabase database;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_beacon);

        createUserButton = findViewById(R.id.add_beacon_button);
        userInput = findViewById(R.id.user_input);
        passInput = findViewById(R.id.password_input);
        ea = new EncryptionAlgorithm();

        createUserButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                createUser();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createUser() {

        String username = userInput.getText().toString();
        String password = passInput.getText().toString();

        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Invalid input, try again...", Toast.LENGTH_SHORT).show();
        }
        else {
            ServerApiKt.registerChild(r -> r.match(
                    ok  -> saveChild(ok.getChildId()),
                    err -> Toast.makeText(this, "Error registering child, please try again...", Toast.LENGTH_SHORT).show()
            ),guardianToken, username, password);

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void saveChild(ChildId childId) {
        Key key = null;
        try {
            key = ea.generateSecretKey("SharedSecret");
        } catch (Exception e) {
            e.printStackTrace();
        }
        BeaconUser beaconUser = new BeaconUser(key, childId);

        //SAVE TO DATABASE -> BEACONUSER


    }


}
