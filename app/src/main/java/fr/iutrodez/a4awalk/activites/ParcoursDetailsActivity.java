package fr.iutrodez.a4awalk.activites;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.SuiviParcour.SuiviParcours;
import fr.iutrodez.a4awalk.modeles.entites.Course;
import fr.iutrodez.a4awalk.modeles.entites.GeoCoordinate;
import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceParcours;

public class ParcoursDetailsActivity extends HeaderActivity {

    private static final String TAG = "ParcoursDetails";
    private static final String BASE_URL = "http://98.94.8.220:8080";

    private MapView map;
    private List<GeoPoint> coursePoints;
    private TextView tvNomParcours, tvDate, tvRandonnee;
    private ImageButton btnRetour;
    private Button btnReprendre, btnSupprimer;

    private Course currentCourse;
    private TokenManager tokenManager;
    private String courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupOsmdroidConfig();
        setContentView(R.layout.details_parcours);

        // Initialisation du header parent
        configurerToolbar();

        tokenManager = new TokenManager(this);

        // Récupération de l'ID passé par l'Intent depuis l'adaptateur
        courseId = getIntent().getStringExtra("COURSE_ID");
        if (courseId == null || courseId.isEmpty()) {
            Toast.makeText(this, "Erreur : Parcours introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        configureMap();
        loadCourseFromApi();
        btnSupprimer.setOnClickListener(v -> ServiceParcours.terminerParcours(this, tokenManager.getToken(), currentCourse.getId()));
    }

    private void setupOsmdroidConfig() {
        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE)
        );
        Configuration.getInstance().setUserAgentValue("A4AWalkApp");
    }

    private void bindViews() {
        map = findViewById(R.id.map);
        tvNomParcours = findViewById(R.id.tvNomParcours);
        tvDate = findViewById(R.id.tvDate);
        tvRandonnee = findViewById(R.id.tvRandonnee);
        btnReprendre = findViewById(R.id.btnReprendre);
        btnSupprimer = findViewById(R.id.btnTerminer);
        btnRetour = findViewById(R.id.btnRetour);

        if (btnRetour != null) {
            btnRetour.setOnClickListener(v -> finish());
        }

        // Mode Consultation : on masque les boutons d'action
        btnReprendre.setVisibility(View.VISIBLE);
        btnSupprimer.setVisibility(View.VISIBLE);
    }

    private void configureMap() {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setMaxZoomLevel(19.0);
        map.setMinZoomLevel(12.0);
    }

    private void loadCourseFromApi() {
        String url = BASE_URL + "/courses/" + courseId;

        AppelAPI.get(url, tokenManager.getToken(), this, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    currentCourse = ServiceParcours.createCourse(result);
                    displayCourseData(currentCourse);

                    loadHikeNameFromApi(currentCourse.getHikeId());
                } catch (Exception e) {
                    Log.e(TAG, "Erreur parsing JSON Course", e);
                }
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(ParcoursDetailsActivity.this, "Impossible de charger le parcours", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadHikeNameFromApi(long hikeId) {
        String url = BASE_URL + "/hikes/" +hikeId;

        AppelAPI.get(url, tokenManager.getToken(), this, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) throws JSONException {
                tvRandonnee.setText(result.getString("libelle"));
            }

            @Override
            public void onError(VolleyError error) {
                error.getMessage();
            }
        });
    }

    private void displayCourseData(Course course) {
        String nomParcoursStr = getIntent().getStringExtra("NOM_PARCOURS");

        if (nomParcoursStr != null) {
            tvNomParcours.setText(nomParcoursStr);
        } else {
            tvNomParcours.setText("Détails du parcours");
        }

        if (course.getDateRealisation() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            tvDate.setText(course.getDateRealisation().format(formatter));
        } else {
            tvDate.setText("Date inconnue");
        }

        // Si le parcours n'est pas terminé, on affiche toujours le bouton pour accéder au suivi
        if (!course.isFinished()) {
            btnReprendre.setVisibility(View.VISIBLE);

            // On adapte le texte du bouton selon l'état actuel du parcours
            if (course.isPaused()) {
                btnReprendre.setText("Reprendre");
            } else {
                btnReprendre.setText("Voir le suivi");
            }

            btnReprendre.setOnClickListener(v -> {
                Intent intent = new Intent(this, SuiviParcours.class);
                intent.putExtra("COURSE_ID", course.getId());
                intent.putExtra("DATE_REALISATION", tvDate.getText().toString());
                intent.putExtra("NOM_RANDONNEE", tvRandonnee.getText().toString());
                intent.putExtra("NOM_PARCOURS", tvNomParcours.getText().toString());
                startActivity(intent);
            });
        } else {
            btnReprendre.setVisibility(View.GONE);
        }

        coursePoints = new ArrayList<>();
        // On utilise getTrajetsRealises() comme défini dans ton modèle
        if (course.getTrajetsRealises() != null && !course.getTrajetsRealises().isEmpty()) {
            for (GeoCoordinate point : course.getTrajetsRealises()) {
                coursePoints.add(new GeoPoint(point.getLatitude(), point.getLongitude()));
            }

            GeoPoint departPoint = coursePoints.get(0);
            GeoPoint arriveePoint = coursePoints.get(coursePoints.size() - 1);

            map.getController().setZoom(14.5);
            map.getController().setCenter(departPoint);

            ajouterMarkersDepartArrivee(departPoint, arriveePoint);
            tracerItineraire(coursePoints);
        } else {
            Toast.makeText(this, "Aucun tracé GPS trouvé", Toast.LENGTH_SHORT).show();
        }
    }

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