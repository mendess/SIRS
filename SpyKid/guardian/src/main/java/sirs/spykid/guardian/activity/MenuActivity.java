package sirs.spykid.guardian.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import sirs.spykid.guardian.R;
import sirs.spykid.util.Child;
import sirs.spykid.util.ServerApiKt;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MenuActivity extends AppCompatActivity {

    private ListView listView;
    private List<PrettyChild> children = new ArrayList<>();
    private TextView error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        error = findViewById(R.id.menu_error);
        findViewById(R.id.add_beacon_button).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AddBeaconActivity.class);
            startActivityForResult(intent, 0);
            listChildren();
        });
        findViewById(R.id.refresh_button).setOnClickListener(v -> {
            listChildren();
        });

        listChildren();
        listView = findViewById(R.id.beacon_list);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Child child = children.get(position).child;
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            intent.putExtra("child", child);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        listChildren();
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void listChildren() {
        ServerApiKt.listChildren(r -> r.match(
                ok -> runOnUiThread(() -> {
                    children.clear();
                    children.addAll(ok.getChildren().stream().map(PrettyChild::new).collect(Collectors.toList()));
                    listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, children));
                }),
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PrettyChild that = (PrettyChild) o;
            return child.equals(that.child);
        }

        @Override
        public int hashCode() {
            return Objects.hash(child);
        }
    }
}
