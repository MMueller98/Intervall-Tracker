package com.trier.intervalltracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    // Constants
    public static final int DEFAULT_UPDATE_INTERVAL = 1;
    public static final int FAST_UPDATE_INTERVAL = 1;
    private static final int PERMISSION_FINE_LOCATION = 99;
    private static final int VIBRATION_TIME = 500;

    // UI-Elements
    TextView tv_accuracy, tv_speed, tv_min_km, tv_distance;
    Switch sw_locations_updates;

    // Location request ist a config file for all setting related to FusedLocationProvider
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    // Google's API for location services
    FusedLocationProviderClient fusedLocationProviderClient;

    // distance calculation
    LonLatLocation currentLocation;
    private float total_distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI-Elements
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_min_km = findViewById(R.id.tv_min_km);
        tv_distance = findViewById(R.id.tv_distance);
        sw_locations_updates = findViewById(R.id.sw_locations_updates);

        // set all properties of Location Request
        locationRequest = new LocationRequest();

        // how often does default location check occur?
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);

        // how often does the location check occur when set to the most frequent update?
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Vibrator
        Vibrator v = (Vibrator) getSystemService((Context.VIBRATOR_SERVICE));
        // start without delay, vibrate 500 ms, sleep for 500 ms
        long[] pattern = {0,500,500};

        // initialize total distance
        total_distance = 0;

        // event that is triggered whenever the update interval is met
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location lastLocation = locationResult.getLastLocation();

                // calculate speed in min/km
                float min_km = (float) Math.pow((lastLocation.getSpeed() * ((double) 60 / (double) 1000)), -1);

                if(min_km > 10) {
                    v.vibrate(VIBRATION_TIME);
                }

                // distance calculation
                float[] distance = new float[1];
                Location.distanceBetween(currentLocation.getLat(), currentLocation.getLon(), lastLocation.getLatitude(), lastLocation.getLongitude(), distance);
                currentLocation.setLat(lastLocation.getLatitude());
                currentLocation.setLon(lastLocation.getLongitude());
                total_distance += distance[0];
                // save the location
                updateUIValues(lastLocation.getAccuracy(), lastLocation.getSpeed(), min_km, total_distance);
            }
        };

        sw_locations_updates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_locations_updates.isChecked()) {
                    // turn on location tracking
                    startLocationUpdates();
                } else {
                    // turn of tracking
                    stopLocationUpdates();
                }
            }
        });

        updateGPS();

    } // end onCreate method

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        updateGPS();
    }

    private void stopLocationUpdates() {

        tv_accuracy.setText("NOT tracking location");
        tv_speed.setText("NOT tracking location");
        tv_min_km.setText("NOT tracking location");

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSION_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateGPS();
                }
                else{
                    Toast.makeText(this, "This app requires permission to be granted in order to work properly", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void updateGPS(){
        // get permissions from the user to track GPS
        // get the current location from the fused client
        // update the UI - i.e. set all properties in their association text view items

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED){
            // user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // we got permissions. Put the values of location XXX into the UI components
                    currentLocation = new LonLatLocation(location.getLongitude(), location.getLatitude());
                }
            });
        }
        else{
            // permissions not granted yet
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }

    }

    private void updateUIValues(float accuracy, float speed, float min_km, float distance) {

        // update all of the text view objects with a new location
        tv_accuracy.setText(String.valueOf(accuracy));
        tv_speed.setText(String.valueOf(speed));
        tv_min_km.setText(String.valueOf(min_km));
        tv_distance.setText(String.valueOf(distance));

    }
}