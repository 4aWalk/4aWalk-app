package fr.iutrodez.a4awalk.DetailParcours;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.SuiviParcour.SuiviParcours;

public class ParcoursDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ParcoursDetails";
    private static final String API_COURSE_URL = "http://98.94.8.220:8080/courses/69899eab5a19517b5cfcb121";
    private static final String API_HIKE_BASE_URL = "http://98.94.8.220:8080/hikes/";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QDRhd2Fsay5mciIsInVzZXJJZCI6MiwiaWF0IjoxNzcwNjI2NjcyLCJleHAiOjE3NzA3MTMwNzJ9.nausJMeBnvdXsahvH48CpGdM5rXuq02bOs5N4EpgHXc";

    private MapView map;
    private List<GeoPoint> coursePoints;
    private TextView tvNomParcours;
    private TextView tvDepart;
    private TextView tvArrivee;
    private TextView tvDate;
    private TextView tvRandonnee;
    private Button btnReprendre;

    private Course currentCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupOsmdroidConfig();
        setContentView(R.layout.details_parcours);
        bindViews();
        configureMap();

        loadCourseFromApi();
    }

    /**
     * Configure la librairie osmdroid.
     */
    private void setupOsmdroidConfig() {
        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE)
        );
        Configuration.getInstance().setUserAgentValue("A4AWalkApp");
    }

    /**
     * Lie les vues XML aux variables Java.
     */
    private void bindViews() {
        map = findViewById(R.id.map);
        tvNomParcours = findViewById(R.id.tvNomParcours);
        tvDepart = findViewById(R.id.tvDepart);
        tvArrivee = findViewById(R.id.tvArrivee);
        tvDate = findViewById(R.id.tvDate);
        tvRandonnee = findViewById(R.id.tvRandonnee);
        btnReprendre = findViewById(R.id.btnReprendre);
    }

    /**
     * Configure la carte.
     */
    private void configureMap() {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setMaxZoomLevel(19.0);
        map.setMinZoomLevel(12.0);
    }

    /**
     * Charge les informations du parcours depuis l'API.
     */
    private void loadCourseFromApi() {
        Log.d(TAG, "Début de l'appel API Course vers: " + API_COURSE_URL);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                API_COURSE_URL,
                null,
                response -> {
                    try {
                        currentCourse = new Gson().fromJson(response.toString(), Course.class);
                        displayCourseData(currentCourse);
                        loadHikeNameFromApi(currentCourse.getHikeId());
                        Log.d(TAG, "Parcours chargé avec succès");
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur parsing JSON Course", e);
                        Toast.makeText(this, "Erreur lors de l'analyse des données du parcours", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    logApiError(error.toString(), error.networkResponse != null ? error.networkResponse.statusCode : null);
                    Toast.makeText(this, "Erreur de connexion à l'API", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", AUTH_TOKEN);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Charge le nom de la randonnée associée depuis l'API.
     */
    private void loadHikeNameFromApi(int hikeId) {
        String url = API_HIKE_BASE_URL + hikeId;
        Log.d(TAG, "Début de l'appel API Hike vers: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Hike hike = new Gson().fromJson(response.toString(), Hike.class);
                        String hikeName = (hike != null && hike.getLibelle() != null)
                                ? hike.getLibelle()
                                : "Randonnée " + hikeId;
                        tvRandonnee.setText(hikeName);
                        Log.d(TAG, "Nom de la randonnée: " + hikeName);
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur parsing JSON Hike", e);
                        tvRandonnee.setText("Randonnée " + hikeId);
                    }
                },
                error -> {
                    logApiError(error.toString(), error.networkResponse != null ? error.networkResponse.statusCode : null);
                    tvRandonnee.setText("Randonnée " + hikeId);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", AUTH_TOKEN);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Affiche les informations du parcours dans la vue.
     */
    private void displayCourseData(Course course) {
        tvNomParcours.setText("Parcours " + course.getId());

        tvDepart.setText(course.getDepart() != null && course.getDepart().getDescription() != null
                ? course.getDepart().getDescription()
                : "Non défini");

        tvArrivee.setText(course.getArrivee() != null && course.getArrivee().getDescription() != null
                ? course.getArrivee().getDescription()
                : "Non défini");

        tvDate.setText(formaterDate(course.getDateRealisation()));
        tvRandonnee.setText("Chargement...");

        // ===== Bouton Reprendre =====
        if (course.isPaused() && !course.isFinished()) {
            btnReprendre.setVisibility(View.VISIBLE);
            btnReprendre.setOnClickListener(v -> {
                Intent intent = new Intent(this, SuiviParcours.class);
                intent.putExtra("courseId", course.getId());
                startActivity(intent);
            });
        } else {
            btnReprendre.setVisibility(View.GONE);
        }

        // GeoPoints
        coursePoints = new ArrayList<>();
        if (course.getPath() != null && !course.getPath().isEmpty()) {
            for (Point point : course.getPath()) {
                coursePoints.add(new GeoPoint(point.getLatitude(), point.getLongitude()));
            }

            GeoPoint departPoint = coursePoints.get(0);
            GeoPoint arriveePoint = coursePoints.get(coursePoints.size() - 1);

            map.getController().setZoom(14.5);
            map.getController().setCenter(departPoint);

            ajouterMarkersDepartArrivee(departPoint, arriveePoint);
            tracerItineraire(coursePoints);
        } else {
            Toast.makeText(this, "Aucun point trouvé pour ce parcours", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Formate une date ISO en format DD/MM/YYYY.
     */
    private String formaterDate(String dateISO) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.FRANCE);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
            Date date = inputFormat.parse(dateISO);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "Erreur formatage date", e);
            return dateISO;
        }
    }

    /**
     * Ajoute des markers pour le départ et l'arrivée.
     */
    private void ajouterMarkersDepartArrivee(GeoPoint depart, GeoPoint arrivee) {
        Marker markerDepart = new Marker(map);
        markerDepart.setPosition(depart);
        markerDepart.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        markerDepart.setTitle("Départ");
        map.getOverlays().add(markerDepart);

        Marker markerArrivee = new Marker(map);
        markerArrivee.setPosition(arrivee);
        markerArrivee.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        markerArrivee.setTitle("Arrivée");
        map.getOverlays().add(markerArrivee);
    }

    /**
     * Trace l'itinéraire sur la carte.
     */
    private void tracerItineraire(List<GeoPoint> points) {
        if (points.size() < 2) return;

        new Thread(() -> {
            try {
                OSRMRoadManager roadManager = new OSRMRoadManager(this, "A4AWalkApp");
                roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT);

                Road road = roadManager.getRoad(new ArrayList<>(points));
                Polyline overlay = RoadManager.buildRoadOverlay(road);
                overlay.setColor(Color.BLUE);
                overlay.setWidth(12f);

                runOnUiThread(() -> {
                    map.getOverlays().add(overlay);
                    map.invalidate();
                });

            } catch (Exception e) {
                Log.e(TAG, "Erreur tracé itinéraire", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Erreur lors du tracé de l'itinéraire", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    /**
     * Log une erreur API.
     */
    private void logApiError(String message, Integer httpCode) {
        Log.e(TAG, "Erreur API: " + message);
        if (httpCode != null) {
            Log.e(TAG, "Code erreur HTTP: " + httpCode);
        } else {
            Log.e(TAG, "Pas de réponse réseau - Vérifiez votre connexion Internet");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }
}