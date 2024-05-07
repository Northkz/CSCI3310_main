package com.example.CUSplit.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationUtil {
    private FusedLocationProviderClient fusedLocationClient;
    private Context context;
    private LocationResultListener locationResultListener;

    public interface LocationResultListener {
        void onLocationResult(Location location);
    }

    public LocationUtil(Context context, LocationResultListener listener) {
        this.context = context;
        this.locationResultListener = listener;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void getLastLocation() {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                        if (location != null && locationResultListener != null) {
                            locationResultListener.onLocationResult(location);
                        } else {
                            Toast.makeText(context,"Location is turned off", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context,"Error fetching your location", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(context,"Location Permission not granted", Toast.LENGTH_SHORT).show();
        }
    }
}
