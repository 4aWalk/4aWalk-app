package fr.iutrodez.a4awalk.SuiviParcour;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.*;

/**
 * Gestionnaire de localisation utilisant FusedLocationProviderClient.
 * <p>
 * Permet de démarrer et arrêter la réception des mises à jour GPS avec
 * un intervalle personnalisé et une haute précision.
 */
public class LocationManager {

    /** Client de localisation Google */
    private final FusedLocationProviderClient fusedLocationClient;

    /** Callback pour recevoir les mises à jour de localisation */
    private LocationCallback locationCallback;

    /** Contexte de l'application ou de l'activité */
    private final Context context;

    /**
     * Crée un LocationManager pour l'application.
     *
     * @param context Contexte de l'application ou activité
     */
    public LocationManager(Context context) {
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Démarre les mises à jour de localisation GPS.
     * <p>
     * Vérifie explicitement que la permission ACCESS_FINE_LOCATION est accordée.
     *
     * @param callback Callback à appeler pour chaque mise à jour
     */
    public void startLocationUpdates(LocationCallback callback) {
        this.locationCallback = callback;

        // Vérification des permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Configuration de la requête de localisation
        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(1500)
                .setMaxUpdateDelayMillis(5000)
                .build();

        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
    }

    /**
     * Arrête les mises à jour de localisation.
     * <p>
     * Doit être appelé lorsque les mises à jour ne sont plus nécessaires pour économiser la batterie.
     */
    public void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
