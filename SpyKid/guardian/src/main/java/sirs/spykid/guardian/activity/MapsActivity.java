package sirs.spykid.guardian.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Call AsyncTask
        new LocationBackgroundTask().execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateLocation(List<Location> locations) {
        Location location = locations.get(0);
        LatLng position = new LatLng(location.getX(), location.getY());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(position).title(child.getUsername()));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
    }

    class LocationBackgroundTask extends AsyncTask<Void, Void, Void> {

        @RequiresApi(api = Build.VERSION_CODES.O)
        private void getLocation() {
            ServerApiKt.childLocation(child.getId(), r -> r.match(
                    ok -> updateLocation(ok.getLocations()),
                    error -> {
                        // TODO: Warn the user
                    }
            ));
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                getLocation();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }

}
