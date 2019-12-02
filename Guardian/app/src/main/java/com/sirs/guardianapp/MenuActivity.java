package com.sirs.guardianapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.sirs.guardianapp.service.QRGenerator;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private Button signOutButton;
    private FloatingActionButton addBeaconButton;
    private ImageView image;
    private ListView listView;


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
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MenuActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

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

        FirebaseUser user = getIntent().getParcelableExtra("User");
        if(user != null) {
            System.out.println(user.getEmail());
        }
        //API CALL -> get Beacons assigned to current guardian
        final List<String> list = new ArrayList<>();
        for(int i = 1 ; i < 5 ; i++) {
            list.add("Beacon " + i);
        }

        listView = findViewById(R.id.beacon_list);
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("Beacon user", (String) parent.getSelectedItem());
                startActivity(intent);
            }
        });

        //TODO: change this!!
        Bitmap bmp = QRGenerator.qrFromString("THIS IS MY SECRET KEY FOR THE CHILD BEACON APP");
        ImageView imageView = (ImageView) findViewById(R.id.qrcode_image_view);
        imageView.setImageBitmap(bmp);
        imageView.setVisibility(View.VISIBLE);

    }


}
