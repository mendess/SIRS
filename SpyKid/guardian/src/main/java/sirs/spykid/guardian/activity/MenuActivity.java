package sirs.spykid.guardian.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sirs.spykid.guardian.R;
import sirs.spykid.guardian.service.QRGenerator;
import sirs.spykid.util.Child;
import sirs.spykid.util.ChildToken;
import sirs.spykid.util.GuardianToken;
import sirs.spykid.util.ServerApiKt;

public class MenuActivity extends AppCompatActivity {

    private Button signOutButton;
    private FloatingActionButton addBeaconButton;
    private ImageView image;
    private ListView listView;

    private List<Child> children = new ArrayList<>();
    private FirebaseUser user;
    private GuardianToken guardianToken;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        signOutButton = findViewById(R.id.btn_sign_out);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Logout
                AuthUI.getInstance()
                        .signOut(MenuActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }).addOnFailureListener(e -> Toast.makeText(MenuActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show());

            }
        });
        addBeaconButton = findViewById(R.id.add_beacon_button);
        addBeaconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddBeaconActivity.class);
                startActivity(intent);
            }
        });

        user = getIntent().getParcelableExtra("user");
        guardianToken = getIntent().getParcelableExtra("guardianToken");
        if(user == null || guardianToken == null)
            Toast.makeText(this, "Invalid user and/or guardian token", Toast.LENGTH_SHORT).show();

        listChildren(guardianToken);
        listView = findViewById(R.id.beacon_list);
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, children);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            Child child = (Child) parent.getSelectedItem();
            startMapActivity(child);
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void listChildren(GuardianToken token) {
        ServerApiKt.listChildren(r -> r.match(
                ok -> children.addAll(ok.getChildren()),
                error -> Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
        ), token);
    }

    private void startMapActivity(Child child) {
        Intent intent = getIntent();
        intent.putExtra("child", child);
        intent.putExtra("guardianToken", guardianToken);
        startActivity(intent);
        finish();
    }

}
