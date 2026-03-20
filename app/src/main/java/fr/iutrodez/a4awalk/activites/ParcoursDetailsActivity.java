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

/**
 * Activité affichant le détail d'un parcours (course) identifié par son ID.
 * <p>
 * Elle charge les données du parcours depuis l'API, affiche les informations
 * textuelles (nom, date, randonnée associée) ainsi qu'une carte OSM montrant
 * le tracé GPS réalisé et les marqueurs de départ/arrivée.
 * Elle propose également des actions : reprendre ou terminer le parcours.
 */
public class ParcoursDetailsActivity extends HeaderActivity {

    private static final String TAG = "ParcoursDetails";
    private static final String BASE_URL = "http://98.94.8.220:8080";

    private MapView map;
    private TextView tvNomParcours, tvDate, tvRandonnee;
    private ImageButton btnRetour;
    private Button btnReprendre, btnSupprimer;

    /** Parcours actuellement affiché, chargé depuis l'API. */
    private Course currentCourse;

    /** Gestionnaire du token d'authentification de l'utilisateur connecté. */
    private TokenManager tokenManager;

    /** Identifiant unique du parcours transmis via l'Intent. */
    private String courseId;

    /**
     * Point d'entrée de l'activité.
     * Initialise OSMDroid, récupère l'ID du parcours depuis l'Intent,
     * lie les vues, configure la carte et déclenche le chargement depuis l'API.
     *
     * @param savedInstanceState État sauvegardé de l'activité (peut être null).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupOsmdroidConfig();
        setContentView(R.layout.details_parcours);

        configurerToolbar();

        tokenManager = new TokenManager(this);

        // Récupération de l'ID du parcours transmis par l'activité appelante
        courseId = getIntent().getStringExtra("COURSE_ID");
        if (courseId == null || courseId.isEmpty()) {
            Toast.makeText(this, "Erreur : Parcours introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        configureMap();
        loadCourseFromApi();
    }

    /**
     * Configure la bibliothèque OSMDroid en chargeant les préférences partagées
     * et en définissant l'User-Agent utilisé pour les requêtes de tuiles cartographiques.
     */
    private void setupOsmdroidConfig() {
        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE)
        );
        Configuration.getInstance().setUserAgentValue("A4AWalkApp");
    }

    /**
     * Lie les composants de l'interface aux variables membres et configure
     * les listeners de base (bouton retour, visibilité des boutons d'action).
     */
    private void bindViews() {
        map = findViewById(R.id.map);
        tvNomParcours = findViewById(R.id.tvNomParcours);
        tvDate = findViewById(R.id.tvDate);
        tvRandonnee = findViewById(R.id.tvRandonnee);
        btnReprendre = findViewById(R.id.btnReprendre);
        btnSupprimer = findViewById(R.id.btnTerminer);
        btnRetour = findViewById(R.id.btnRetour);

        // Le bouton retour ferme simplement l'activité courante
        if (btnRetour != null) {
            btnRetour.setOnClickListener(v -> finish());
        }

        // Les deux boutons d'action sont rendus visibles par défaut ;
        // leur état sera affiné après le chargement des données du parcours
        btnReprendre.setVisibility(View.VISIBLE);
        btnSupprimer.setVisibility(View.VISIBLE);
    }

    /**
     * Configure les paramètres de base de la carte OSM :
     * source de tuiles, contrôles multi-touch et niveaux de zoom min/max.
     */
    private void configureMap() {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setMaxZoomLevel(19.0);
        map.setMinZoomLevel(12.0);
    }

    /**
     * Charge les données du parcours depuis l'API REST via un appel GET.
     * <p>
     * En cas de succès, construit l'objet {@link Course}, affiche les données
     * et configure le bouton "Terminer". En cas d'erreur réseau, affiche un message.
     */
    private void loadCourseFromApi() {
        String url = BASE_URL + "/courses/" + courseId;

        AppelAPI.get(url, tokenManager.getToken(), this, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    // Désérialisation du JSON en objet Course
                    currentCourse = ServiceParcours.createCourse(result);
                    displayCourseData(currentCourse);

                    // Chargement du nom de la randonnée associée au parcours
                    loadHikeNameFromApi(currentCourse.getHikeId());

                    // Configuration du bouton "Terminer" après chargement du parcours
                    btnSupprimer.setOnClickListener(v -> {
                        if (currentCourse == null) return;

                        // Empêche de terminer un parcours déjà clos
                        if (currentCourse.isFinished()) {
                            Toast.makeText(ParcoursDetailsActivity.this,
                                    "Ce parcours est déjà terminé.", Toast.LENGTH_SHORT).show();
                            btnSupprimer.setVisibility(View.GONE);
                            return;
                        }

                        // Appel API pour marquer le parcours comme terminé
                        ServiceParcours.terminerParcours(
                                ParcoursDetailsActivity.this,
                                tokenManager.getToken(),
                                currentCourse.getId()
                        );

                        // Masquage des boutons d'action une fois le parcours terminé
                        btnSupprimer.setVisibility(View.GONE);
                        btnReprendre.setVisibility(View.GONE);
                        Toast.makeText(ParcoursDetailsActivity.this,
                                "Parcours terminé ✔", Toast.LENGTH_SHORT).show();
                    });

                } catch (Exception e) {
                    Log.e(TAG, "Erreur parsing JSON Course", e);
                }
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(ParcoursDetailsActivity.this,
                        "Impossible de charger le parcours", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Charge le nom et les points géographiques (départ, arrivée, POI)
     * de la randonnée associée au parcours depuis l'API REST.
     * <p>
     * Les points sont ajoutés comme marqueurs sur la carte OSM.
     *
     * @param hikeId Identifiant de la randonnée à charger.
     */
    private void loadHikeNameFromApi(long hikeId) {
        String url = BASE_URL + "/hikes/" + hikeId;

        AppelAPI.get(url, tokenManager.getToken(), this, new AppelAPI.VolleyObjectCallback() {
            public void onSuccess(JSONObject result) throws JSONException {
                // Affichage du libellé de la randonnée
                tvRandonnee.setText(result.getString("libelle"));

                // Ajout du marqueur de départ de la randonnée si présent
                if (!result.isNull("depart")) {
                    JSONObject dep = result.getJSONObject("depart");
                    GeoPoint pt = new GeoPoint(dep.getDouble("latitude"), dep.getDouble("longitude"));
                    String nom = dep.optString("libelle", "Départ randonnée");
                    ajouterMarkerHike(pt, nom, true, false);
                }

                // Ajout du marqueur d'arrivée de la randonnée si présent
                if (!result.isNull("arrivee")) {
                    JSONObject arr = result.getJSONObject("arrivee");
                    GeoPoint pt = new GeoPoint(arr.getDouble("latitude"), arr.getDouble("longitude"));
                    String nom = arr.optString("libelle", "Arrivée randonnée");
                    ajouterMarkerHike(pt, nom, false, true);
                }

                // Ajout des points d'intérêt optionnels de la randonnée
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

                // Rafraîchissement de la carte pour afficher les nouveaux marqueurs
                map.invalidate();
            }

            @Override
            public void onError(VolleyError error) {
                error.getMessage();
            }
        });
    }

    /**
     * Peuple l'interface avec les données du parcours fourni.
     * <p>
     * Gère l'affichage conditionnel des boutons selon l'état du parcours
     * (en cours, en pause ou terminé) et trace les points GPS sur la carte.
     *
     * @param course L'objet {@link Course} dont les données sont à afficher.
     */
    private void displayCourseData(Course course) {
        // Affichage du nom du parcours transmis par l'Intent (ou valeur par défaut)
        String nomParcoursStr = getIntent().getStringExtra("NOM_PARCOURS");
        tvNomParcours.setText(nomParcoursStr != null ? nomParcoursStr : "Détails du parcours");

        // Formatage et affichage de la date de réalisation
        if (course.getDateRealisation() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            tvDate.setText(course.getDateRealisation().format(formatter));
        } else {
            tvDate.setText("Date inconnue");
        }

        if (!course.isFinished()) {
            // Parcours non terminé : affichage du bouton "Reprendre" ou "Voir le suivi"
            btnReprendre.setVisibility(View.VISIBLE);
            btnReprendre.setText(course.isPaused() ? "Reprendre" : "Voir le suivi");

            btnReprendre.setOnClickListener(v -> {
                // Lancement de l'activité de suivi avec toutes les données nécessaires
                Intent intent = new Intent(this, SuiviParcours.class);
                intent.putExtra("COURSE_ID", course.getId());
                intent.putExtra("DATE_REALISATION", tvDate.getText().toString());
                intent.putExtra("NOM_RANDONNEE", tvRandonnee.getText().toString());
                intent.putExtra("NOM_PARCOURS", tvNomParcours.getText().toString());
                intent.putExtra("HIKE_ID", course.getHikeId());
                startActivity(intent);
            });

            // Bouton "Terminer" visible uniquement si le parcours n'est pas encore clos
            btnSupprimer.setVisibility(View.VISIBLE);
            btnSupprimer.setOnClickListener(v -> {
                if (currentCourse == null) return;

                if (currentCourse.isFinished()) {
                    Toast.makeText(ParcoursDetailsActivity.this,
                            "Ce parcours est déjà terminé.", Toast.LENGTH_SHORT).show();
                    btnSupprimer.setVisibility(View.GONE);
                    return;
                }

                ServiceParcours.terminerParcours(
                        ParcoursDetailsActivity.this,
                        tokenManager.getToken(),
                        currentCourse.getId()
                );
                btnSupprimer.setVisibility(View.GONE);
                btnReprendre.setVisibility(View.GONE);
                Toast.makeText(ParcoursDetailsActivity.this,
                        "Parcours terminé ✔", Toast.LENGTH_SHORT).show();
            });

        } else {
            // Parcours déjà terminé : les deux boutons d'action sont masqués
            btnReprendre.setVisibility(View.GONE);
            btnSupprimer.setVisibility(View.GONE);
        }

        // --- Tracé du parcours sur la carte ---
        List<GeoCoordinate> trajets = course.getTrajetsRealises();
        if (trajets == null || trajets.isEmpty()) {
            Toast.makeText(this, "Aucun tracé GPS trouvé", Toast.LENGTH_SHORT).show();
            return;
        }

        // Centrage de la carte sur le premier point du tracé
        GeoCoordinate premierPoint = trajets.get(0);
        map.getController().setZoom(14.5);
        map.getController().setCenter(new GeoPoint(premierPoint.getLatitude(), premierPoint.getLongitude()));

        // Ajout d'un marqueur pour chaque point GPS du tracé réalisé
        for (int i = 0; i < trajets.size(); i++) {
            GeoCoordinate coord = trajets.get(i);
            GeoPoint geoPoint = new GeoPoint(coord.getLatitude(), coord.getLongitude());

            Marker marker = new Marker(map);
            marker.setPosition(geoPoint);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle("Point " + (i + 1));
            map.getOverlays().add(marker);
        }

        // Détermination du point de départ : depuis la course si défini, sinon premier point GPS
        GeoPoint pointDepart = course.getDepart() != null
                ? new GeoPoint(course.getDepart().getLatitude(), course.getDepart().getLongitude())
                : new GeoPoint(premierPoint.getLatitude(), premierPoint.getLongitude());

        // Détermination du point d'arrivée : depuis la course si défini, sinon dernier point GPS
        GeoCoordinate dernierPoint = trajets.get(trajets.size() - 1);
        GeoPoint pointArrivee = course.getArrivee() != null
                ? new GeoPoint(course.getArrivee().getLatitude(), course.getArrivee().getLongitude())
                : new GeoPoint(dernierPoint.getLatitude(), dernierPoint.getLongitude());

        ajouterMarkersDepartArrivee(pointDepart, pointArrivee);
    }

    /**
     * Ajoute deux marqueurs distincts sur la carte pour matérialiser
     * le point de départ et le point d'arrivée du parcours réalisé.
     *
     * @param depart  Coordonnées géographiques du point de départ.
     * @param arrivee Coordonnées géographiques du point d'arrivée.
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

        // Rafraîchissement de la carte pour afficher les marqueurs ajoutés
        map.invalidate();
    }

    /**
     * Ajoute un marqueur sur la carte pour un point appartenant à la randonnée
     * (départ officiel, arrivée officielle ou point d'intérêt optionnel).
     * <p>
     * L'icône utilisée est le marqueur par défaut d'OSMDroid, muté pour
     * permettre une coloration indépendante si nécessaire.
     *
     * @param point     Coordonnées géographiques du point à afficher.
     * @param titre     Texte affiché dans la bulle d'information du marqueur.
     * @param isDepart  {@code true} si ce point est le départ de la randonnée.
     * @param isArrivee {@code true} si ce point est l'arrivée de la randonnée.
     */
    private void ajouterMarkerHike(GeoPoint point, String titre, boolean isDepart, boolean isArrivee) {
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setTitle(titre);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Utilisation du marqueur par défaut OSMDroid ; mutate() permet de modifier
        // l'icône de manière indépendante sans affecter les autres marqueurs
        Drawable icon = ContextCompat.getDrawable(this, org.osmdroid.library.R.drawable.marker_default);
        if (icon != null) {
            icon = icon.mutate();
            marker.setIcon(icon);
        }

        map.getOverlays().add(marker);
    }

    /**
     * Reprend le cycle de vie de la carte OSM lorsque l'activité redevient visible.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    /**
     * Suspend le cycle de vie de la carte OSM lorsque l'activité passe en arrière-plan,
     * afin de libérer les ressources réseau et graphiques.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }
}