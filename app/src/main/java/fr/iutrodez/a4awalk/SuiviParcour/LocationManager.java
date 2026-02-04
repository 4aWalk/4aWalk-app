package fr.iutrodez.a4awalk.SuiviParcour;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.*;

public class LocationManager {
    private final FusedLocationProviderClient fusedLocationClient;
    private LocationCallback callback;
    private final Context context;

    public LocationManager(Context context) {
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void startLocationUpdates(LocationCallback locationCallback) {
        this.callback = locationCallback;

        // Vérification explicite de la permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(1500)
                .setMaxUpdateDelayMillis(5000)
                .build();

        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper());
    }

    public void stopLocationUpdates() {
        if (callback != null) fusedLocationClient.removeLocationUpdates(callback);
    }
}

