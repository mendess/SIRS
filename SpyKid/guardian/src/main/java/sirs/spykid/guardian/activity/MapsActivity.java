package sirs.spykid.guardian.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import sirs.spykid.guardian.R;
import sirs.spykid.util.Child;
import sirs.spykid.util.Location;
import sirs.spykid.util.ServerApiKt;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Child child;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent intent = getIntent();
        child = intent.getParcelableExtra("child");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Call AsyncTask
        startLocationListener();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private synchronized void updateLocation(List<Location> locations) {
        Location location = locations.get(0);
        LatLng position = new LatLng(location.getX(), location.getY());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(position).title(child.getUsername()));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startLocationListener() {
        // Calling an async task inside another async task is a deadlock, so we use a thread
        new Thread(() -> {
            try {
                while (true) {
                    ServerApiKt.childLocation(child.getId(), r -> r.match(
                            ok -> updateLocation(ok.getLocations()),
                            error -> Toast.makeText(this, "Error fetching location", Toast.LENGTH_SHORT).show()
                    ));
                    Thread.sleep(3000);
                }
            } catch (InterruptedException e) {
                Toast.makeText(this, "Location loop stopped", Toast.LENGTH_SHORT).show();
                Log.d("ERROR", "Location loop stopped");
            }
        }).start();
    }
}
