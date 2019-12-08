package sirs.spykid.guardian.activity;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import javax.crypto.SecretKey;

import sirs.spykid.guardian.R;
import sirs.spykid.util.Child;
import sirs.spykid.util.ChildId;
import sirs.spykid.util.ChildToken;
import sirs.spykid.util.GuardianToken;
import sirs.spykid.util.Location;
import sirs.spykid.util.ServerApiKt;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GuardianToken guardianToken;
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
        guardianToken = intent.getParcelableExtra("guardianToken");

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

    class LocationBackgroundTask extends AsyncTask<Void, Void, Location> {

        @RequiresApi(api = Build.VERSION_CODES.O)
        private void getLocation() {
            ServerApiKt.childLocation(r -> r.match(
                    ok -> updateLocation(ok.getLocations()),
                    error -> Toast.makeText(MapsActivity.this, "Error getting location...", Toast.LENGTH_SHORT).show()
            ), guardianToken, child.getId());
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Location doInBackground(Void... voids) {
            while(true) {
                getLocation();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
