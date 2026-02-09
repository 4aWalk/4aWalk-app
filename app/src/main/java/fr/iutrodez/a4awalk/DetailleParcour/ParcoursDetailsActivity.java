package fr.iutrodez.a4awalk.DetailleParcour;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import fr.iutrodez.a4awalk.DetailleParcour.VolleySingleton;
import fr.iutrodez.a4awalk.DetailleParcour.Course;
import fr.iutrodez.a4awalk.DetailleParcour.Hike;

public class ParcoursDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ParcoursDetaille";
    private static final String API_COURSE_URL = "http://98.94.8.220:8080/courses/69899eab5a19517b5cfcb121";
    private static final String API_HIKE_BASE_URL = "http://98.94.8.220:8080/hikes/";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QDRhd2Fsay5mciIsInVzZXJJZCI6MiwiaWF0IjoxNzcwNjI2NjcyLCJleHAiOjE3NzA3MTMwNzJ9.nausJMeBnvdXsahvH48CpGdM5rXuq02bOs5N4EpgHXc";

    private MapView map;
    private List<GeoPoint> pointsParcours;

    // TextViews de la vue
    private TextView tvNomParcours, tvDepart, tvArrivee, tvDate, tvRandonnee;

    // Pour stocker temporairement les données du parcours
    private Course currentCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ======================================
        // ⚡ Configuration osmdroid
        // ======================================
        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE)
        );
        Configuration.getInstance().setUserAgentValue("A4AWalkApp");

        setContentView(R.layout.detaille_parcour);

        // ======================================
        // RÉFÉRENCES VUES
        // ======================================
        map = findViewById(R.id.map);
        tvNomParcours = findViewById(R.id.tvNomParcours);
        tvDepart = findViewById(R.id.tvDepart);
        tvArrivee = findViewById(R.id.tvArrivee);
        tvDate = findViewById(R.id.tvDate);
        tvRandonnee = findViewById(R.id.tvRandonnee);

        // ======================================
        // CONFIGURATION CARTE
        // ======================================
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setMaxZoomLevel(19.0);
        map.setMinZoomLevel(12.0);

        // ======================================
        // CHARGEMENT DES DONNÉES DEPUIS L'API
        // ======================================
        loadCourseFromApi();
    }

    /**
     * Charge les données du parcours depuis l'API avec Volley
     */
    private void loadCourseFromApi() {
        Log.d(TAG, "🚀 Début de l'appel API Course vers: " + API_COURSE_URL);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                API_COURSE_URL,
                null,
                response -> {
                    try {
                        Log.d(TAG, "✅ Réponse Course reçue");

                        Gson gson = new Gson();
                        Course course = gson.fromJson(response.toString(), Course.class);

                        // Stockage temporaire du parcours
                        currentCourse = course;

                        // Affichage des données du parcours
                        displayCourseData(course);

                        // Chargement du nom de la randonnée
                        loadHikeNameFromApi(course.getHikeId());

                        Log.d(TAG, "✅ Parcours chargé avec succès");
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Erreur parsing JSON Course", e);
                        Toast.makeText(this, "Erreur lors de l'analyse des données du parcours", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "❌ Erreur API Course: " + error.toString());

                    if (error.networkResponse != null) {
                        Log.e(TAG, "❌ Code erreur HTTP: " + error.networkResponse.statusCode);
                    } else {
                        Log.e(TAG, "❌ Pas de réponse réseau - Vérifiez votre connexion Internet");
                    }

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

        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Charge le nom de la randonnée depuis l'API
     */
    private void loadHikeNameFromApi(int hikeId) {
        String hikeUrl = API_HIKE_BASE_URL + hikeId;
        Log.d(TAG, "🚀 Début de l'appel API Hike vers: " + hikeUrl);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                hikeUrl,
                null,
                response -> {
                    try {
                        Log.d(TAG, "✅ Réponse Hike reçue");

                        Gson gson = new Gson();
                        Hike hike = gson.fromJson(response.toString(), Hike.class);

                        // Mise à jour du TextView avec le nom de la randonnée
                        if (hike != null && hike.getLibelle() != null) {
                            tvRandonnee.setText(hike.getLibelle());
                            Log.d(TAG, "✅ Nom de la randonnée: " + hike.getLibelle());
                        } else {
                            tvRandonnee.setText("Randonnée " + hikeId);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "❌ Erreur parsing JSON Hike", e);
                        tvRandonnee.setText("Randonnée " + hikeId);
                    }
                },
                error -> {
                    Log.e(TAG, "❌ Erreur API Hike: " + error.toString());

                    if (error.networkResponse != null) {
                        Log.e(TAG, "❌ Code erreur HTTP Hike: " + error.networkResponse.statusCode);
                    }

                    // En cas d'erreur, on affiche juste l'ID
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

        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Affiche les données du parcours reçues de l'API
     */
    private void displayCourseData(Course course) {
        // ======================================
        // MISE À JOUR DES TEXTVIEWS
        // ======================================

        // Nom du parcours (utilise l'ID du parcours)
        tvNomParcours.setText("Parcours " + course.getId());

        // Description du point de départ
        if (course.getDepart() != null) {
            String departDesc = course.getDepart().getDescription();
            tvDepart.setText(departDesc != null ? departDesc : "Non défini");
            Log.d(TAG, "Départ: " + departDesc);
        }

        // Description du point d'arrivée
        if (course.getArrivee() != null) {
            String arriveeDesc = course.getArrivee().getDescription();
            tvArrivee.setText(arriveeDesc != null ? arriveeDesc : "Non défini");
            Log.d(TAG, "Arrivée: " + arriveeDesc);
        }

        // Date de réalisation (formatage de la date ISO)
        if (course.getDateRealisation() != null) {
            String dateFormatee = formaterDate(course.getDateRealisation());
            tvDate.setText(dateFormatee);
            Log.d(TAG, "Date: " + dateFormatee);
        }

        // Le nom de la randonnée sera mis à jour par loadHikeNameFromApi()
        tvRandonnee.setText("Chargement...");

        // ======================================
        // CRÉATION DES GEOPOINTS
        // ======================================
        pointsParcours = new ArrayList<>();

        // Vérification que le path existe et n'est pas vide
        if (course.getPath() != null && !course.getPath().isEmpty()) {

            // Ajout de tous les points du path
            for (fr.iutrodez.a4awalk.DetailleParcour.Point point : course.getPath()) {
                GeoPoint geoPoint = new GeoPoint(point.getLatitude(), point.getLongitude());
                pointsParcours.add(geoPoint);
                Log.d(TAG, "Point ajouté: " + point.getLatitude() + ", " + point.getLongitude());
            }

            // Récupération du premier et dernier point pour les markers
            GeoPoint departGeoPoint = pointsParcours.get(0);
            GeoPoint arriveeGeoPoint = pointsParcours.get(pointsParcours.size() - 1);

            // ======================================
            // CENTRAGE CARTE
            // ======================================
            map.getController().setZoom(14.5);
            map.getController().setCenter(departGeoPoint);

            // ======================================
            // AJOUT DES MARKERS Départ / Arrivée
            // ======================================
            ajouterMarkersDepartArrivee(departGeoPoint, arriveeGeoPoint);

            // ======================================
            // TRACÉ DE L'ITINÉRAIRE
            // ======================================
            tracerItineraire(pointsParcours);

            Log.d(TAG, "Nombre total de points dans le parcours: " + pointsParcours.size());
        } else {
            Toast.makeText(this, "Aucun point trouvé pour ce parcours", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Liste de points (path) vide ou null");
        }
    }

    /**
     * Formate une date ISO 8601 en format DD/MM/YYYY
     */
    private String formaterDate(String dateISO) {
        try {
            // Format d'entrée: 2026-02-09T08:45:31.991
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.FRANCE);
            // Format de sortie: 09/02/2026
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);

            Date date = inputFormat.parse(dateISO);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "Erreur formatage date", e);
            return dateISO; // Retourne la date brute en cas d'erreur
        }
    }

    /**
     * Ajoute les markers uniquement pour le départ et l'arrivée
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
     * Tracer un itinéraire entre tous les points
     */
    private void tracerItineraire(List<GeoPoint> points) {
        if (points.size() < 2) return;

        new Thread(() -> {
            try {
                OSRMRoadManager roadManager = new OSRMRoadManager(this, "A4AWalkApp");
                roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT);

                ArrayList<GeoPoint> roadPoints = new ArrayList<>(points);
                Road road = roadManager.getRoad(roadPoints);

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
