package fr.iutrodez.a4awalk.SuiviParcour;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
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
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.activites.HeaderActivity;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceParcours;

/**
 * Activité de suivi GPS en temps réel d'un parcours de randonnée.
 *
 * <p>Elle assure les fonctions suivantes :</p>
 * <ul>
 *   <li>Affichage de l'itinéraire calculé sur une carte OSMDroid.</li>
 *   <li>Mise à jour continue de la position de l'utilisateur via FusedLocation.</li>
 *   <li>Envoi périodique des positions GPS vers l'API REST (toutes les 60 s
 *       ou dès que le buffer atteint 10 positions).</li>
 *   <li>Détection de la proximité des points d'intérêt (POI) avec alerte sonore
 *       et toast lorsque l'utilisateur est à moins de {@value #SEUIL_APPROCHE_METRES} m.</li>
 *   <li>Gestion de la pause (arrêt du suivi GPS et de l'envoi des positions)
 *       et de la reprise.</li>
 *   <li>Fin de randonnée (envoi de la dernière position + appel API {@code /finish}).</li>
 * </ul>
 *
 * <p>L'ID de la course ({@code "COURSE_ID"}) est transmis via l'intent.
 * Si absent ou vide, un ID de repli {@link #COURSE_ID_FALLBACK} est utilisé
 * pour faciliter les tests.</p>
 */
public class SuiviParcours extends HeaderActivity {

    // =========================================================
    //  Constantes
    // =========================================================

    /** ID de course utilisé en développement si aucun n'est transmis par l'intent. */
    private static final String COURSE_ID_FALLBACK = "6989e2f5a5b0b8078ee29a24";

    /** URL de base pour les endpoints de course (PUT positions, PUT finish). */
    private static final String BASE_URL = "http://98.94.8.220:8080/courses/";

    /** Code de requête pour la permission de localisation fine. */
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    /**
     * Distance en mètres en deçà de laquelle un POI est considéré comme "approché"
     * et déclenche une alerte sonore + toast.
     */
    private static final float SEUIL_APPROCHE_METRES = 100f;

    // =========================================================
    //  État et données
    // =========================================================

    /** Gestionnaire du token JWT d'authentification. */
    public TokenManager tokenManager;

    /** Identifiant de la course suivie (depuis l'intent ou valeur de repli). */
    private String currentCourseId;

    // --- Vues ---
    private MapView mapView;
    private TextView tvDate, tvRandonnee, tvNomParcours;
    private Button btnPause, btnTerminer;
    private ImageButton btnRetour;

    // --- Managers délégués ---
    /** Gestion des couches et de l'itinéraire sur la carte OSMDroid. */
    private MapManager mapManager;

    /** Calcul et mise à jour des distances restantes jusqu'au prochain point et à l'arrivée. */
    private DistanceManager distanceManager;

    /** Gestion des mises à jour de localisation GPS (FusedLocationProviderClient). */
    private LocationManager locationManager;

    /** Lecture d'un son de notification lors de l'approche d'un POI. */
    private AudioManager audioManager;

    // --- Tracé et état de la course ---

    /** Liste ordonnée des points GPS constituant l'itinéraire à parcourir. */
    private List<GeoPoint> parcoursPoints = new ArrayList<>();

    /** {@code true} si la randonnée est actuellement en pause. */
    private boolean isPaused = false;

    // --- Tracking périodique ---

    /** File de requêtes Volley utilisée pour l'envoi des positions GPS. */
    private RequestQueue requestQueue;

    /**
     * Buffer local des positions GPS accumulées entre deux envois API.
     * Vidé après chaque envoi ou à la pause/fin.
     */
    private List<Location> locationBuffer = new ArrayList<>();

    /** Handler Android utilisé pour planifier les envois périodiques (toutes les 60 s). */
    private Handler trackingHandler = new Handler();

    /** Runnable planifié par {@link #trackingHandler} pour l'envoi périodique. */
    private Runnable trackingRunnable;

    /** Dernière position GPS connue, utilisée pour un envoi final avant pause ou fin. */
    private Location lastKnownLocation;

    /** Callback FusedLocation enregistré pour recevoir les mises à jour de position. */
    private LocationCallback locationCallback;

    // --- Points d'intérêt (POI) ---

    /** Coordonnées de tous les POI de la randonnée (départ, arrivée, POI optionnels). */
    private List<GeoPoint> poiPoints = new ArrayList<>();

    /** Noms des POI dans le même ordre que {@link #poiPoints}. */
    private List<String> poiNoms = new ArrayList<>();

    /**
     * Tableau de flags indiquant si l'alerte d'approche a déjà été déclenchée
     * pour chaque POI (évite les doublons d'alerte).
     */
    private boolean[] poiAlerted;

    // =========================================================
    //  Cycle de vie Android
    // =========================================================

    /**
     * Initialise l'activité : configuration OSMDroid, récupération des extras
     * de l'intent, initialisation des managers et démarrage du tracking GPS.
     *
     * @param savedInstanceState état sauvegardé (peut être null).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuration OSMDroid (user-agent et préférences)
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.details_parcours);
        configurerToolbar();

        tokenManager = new TokenManager(this);

        // Récupération de l'ID de la course avec valeur de repli pour les tests
        String intentCourseId = getIntent().getStringExtra("COURSE_ID");
        currentCourseId = (intentCourseId != null && !intentCourseId.isEmpty())
                ? intentCourseId
                : COURSE_ID_FALLBACK;

        String dateRealisation = getIntent().getStringExtra("DATE_REALISATION");
        String nomRandonnee    = getIntent().getStringExtra("NOM_RANDONNEE");
        String nomParcours     = getIntent().getStringExtra("NOM_PARCOURS");

        initViews();

        // Remplissage des labels depuis l'intent
        if (dateRealisation != null) tvDate.setText(dateRealisation);
        if (nomRandonnee    != null) tvRandonnee.setText(nomRandonnee);
        if (nomParcours     != null) tvNomParcours.setText(nomParcours);

        initManagers();
        initParcours();
        initDistanceManager();
        initPauseButton();
        initTerminerButton();

        requestQueue = Volley.newRequestQueue(this);
        startTrackingTimer();

        verifierPermissionsEtDemarrer();
    }

    /**
     * Reprend la carte OSMDroid et relance le suivi GPS si la randonnée
     * n'est pas en pause.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (!isPaused) startLocation();
    }

    /**
     * Met en pause la carte OSMDroid et arrête les mises à jour de localisation
     * pour préserver la batterie.
     */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.stopLocationUpdates();
        mapView.onPause();
    }

    /**
     * Libère les ressources audio et annule le timer de tracking périodique
     * pour éviter des fuites mémoire.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioManager.release();
        trackingHandler.removeCallbacks(trackingRunnable);
    }

    // =========================================================
    //  Initialisation
    // =========================================================

    /**
     * Lie les variables Java aux vues XML et configure les comportements de base
     * (bouton retour, état initial des boutons de contrôle).
     */
    private void initViews() {
        mapView       = findViewById(R.id.map);
        btnPause      = findViewById(R.id.btnReprendre); // réutilisation du même ID pour pause/reprise
        btnTerminer   = findViewById(R.id.btnTerminer);
        btnRetour     = findViewById(R.id.btnRetour);
        tvDate        = findViewById(R.id.tvDate);
        tvRandonnee   = findViewById(R.id.tvRandonnee);
        tvNomParcours = findViewById(R.id.tvNomParcours);

        btnPause.setText("Mettre en pause");
        // Le bouton "Terminer" est masqué initialement et n'apparaît qu'après une pause
        btnTerminer.setVisibility(View.GONE);

        btnRetour.setOnClickListener(v -> finish());

        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
    }

    /**
     * Instancie les managers délégués (carte, audio, localisation).
     */
    private void initManagers() {
        mapManager      = new MapManager(mapView, this);
        audioManager    = new AudioManager(this, R.raw.notif);
        locationManager = new LocationManager(this);
    }

    /**
     * Charge et affiche l'itinéraire du parcours.
     *
     * <p>Deux cas de figure :</p>
     * <ul>
     *   <li><b>Nouveau démarrage</b> : les coordonnées sont transmises par l'intent
     *       sous forme de tableaux {@code LATITUDES}/{@code LONGITUDES}.</li>
     *   <li><b>Reprise</b> : les tableaux sont absents ; le tracé est rechargé
     *       depuis l'API via {@link #chargerParcoursDepuisAPI()}.</li>
     * </ul>
     * <p>Dans les deux cas, les POI de la randonnée sont ensuite affichés sur la carte.</p>
     */
    private void initParcours() {
        parcoursPoints.clear();

        double[] latitudes  = getIntent().getDoubleArrayExtra("LATITUDES");
        double[] longitudes = getIntent().getDoubleArrayExtra("LONGITUDES");

        if (latitudes != null && longitudes != null && latitudes.length == longitudes.length) {
            // Cas normal : reconstruction des GeoPoints depuis les tableaux de l'intent
            for (int i = 0; i < latitudes.length; i++) {
                parcoursPoints.add(new GeoPoint(latitudes[i], longitudes[i]));
            }
            mapManager.calculerItineraire(parcoursPoints);
            if (!parcoursPoints.isEmpty()) {
                mapView.getController().setZoom(15.0);
                mapView.getController().setCenter(parcoursPoints.get(0));
            }
            afficherPOIsRandonnee();
        } else {
            // Cas reprise : pas de données dans l'intent, rechargement depuis l'API
            chargerParcoursDepuisAPI();
        }
    }

    /**
     * Affiche les points d'intérêt de la randonnée sur la carte à partir
     * des extras de l'intent (départ, arrivée, POI optionnels).
     *
     * <p>Chaque type de POI utilise une icône distincte :
     * {@code ic_marker_depart}, {@code ic_marker_arrivee}, {@code ic_marker_poi}.</p>
     *
     * <p>Initialise également {@link #poiAlerted} avec autant de cases
     * que de POI collectés, pour le contrôle des alertes de proximité.</p>
     */
    private void afficherPOIsRandonnee() {
        poiPoints.clear();
        poiNoms.clear();

        // --- Départ ---
        double hikeDepartLat = getIntent().getDoubleExtra("HIKE_DEPART_LAT", Double.NaN);
        double hikeDepartLon = getIntent().getDoubleExtra("HIKE_DEPART_LON", Double.NaN);
        String hikeDepartNom = getIntent().getStringExtra("HIKE_DEPART_NOM");
        if (!Double.isNaN(hikeDepartLat)) {
            ajouterMarkerPOI(hikeDepartLat, hikeDepartLon,
                    hikeDepartNom != null ? hikeDepartNom : "Départ",
                    R.drawable.ic_marker_depart);
            poiPoints.add(new GeoPoint(hikeDepartLat, hikeDepartLon));
            poiNoms.add(hikeDepartNom != null ? hikeDepartNom : "Départ");
        }

        // --- Arrivée ---
        double hikeArriveeLat = getIntent().getDoubleExtra("HIKE_ARRIVEE_LAT", Double.NaN);
        double hikeArriveeLon = getIntent().getDoubleExtra("HIKE_ARRIVEE_LON", Double.NaN);
        String hikeArriveeNom = getIntent().getStringExtra("HIKE_ARRIVEE_NOM");
        if (!Double.isNaN(hikeArriveeLat)) {
            ajouterMarkerPOI(hikeArriveeLat, hikeArriveeLon,
                    hikeArriveeNom != null ? hikeArriveeNom : "Arrivée",
                    R.drawable.ic_marker_arrivee);
            poiPoints.add(new GeoPoint(hikeArriveeLat, hikeArriveeLon));
            poiNoms.add(hikeArriveeNom != null ? hikeArriveeNom : "Arrivée");
        }

        // --- POI optionnels (tableaux parallèles lat/lon/nom) ---
        double[] poiLats    = getIntent().getDoubleArrayExtra("HIKE_POI_LATS");
        double[] poiLons    = getIntent().getDoubleArrayExtra("HIKE_POI_LONS");
        String[] poiNomArray = getIntent().getStringArrayExtra("HIKE_POI_NOMS");
        if (poiLats != null && poiLons != null) {
            for (int i = 0; i < poiLats.length; i++) {
                String nom = (poiNomArray != null && i < poiNomArray.length)
                        ? poiNomArray[i] : "POI " + (i + 1);
                ajouterMarkerPOI(poiLats[i], poiLons[i], nom, R.drawable.ic_marker_poi);
                poiPoints.add(new GeoPoint(poiLats[i], poiLons[i]));
                poiNoms.add(nom);
            }
        }

        // Initialisation du tableau d'alertes après collecte complète des POI
        poiAlerted = new boolean[poiPoints.size()];
    }

    /**
     * Ajoute un marqueur POI sur la carte avec une icône redimensionnée à 20×20 dp.
     *
     * @param lat       latitude du marqueur.
     * @param lon       longitude du marqueur.
     * @param titre     texte affiché dans la bulle au clic.
     * @param iconResId ressource drawable utilisée comme icône.
     */
    private void ajouterMarkerPOI(double lat, double lon, String titre, int iconResId) {
        GeoPoint point = new GeoPoint(lat, lon);
        Marker marker  = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(titre);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        Drawable icon = ContextCompat.getDrawable(this, iconResId);
        if (icon != null) {
            // Redimensionnement en dp indépendant de la densité de l'écran
            int size = (int) (20 * getResources().getDisplayMetrics().density);
            Bitmap bitmap = Bitmap.createScaledBitmap(
                    drawableToBitmap(icon), size, size, true);
            marker.setIcon(new BitmapDrawable(getResources(), bitmap));
        }

        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }

    /**
     * Initialise le {@link DistanceManager} avec les points du parcours et
     * configure son listener pour réagir aux événements de distance.
     *
     * <p>Le listener affiche des alertes d'approche et valide les points atteints.
     * L'affichage des distances dans les TextViews est actuellement commenté
     * (en attente d'une future implémentation).</p>
     */
    private void initDistanceManager() {
        distanceManager = new DistanceManager(parcoursPoints);

        distanceManager.setListener(new DistanceListener() {
            @Override
            public void onDistanceUpdated(float next, float end) {
                // Mise à jour des indicateurs de distance (non implémentée pour le moment)
                // runOnUiThread(() -> {
                //     tvProchainPoint.setText(formatDistanceProchain(next));
                //     tvArrivee.setText(formatDistanceArrivee(end));
                // });
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

    /**
     * Configure le listener du bouton Pause/Reprendre pour alterner entre
     * {@link #pauseRandonnee()} et {@link #reprendreRandonnee()}.
     */
    private void initPauseButton() {
        btnPause.setOnClickListener(v -> {
            if (!isPaused) {
                pauseRandonnee();
            } else {
                reprendreRandonnee();
            }
        });
    }

    /**
     * Configure le listener du bouton "Terminer" pour appeler
     * {@link #terminerRandonnee()}.
     */
    private void initTerminerButton() {
        btnTerminer.setOnClickListener(v -> terminerRandonnee());
    }

    // =========================================================
    //  Gestion de la pause et de la fin
    // =========================================================

    /**
     * Met la randonnée en pause :
     * <ol>
     *   <li>Ajoute la dernière position connue au buffer et l'envoie immédiatement.</li>
     *   <li>Notifie l'API du changement de statut (pause = true).</li>
     *   <li>Arrête les mises à jour de localisation GPS.</li>
     *   <li>Rend le bouton retour visible et met à jour le libellé du bouton.</li>
     * </ol>
     */
    private void pauseRandonnee() {
        // Envoi immédiat des positions en attente avant de stopper le suivi
        if (lastKnownLocation != null) {
            ajouterPositionBuffer(lastKnownLocation);
            if (!locationBuffer.isEmpty()) {
                envoyerPositionsAPI();
                locationBuffer.clear();
            }
        }

        envoyerStatutPauseAPI(true);
        btnRetour.setVisibility(View.VISIBLE); // Autorise la navigation retour pendant la pause
        locationManager.stopLocationUpdates();
        btnPause.setText("Reprendre la randonnée");
        isPaused = true;
    }

    /**
     * Reprend la randonnée après une pause :
     * notifie l'API, relance le suivi GPS et remet à jour le libellé du bouton.
     */
    private void reprendreRandonnee() {
        envoyerStatutPauseAPI(false);
        startLocation();
        btnPause.setText("Mettre en pause");
        isPaused = false;
    }

    /**
     * Termine la randonnée définitivement :
     * <ol>
     *   <li>Envoie les dernières positions en attente.</li>
     *   <li>Appelle l'endpoint {@code /finish} via {@link #envoyerFinRandoneeAPI()}.</li>
     *   <li>Arrête le suivi GPS et désactive les boutons.</li>
     * </ol>
     */
    private void terminerRandonnee() {
        // Flush du buffer avant la fin
        if (lastKnownLocation != null) {
            ajouterPositionBuffer(lastKnownLocation);
            if (!locationBuffer.isEmpty()) {
                envoyerPositionsAPI();
                locationBuffer.clear();
            }
        }

        envoyerFinRandoneeAPI();
        locationManager.stopLocationUpdates();

        // Désactivation des boutons pour éviter les actions après fin
        btnPause.setEnabled(false);
        btnTerminer.setEnabled(false);
        isPaused = true;
    }

    // =========================================================
    //  Gestion des permissions et localisation
    // =========================================================

    /**
     * Vérifie la permission {@code ACCESS_FINE_LOCATION} et la demande si nécessaire.
     * Si déjà accordée, démarre directement le suivi GPS.
     */
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

    /**
     * Démarre les mises à jour de localisation GPS via FusedLocationProviderClient.
     *
     * <p>À chaque position reçue :</p>
     * <ul>
     *   <li>Met à jour {@link #lastKnownLocation}.</li>
     *   <li>Actualise le marqueur de position sur la carte.</li>
     *   <li>Notifie le {@link DistanceManager}.</li>
     *   <li>Vérifie la proximité des POI ({@link #verifierProximitePOIs}).</li>
     * </ul>
     *
     * <p>Le callback est créé une seule fois (singleton par session).</p>
     */
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
                        Drawable icon = ContextCompat.getDrawable(SuiviParcours.this,
                                R.drawable.ic_pin_user);
                        mapManager.updateUserPosition(userPos, icon);
                        distanceManager.updateLocation(loc);
                        verifierProximitePOIs(loc);
                    }
                }
            };
        }

        locationManager.startLocationUpdates(locationCallback);
    }

    /**
     * Traite le résultat de la demande de permission GPS.
     * Lance le suivi si la permission est accordée.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocation();
        }
    }

    // =========================================================
    //  Timer de tracking périodique
    // =========================================================

    /**
     * Démarre le timer de tracking qui s'exécute toutes les 60 secondes.
     *
     * <p>À chaque tick :</p>
     * <ul>
     *   <li>Si la randonnée n'est pas en pause et qu'une position est connue,
     *       la dernière position est ajoutée au buffer.</li>
     *   <li>Dès que le buffer atteint 10 positions, elles sont envoyées
     *       à l'API en lot et le buffer est vidé.</li>
     * </ul>
     *
     * <p>Cette stratégie de batching réduit le nombre d'appels réseau
     * tout en gardant un suivi cohérent.</p>
     */
    private void startTrackingTimer() {
        trackingRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPaused && lastKnownLocation != null) {
                    ajouterPositionBuffer(lastKnownLocation);
                    if (locationBuffer.size() >= 10) {
                        // Envoi groupé et vidage du buffer
                        envoyerPositionsAPI();
                        locationBuffer.clear();
                    }
                }
                // Replanification de la prochaine exécution dans 60 secondes
                trackingHandler.postDelayed(this, 60000);
            }
        };
        trackingHandler.post(trackingRunnable);
    }

    /**
     * Ajoute une position GPS au buffer local et affiche un toast de confirmation
     * avec le nombre de positions accumulées.
     *
     * @param location position GPS à ajouter.
     */
    private void ajouterPositionBuffer(Location location) {
        locationBuffer.add(location);
        Toast.makeText(this,
                "Coordonnée ajoutée (" + locationBuffer.size() + "/10)",
                Toast.LENGTH_SHORT).show();
    }

    // =========================================================
    //  Appels API
    // =========================================================

    /**
     * Envoie les positions GPS accumulées dans le buffer vers l'API REST
     * via PUT {@code /courses/{id}}.
     *
     * <p>Le corps de la requête est un tableau JSON de coordonnées :
     * {@code [{"latitude": x, "longitude": y}, ...]}</p>
     *
     * <p>La requête utilise {@link StringRequest} avec override de {@code getBody()}
     * et des headers JWT pour l'authentification. La file Volley gère
     * l'envoi de façon asynchrone.</p>
     */
    private void envoyerPositionsAPI() {
        String url = BASE_URL + currentCourseId;
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

        // Capture du nombre de positions avant l'envoi (pour le toast dans le callback)
        int nbPositions = locationBuffer.size();

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> Toast.makeText(this,
                        nbPositions + " positions envoyées ✔", Toast.LENGTH_LONG).show(),
                error -> Toast.makeText(this,
                        "Erreur API : " + error.toString(), Toast.LENGTH_LONG).show()
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
                headers.put("Authorization", "Bearer " + tokenManager.getToken());
                return headers;
            }
        };

        requestQueue.add(request);
    }

    /**
     * Notifie l'API du changement de statut de pause via le service dédié.
     *
     * @param paused {@code true} pour passer en pause, {@code false} pour reprendre.
     */
    private void envoyerStatutPauseAPI(boolean paused) {
        ServiceParcours.changerStatutPause(this, tokenManager.getToken(),
                currentCourseId, paused,
                new AppelAPI.VolleyObjectCallback() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        Toast.makeText(SuiviParcours.this,
                                paused ? "Statut: En pause ⏸" : "Statut: En cours ▶",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Toast.makeText(SuiviParcours.this,
                                "Erreur changement statut : " + error.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Marque la randonnée comme terminée côté API via PUT {@code /courses/{id}/finish}.
     *
     * <p>Utilise directement un {@link StringRequest} (sans corps) et
     * injecte le token JWT dans les headers.</p>
     */
    private void envoyerFinRandoneeAPI() {
        String url = BASE_URL + currentCourseId + "/finish";

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> Toast.makeText(this,
                        "Randonnée terminée ✔", Toast.LENGTH_LONG).show(),
                error -> Toast.makeText(this,
                        "Erreur fin randonnée : " + error.toString(),
                        Toast.LENGTH_LONG).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + tokenManager.getToken());
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    // =========================================================
    //  Chargement depuis l'API (mode reprise)
    // =========================================================

    /**
     * Charge le tracé du parcours depuis l'API REST (GET {@code /courses/{id}})
     * en cas de reprise (pas de données dans l'intent).
     *
     * <p>Reconstruit {@link #parcoursPoints} à partir du tableau {@code "path"}
     * de la réponse JSON, calcule l'itinéraire sur la carte, puis charge
     * les POI de la randonnée associée via {@link #chargerPOIsDepuisHike(int)}.</p>
     */
    private void chargerParcoursDepuisAPI() {
        String url = "http://98.94.8.220:8080/courses/" + currentCourseId;

        AppelAPI.get(url, tokenManager.getToken(), this, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    parcoursPoints.clear();
                    JSONArray path = result.optJSONArray("path");
                    if (path != null) {
                        for (int i = 0; i < path.length(); i++) {
                            JSONObject pt = path.getJSONObject(i);
                            parcoursPoints.add(new GeoPoint(
                                    pt.getDouble("latitude"),
                                    pt.getDouble("longitude")));
                        }
                    }

                    mapManager.calculerItineraire(parcoursPoints);
                    if (!parcoursPoints.isEmpty()) {
                        mapView.getController().setZoom(15.0);
                        mapView.getController().setCenter(parcoursPoints.get(0));
                    }

                    // Chargement des POI via la randonnée associée
                    int hikeId = result.optInt("hikeId", 0);
                    if (hikeId != 0) {
                        chargerPOIsDepuisHike(hikeId);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(SuiviParcours.this,
                        "Impossible de charger le tracé", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Charge les points d'intérêt d'une randonnée via GET {@code /hikes/{id}}
     * et les affiche sur la carte (mode reprise uniquement).
     *
     * <p>Traite les champs {@code depart}, {@code arrivee} et
     * {@code optionalPoints} de la réponse JSON. Réinitialise ensuite
     * {@link #poiAlerted} avec le bon nombre de POI chargés.</p>
     *
     * @param hikeId identifiant de la randonnée dont on charge les POI.
     */
    private void chargerPOIsDepuisHike(int hikeId) {
        String url = "http://98.94.8.220:8080/hikes/" + hikeId;

        AppelAPI.get(url, tokenManager.getToken(), this, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) throws JSONException {
                poiPoints.clear();
                poiNoms.clear();

                // --- Départ ---
                if (!result.isNull("depart")) {
                    JSONObject dep = result.getJSONObject("depart");
                    double lat = dep.getDouble("latitude");
                    double lon = dep.getDouble("longitude");
                    String nom = dep.optString("nom", "Départ");
                    ajouterMarkerPOI(lat, lon, nom, R.drawable.ic_marker_depart);
                    poiPoints.add(new GeoPoint(lat, lon));
                    poiNoms.add(nom);
                }

                // --- Arrivée ---
                if (!result.isNull("arrivee")) {
                    JSONObject arr = result.getJSONObject("arrivee");
                    double lat = arr.getDouble("latitude");
                    double lon = arr.getDouble("longitude");
                    String nom = arr.optString("nom", "Arrivée");
                    ajouterMarkerPOI(lat, lon, nom, R.drawable.ic_marker_arrivee);
                    poiPoints.add(new GeoPoint(lat, lon));
                    poiNoms.add(nom);
                }

                // --- POI optionnels ---
                JSONArray optPoints = result.optJSONArray("optionalPoints");
                if (optPoints != null) {
                    for (int i = 0; i < optPoints.length(); i++) {
                        JSONObject poi = optPoints.getJSONObject(i);
                        double lat = poi.getDouble("latitude");
                        double lon = poi.getDouble("longitude");
                        String nom = poi.optString("nom", "POI " + (i + 1));
                        ajouterMarkerPOI(lat, lon, nom, R.drawable.ic_marker_poi);
                        poiPoints.add(new GeoPoint(lat, lon));
                        poiNoms.add(nom);
                    }
                }

                // Réinitialisation des alertes avec le nombre exact de POI chargés
                poiAlerted = new boolean[poiPoints.size()];
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(SuiviParcours.this,
                        "Impossible de charger les POIs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================================================
    //  Alertes de proximité POI
    // =========================================================

    /**
     * Vérifie si l'utilisateur s'approche d'un POI non encore alerté.
     *
     * <p>Pour chaque POI dont l'alerte n'a pas encore été déclenchée,
     * calcule la distance entre la position de l'utilisateur et le POI.
     * Si cette distance est inférieure ou égale à {@link #SEUIL_APPROCHE_METRES} :</p>
     * <ul>
     *   <li>Le flag {@link #poiAlerted}[i] est mis à {@code true} pour éviter les répétitions.</li>
     *   <li>Un toast est affiché sur le thread principal avec le nom du POI et la distance.</li>
     *   <li>Un son de notification est joué via {@link AudioManager#play()}.</li>
     * </ul>
     *
     * @param userLocation position GPS actuelle de l'utilisateur.
     */
    private void verifierProximitePOIs(Location userLocation) {
        if (poiPoints == null || poiAlerted == null) return;

        for (int i = 0; i < poiPoints.size(); i++) {
            if (poiAlerted[i]) continue; // Alerte déjà déclenchée pour ce POI

            GeoPoint poi = poiPoints.get(i);
            float[] results = new float[1];
            // Calcul de la distance orthodromique entre deux coordonnées GPS
            Location.distanceBetween(
                    userLocation.getLatitude(), userLocation.getLongitude(),
                    poi.getLatitude(), poi.getLongitude(),
                    results
            );

            if (results[0] <= SEUIL_APPROCHE_METRES) {
                poiAlerted[i] = true; // Marquage pour ne pas répéter l'alerte
                String nomPoi = poiNoms.get(i);

                runOnUiThread(() -> Toast.makeText(
                        SuiviParcours.this,
                        "📍 Vous approchez de : " + nomPoi + " (" + (int) results[0] + " m)",
                        Toast.LENGTH_LONG
                ).show());

                audioManager.play();
            }
        }
    }

    // =========================================================
    //  Utilitaires
    // =========================================================

    /**
     * Formate une distance vers le prochain point de l'itinéraire en chaîne lisible.
     *
     * @param meters distance en mètres.
     * @return chaîne formatée (km si ≥ 1000 m, sinon m).
     */
    private String formatDistanceProchain(float meters) {
        return meters >= 1000
                ? String.format("Prochain point %.2f km", meters / 1000)
                : String.format("Prochain point %.0f m", meters);
    }

    /**
     * Formate une distance jusqu'à l'arrivée en chaîne lisible.
     *
     * @param meters distance en mètres.
     * @return chaîne formatée (km si ≥ 1000 m, sinon m).
     */
    private String formatDistanceArrivee(float meters) {
        return meters >= 1000
                ? String.format("Arrivée %.2f km", meters / 1000)
                : String.format("Arrivée %.0f m", meters);
    }

    /**
     * Convertit un {@link Drawable} en {@link Bitmap} pour pouvoir
     * le redimensionner avec {@link Bitmap#createScaledBitmap}.
     *
     * <p>Dessine le drawable sur un canvas de la taille intrinsèque du drawable.</p>
     *
     * @param drawable drawable source à convertir.
     * @return bitmap résultant au format ARGB_8888.
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}