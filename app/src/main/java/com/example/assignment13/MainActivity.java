package com.example.assignment13;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.assignment13.models.PathResponse;
import com.example.assignment13.models.Point;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final OkHttpClient client = new OkHttpClient();
    final String TAG = "demo";
    private GoogleMap mMap;

    private PathResponse mPathResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main, mapFragment)
                .commit();

        mapFragment.getMapAsync(this);

        getPath();
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // If the data is already loaded, draw it now
        if (mPathResponse != null) {
            drawPathOnMap();
            zoomToPath();
        }
    }
    private void drawPathOnMap() {
        if (mMap == null || mPathResponse == null) return;

        PolylineOptions mPolyLineOptions = new PolylineOptions()
                .clickable(true)
                .width(5)
                .color(Color.BLUE);


        for (Point p : mPathResponse.getPath()) {
            mPolyLineOptions.add(new LatLng(p.getLatitude(), p.getLongitude()));
        }

        mMap.addPolyline(mPolyLineOptions);

        // Add a marker at the first and last points
        List<Point> path = mPathResponse.getPath();
        if (!path.isEmpty()) {
            Point start = path.get(0);
            Point end = path.get(path.size() - 1);
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(start.getLatitude(), start.getLongitude()))
                    .title("Start"));
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(end.getLatitude(), end.getLongitude()))
                    .title("End"));
        }
    }
    public void zoomToPath() {
        if (mMap == null || mPathResponse == null) return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Point point : mPathResponse.getPath()) {
            builder.include(new LatLng(point.getLatitude(), point.getLongitude()));
        }
        LatLngBounds bounds = builder.build();

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }
    public PathResponse getPath() {
        Request request = new Request.Builder()
                .url("https://www.theappsdr.com/map/route")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    String jsonResponse = responseBody.string();

                    Gson gson = new Gson();
                    PathResponse pathResponse = gson.fromJson(jsonResponse, PathResponse.class);
                    Log.d(TAG, "onResponse: " + pathResponse.getTitle());
                    Log.d(TAG, "Points count: " + pathResponse.getPath().size());
                    Log.d(TAG, "Full object: " + pathResponse.toString());

                    mPathResponse = pathResponse;

                    runOnUiThread(() -> drawPathOnMap());
                    runOnUiThread(() -> zoomToPath());

                } else {
                    ResponseBody responseBody = response.body();
                    String body = responseBody.string();
                    Log.d(TAG, "onResponse: " + body);
                }
            }
        });
        return mPathResponse;
    }
}