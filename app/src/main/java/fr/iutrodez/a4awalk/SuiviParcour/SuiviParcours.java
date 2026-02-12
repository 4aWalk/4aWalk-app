package fr.iutrodez.a4awalk.SuiviParcour;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.iutrodez.a4awalk.R;

/**
 * Activité pour le suivi en temps réel d'un parcours.
 * <p>
 * Cette activité gère :
 * <ul>
 *     <li>L'affichage de la carte avec OpenStreetMap (OSMDroid)</li>
 *     <li>Le suivi GPS de l'utilisateur</li>
 *     <li>La mise à jour des distances vers le prochain point et l'arrivée</li>
 *     <li>La validation des points et alertes d'approche</li>
 *     <li>L'envoi périodique des positions vers une API REST</li>
 * </ul>
 */
public class SuiviParcours extends AppCompatActivity {

    // ===== Constantes API =====
    private static final String COURSE_ID = "6989e2f5a5b0b8078ee29a24";
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QDRhd2Fsay5mciIsInVzZXJJZCI6MiwiaWF0IjoxNzcwODg2MTcwLCJleHAiOjE3NzA5NzI1NzB9.ZhJWOvc0xvnphoQ45c0r3t4mYtIswgTgza0YaRdQpFw";
    private static final String BASE_URL = "http://98.94.8.220:8080/courses/";
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    // ===== UI =====
    private MapView mapView;
    private MaterialButton btnPause;
    private MaterialButton btnTerminer;
    private TextView tvProchainPoint, tvArrivee;

    // ===== Managers =====
    private MapManager mapManager;
    private DistanceManager distanceManager;
    private LocationManager locationManager;
    private AudioManager audioManager;

    // ===== Parcours =====
    private List<GeoPoint> parcoursPoints = new ArrayList<>();
    private boolean isPaused = false;

    // ===== Tracking =====
    private RequestQueue requestQueue;
    private List<Location> locationBuffer = new ArrayList<>();
    private Handler trackingHandler = new Handler();
    private Runnable trackingRunnable;
    private Location lastKnownLocation;
    private LocationCallback locationCallback;

    // =========================================================
    // ====================== ON CREATE =========================
    // =========================================================

    /**
     * Méthode appelée lors de la création de l'activité.
     * Initialise la carte, les managers, le parcours, le DistanceManager,
     * le bouton pause et le timer de tracking.
     *
     * @param savedInstanceState état précédent de l'activité
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.main_activity);

        initViews();
        initManagers();
        initParcours();
        initDistanceManager();
        initPauseButton();
        initTerminerButton();

        requestQueue = Volley.newRequestQueue(this);
        startTrackingTimer();

        verifierPermissionsEtDemarrer();
    }

    // =========================================================
    // ====================== INITIALISATIONS ==================
    // =========================================================

    /** Initialise les vues de l'activité (MapView, TextView, Button) */
    private void initViews() {
        mapView = findViewById(R.id.mapView);
        btnPause = findViewById(R.id.btnPause);
        btnTerminer = findViewById(R.id.btnTerminer);
        tvProchainPoint = findViewById(R.id.tvProchainPoint);
        tvArrivee = findViewById(R.id.tvArrivee);

        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
    }

    /** Initialise les managers pour la carte, le son et la localisation */
    private void initManagers() {
        mapManager = new MapManager(mapView, this);
        audioManager = new AudioManager(this, R.raw.notif);
        locationManager = new LocationManager(this);
    }

    /** Initialise le parcours avec une liste de GeoPoints et la carte */
    private void initParcours() {
        parcoursPoints.add(new GeoPoint(44.360369301617794, 2.5758112393065384));
        parcoursPoints.add(new GeoPoint(44.351077610605785, 2.5740525086171298));
        parcoursPoints.add(new GeoPoint(44.34951392435509, 2.576044023961549));
        parcoursPoints.add(new GeoPoint(44.352487429572584, 2.5677165393061494));
        parcoursPoints.add(new GeoPoint(44.360369301617794, 2.5758112393065384));

        mapManager.addMarkers(parcoursPoints);
        mapManager.calculerItineraire(parcoursPoints);

        if (!parcoursPoints.isEmpty()) {
            mapView.getController().setZoom(14.0);
            mapView.getController().setCenter(parcoursPoints.get(0));
        }
    }

    /** Initialise le DistanceManager et ses callbacks pour le suivi du parcours */
    private void initDistanceManager() {
        distanceManager = new DistanceManager(parcoursPoints);

        distanceManager.setListener(new DistanceListener() {
            @Override
            public void onDistanceUpdated(float next, float end) {
                runOnUiThread(() -> {
                    tvProchainPoint.setText(formatDistanceProchain(next));
                    tvArrivee.setText(formatDistanceArrivee(end));
                });
            }

            @Override
            public void onPointValidated() {
                Toast.makeText(SuiviParcours.this, "Point validé", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onApproachAlert(float distance) {
                Toast.makeText(SuiviParcours.this,
                        "Approche du point (" + (int) distance + " m)",
                        Toast.LENGTH_LONG).show();
                audioManager.play();
            }
        });
    }

    /** Initialise le bouton pause/reprise pour la randonnée */
    private void initPauseButton() {
        btnPause.setOnClickListener(v -> {
            if (!isPaused) pauseRandonnee();
            else reprendreRandonnee();
        });
    }

    /** Initialise le bouton terminer pour la randonnée */
    private void initTerminerButton() {
        btnTerminer.setOnClickListener(v -> terminerRandonnee());
    }

    // =========================================================
    // ====================== GESTION PAUSE ====================
    // =========================================================

    /** Met la randonnée en pause et envoie les positions accumulées à l'API */
    private void pauseRandonnee() {
        if (lastKnownLocation != null) {
            ajouterPositionBuffer(lastKnownLocation);

            if (!locationBuffer.isEmpty()) {
                envoyerPositionsAPI();
                locationBuffer.clear();
            }
        }

        // Envoi du statut de pause à l'API
        envoyerStatutPauseAPI(true);

        locationManager.stopLocationUpdates();
        btnPause.setText("Reprendre la randonnée");
        isPaused = true;
    }

    /** Reprend la randonnée et le suivi GPS */
    private void reprendreRandonnee() {
        // Envoi du statut de reprise à l'API
        envoyerStatutPauseAPI(false);

        startLocation();
        btnPause.setText("Mettre en pause");
        isPaused = false;
    }

    /** Termine la randonnée et envoie les dernières positions à l'API */
    private void terminerRandonnee() {
        // Envoie les positions restantes si nécessaire
        if (lastKnownLocation != null) {
            ajouterPositionBuffer(lastKnownLocation);

            if (!locationBuffer.isEmpty()) {
                envoyerPositionsAPI();
                locationBuffer.clear();
            }
        }

        // Envoi du statut de fin à l'API
        envoyerFinRandoneeAPI();

        // Arrêt de la localisation
        locationManager.stopLocationUpdates();

        // Désactivation des boutons
        btnPause.setEnabled(false);
        btnTerminer.setEnabled(false);

        isPaused = true;
    }

    // =========================================================
    // ====================== LOCALISATION =====================
    // =========================================================

    /** Vérifie les permissions de localisation et démarre le suivi si autorisé */
    private void verifierPermissionsEtDemarrer() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            startLocation();
        }
    }

    /** Démarre le suivi GPS et met à jour la position de l'utilisateur */
    private void startLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        if (locationCallback == null) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult result) {
                    if (result == null) return;

                    for (Location loc : result.getLocations()) {
                        lastKnownLocation = loc;

                        GeoPoint userPos = new GeoPoint(loc.getLatitude(), loc.getLongitude());
                        Drawable icon = ContextCompat.getDrawable(SuiviParcours.this, R.drawable.ic_pin_user);

                        mapManager.updateUserPosition(userPos, icon);
                        distanceManager.updateLocation(loc);
                    }
                }
            };
        }

        locationManager.startLocationUpdates(locationCallback);
    }

    // =========================================================
    // ====================== TRACKING TIMER ==================
    // =========================================================

    /** Démarre un timer qui envoie les positions toutes les 60 secondes */
    private void startTrackingTimer() {
        trackingRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPaused && lastKnownLocation != null) {
                    ajouterPositionBuffer(lastKnownLocation);

                    if (locationBuffer.size() >= 10) {
                        envoyerPositionsAPI();
                        locationBuffer.clear();
                    }
                }
                trackingHandler.postDelayed(this, 60000);
            }
        };
        trackingHandler.post(trackingRunnable);
    }

    /** Ajoute une position GPS au buffer avant envoi à l'API */
    private void ajouterPositionBuffer(Location location) {
        locationBuffer.add(location);
        Toast.makeText(this,
                "Coordonnée ajoutée (" + locationBuffer.size() + "/10)",
                Toast.LENGTH_SHORT).show();
    }

    /** Envoie les positions accumulées au serveur via API REST */
    private void envoyerPositionsAPI() {
        String url = BASE_URL + COURSE_ID;
        JSONArray jsonArray = new JSONArray();

        try {
            for (Location loc : locationBuffer) {
                JSONObject obj = new JSONObject();
                obj.put("latitude", loc.getLatitude());
                obj.put("longitude", loc.getLongitude());
                jsonArray.put(obj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int nbPositions = locationBuffer.size();

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> Toast.makeText(this,
                        nbPositions + " positions envoyées ✔",
                        Toast.LENGTH_LONG).show(),
                error -> Toast.makeText(this,
                        "Erreur API : " + error.toString(),
                        Toast.LENGTH_LONG).show()
        ) {
            @Override
            public byte[] getBody() {
                return jsonArray.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + TOKEN);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    /** Envoie le statut de pause/reprise au serveur via API REST */
    private void envoyerStatutPauseAPI(boolean paused) {
        String url = BASE_URL + COURSE_ID + "/state?paused=" + paused;

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> Toast.makeText(this,
                        paused ? "Statut: En pause ⏸" : "Statut: En cours ▶",
                        Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this,
                        "Erreur changement statut : " + error.toString(),
                        Toast.LENGTH_LONG).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + TOKEN);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    /** Envoie la fin de la randonnée au serveur via API REST */
    private void envoyerFinRandoneeAPI() {
        String url = BASE_URL + COURSE_ID + "/finish";

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> Toast.makeText(this,
                        "Randonnée terminée ✔",
                        Toast.LENGTH_LONG).show(),
                error -> Toast.makeText(this,
                        "Erreur fin randonnée : " + error.toString(),
                        Toast.LENGTH_LONG).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + TOKEN);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    // =========================================================
    // ====================== FORMATAGE ========================
    // =========================================================

    /** Formatte la distance vers le prochain point pour affichage */
    private String formatDistanceProchain(float meters) {
        return meters >= 1000
                ? String.format("Prochain point %.2f km", meters / 1000)
                : String.format("Prochain point %.0f m", meters);
    }

    /** Formatte la distance restante vers l'arrivée pour affichage */
    private String formatDistanceArrivee(float meters) {
        return meters >= 1000
                ? String.format("Arrivée %.2f km", meters / 1000)
                : String.format("Arrivée %.0f m", meters);
    }

    // =========================================================
    // ====================== CALLBACKS ========================
    // =========================================================

    /**
     * Callback pour la demande de permissions de localisation.
     *
     * @param requestCode  code de la requête
     * @param permissions  tableau des permissions demandées
     * @param grantResults résultats de l'autorisation
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocation();
        }
    }

    // =========================================================
    // ====================== CYCLE VIE ========================
    // =========================================================

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.stopLocationUpdates();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        if (!isPaused) startLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioManager.release();
        trackingHandler.removeCallbacks(trackingRunnable);
    }
}