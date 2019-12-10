package sirs.spykid.guardian.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import sirs.spykid.guardian.R;
import sirs.spykid.util.Child;
import sirs.spykid.util.ServerApiKt;

public class MenuActivity extends AppCompatActivity {

    private Button signOutButton;
    private FloatingActionButton addBeaconButton;
    private ImageView image;
    private ListView listView;
    private TextView error;

    private List<PrettyChild> children = new ArrayList<>();
//    private FirebaseUser user;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        error = findViewById(R.id.menu_error);
        signOutButton = findViewById(R.id.btn_sign_out);
        signOutButton.setOnClickListener(v -> {
            //Logout
            AuthUI.getInstance()
                    .signOut(MenuActivity.this)
                    .addOnCompleteListener(task -> {
                        Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                        startActivity(intent);
                    }).addOnFailureListener(e -> Toast.makeText(MenuActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show());

        });
        addBeaconButton = findViewById(R.id.add_beacon_button);
        addBeaconButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AddBeaconActivity.class);
            startActivity(intent);
            listChildren();
        });

/*
        user = getIntent().getParcelableExtra("user");
        if(user == null)
            Toast.makeText(this, "Invalid user and/or guardian token", Toast.LENGTH_SHORT).show();
*/

        findViewById(R.id.refresh_button).setOnClickListener(v -> {
            listChildren();
        });

        listChildren();
        listView = findViewById(R.id.beacon_list);
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, children);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Child child = children.get(position).child;
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            intent.putExtra("child", child);
            startActivity(intent);
        });

    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void listChildren() {
        ServerApiKt.listChildren(r -> r.match(
                ok -> runOnUiThread(() -> children.addAll(ok.getChildren().stream().map(PrettyChild::new).collect(Collectors.toList()))),
                err -> runOnUiThread(() -> error.setText("Error listing children: " + err))
        ));
    }

    private class PrettyChild {
        private Child child;

        private PrettyChild(Child child) {
            this.child = child;
        }

        @NonNull
        @Override
        public String toString() {
            return child.getUsername();
        }
    }
}
