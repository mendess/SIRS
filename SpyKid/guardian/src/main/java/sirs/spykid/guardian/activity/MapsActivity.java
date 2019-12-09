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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import sirs.spykid.guardian.R;
import sirs.spykid.util.Child;
import sirs.spykid.util.Location;
import sirs.spykid.util.ServerApiKt;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MapsActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Intent intent = getIntent();
        Child child = intent.getParcelableExtra("child");
        if (mapFragment != null) {
            mapFragment.getMapAsync(map -> startLocationListener(map, child));
        }
    }

    private void updateLocation(final GoogleMap map, final Child child, final List<Location> locations) {
        if (!locations.isEmpty()) {
            Location location = locations.get(0);
            if (location.getTimestamp().isBefore(LocalDateTime.now().minus(Duration.ofMinutes(10)))) {
                //TODO: make this a notification
                Toast.makeText(this, "Warning child hasn't said anything in a while", Toast.LENGTH_LONG).show();
            }
            LatLng position = new LatLng(location.getX(), location.getY());
            map.clear();
            map.addMarker(new MarkerOptions().position(position).title(child.getUsername()));
            map.moveCamera(CameraUpdateFactory.newLatLng(position));
        }
    }

    private void startLocationListener(final GoogleMap map, final Child child) {
        new Thread(() -> {
            try {
                while (true) {
                    ServerApiKt.childLocation(child.getId(), r -> r.match(
                            ok -> updateLocation(map, child, ok.getLocations()),
                            error -> Log.d("INFO", "Error fetching location")//Toast.makeText(this, "Error fetching location", Toast.LENGTH_SHORT).show()
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
