package fr.iutrodez.a4awalk.activites;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.time.format.DateTimeFormatter;
import java.util.List;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.SuiviParcour.SuiviParcours;
import fr.iutrodez.a4awalk.modeles.entites.Course;
import fr.iutrodez.a4awalk.modeles.entites.GeoCoordinate;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceParcours;

public class ParcoursDetailsActivity extends HeaderActivity {

    private static final String TAG = "ParcoursDetails";
    private static final String BASE_URL = "http://98.94.8.220:8080";

    private MapView map;
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

        configurerToolbar();

        tokenManager = new TokenManager(this);

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
        String url = BASE_URL + "/hikes/" + hikeId;

        AppelAPI.get(url, tokenManager.getToken(), this, new AppelAPI.VolleyObjectCallback() {
            public void onSuccess(JSONObject result) throws JSONException {
                tvRandonnee.setText(result.getString("libelle"));

                // Départ de la randonnée
                if (!result.isNull("depart")) {
                    JSONObject dep = result.getJSONObject("depart");
                    GeoPoint pt = new GeoPoint(dep.getDouble("latitude"), dep.getDouble("longitude"));
                    String nom = dep.optString("libelle", "Départ randonnée");
                    ajouterMarkerHike(pt, nom, true, false);
                }

                // Arrivée de la randonnée
                if (!result.isNull("arrivee")) {
                    JSONObject arr = result.getJSONObject("arrivee");
                    GeoPoint pt = new GeoPoint(arr.getDouble("latitude"), arr.getDouble("longitude"));
                    String nom = arr.optString("libelle", "Arrivée randonnée");
                    ajouterMarkerHike(pt, nom, false, true);
                }

                // Points d'intérêt optionnels
                JSONArray optPoints = result.optJSONArray("optionalPoints");
                if (optPoints != null) {
                    for (int i = 0; i < optPoints.length(); i++) {
                        JSONObject poi = optPoints.getJSONObject(i);
                        GeoPoint pt = new GeoPoint(
                                poi.getDouble("latitude"),
                                poi.getDouble("longitude"));
                        String nom = poi.optString("libelle", "POI " + (i + 1));
                        ajouterMarkerHike(pt, nom, false, false);
                    }
                }

                map.invalidate();
            }

            @Override
            public void onError(VolleyError error) {
                error.getMessage();
            }
        });
    }

    private void displayCourseData(Course course) {
        String nomParcoursStr = getIntent().getStringExtra("NOM_PARCOURS");
        tvNomParcours.setText(nomParcoursStr != null ? nomParcoursStr : "Détails du parcours");

        if (course.getDateRealisation() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            tvDate.setText(course.getDateRealisation().format(formatter));
        } else {
            tvDate.setText("Date inconnue");
        }

        if (!course.isFinished()) {
            btnReprendre.setVisibility(View.VISIBLE);
            btnReprendre.setText(course.isPaused() ? "Reprendre" : "Voir le suivi");
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

        // --- Affichage de la carte ---
        List<GeoCoordinate> trajets = course.getTrajetsRealises();
        if (trajets == null || trajets.isEmpty()) {
            Toast.makeText(this, "Aucun tracé GPS trouvé", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Centrer la carte sur le premier point du trajet
        GeoCoordinate premierPoint = trajets.get(0);
        map.getController().setZoom(14.5);
        map.getController().setCenter(new GeoPoint(premierPoint.getLatitude(), premierPoint.getLongitude()));

        // 2. Ajouter un marqueur pour chaque point de trajetsRealises
        for (int i = 0; i < trajets.size(); i++) {
            GeoCoordinate coord = trajets.get(i);
            GeoPoint geoPoint = new GeoPoint(coord.getLatitude(), coord.getLongitude());

            Marker marker = new Marker(map);
            marker.setPosition(geoPoint);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle("Point " + (i + 1));
            map.getOverlays().add(marker);
        }

        GeoPoint pointDepart;
        if (course.getDepart() != null) {
            pointDepart = new GeoPoint(
                    course.getDepart().getLatitude(),
                    course.getDepart().getLongitude()
            );
        } else {
            pointDepart = new GeoPoint(premierPoint.getLatitude(), premierPoint.getLongitude());
        }

        GeoCoordinate dernierPoint = trajets.get(trajets.size() - 1);
        GeoPoint pointArrivee;
        if (course.getArrivee() != null) {
            pointArrivee = new GeoPoint(
                    course.getArrivee().getLatitude(),
                    course.getArrivee().getLongitude()
            );
        } else {
            pointArrivee = new GeoPoint(dernierPoint.getLatitude(), dernierPoint.getLongitude());
        }

        ajouterMarkersDepartArrivee(pointDepart, pointArrivee);
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

        map.invalidate();
    }

    private void ajouterMarkerHike(GeoPoint point, String titre, boolean isDepart, boolean isArrivee) {
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setTitle(titre);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        Drawable icon = ContextCompat.getDrawable(this, org.osmdroid.library.R.drawable.marker_default);
        if (icon != null) {
            icon = icon.mutate();
            marker.setIcon(icon);
        }

        map.getOverlays().add(marker);
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