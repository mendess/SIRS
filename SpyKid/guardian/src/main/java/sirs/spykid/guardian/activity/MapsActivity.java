package sirs.spykid.guardian.activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import sirs.spykid.guardian.R;
import sirs.spykid.util.Child;
import sirs.spykid.util.Location;
import sirs.spykid.util.ServerApiKt;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MapsActivity extends FragmentActivity {

    private static final String NOTF_CHANNEL_ID = "Alerts";

    private Notification missingChild;

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
        createNotificationChannel();
        this.missingChild = new NotificationCompat.Builder(this, NOTF_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Oh no 😱")
                .setContentText("We can't find your child!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
    }

    private void updateLocation(final GoogleMap map, final Child child, final List<Location> locations) {
        if (!locations.isEmpty()) {
            Location location = locations.get(0);
            if (location.getTimestamp().isBefore(LocalDateTime.now().minus(Duration.ofMinutes(10)))) {
                NotificationManagerCompat.from(this).notify(0, missingChild);
            }
            Log.d("INFO", "Location" + location);
            LatLng position = new LatLng(location.getX(), location.getY());
            this.runOnUiThread(() -> {
                map.clear();
                map.addMarker(new MarkerOptions().position(position).title(child.getUsername()));
                map.moveCamera(CameraUpdateFactory.newLatLng(position));
            });
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = getString(R.string.channel_name);
            String descriptionText = getString(R.string.channel_descritpion);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTF_CHANNEL_ID, name, importance);
            channel.setDescription(descriptionText);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }
    }
}
