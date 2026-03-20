package fr.iutrodez.a4awalk.activites;

import static fr.iutrodez.a4awalk.services.gestionAPI.ServiceParticipant.buildParticipantJSON;
import static fr.iutrodez.a4awalk.services.gestionAPI.ServiceParticipant.supprimerParticipantAPI;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.VolleyError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.SuiviParcour.SuiviParcours;
import fr.iutrodez.a4awalk.modeles.ParticipantCallback;
import fr.iutrodez.a4awalk.modeles.entites.Course;
import fr.iutrodez.a4awalk.modeles.entites.EquipmentItem;
import fr.iutrodez.a4awalk.modeles.entites.FoodProduct;
import fr.iutrodez.a4awalk.modeles.entites.GeoCoordinate;
import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.entites.PointOfInterest;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.modeles.enums.ModeRandonnee;
import fr.iutrodez.a4awalk.modeles.enums.TypeEquipment;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceEquipment;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceFoodProduct;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceOptimisation;
import fr.iutrodez.a4awalk.services.gestionAPI.ServicePOI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceParcours;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceParticipant;
import fr.iutrodez.a4awalk.services.gestionAPI.randonnee.ServiceCreationRandonnee;
import fr.iutrodez.a4awalk.services.gestionAPI.randonnee.ServiceModificationRandonnee;
import fr.iutrodez.a4awalk.utils.MapPickerDialog;
import fr.iutrodez.a4awalk.utils.PopupUtil;
import fr.iutrodez.a4awalk.utils.validators.PoiValidator;
import fr.iutrodez.a4awalk.utils.validators.ValidateurRandonnee;

/**
 * Activité polyvalente de gestion d'une randonnée.
 *
 * <p>Elle fonctionne selon trois modes exclusifs, transmis via l'extra d'intent
 * {@code "ID_PAGE"} :</p>
 * <ul>
 *   <li>{@link #MODE_CONSULTATION} (1) : affichage lecture seule de la randonnée,
 *       avec possibilité de lancer l'optimisation ou de démarrer une course.</li>
 *   <li>{@link #MODE_CREATION} (2) : saisie d'une nouvelle randonnée
 *       (libellé, coordonnées, durée).</li>
 *   <li>{@link #MODE_MODIFICATION} (3) : modification d'une randonnée existante,
 *       incluant la gestion des POI, participants, produits alimentaires et équipements.</li>
 * </ul>
 *
 * <p>Les données de la randonnée courante ({@link Hike}) et de l'utilisateur ({@link User})
 * sont transmises en Parcelable via l'intent.</p>
 */
public class ActiviteGestionRandonnee extends HeaderActivity {

    /** Mode affichage : consultation lecture seule. */
    private static final int MODE_CONSULTATION = 1;

    /** Mode affichage : création d'une nouvelle randonnée. */
    private static final int MODE_CREATION = 2;

    /** Mode affichage : modification d'une randonnée existante. */
    private static final int MODE_MODIFICATION = 3;

    /** Tag utilisé comme message d'erreur dans les résultats d'intent. */
    private final String ERREUR = "ERREUR";

    /** Code de requête pour la permission de localisation GPS. */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    // --- Champs de saisie ---
    private EditText libelle, departLat, departLon, arriveeLat, arriveeLon;

    // --- Listes et leur contenu ---
    private ListView listePoints, listeParticipants, listeFoodProducts, listeEquipments;

    /** Spinner pour la sélection du nombre de jours de la randonnée. */
    private Spinner nbJours;

    // --- Boutons d'ajout d'éléments ---
    private ImageButton btnAjouterPOI, btnAjouterParticipant;
    private Button btnOptimizeHike, btnSaveHike, btnStartCourse;
    private Button btnAddFoodProduct, btnAddEquipment;

    // --- Conteneurs de sections (pour affichage conditionnel selon le mode) ---
    private LinearLayout containerPoi, containerParticipants, containerFoodProducts, containerEquipments;

    // -------------------------------------------------------------------------
    // Listes temporaires (état de l'IHM, modifié avant persistance API)
    // et listes originales (état initial chargé depuis l'API, pour le diff)
    // -------------------------------------------------------------------------

    /** Liste de POI en cours d'édition dans l'interface. */
    private ArrayList<PointOfInterest> listeTemporairePOI = new ArrayList<>();

    /** Snapshot des POI au moment du chargement, utilisé pour le diff en modification. */
    private ArrayList<PointOfInterest> poiOriginaux = new ArrayList<>();
    private ArrayAdapter<PointOfInterest> adapterPOI;

    private ArrayList<Participant> listeTemporaireParticipants = new ArrayList<>();
    private ArrayList<Participant> participantsOriginaux = new ArrayList<>();
    private ArrayAdapter<Participant> adapterParticipants;

    private ArrayList<FoodProduct> listeTemporaireFoodProducts = new ArrayList<>();
    private ArrayList<FoodProduct> foodProductsOriginaux = new ArrayList<>();
    private ArrayAdapter<FoodProduct> adapterFoodProducts;

    private ArrayList<EquipmentItem> listeTemporaireEquipments = new ArrayList<>();
    private ArrayList<EquipmentItem> equipmentsOriginaux = new ArrayList<>();
    private ArrayAdapter<EquipmentItem> adapterEquipments;

    // --- Données métier ---
    private TokenManager tokenManager;
    private User currentUser;
    private Hike currentHike;

    // --- Affichage des coordonnées sélectionnées ---
    private TextView tvCoordsDepart, tvCoordsArrivee;
    private Button btnChoisirDepart, btnChoisirArrivee;

    // =========================================================
    //  Cycle de vie Android
    // =========================================================

    /**
     * Point d'entrée de l'activité.
     *
     * <p>Initialise les composants graphiques, récupère les données transmises
     * par l'intent, puis délègue la configuration au sous-mode concerné
     * ({@link #consultationRandonnee()}, {@link #creationRandonnee()} ou
     * {@link #modificationRandonnee()}).</p>
     *
     * <p>En mode modification, les listes "originales" sont remplies comme
     * snapshot de l'état initial afin de permettre le calcul du diff lors
     * de la sauvegarde.</p>
     *
     * @param savedInstanceState état sauvegardé (peut être null).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_details_randonnee);

        configurerToolbar();

        tokenManager = new TokenManager(this);
        recupererDonneesUtilisateur();
        recupererHikeIntent();
        initElementsGraphiques();

        int pageID = getIntent().getIntExtra("ID_PAGE", 0);

        switch (pageID) {
            case MODE_CONSULTATION:
                if (currentHike != null) remplissageChamps(currentHike);
                consultationRandonnee();
                break;
            case MODE_CREATION:
                creationRandonnee();
                break;
            case MODE_MODIFICATION:
                if (currentHike != null) {
                    remplissageChamps(currentHike);
                    // Snapshot des listes pour le diff lors de la sauvegarde
                    participantsOriginaux.addAll(listeTemporaireParticipants);
                    poiOriginaux.addAll(listeTemporairePOI);
                    foodProductsOriginaux.addAll(listeTemporaireFoodProducts);
                    equipmentsOriginaux.addAll(listeTemporaireEquipments);
                }
                modificationRandonnee();
                break;
            default:
                // Mode inconnu : on ferme l'activité avec un résultat d'erreur
                fermerAvecResultat(Activity.RESULT_CANCELED, ERREUR);
        }
    }

    // =========================================================
    //  Récupération des données depuis l'intent
    // =========================================================

    /**
     * Récupère l'objet {@link User} transmis en Parcelable dans l'intent.
     * Utilise l'API typée disponible depuis Android 13 (TIRAMISU) si applicable.
     */
    private void recupererDonneesUtilisateur() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            currentUser = getIntent().getParcelableExtra("USER_DATA", User.class);
        } else {
            currentUser = getIntent().getParcelableExtra("USER_DATA");
        }
    }

    /**
     * Récupère l'objet {@link Hike} transmis en Parcelable dans l'intent.
     * Peut être null en mode création.
     */
    private void recupererHikeIntent() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            currentHike = getIntent().getParcelableExtra("HIKE_OBJECT", Hike.class);
        } else {
            currentHike = getIntent().getParcelableExtra("HIKE_OBJECT");
        }
    }

    // =========================================================
    //  Initialisation des vues
    // =========================================================

    /**
     * Lie les variables Java aux vues XML, configure les adaptateurs de liste,
     * et positionne les listeners des boutons de sélection de coordonnées
     * (ouverture du {@link MapPickerDialog}).
     */
    private void initElementsGraphiques() {
        libelle      = findViewById(R.id.nom_rando);
        departLat    = findViewById(R.id.depart_rando_lat);
        departLon    = findViewById(R.id.depart_rando_lon);
        arriveeLat   = findViewById(R.id.arrivee_rando_lat);
        arriveeLon   = findViewById(R.id.arrivee_rando_lon);
        btnChoisirDepart  = findViewById(R.id.btn_choisir_depart);
        btnChoisirArrivee = findViewById(R.id.btn_choisir_arrivee);
        tvCoordsDepart    = findViewById(R.id.tv_coords_depart);
        tvCoordsArrivee   = findViewById(R.id.tv_coords_arrivee);

        listePoints       = findViewById(R.id.points_list);
        listeParticipants = findViewById(R.id.participants_list);
        listeFoodProducts = findViewById(R.id.food_products_list);
        listeEquipments   = findViewById(R.id.equipments_list);
        nbJours           = findViewById(R.id.spinner_jours);

        containerPoi          = findViewById(R.id.container_poi);
        containerParticipants = findViewById(R.id.container_participants);
        containerFoodProducts = findViewById(R.id.container_food_products);
        containerEquipments   = findViewById(R.id.container_equipments);

        btnOptimizeHike   = findViewById(R.id.btn_optimize_hike);
        btnSaveHike       = findViewById(R.id.btn_save_hike);
        btnStartCourse    = findViewById(R.id.btn_start_course);
        btnAjouterPOI     = findViewById(R.id.btn_add_poi);
        btnAjouterParticipant = findViewById(R.id.btn_add_participant);
        btnAddFoodProduct = findViewById(R.id.btn_add_food_product);
        btnAddEquipment   = findViewById(R.id.btn_add_equipment);

        // Population du spinner avec les valeurs 1 à 3 jours
        List<Integer> jours = new ArrayList<>();
        for (int i = 1; i <= 3; i++) jours.add(i);
        ArrayAdapter<Integer> adapterJours = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, jours);
        adapterJours.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nbJours.setAdapter(adapterJours);

        // Liaison des adaptateurs aux listes visuelles
        adapterPOI = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listeTemporairePOI);
        listePoints.setAdapter(adapterPOI);

        adapterParticipants = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listeTemporaireParticipants);
        listeParticipants.setAdapter(adapterParticipants);

        adapterFoodProducts = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listeTemporaireFoodProducts);
        listeFoodProducts.setAdapter(adapterFoodProducts);

        adapterEquipments = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listeTemporaireEquipments);
        listeEquipments.setAdapter(adapterEquipments);

        // Sélection du point de départ via la carte interactive
        btnChoisirDepart.setOnClickListener(v -> {
            double lat = departLat.getText().toString().isEmpty() ? 0 : Double.parseDouble(departLat.getText().toString());
            double lon = departLon.getText().toString().isEmpty() ? 0 : Double.parseDouble(departLon.getText().toString());
            MapPickerDialog.afficher(this, lat, lon, "Point de départ", (latitude, longitude) -> {
                departLat.setText(String.valueOf(latitude));
                departLon.setText(String.valueOf(longitude));
                tvCoordsDepart.setText(String.format("📍 %.5f, %.5f", latitude, longitude));
                tvCoordsDepart.setVisibility(View.VISIBLE);
            });
        });

        // Sélection du point d'arrivée via la carte interactive
        btnChoisirArrivee.setOnClickListener(v -> {
            double lat = arriveeLat.getText().toString().isEmpty() ? 0 : Double.parseDouble(arriveeLat.getText().toString());
            double lon = arriveeLon.getText().toString().isEmpty() ? 0 : Double.parseDouble(arriveeLon.getText().toString());
            MapPickerDialog.afficher(this, lat, lon, "Point d'arrivée", (latitude, longitude) -> {
                arriveeLat.setText(String.valueOf(latitude));
                arriveeLon.setText(String.valueOf(longitude));
                tvCoordsArrivee.setText(String.format("📍 %.5f, %.5f", latitude, longitude));
                tvCoordsArrivee.setVisibility(View.VISIBLE);
            });
        });
    }

    // =========================================================
    //  Configuration selon le mode
    // =========================================================

    /**
     * Configure l'interface en mode <b>Consultation</b> :
     * tous les champs sont en lecture seule, les boutons d'ajout sont masqués,
     * et seuls "Optimiser" et éventuellement "Démarrer" sont visibles.
     *
     * <p>Le bouton "Démarrer la course" n'est affiché que si la randonnée
     * a déjà été optimisée ({@link Hike#getOptimize()} == true).</p>
     */
    private void consultationRandonnee() {
        // Masquage des boutons de sélection de coordonnées (non éditables)
        btnChoisirDepart.setVisibility(View.GONE);
        btnChoisirArrivee.setVisibility(View.GONE);

        // Affichage de toutes les sections de données
        containerPoi.setVisibility(View.VISIBLE);
        containerParticipants.setVisibility(View.VISIBLE);
        containerFoodProducts.setVisibility(View.VISIBLE);
        containerEquipments.setVisibility(View.VISIBLE);

        Log.i("OPTIMISER", "isOptimize: " + currentHike.getOptimize());

        setChampsEditables(false);

        // Masquage de tous les boutons d'ajout en mode lecture seule
        btnAjouterPOI.setVisibility(View.GONE);
        btnAjouterParticipant.setVisibility(View.GONE);
        btnAddFoodProduct.setVisibility(View.GONE);
        btnAddEquipment.setVisibility(View.GONE);

        // Les listes restent cliquables pour afficher les détails
        listePoints.setEnabled(true);
        listeParticipants.setEnabled(true);
        listeFoodProducts.setEnabled(true);
        listeEquipments.setEnabled(true);

        // Clic sur un participant : affichage en mode consultation
        listeParticipants.setOnItemClickListener((parent, view, position, id) -> {
            Participant p = listeTemporaireParticipants.get(position);
            PopUpParticipant.afficherDialogParticipant(this, ModeRandonnee.CONSULTATION,
                    tokenManager.getToken(), currentHike.getId(), p, null);
        });

        // Clic sur un POI : ouverture en lecture seule
        listePoints.setOnItemClickListener((parent, view, position, id) ->
                gererDialogPOI(position, true));

        // Clic sur un produit alimentaire : popup de détails
        listeFoodProducts.setOnItemClickListener((parent, view, position, id) -> {
            FoodProduct fp = listeTemporaireFoodProducts.get(position);
            PopUpFoodProduct.afficherPopupDetailsFoodProduct(ActiviteGestionRandonnee.this, fp);
        });

        // Clic sur un équipement : popup de détails avec liste des participants
        listeEquipments.setOnItemClickListener((parent, view, position, id) -> {
            EquipmentItem eq = listeTemporaireEquipments.get(position);
            PopUpEquipment.afficherPopupDetailsEquipment(ActiviteGestionRandonnee.this, eq,
                    listeTemporaireParticipants);
        });

        btnSaveHike.setVisibility(View.GONE);

        // Le bouton "Démarrer" n'est disponible que si la randonnée a été optimisée
        if (currentHike.getOptimize()) {
            btnStartCourse.setVisibility(View.VISIBLE);
        }
        btnOptimizeHike.setVisibility(View.VISIBLE);
        btnOptimizeHike.setOnClickListener(v -> lancerOptimisation());
        btnStartCourse.setOnClickListener(v -> lancerDemarrageCourse());
    }

    /**
     * Configure l'interface en mode <b>Création</b> :
     * tous les champs sont éditables, les sections (POI, participants, etc.)
     * sont masquées car elles n'existent pas encore côté serveur.
     */
    private void creationRandonnee() {
        btnChoisirDepart.setVisibility(View.VISIBLE);
        btnChoisirArrivee.setVisibility(View.VISIBLE);

        // En création, les sections de données n'existent pas encore
        containerPoi.setVisibility(View.GONE);
        containerParticipants.setVisibility(View.GONE);
        containerFoodProducts.setVisibility(View.GONE);
        containerEquipments.setVisibility(View.GONE);

        btnSaveHike.setText("Créer la randonnée");
        btnSaveHike.setOnClickListener(v -> traiterCreation());

        setChampsEditables(true);
    }

    /**
     * Configure l'interface en mode <b>Modification</b> :
     * tous les champs sont éditables, toutes les sections sont visibles,
     * et tous les boutons d'ajout sont actifs.
     *
     * <p>Gestion avancée des participants : la suppression d'un participant
     * propose de retirer ou conserver ses équipements liés (ceux dont
     * {@link EquipmentItem#getOwnerId()} correspond à l'id du participant).</p>
     */
    private void modificationRandonnee() {
        btnChoisirDepart.setVisibility(View.VISIBLE);
        btnChoisirArrivee.setVisibility(View.VISIBLE);

        containerPoi.setVisibility(View.VISIBLE);
        containerParticipants.setVisibility(View.VISIBLE);
        containerFoodProducts.setVisibility(View.VISIBLE);
        containerEquipments.setVisibility(View.VISIBLE);

        setChampsEditables(true);

        btnAjouterPOI.setVisibility(View.VISIBLE);
        btnAjouterParticipant.setVisibility(View.VISIBLE);
        btnAddFoodProduct.setVisibility(View.VISIBLE);
        btnAddEquipment.setVisibility(View.VISIBLE);

        listePoints.setEnabled(true);
        listeParticipants.setEnabled(true);
        listeFoodProducts.setEnabled(true);
        listeEquipments.setEnabled(true);

        btnSaveHike.setVisibility(View.VISIBLE);
        btnOptimizeHike.setVisibility(View.GONE);
        btnStartCourse.setVisibility(View.GONE);

        // Mise à jour de l'état du bouton d'ajout de participant (max 3)
        updateBtnParticipantState();

        // --- GESTION DES POI ---
        // Clic sur "Ajouter POI" : ouverture du dialog en mode création (position = -1)
        btnAjouterPOI.setOnClickListener(v -> gererDialogPOI(-1, false));
        // Clic sur un POI existant : ouverture du dialog en mode édition
        listePoints.setOnItemClickListener((parent, view, position, id) ->
                gererDialogPOI(position, false));

        // --- GESTION DES PARTICIPANTS ---
        btnAjouterParticipant.setOnClickListener(v -> {
            PopUpParticipant.afficherDialogParticipant(this, ModeRandonnee.CREATION,
                    tokenManager.getToken(), currentHike.getId(), null,
                    new ParticipantCallback() {
                        @Override
                        public void onActionSuccess(Participant newParticipant) {
                            // Confirmation avant sauvegarde : l'ajout de participant
                            // nécessite une persistance immédiate via l'API
                            new AlertDialog.Builder(ActiviteGestionRandonnee.this)
                                    .setTitle("Confirmer l'ajout")
                                    .setMessage("Le participant va être sauvegardé et vous serez "
                                            + "redirigé vers la consultation de la randonnée.")
                                    .setPositiveButton("Oui", (dialog, which) -> {
                                        ServiceParticipant.ajouterParticipantAPI(
                                                ActiviteGestionRandonnee.this,
                                                tokenManager.getToken(),
                                                currentHike.getId(),
                                                newParticipant,
                                                new AppelAPI.VolleyObjectCallback() {
                                                    @Override
                                                    public void onSuccess(JSONObject result) {
                                                        Toast.makeText(ActiviteGestionRandonnee.this,
                                                                "Participant " + newParticipant.getPrenom() + " ajouté",
                                                                Toast.LENGTH_SHORT).show();
                                                        fermerAvecResultat(Activity.RESULT_OK, null);
                                                    }
                                                    @Override
                                                    public void onError(VolleyError error) {
                                                        Toast.makeText(ActiviteGestionRandonnee.this,
                                                                "Erreur lors de l'ajout du participant",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    })
                                    .setNegativeButton("Annuler", null)
                                    .show();
                        }
                        @Override
                        public void onDeleteAction(Participant participantToDelete) {}
                    });
        });

        // Clic sur un participant existant : ouverture en mode modification
        // La suppression déclenche un contrôle des équipements liés avant suppression API
        listeParticipants.setOnItemClickListener((parent, view, position, id) -> {
            Participant pToEdit = listeTemporaireParticipants.get(position);
            if (pToEdit.getIdRando() == 0) pToEdit.setIdRando(currentHike.getId());

            PopUpParticipant.afficherDialogParticipant(this, ModeRandonnee.MODIFICATION,
                    tokenManager.getToken(), currentHike.getId(), pToEdit,
                    new ParticipantCallback() {
                        @Override
                        public void onActionSuccess(Participant updatedParticipant) {
                            ServiceParticipant.modifierParticipantAPI(
                                    ActiviteGestionRandonnee.this,
                                    tokenManager.getToken(),
                                    currentHike.getId(),
                                    updatedParticipant,
                                    new AppelAPI.VolleyObjectCallback() {
                                        @Override
                                        public void onSuccess(JSONObject result) {
                                            listeTemporaireParticipants.set(position, updatedParticipant);
                                            adapterParticipants.notifyDataSetChanged();
                                            Toast.makeText(ActiviteGestionRandonnee.this,
                                                    "Participant mis à jour", Toast.LENGTH_SHORT).show();
                                        }
                                        @Override
                                        public void onError(VolleyError error) {
                                            Toast.makeText(ActiviteGestionRandonnee.this,
                                                    "Erreur mise à jour participant", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                        @Override
                        public void onDeleteAction(Participant participantToDelete) {
                            // Recherche des équipements portés par ce participant
                            List<EquipmentItem> equipementsLies = new ArrayList<>();
                            for (EquipmentItem eq : listeTemporaireEquipments) {
                                if (eq.getOwnerId() != null
                                        && eq.getOwnerId() == participantToDelete.getId()) {
                                    equipementsLies.add(eq);
                                }
                            }

                            if (!equipementsLies.isEmpty()) {
                                // Construction de la liste des noms pour l'affichage
                                StringBuilder noms = new StringBuilder();
                                for (EquipmentItem eq : equipementsLies) {
                                    noms.append("• ").append(eq.getNom()).append("\n");
                                }

                                // Dialogue de choix : retirer les équipements liés ou les conserver
                                new AlertDialog.Builder(ActiviteGestionRandonnee.this)
                                        .setTitle("Équipements liés")
                                        .setMessage("Ce participant possède :\n\n" + noms
                                                + "\nVoulez-vous aussi les retirer de la randonnée ?")
                                        .setPositiveButton("Retirer aussi", (dialog, which) -> {
                                            listeTemporaireEquipments.removeAll(equipementsLies);
                                            adapterEquipments.notifyDataSetChanged();
                                            executerSuppressionParticipant(participantToDelete);
                                        })
                                        .setNegativeButton("Conserver", (dialog, which) -> {
                                            // On désaffecte les équipements sans les supprimer
                                            for (EquipmentItem eq : equipementsLies) eq.setOwnerId(null);
                                            adapterEquipments.notifyDataSetChanged();
                                            executerSuppressionParticipant(participantToDelete);
                                        })
                                        .show();
                            } else {
                                // Aucun équipement lié : suppression directe
                                executerSuppressionParticipant(participantToDelete);
                            }
                        }
                    });
        });

        // --- GESTION DES PRODUITS ALIMENTAIRES ---
        listeFoodProducts.setOnItemClickListener((parent, view, position, id) -> {
            FoodProduct fp = listeTemporaireFoodProducts.get(position);
            PopUpFoodProduct.afficherPopupDetailsFoodProduct(ActiviteGestionRandonnee.this, fp);
        });

        btnAddFoodProduct.setOnClickListener(v -> afficherPopupSelectionFoodProduct());

        // Appui long sur un produit : demande de confirmation de suppression locale
        listeFoodProducts.setOnItemLongClickListener((parent, view, position, id) -> {
            FoodProduct fpASupprimer = listeTemporaireFoodProducts.get(position);
            afficherConfirmationSuppressionFoodProduct(fpASupprimer, position);
            return true;
        });

        // --- GESTION DES EQUIPEMENTS ---
        listeEquipments.setOnItemClickListener((parent, view, position, id) -> {
            EquipmentItem eq = listeTemporaireEquipments.get(position);
            PopUpEquipment.afficherPopupDetailsEquipment(ActiviteGestionRandonnee.this, eq,
                    listeTemporaireParticipants);
        });

        btnAddEquipment.setOnClickListener(v -> afficherPopupSelectionEquipment());

        // Appui long sur un équipement : demande de confirmation de suppression locale
        listeEquipments.setOnItemLongClickListener((parent, view, position, id) -> {
            EquipmentItem eqASupprimer = listeTemporaireEquipments.get(position);
            afficherConfirmationSuppressionEquipment(eqASupprimer, position);
            return true;
        });

        btnSaveHike.setOnClickListener(v -> traiterMiseAJour());
    }

    // =========================================================
    //  Opérations de persistance (création / mise à jour)
    // =========================================================

    /**
     * Valide et envoie les données du formulaire pour créer une nouvelle randonnée.
     *
     * <p>Flux :</p>
     * <ol>
     *   <li>Lecture et nettoyage des champs de saisie.</li>
     *   <li>Validation via {@link ValidateurRandonnee#verifierDonnees}.</li>
     *   <li>Appel à {@link ServiceCreationRandonnee#creerRandonnee} en cas de succès.</li>
     *   <li>Fermeture de l'activité avec {@code RESULT_OK} si la création réussit.</li>
     * </ol>
     *
     * <p>Le nom du départ et de l'arrivée sont générés automatiquement
     * à partir du libellé de la randonnée.</p>
     */
    private void traiterCreation() {
        String nom    = libelle.getText().toString().trim();
        String latDStr = departLat.getText().toString().trim();
        String lonDStr = departLon.getText().toString().trim();
        String latAStr = arriveeLat.getText().toString().trim();
        String lonAStr = arriveeLon.getText().toString().trim();

        int duree = 1;
        if (nbJours.getSelectedItem() != null) {
            try {
                duree = Integer.parseInt(nbJours.getSelectedItem().toString());
            } catch (NumberFormatException e) {
                // Valeur par défaut conservée en cas d'erreur de parsing
            }
        }

        // Validation des données saisies avant tout appel réseau
        String erreurValidation = ValidateurRandonnee.verifierDonnees(nom, latDStr, lonDStr, latAStr, lonAStr, duree);
        if (erreurValidation != null) {
            Toast.makeText(this, erreurValidation, Toast.LENGTH_LONG).show();
            return;
        }

        // Normalisation du séparateur décimal (virgule → point)
        double latD = Double.parseDouble(latDStr.replace(",", "."));
        double lonD = Double.parseDouble(lonDStr.replace(",", "."));
        double latA = Double.parseDouble(latAStr.replace(",", "."));
        double lonA = Double.parseDouble(lonAStr.replace(",", "."));

        // Les libellés des points de départ/arrivée sont dérivés du nom de la randonnée
        String nomDepart  = "Départ : " + nom;
        String descDepart = "Point de départ de la randonnée";
        String nomArrivee = "Arrivée : " + nom;
        String descArrivee = "Point d'arrivée de la randonnée";

        ServiceCreationRandonnee.creerRandonnee(
                this, tokenManager.getToken(),
                nom, duree,
                nomDepart, descDepart, latD, lonD,
                nomArrivee, descArrivee, latA, lonA,
                new ServiceCreationRandonnee.FullCreationCallback() {
                    @Override
                    public void onSuccess(long hikeId) {
                        Toast.makeText(ActiviteGestionRandonnee.this,
                                "Randonnée créée avec succès !", Toast.LENGTH_SHORT).show();
                        fermerAvecResultat(Activity.RESULT_OK, null);
                    }
                    @Override
                    public void onError(String message) {
                        // Cas le plus fréquent : nom de randonnée déjà utilisé
                        Toast.makeText(ActiviteGestionRandonnee.this,
                                "Erreur lors de la création : Nom déjà utilisé",
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    /**
     * Valide et envoie les modifications de la randonnée courante vers l'API.
     *
     * <p>En cas de succès de la mise à jour principale, déclenche en chaîne
     * la synchronisation des participants, des POI, des produits alimentaires
     * et des équipements, puis ferme l'activité.</p>
     */
    private void traiterMiseAJour() {
        if (currentHike == null || currentHike.getId() == 0) {
            Toast.makeText(this, "Erreur ID Randonnée", Toast.LENGTH_SHORT).show();
            return;
        }

        String libelleTexte = libelle.getText().toString();
        int    dureeJours   = Integer.parseInt(nbJours.getSelectedItem().toString());
        double latitudeD    = Double.parseDouble(departLat.getText().toString());
        double longitudeD   = Double.parseDouble(departLon.getText().toString());
        double latitudeA    = Double.parseDouble(arriveeLat.getText().toString());
        double longitudeA   = Double.parseDouble(arriveeLon.getText().toString());

        ServiceModificationRandonnee.modifierRandonneeAPI(
                this, tokenManager.getToken(),
                currentHike.getId(), libelleTexte, dureeJours,
                latitudeD, longitudeD, latitudeA, longitudeA,
                new ServiceModificationRandonnee.UpdateHikeCallback() {

                    @Override
                    public void onSuccess() {
                        // Synchronisations enchaînées après mise à jour réussie
                        traiterMiseAJourParticipants(currentHike.getId());
                        traiterMiseAJourPOI(currentHike.getId());

                        // Diff et synchronisation des produits alimentaires
                        ServiceFoodProduct.synchroniserFoodProducts(
                                ActiviteGestionRandonnee.this,
                                tokenManager.getToken(),
                                currentHike.getId(),
                                currentHike.getFoodCatalogue(),
                                listeTemporaireFoodProducts
                        );

                        // Diff et synchronisation des équipements
                        ServiceEquipment.synchroniserEquipments(
                                ActiviteGestionRandonnee.this,
                                tokenManager.getToken(),
                                currentHike.getId(),
                                currentHike.getEquipmentGroups(),
                                listeTemporaireEquipments
                        );

                        Toast.makeText(ActiviteGestionRandonnee.this,
                                "Modifications enregistrées", Toast.LENGTH_SHORT).show();
                        fermerAvecResultat(Activity.RESULT_OK, null);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(ActiviteGestionRandonnee.this,
                                "Erreur MAJ Rando", Toast.LENGTH_SHORT).show();
                        Log.e("erreur rando", message);
                    }
                });
    }

    /**
     * Délègue la synchronisation des participants au service dédié.
     *
     * @param hikeId identifiant de la randonnée à mettre à jour.
     */
    private void traiterMiseAJourParticipants(int hikeId) {
        ServiceParticipant.traiterMAJParticipants(this, hikeId,
                listeTemporaireParticipants, participantsOriginaux, tokenManager);
    }

    /**
     * Délègue la synchronisation des points d'intérêt au service dédié.
     *
     * @param hikeId identifiant de la randonnée à mettre à jour.
     */
    private void traiterMiseAJourPOI(int hikeId) {
        ServicePOI.traiterMAJPOI(this, hikeId, listeTemporairePOI, tokenManager.getToken());
    }

    // =========================================================
    //  Remplissage et édition des champs
    // =========================================================

    /**
     * Remplit tous les champs de l'interface à partir des données d'un objet {@link Hike}.
     *
     * <p>Initialise également les listes temporaires (POI, participants,
     * produits alimentaires, équipements) en les synchronisant avec les
     * collections de la randonnée, puis notifie les adaptateurs.</p>
     *
     * @param hike randonnée dont les données doivent être affichées.
     */
    private void remplissageChamps(Hike hike) {
        libelle.setText(hike.getLibelle());

        if (hike.getDepart() != null) {
            departLat.setText(String.valueOf(hike.getDepart().getLatitude()));
            departLon.setText(String.valueOf(hike.getDepart().getLongitude()));
        }
        if (hike.getArrivee() != null) {
            arriveeLat.setText(String.valueOf(hike.getArrivee().getLatitude()));
            arriveeLon.setText(String.valueOf(hike.getArrivee().getLongitude()));
        }

        // Affichage des coordonnées sous forme lisible
        if (hike.getDepart() != null) {
            tvCoordsDepart.setText(String.format("📍 %.5f, %.5f",
                    hike.getDepart().getLatitude(), hike.getDepart().getLongitude()));
            tvCoordsDepart.setVisibility(View.VISIBLE);
        }
        if (hike.getArrivee() != null) {
            tvCoordsArrivee.setText(String.format("📍 %.5f, %.5f",
                    hike.getArrivee().getLatitude(), hike.getArrivee().getLongitude()));
            tvCoordsArrivee.setVisibility(View.VISIBLE);
        }

        // Positionnement du spinner sur la valeur correspondante (1-indexé → 0-indexé)
        if (hike.getDureeJours() > 0 && hike.getDureeJours() <= 10) {
            nbJours.setSelection(hike.getDureeJours() - 1);
        }

        // Initialisation des listes temporaires à partir des données de la randonnée
        listeTemporairePOI.clear();
        if (hike.getOptionalPoints() != null) listeTemporairePOI.addAll(hike.getOptionalPoints());
        adapterPOI.notifyDataSetChanged();

        listeTemporaireParticipants.clear();
        if (hike.getParticipants() != null) listeTemporaireParticipants.addAll(hike.getParticipants());
        adapterParticipants.notifyDataSetChanged();

        listeTemporaireFoodProducts.clear();
        if (hike.getFoodCatalogue() != null) listeTemporaireFoodProducts.addAll(hike.getFoodCatalogue());
        adapterFoodProducts.notifyDataSetChanged();

        listeTemporaireEquipments.clear();
        if (hike.getEquipmentGroups() != null) listeTemporaireEquipments.addAll(hike.getEquipmentGroups());
        adapterEquipments.notifyDataSetChanged();

        updateBtnParticipantState();
    }

    /**
     * Active ou désactive l'édition de tous les champs de saisie principaux
     * (libellé, coordonnées, durée).
     *
     * @param editable {@code true} pour rendre les champs modifiables,
     *                 {@code false} pour les mettre en lecture seule.
     */
    private void setChampsEditables(boolean editable) {
        libelle.setEnabled(editable);
        departLat.setEnabled(editable);
        departLon.setEnabled(editable);
        arriveeLat.setEnabled(editable);
        arriveeLon.setEnabled(editable);
        nbJours.setEnabled(editable);
    }

    /**
     * Met à jour l'état visuel du bouton "Ajouter participant" selon la limite
     * maximale de 3 participants par randonnée.
     *
     * <p>Quand la limite est atteinte, le bouton est grisé et désactivé.</p>
     */
    private void updateBtnParticipantState() {
        if (listeTemporaireParticipants.size() >= 3) {
            btnAjouterParticipant.setEnabled(false);
            btnAjouterParticipant.setAlpha(0.5f); // Indication visuelle de désactivation
        } else {
            btnAjouterParticipant.setEnabled(true);
            btnAjouterParticipant.setAlpha(1.0f);
        }
    }

    // =========================================================
    //  Gestion des POI (Points d'intérêt)
    // =========================================================

    /**
     * Affiche un dialog de création ou de modification d'un Point d'Intérêt (POI).
     *
     * <p>Le dialog intègre une mini-carte OSMDroid interactive permettant de
     * positionner le marqueur par simple tap. Les champs latitude/longitude
     * sont mis à jour automatiquement lors du tap.</p>
     *
     * <p>En mode création ({@code position == -1}), un nouveau POI est ajouté
     * à {@link #listeTemporairePOI}. En mode édition, le POI existant est modifié
     * en place. En mode lecture seule, le bouton "Valider" est remplacé par "Fermer".</p>
     *
     * @param position  index du POI dans la liste (-1 pour une création).
     * @param isReadOnly {@code true} pour un affichage en consultation pure.
     */
    private void gererDialogPOI(int position, boolean isReadOnly) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ajout_poi, null);
        boolean isCreation = (position == -1);
        PointOfInterest poi = (!isCreation) ? listeTemporairePOI.get(position) : null;

        final EditText inputNom = view.findViewById(R.id.edit_poi_nom);
        final EditText inputLat = view.findViewById(R.id.edit_poi_lat);
        final EditText inputLon = view.findViewById(R.id.edit_poi_lon);
        final MapView mapPoi   = view.findViewById(R.id.map_poi);

        // Configuration OSMDroid de la mini-carte du dialog
        Configuration.getInstance().setUserAgentValue(getPackageName());
        mapPoi.setTileSource(TileSourceFactory.MAPNIK);
        mapPoi.setMultiTouchControls(true);

        // Centrage sur le POI existant ou sur la France entière (zoom 6)
        double latInit  = (poi != null) ? poi.getLatitude()  : 46.2276;
        double lonInit  = (poi != null) ? poi.getLongitude() : 2.2137;
        double zoomInit = (poi != null) ? 15.0 : 6.0;

        GeoPoint startPoint = new GeoPoint(latInit, lonInit);
        mapPoi.getController().setZoom(zoomInit);
        mapPoi.getController().setCenter(startPoint);

        // Marqueur initial (pour édition uniquement)
        final Marker[] marker = {null};
        if (poi != null) {
            marker[0] = new Marker(mapPoi);
            marker[0].setPosition(startPoint);
            marker[0].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapPoi.getOverlays().add(marker[0]);
        }

        if (poi != null) {
            inputNom.setText(poi.getNom());
            inputLat.setText(String.valueOf(poi.getLatitude()));
            inputLon.setText(String.valueOf(poi.getLongitude()));
        }

        if (!isReadOnly) {
            // Overlay de capture des taps : déplace le marqueur et met à jour les champs
            MapEventsOverlay eventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
                @Override
                public boolean singleTapConfirmedHelper(GeoPoint p) {
                    if (marker[0] != null) mapPoi.getOverlays().remove(marker[0]);
                    marker[0] = new Marker(mapPoi);
                    marker[0].setPosition(p);
                    marker[0].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    mapPoi.getOverlays().add(marker[0]);
                    mapPoi.invalidate();
                    inputLat.setText(String.valueOf(p.getLatitude()));
                    inputLon.setText(String.valueOf(p.getLongitude()));
                    return true;
                }
                @Override
                public boolean longPressHelper(GeoPoint p) { return false; }
            });
            // Ajout en position 0 pour que l'overlay soit sous les marqueurs
            mapPoi.getOverlays().add(0, eventsOverlay);
        }

        // Construction du dialog (titre adapté au mode)
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isCreation ? "Nouveau POI" : (isReadOnly ? "Détails POI" : "Modifier POI"));

        if (isReadOnly) {
            inputNom.setEnabled(false); inputLat.setEnabled(false); inputLon.setEnabled(false);
            builder.setPositiveButton("Fermer", (d, w) -> mapPoi.onDetach());
        } else {
            builder.setPositiveButton("Valider", null); // Listener défini après show() pour validation
            builder.setNegativeButton("Annuler", (d, w) -> mapPoi.onDetach());
            if (!isCreation) {
                // Bouton neutre "Supprimer" uniquement en mode édition d'un POI existant
                builder.setNeutralButton("Supprimer", (dialog, which) -> {
                    listeTemporairePOI.remove(position);
                    adapterPOI.notifyDataSetChanged();
                    mapPoi.onDetach();
                });
            }
        }

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        if (!isReadOnly) {
            // Listener "Valider" défini après show() pour empêcher la fermeture automatique
            // en cas d'erreur de validation (comportement par défaut des AlertDialog)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String nom    = inputNom.getText().toString();
                String latStr = inputLat.getText().toString();
                String lonStr = inputLon.getText().toString();

                PoiValidator.ValidationResult result = PoiValidator.valider(nom, latStr, lonStr);
                if (result.isValid()) {
                    if (poi != null) {
                        // Mise à jour en place du POI existant
                        poi.setNom(nom);
                        poi.setLatitude(result.getLatitude());
                        poi.setLongitude(result.getLongitude());
                    } else {
                        // Création d'un nouveau POI avec id=0 (sera attribué par le serveur)
                        listeTemporairePOI.add(new PointOfInterest(0, nom,
                                result.getLatitude(), result.getLongitude(), null, 0));
                    }
                    adapterPOI.notifyDataSetChanged();
                    mapPoi.onDetach();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    // =========================================================
    //  Optimisation de la randonnée
    // =========================================================

    /**
     * Déclenche l'optimisation de l'ordre des POI via l'API REST.
     *
     * <p>En cas de succès, l'activité se ferme avec {@code RESULT_OK}
     * afin que le fragment parent puisse rafraîchir ses données.
     * En cas d'erreur, un message est affiché sans fermer l'activité.</p>
     */
    private void lancerOptimisation() {
        if (currentHike == null || currentHike.getId() == 0) {
            Toast.makeText(this,
                    "Impossible d'optimiser une randonnée non enregistrée.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Optimisation en cours...", Toast.LENGTH_SHORT).show();

        ServiceOptimisation.optimiserRandonnee(
                this,
                tokenManager.getToken(),
                currentHike.getId(),
                new ServiceOptimisation.OptimisationCallback() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        Toast.makeText(ActiviteGestionRandonnee.this,
                                "Randonnée optimisée avec succès !", Toast.LENGTH_SHORT).show();
                        // Fermeture avec RESULT_OK pour signaler le changement au fragment parent
                        setResult(Activity.RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        // L'activité reste ouverte pour permettre une nouvelle tentative
                        PopupUtil.showLongToast(ActiviteGestionRandonnee.this,
                                "Erreur d'optimisation : " + message);
                    }
                }
        );
    }

    // =========================================================
    //  Gestion des produits alimentaires
    // =========================================================

    /**
     * Charge le catalogue complet de produits alimentaires depuis l'API et
     * affiche un dialog de sélection.
     *
     * <p>Les produits déjà présents dans la liste temporaire sont ajoutés
     * via {@link #ajouterProduitLocal(FoodProduct)} qui effectue un contrôle de doublon.</p>
     */
    private void afficherPopupSelectionFoodProduct() {
        ServiceFoodProduct.getAllFoodProducts(this, tokenManager.getToken(),
                new AppelAPI.VolleyCallback() {
                    @Override
                    public void onSuccess(JSONArray result) {
                        try {
                            List<FoodProduct> catalogueComplet = new ArrayList<>();
                            List<String> nomsProduits = new ArrayList<>();

                            for (int i = 0; i < result.length(); i++) {
                                FoodProduct fp = ServiceFoodProduct.constructFPFromJson(
                                        result.getJSONObject(i));
                                catalogueComplet.add(fp);
                                nomsProduits.add(fp.getNom() + " (" + fp.getMasseGrammes() + "g)");
                            }

                            String[] tableauNoms = nomsProduits.toArray(new String[0]);
                            new AlertDialog.Builder(ActiviteGestionRandonnee.this)
                                    .setTitle("Sélectionner un produit")
                                    .setItems(tableauNoms, (dialog, which) -> {
                                        FoodProduct produitSelectionne = catalogueComplet.get(which);
                                        ajouterProduitLocal(produitSelectionne);
                                    })
                                    .setNegativeButton("Annuler", null)
                                    .show();

                        } catch (JSONException e) {
                            Toast.makeText(ActiviteGestionRandonnee.this,
                                    "Erreur de lecture des produits", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Toast.makeText(ActiviteGestionRandonnee.this,
                                "Impossible de charger le catalogue", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Ajoute un produit alimentaire à la liste temporaire locale,
     * après vérification qu'il n'y est pas déjà présent.
     *
     * <p>Cette méthode ne fait qu'une modification locale : la persistance
     * vers l'API se fait lors de l'appel à {@link #traiterMiseAJour()}.</p>
     *
     * @param produit produit à ajouter.
     */
    private void ajouterProduitLocal(FoodProduct produit) {
        // Vérification de doublon par id
        for (FoodProduct fp : listeTemporaireFoodProducts) {
            if (fp.getId() == produit.getId()) {
                Toast.makeText(this, "Ce produit est déjà dans la randonnée", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        listeTemporaireFoodProducts.add(produit);
        adapterFoodProducts.notifyDataSetChanged();
        Toast.makeText(this, produit.getNom() + " ajouté à la liste !", Toast.LENGTH_SHORT).show();
    }

    /**
     * Affiche un dialog de confirmation avant de retirer un produit alimentaire
     * de la liste temporaire locale.
     *
     * @param produit  produit candidat à la suppression.
     * @param position index du produit dans la liste.
     */
    private void afficherConfirmationSuppressionFoodProduct(FoodProduct produit, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Retirer le produit")
                .setMessage("Voulez-vous retirer " + produit.getNom() + " de la randonnée ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    listeTemporaireFoodProducts.remove(position);
                    adapterFoodProducts.notifyDataSetChanged();
                    Toast.makeText(ActiviteGestionRandonnee.this,
                            "Produit retiré de la liste", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    // =========================================================
    //  Gestion des équipements
    // =========================================================

    /**
     * Charge le catalogue complet d'équipements depuis l'API et affiche
     * un dialog de sélection.
     *
     * <p>Note : la construction de l'objet {@link EquipmentItem} est faite
     * manuellement ici (pas via {@link ServiceEquipment#constructEqFromJson})
     * car la réponse est un tableau JSON simple, pas un objet groupé.</p>
     * <p>Le champ {@code masseAVide} est traité manuellement pour éviter que
     * {@code optDouble} retourne {@code NaN} quand la clé est présente mais nulle.</p>
     */
    private void afficherPopupSelectionEquipment() {
        ServiceEquipment.getAllEquipments(this, tokenManager.getToken(),
                new AppelAPI.VolleyCallback() {
                    @Override
                    public void onSuccess(JSONArray result) {
                        try {
                            List<EquipmentItem> catalogueComplet = new ArrayList<>();
                            List<String> nomsEquipments = new ArrayList<>();

                            for (int i = 0; i < result.length(); i++) {
                                JSONObject obj = result.getJSONObject(i);
                                EquipmentItem eq = new EquipmentItem();
                                eq.setId(obj.getInt("id"));
                                eq.setNom(obj.getString("nom"));
                                eq.setDescription(obj.optString("description", ""));
                                eq.setMasseGrammes(obj.getDouble("masseGrammes"));
                                eq.setNbItem(obj.getInt("nbItem"));
                                eq.setType(TypeEquipment.valueOf(obj.getString("type")));
                                // Vérification explicite pour éviter NaN sur un champ null
                                if (!obj.isNull("masseAVide")) {
                                    eq.setMasseAVide(obj.getDouble("masseAVide"));
                                }

                                catalogueComplet.add(eq);
                                nomsEquipments.add(eq.getNom() + " (" + eq.getMasseGrammes() + "g)");
                            }

                            String[] tableauNoms = nomsEquipments.toArray(new String[0]);
                            new AlertDialog.Builder(ActiviteGestionRandonnee.this)
                                    .setTitle("Sélectionner un équipement")
                                    .setItems(tableauNoms, (dialog, which) -> {
                                        EquipmentItem eqSelectionne = catalogueComplet.get(which);
                                        gererAjoutEquipment(eqSelectionne);
                                    })
                                    .setNegativeButton("Annuler", null)
                                    .show();

                        } catch (JSONException e) {
                            Toast.makeText(ActiviteGestionRandonnee.this,
                                    "Erreur de lecture des équipements", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Toast.makeText(ActiviteGestionRandonnee.this,
                                "Impossible de charger le catalogue", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Orchestre l'ajout d'un équipement à la liste temporaire.
     *
     * <p>Règles d'ajout :</p>
     * <ul>
     *   <li>Si l'équipement est déjà dans la liste, un message l'indique.</li>
     *   <li>Si le type est {@link TypeEquipment#VETEMENT} ou {@link TypeEquipment#REPOS},
     *       un propriétaire doit être sélectionné parmi les participants
     *       ({@link #demanderProprietaireEtAjouter}).</li>
     *   <li>Sinon, l'équipement est ajouté directement sans propriétaire.</li>
     * </ul>
     *
     * @param eq équipement sélectionné dans le catalogue.
     */
    private void gererAjoutEquipment(EquipmentItem eq) {
        // Contrôle de doublon par id
        for (EquipmentItem item : listeTemporaireEquipments) {
            if (item.getId() == eq.getId()) {
                Toast.makeText(this, "Cet équipement est déjà dans la randonnée",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Les équipements personnels (VETEMENT, REPOS) nécessitent un propriétaire
        if (eq.getType() == TypeEquipment.VETEMENT || eq.getType() == TypeEquipment.REPOS) {
            demanderProprietaireEtAjouter(eq);
        } else {
            ajouterEquipmentFinal(eq, null);
        }
    }

    /**
     * Affiche un dialog de sélection du participant propriétaire d'un équipement
     * personnel, puis délègue l'ajout à {@link #ajouterEquipmentFinal}.
     *
     * <p>Si aucun participant n'est encore ajouté à la randonnée, un message
     * guide l'utilisateur à en ajouter un d'abord.</p>
     *
     * @param eq équipement dont il faut désigner le propriétaire.
     */
    private void demanderProprietaireEtAjouter(EquipmentItem eq) {
        if (listeTemporaireParticipants.isEmpty()) {
            Toast.makeText(this,
                    "Veuillez d'abord ajouter des participants à la randonnée.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Construction de la liste de noms pour le dialog de sélection
        List<String> nomParticipants = new ArrayList<>();
        for (Participant p : listeTemporaireParticipants) {
            nomParticipants.add(p.getPrenom() + " " + p.getNom());
        }

        String[] tableauParticipants = nomParticipants.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("À qui appartient cet équipement ?")
                .setItems(tableauParticipants, (dialog, which) -> {
                    int idProprietaire = listeTemporaireParticipants.get(which).getId();
                    ajouterEquipmentFinal(eq, idProprietaire);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    /**
     * Ajoute l'équipement à la liste temporaire locale en y associant l'id du propriétaire.
     *
     * <p>La persistance vers l'API se fait lors de l'appel à {@link #traiterMiseAJour()}.</p>
     *
     * @param eq      équipement à ajouter.
     * @param ownerId id du participant propriétaire, ou {@code null} si l'équipement
     *                est partagé / sans propriétaire désigné.
     */
    private void ajouterEquipmentFinal(EquipmentItem eq, Integer ownerId) {
        eq.setOwnerId(ownerId);
        listeTemporaireEquipments.add(eq);
        adapterEquipments.notifyDataSetChanged();
        Toast.makeText(this, eq.getNom() + " ajouté à la liste !", Toast.LENGTH_SHORT).show();
    }

    /**
     * Ajoute un équipement à la liste locale sans gestion du propriétaire
     * (alias simplifié, utilisé dans certains flux de création directe).
     *
     * @param eq équipement à ajouter.
     */
    private void ajouterEquipmentLocal(EquipmentItem eq) {
        for (EquipmentItem item : listeTemporaireEquipments) {
            if (item.getId() == eq.getId()) {
                Toast.makeText(this, "Cet équipement est déjà dans la randonnée",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }
        listeTemporaireEquipments.add(eq);
        adapterEquipments.notifyDataSetChanged();
        Toast.makeText(this, eq.getNom() + " ajouté à la liste !", Toast.LENGTH_SHORT).show();
    }

    /**
     * Affiche un dialog de confirmation avant de retirer un équipement
     * de la liste temporaire locale.
     *
     * @param eq       équipement candidat à la suppression.
     * @param position index de l'équipement dans la liste.
     */
    private void afficherConfirmationSuppressionEquipment(EquipmentItem eq, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Retirer l'équipement")
                .setMessage("Voulez-vous retirer " + eq.getNom() + " de la randonnée ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    listeTemporaireEquipments.remove(position);
                    adapterEquipments.notifyDataSetChanged();
                    Toast.makeText(ActiviteGestionRandonnee.this,
                            "Équipement retiré de la liste", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    // =========================================================
    //  Démarrage de la course (GPS)
    // =========================================================

    /**
     * Vérifie la permission de localisation puis récupère la dernière position
     * GPS connue pour démarrer une course.
     *
     * <p>Si la permission n'est pas accordée, Android affiche la demande système
     * et le résultat est traité dans {@link #onRequestPermissionsResult}.</p>
     *
     * <p>Le bouton "Démarrer" est désactivé pendant la recherche GPS pour éviter
     * les doubles clics.</p>
     */
    private void lancerDemarrageCourse() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        btnStartCourse.setEnabled(false);
        Toast.makeText(this, "Recherche de la position GPS...", Toast.LENGTH_SHORT).show();

        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        // Récupération de la dernière position connue (fusion réseau + GPS)
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                appelerAPIStartCourse(location.getLatitude(), location.getLongitude());
            } else {
                btnStartCourse.setEnabled(true);
                Toast.makeText(this,
                        "Position introuvable. Veuillez vérifier que votre GPS est activé.",
                        Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            btnStartCourse.setEnabled(true);
            Toast.makeText(this,
                    "Erreur lors de la récupération de la position.",
                    Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Appelle l'API REST pour créer une nouvelle course ({@code POST /courses})
     * à partir de la position GPS courante.
     *
     * <p>En cas de succès, construit l'intent vers {@link SuiviParcours} en
     * y injectant toutes les données nécessaires :</p>
     * <ul>
     *   <li>ID de la course, nom et date.</li>
     *   <li>Tableaux de latitudes/longitudes du tracé initial.</li>
     *   <li>Coordonnées et noms des points clés de la randonnée (départ, arrivée, POI).</li>
     * </ul>
     *
     * @param lat latitude de la position GPS de départ.
     * @param lon longitude de la position GPS de départ.
     */
    private void appelerAPIStartCourse(double lat, double lon) {
        if (currentHike == null || currentHike.getId() == 0) {
            btnStartCourse.setEnabled(true);
            Toast.makeText(this, "Erreur : ID Randonnée invalide.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Création de la course en cours...", Toast.LENGTH_SHORT).show();

        ServiceParcours.demarrerCourse(this, tokenManager.getToken(),
                currentHike.getId(), lat, lon,
                new ServiceParcours.CourseCreationCallback() {

                    @Override
                    public void onSuccess(Course courseCreee) {
                        btnStartCourse.setEnabled(true);
                        Toast.makeText(ActiviteGestionRandonnee.this,
                                "Course démarrée avec succès !", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(ActiviteGestionRandonnee.this, SuiviParcours.class);
                        intent.putExtra("COURSE_ID", courseCreee.getId());
                        intent.putExtra("NOM_RANDONNEE", currentHike.getLibelle());
                        intent.putExtra("NOM_PARCOURS", "Parcours - " + currentHike.getLibelle());

                        // Formatage de la date renvoyée par l'API
                        java.time.format.DateTimeFormatter formatter =
                                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                        if (courseCreee.getDateRealisation() != null) {
                            intent.putExtra("DATE_REALISATION",
                                    courseCreee.getDateRealisation().format(formatter));
                        }

                        // Conversion de la liste de GeoCoordinate en deux tableaux primitifs
                        // (les Intent ne supportent pas les objets sérialisables complexes facilement)
                        List<GeoCoordinate> trajetsAPI = courseCreee.getTrajetsRealises();
                        double[] latsArray = new double[trajetsAPI.size()];
                        double[] lonsArray = new double[trajetsAPI.size()];
                        for (int i = 0; i < trajetsAPI.size(); i++) {
                            latsArray[i] = trajetsAPI.get(i).getLatitude();
                            lonsArray[i] = trajetsAPI.get(i).getLongitude();
                        }
                        intent.putExtra("LATITUDES", latsArray);
                        intent.putExtra("LONGITUDES", lonsArray);

                        // Transmission du point de départ de la randonnée
                        PointOfInterest dep = currentHike.getDepart();
                        if (dep != null) {
                            intent.putExtra("HIKE_DEPART_LAT", dep.getLatitude());
                            intent.putExtra("HIKE_DEPART_LON", dep.getLongitude());
                            intent.putExtra("HIKE_DEPART_NOM", dep.getNom());
                        }

                        // Transmission du point d'arrivée de la randonnée
                        PointOfInterest arr = currentHike.getArrivee();
                        if (arr != null) {
                            intent.putExtra("HIKE_ARRIVEE_LAT", arr.getLatitude());
                            intent.putExtra("HIKE_ARRIVEE_LON", arr.getLongitude());
                            intent.putExtra("HIKE_ARRIVEE_NOM", arr.getNom());
                        }

                        // Transmission des POI optionnels sous forme de tableaux
                        List<PointOfInterest> optPoints = currentHike.getOptionalPoints();
                        if (optPoints != null && !optPoints.isEmpty()) {
                            double[] poiLats = new double[optPoints.size()];
                            double[] poiLons  = new double[optPoints.size()];
                            String[] poiNoms  = new String[optPoints.size()];
                            for (int i = 0; i < optPoints.size(); i++) {
                                poiLats[i] = optPoints.get(i).getLatitude();
                                poiLons[i]  = optPoints.get(i).getLongitude();
                                poiNoms[i]  = optPoints.get(i).getNom();
                            }
                            intent.putExtra("HIKE_POI_LATS", poiLats);
                            intent.putExtra("HIKE_POI_LONS", poiLons);
                            intent.putExtra("HIKE_POI_NOMS", poiNoms);
                        }

                        // ID de la randonnée pour un éventuel rechargement dans SuiviParcours
                        intent.putExtra("HIKE_ID", currentHike.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onError(VolleyError error) {
                        btnStartCourse.setEnabled(true);
                        Toast.makeText(ActiviteGestionRandonnee.this,
                                "Erreur lors du démarrage : " + error.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Traite le retour de la demande de permission GPS.
     *
     * <p>Si la permission est accordée, relance {@link #lancerDemarrageCourse()}.
     * Sinon, informe l'utilisateur que la permission est requise.</p>
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                lancerDemarrageCourse();
            } else {
                Toast.makeText(this,
                        "Permission GPS requise pour démarrer la course.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // =========================================================
    //  Utilitaires
    // =========================================================

    /**
     * Ferme l'activité en transmettant un code résultat et un message optionnel
     * à l'activité appelante via l'intent de retour.
     *
     * @param resultCode code Android ({@code Activity.RESULT_OK} ou {@code RESULT_CANCELED}).
     * @param message    message à transmettre (clé {@code "CHILD_MESSAGE"}), ou {@code null}.
     */
    private void fermerAvecResultat(int resultCode, String message) {
        Intent intent = new Intent();
        if (message != null) intent.putExtra("CHILD_MESSAGE", message);
        setResult(resultCode, intent);
        finish();
    }

    /**
     * Supprime un participant de la randonnée, côté API et côté liste locale.
     *
     * <p>Si le participant n'a pas encore été persisté (id == 0, cas d'un ajout
     * annulé avant sauvegarde), seule la liste locale est mise à jour.</p>
     *
     * @param participantToDelete participant à supprimer.
     */
    private void executerSuppressionParticipant(Participant participantToDelete) {
        // Participant non encore sauvegardé en base : suppression locale uniquement
        if (participantToDelete.getId() == 0) {
            listeTemporaireParticipants.remove(participantToDelete);
            adapterParticipants.notifyDataSetChanged();
            Toast.makeText(ActiviteGestionRandonnee.this,
                    "Participant retiré", Toast.LENGTH_SHORT).show();
            return;
        }

        // Participant persisté : appel API de suppression
        ServiceParticipant.supprimerParticipantAPI(
                ActiviteGestionRandonnee.this,
                tokenManager.getToken(),
                currentHike.getId(),
                participantToDelete.getId(),
                new AppelAPI.VolleyObjectCallback() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        listeTemporaireParticipants.remove(participantToDelete);
                        adapterParticipants.notifyDataSetChanged();
                        Toast.makeText(ActiviteGestionRandonnee.this,
                                "Participant supprimé avec succès", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Toast.makeText(ActiviteGestionRandonnee.this,
                                "Erreur lors de la suppression du participant",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}