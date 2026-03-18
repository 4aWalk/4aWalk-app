package fr.iutrodez.a4awalk.activites;

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
import fr.iutrodez.a4awalk.utils.validators.PoiValidator;
import fr.iutrodez.a4awalk.utils.validators.ValidateurRandonnee;

public class ActiviteGestionRandonnee extends HeaderActivity {

    private static final int MODE_CONSULTATION = 1;
    private static final int MODE_CREATION = 2;
    private static final int MODE_MODIFICATION = 3;
    private final String ERREUR = "ERREUR";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    private EditText libelle, departLat, departLon, arriveeLat, arriveeLon;
    private ListView listePoints, listeParticipants, listeFoodProducts, listeEquipments;
    private Spinner nbJours;
    private ImageButton btnAjouterPOI, btnAjouterParticipant;
    private Button btnOptimizeHike, btnSaveHike, btnStartCourse;
    private Button btnAddFoodProduct, btnAddEquipment;
    private LinearLayout containerPoi, containerParticipants, containerFoodProducts, containerEquipments;

    private ArrayList<PointOfInterest> listeTemporairePOI = new ArrayList<>();
    private ArrayList<PointOfInterest> poiOriginaux = new ArrayList<>();
    private ArrayAdapter<PointOfInterest> adapterPOI;

    private ArrayList<Participant> listeTemporaireParticipants = new ArrayList<>();
    private ArrayList<Participant> participantsOriginaux = new ArrayList<>();
    private ArrayAdapter<Participant> adapterParticipants;

    private ArrayList<FoodProduct> listeTemporaireFoodProducts = new ArrayList<>();
    private ArrayList<FoodProduct> foodProductsOriginaux = new ArrayList<>();
    private ArrayAdapter<FoodProduct> adapterFoodProducts;

    // --- NOUVEAUX ELEMENTS POUR LES EQUIPEMENTS ---
    private ArrayList<EquipmentItem> listeTemporaireEquipments = new ArrayList<>();
    private ArrayList<EquipmentItem> equipmentsOriginaux = new ArrayList<>();
    private ArrayAdapter<EquipmentItem> adapterEquipments;

    private TokenManager tokenManager;
    private User currentUser;
    private Hike currentHike;

    private TextView tvCoordsDepart, tvCoordsArrivee;

    private Button btnChoisirDepart, btnChoisirArrivee;


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
                    participantsOriginaux.addAll(listeTemporaireParticipants);
                    poiOriginaux.addAll(listeTemporairePOI);
                    foodProductsOriginaux.addAll(listeTemporaireFoodProducts);
                    equipmentsOriginaux.addAll(listeTemporaireEquipments);
                }
                modificationRandonnee();
                break;
            default:
                fermerAvecResultat(Activity.RESULT_CANCELED, ERREUR);
        }
    }

    private void recupererDonneesUtilisateur() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            currentUser = getIntent().getParcelableExtra("USER_DATA", User.class);
        } else {
            currentUser = getIntent().getParcelableExtra("USER_DATA");
        }
    }

    private void initElementsGraphiques() {
        libelle = findViewById(R.id.nom_rando);
        departLat = findViewById(R.id.depart_rando_lat);
        departLon = findViewById(R.id.depart_rando_lon);
        arriveeLat = findViewById(R.id.arrivee_rando_lat);
        arriveeLon = findViewById(R.id.arrivee_rando_lon);
        btnChoisirDepart = findViewById(R.id.btn_choisir_depart);
        btnChoisirArrivee = findViewById(R.id.btn_choisir_arrivee);
        tvCoordsDepart = findViewById(R.id.tv_coords_depart);
        tvCoordsArrivee = findViewById(R.id.tv_coords_arrivee);

        listePoints = findViewById(R.id.points_list);
        listeParticipants = findViewById(R.id.participants_list);
        listeFoodProducts = findViewById(R.id.food_products_list);
        listeEquipments = findViewById(R.id.equipments_list); // Ajout
        nbJours = findViewById(R.id.spinner_jours);

        containerPoi = findViewById(R.id.container_poi);
        containerParticipants = findViewById(R.id.container_participants);
        containerFoodProducts = findViewById(R.id.container_food_products);
        containerEquipments = findViewById(R.id.container_equipments);

        btnOptimizeHike = findViewById(R.id.btn_optimize_hike);
        btnSaveHike = findViewById(R.id.btn_save_hike);
        btnStartCourse = findViewById(R.id.btn_start_course);

        btnAjouterPOI = findViewById(R.id.btn_add_poi);
        btnAjouterParticipant = findViewById(R.id.btn_add_participant);
        btnAddFoodProduct = findViewById(R.id.btn_add_food_product);
        btnAddEquipment = findViewById(R.id.btn_add_equipment);

        List<Integer> jours = new ArrayList<>();
        for (int i = 1; i <= 3; i++) jours.add(i);
        ArrayAdapter<Integer> adapterJours = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, jours);
        adapterJours.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nbJours.setAdapter(adapterJours);

        adapterPOI = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listeTemporairePOI);
        listePoints.setAdapter(adapterPOI);

        adapterParticipants = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listeTemporaireParticipants);
        listeParticipants.setAdapter(adapterParticipants);

        adapterFoodProducts = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listeTemporaireFoodProducts);
        listeFoodProducts.setAdapter(adapterFoodProducts);

        // Adaptateur Equipements
        adapterEquipments = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listeTemporaireEquipments);
        listeEquipments.setAdapter(adapterEquipments);

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

    private void recupererHikeIntent() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            currentHike = getIntent().getParcelableExtra("HIKE_OBJECT", Hike.class);
        } else {
            currentHike = getIntent().getParcelableExtra("HIKE_OBJECT");
        }
    }

    private void updateBtnParticipantState() {
        if (listeTemporaireParticipants.size() >= 3) {
            btnAjouterParticipant.setEnabled(false);
            btnAjouterParticipant.setAlpha(0.5f);
        } else {
            btnAjouterParticipant.setEnabled(true);
            btnAjouterParticipant.setAlpha(1.0f);
        }
    }

    private void consultationRandonnee() {
        btnChoisirDepart.setVisibility(View.GONE);
        btnChoisirArrivee.setVisibility(View.GONE);
        containerPoi.setVisibility(View.VISIBLE);
        containerParticipants.setVisibility(View.VISIBLE);
        containerFoodProducts.setVisibility(View.VISIBLE);
        containerEquipments.setVisibility(View.VISIBLE);
        Log.i("OPTIMISER", "isOptimize: " + currentHike.getOptimize());

        setChampsEditables(false);

        btnAjouterPOI.setVisibility(View.GONE);
        btnAjouterParticipant.setVisibility(View.GONE);
        btnAddFoodProduct.setVisibility(View.GONE);
        btnAddEquipment.setVisibility(View.GONE);

        listePoints.setEnabled(true);
        listeParticipants.setEnabled(true);
        listeFoodProducts.setEnabled(true);
        listeEquipments.setEnabled(true); // Ajout

        listeParticipants.setOnItemClickListener((parent, view, position, id) -> {
            Participant p = listeTemporaireParticipants.get(position);
            PopUpParticipant.afficherDialogParticipant(this, ModeRandonnee.CONSULTATION, tokenManager.getToken(), currentHike.getId(), p, null);
        });

        listePoints.setOnItemClickListener((parent, view, position, id) -> gererDialogPOI(position, true));

        listeFoodProducts.setOnItemClickListener((parent, view, position, id) -> {
            FoodProduct fp = listeTemporaireFoodProducts.get(position);
            PopUpFoodProduct.afficherPopupDetailsFoodProduct(ActiviteGestionRandonnee.this, fp);
        });

        // Afficher les détails d'un équipement
        listeEquipments.setOnItemClickListener((parent, view, position, id) -> {
            EquipmentItem eq = listeTemporaireEquipments.get(position);
            PopUpEquipment.afficherPopupDetailsEquipment(ActiviteGestionRandonnee.this, eq, listeTemporaireParticipants);
        });

        btnSaveHike.setVisibility(View.GONE);
        if (currentHike.getOptimize()) {
            btnStartCourse.setVisibility(View.VISIBLE);
        }
        btnOptimizeHike.setVisibility(View.VISIBLE);
        btnOptimizeHike.setOnClickListener(v -> lancerOptimisation());
        btnStartCourse.setOnClickListener(v -> lancerDemarrageCourse());
    }

    private void creationRandonnee() {
        btnChoisirDepart.setVisibility(View.VISIBLE);
        btnChoisirArrivee.setVisibility(View.VISIBLE);
        containerPoi.setVisibility(View.GONE);
        containerParticipants.setVisibility(View.GONE);
        containerFoodProducts.setVisibility(View.GONE);
        containerEquipments.setVisibility(View.GONE);
        btnSaveHike.setText("Créer la randonnée");

        btnSaveHike.setOnClickListener(v -> traiterCreation());

        setChampsEditables(true);
    }

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

        updateBtnParticipantState();

        btnAjouterPOI.setOnClickListener(v -> gererDialogPOI(-1, false));
        listePoints.setOnItemClickListener((parent, view, position, id) -> gererDialogPOI(position, false));

        btnAjouterParticipant.setOnClickListener(v -> {
            PopUpParticipant.afficherDialogParticipant(this, ModeRandonnee.CREATION, tokenManager.getToken(), currentHike.getId(), null, new ParticipantCallback() {
                @Override
                public void onActionSuccess(Participant newParticipant) {
                    new AlertDialog.Builder(ActiviteGestionRandonnee.this)
                            .setTitle("Confirmer l'ajout")
                            .setMessage("Le participant va être sauvegardé et vous serez redirigé vers la consultation de la randonnée.")
                            .setPositiveButton("Oui", (dialog, which) -> {
                                JSONObject body = buildParticipantBody(newParticipant);
                                if (body == null) return;

                                AppelAPI.post(
                                        "http://98.94.8.220:8080/hikes/" + currentHike.getId() + "/participants",
                                        tokenManager.getToken(), body, ActiviteGestionRandonnee.this,
                                        new AppelAPI.VolleyObjectCallback() {
                                            @Override
                                            public void onSuccess(JSONObject result) {
                                                Toast.makeText(ActiviteGestionRandonnee.this,
                                                        "Participant " + newParticipant.getPrenom() + " " + newParticipant.getNom() + " ajouté",
                                                        Toast.LENGTH_SHORT).show();
                                                fermerAvecResultat(Activity.RESULT_OK, null);
                                            }
                                            @Override
                                            public void onError(VolleyError error) {
                                                Toast.makeText(ActiviteGestionRandonnee.this,
                                                        "Erreur lors de l'ajout du participant", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                );
                            })
                            .setNegativeButton("Annuler", null)
                            .show();
                }
                @Override
                public void onDeleteAction(Participant participantToDelete) {}
            });
        });

        listeParticipants.setOnItemClickListener((parent, view, position, id) -> {
            Participant pToEdit = listeTemporaireParticipants.get(position);
            if (pToEdit.getIdRando() == 0) pToEdit.setIdRando(currentHike.getId());

            PopUpParticipant.afficherDialogParticipant(this, ModeRandonnee.MODIFICATION, tokenManager.getToken(), currentHike.getId(), pToEdit, new ParticipantCallback() {
                @Override
                public void onActionSuccess(Participant updatedParticipant) {
                    // Mise à jour immédiate en API
                    JSONObject body = buildParticipantBody(updatedParticipant);
                    if (body == null) return;

                    AppelAPI.put(
                            "http://98.94.8.220:8080/hikes/" + currentHike.getId() + "/participants/" + pToEdit.getId(),
                            tokenManager.getToken(), body, ActiviteGestionRandonnee.this,
                            new AppelAPI.VolleyObjectCallback() {
                                @Override
                                public void onSuccess(JSONObject result) {
                                    listeTemporaireParticipants.set(position, updatedParticipant);
                                    adapterParticipants.notifyDataSetChanged();
                                    Toast.makeText(ActiviteGestionRandonnee.this, "Participant mis à jour", Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onError(VolleyError error) {
                                    Toast.makeText(ActiviteGestionRandonnee.this, "Erreur mise à jour participant", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                }

                @Override
                public void onDeleteAction(Participant participantToDelete) {
                    // Chercher les équipements liés
                    List<EquipmentItem> equipementsLies = new ArrayList<>();
                    for (EquipmentItem eq : listeTemporaireEquipments) {
                        if (eq.getOwnerId() != null && eq.getOwnerId() == participantToDelete.getId()) {
                            equipementsLies.add(eq);
                        }
                    }

                    if (!equipementsLies.isEmpty()) {
                        StringBuilder noms = new StringBuilder();
                        for (EquipmentItem eq : equipementsLies) {
                            noms.append("• ").append(eq.getNom()).append("\n");
                        }

                        new AlertDialog.Builder(ActiviteGestionRandonnee.this)
                                .setTitle("Équipements liés")
                                .setMessage("Ce participant possède :\n\n" + noms
                                        + "\nVoulez-vous aussi les retirer de la randonnée ?")
                                .setPositiveButton("Retirer aussi", (dialog, which) -> {
                                    listeTemporaireEquipments.removeAll(equipementsLies);
                                    adapterEquipments.notifyDataSetChanged();
                                    supprimerParticipantAPI(participantToDelete);
                                })
                                .setNegativeButton("Conserver", (dialog, which) -> {
                                    for (EquipmentItem eq : equipementsLies) eq.setOwnerId(null);
                                    adapterEquipments.notifyDataSetChanged();
                                    supprimerParticipantAPI(participantToDelete);
                                })
                                .show();
                    } else {
                        supprimerParticipantAPI(participantToDelete);
                    }
                }
            });
        });

        // --- GESTION FOOD PRODUCTS ---
        listeFoodProducts.setOnItemClickListener((parent, view, position, id) -> {
            FoodProduct fp = listeTemporaireFoodProducts.get(position);
            PopUpFoodProduct.afficherPopupDetailsFoodProduct(ActiviteGestionRandonnee.this, fp);
        });

        btnAddFoodProduct.setOnClickListener(v -> afficherPopupSelectionFoodProduct());

        listeFoodProducts.setOnItemLongClickListener((parent, view, position, id) -> {
            FoodProduct fpASupprimer = listeTemporaireFoodProducts.get(position);
            afficherConfirmationSuppressionFoodProduct(fpASupprimer, position);
            return true;
        });

        // --- GESTION EQUIPEMENTS ---
        listeEquipments.setOnItemClickListener((parent, view, position, id) -> {
            EquipmentItem eq = listeTemporaireEquipments.get(position);
            PopUpEquipment.afficherPopupDetailsEquipment(ActiviteGestionRandonnee.this, eq, listeTemporaireParticipants);
        });

        btnAddEquipment.setOnClickListener(v -> afficherPopupSelectionEquipment());

        listeEquipments.setOnItemLongClickListener((parent, view, position, id) -> {
            EquipmentItem eqASupprimer = listeTemporaireEquipments.get(position);
            afficherConfirmationSuppressionEquipment(eqASupprimer, position);
            return true;
        });

        btnSaveHike.setOnClickListener(v -> traiterMiseAJour());
    }

    private void traiterCreation() {
        String nom = libelle.getText().toString().trim();
        String latDStr = departLat.getText().toString().trim();
        String lonDStr = departLon.getText().toString().trim();
        String latAStr = arriveeLat.getText().toString().trim();
        String lonAStr = arriveeLon.getText().toString().trim();

        int duree = 1;
        if (nbJours.getSelectedItem() != null) {
            try {
                duree = Integer.parseInt(nbJours.getSelectedItem().toString());
            } catch (NumberFormatException e) {
                // empty body
            }
        }

        String erreurValidation = ValidateurRandonnee.verifierDonnees(nom, latDStr, lonDStr, latAStr, lonAStr, duree);

        if (erreurValidation != null) {
            Toast.makeText(this, erreurValidation, Toast.LENGTH_LONG).show();
            return;
        }

        double latD = Double.parseDouble(latDStr.replace(",", "."));
        double lonD = Double.parseDouble(lonDStr.replace(",", "."));
        double latA = Double.parseDouble(latAStr.replace(",", "."));
        double lonA = Double.parseDouble(lonAStr.replace(",", "."));

        String nomDepart = "Départ : " + nom;
        String descDepart = "Point de départ de la randonnée";
        String nomArrivee = "Arrivée : " + nom;
        String descArrivee = "Point d'arrivée de la randonnée";

        ServiceCreationRandonnee.creerRandonnee(
                this,
                tokenManager.getToken(),
                nom,
                duree,
                nomDepart, descDepart, latD, lonD,
                nomArrivee, descArrivee, latA, lonA,
                new ServiceCreationRandonnee.FullCreationCallback() {
                    @Override
                    public void onSuccess(long hikeId) {
                        Toast.makeText(ActiviteGestionRandonnee.this, "Randonnée créée avec succès !", Toast.LENGTH_SHORT).show();
                        fermerAvecResultat(Activity.RESULT_OK, null);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(ActiviteGestionRandonnee.this, "Erreur lors de la création : Nom déjà utilisé ", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void traiterMiseAJour() {
        if (currentHike == null || currentHike.getId() == 0) {
            Toast.makeText(this, "Erreur ID Randonnée", Toast.LENGTH_SHORT).show();
            return;
        }

        String libelleTexte = libelle.getText().toString();
        int dureeJours = Integer.parseInt(nbJours.getSelectedItem().toString());
        double latitudeD = Double.parseDouble(departLat.getText().toString());
        double longitudeD = Double.parseDouble(departLon.getText().toString());
        double latitudeA = Double.parseDouble(arriveeLat.getText().toString());
        double longitudeA = Double.parseDouble(arriveeLon.getText().toString());

        ServiceModificationRandonnee.modifierRandonneeAPI(this, tokenManager.getToken(), currentHike.getId(), libelleTexte, dureeJours,
                latitudeD, longitudeD, latitudeA, longitudeA, new ServiceModificationRandonnee.UpdateHikeCallback() {
            @Override
            public void onSuccess() {
                traiterMiseAJourParticipants(currentHike.getId());
                traiterMiseAJourPOI(currentHike.getId());

                ServiceFoodProduct.synchroniserFoodProducts(
                        ActiviteGestionRandonnee.this,
                        tokenManager.getToken(),
                        currentHike.getId(),
                        currentHike.getFoodCatalogue(),
                        listeTemporaireFoodProducts
                );

                // --- Appel à ta future méthode de synchronisation des équipements ---
                // Tu devras la créer dans ServiceEquipment sur le même modèle que ServiceFoodProduct

                ServiceEquipment.synchroniserEquipments(
                        ActiviteGestionRandonnee.this,
                        tokenManager.getToken(),
                        currentHike.getId(),
                        currentHike.getEquipmentGroups(),
                        listeTemporaireEquipments
                );


                Toast.makeText(ActiviteGestionRandonnee.this, "Modifications enregistrées", Toast.LENGTH_SHORT).show();
                fermerAvecResultat(Activity.RESULT_OK, null);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ActiviteGestionRandonnee.this, "Erreur MAJ Rando", Toast.LENGTH_SHORT).show();
                Log.e("erreur rando", message);
            }
        });
    }

    private void traiterMiseAJourParticipants(int hikeId) {
        ServiceParticipant.traiterMAJParticipants(this, hikeId, listeTemporaireParticipants, participantsOriginaux, tokenManager);
    }

    private void traiterMiseAJourPOI(int hikeId) {
        ServicePOI.traiterMAJPOI(this, hikeId, listeTemporairePOI, tokenManager.getToken());
    }

    private void remplissageChamps(Hike hike) {
        libelle.setText(hike.getLibelle());
        if(hike.getDepart() != null) {
            departLat.setText(String.valueOf(hike.getDepart().getLatitude()));
            departLon.setText(String.valueOf(hike.getDepart().getLongitude()));
        }
        if(hike.getArrivee() != null) {
            arriveeLat.setText(String.valueOf(hike.getArrivee().getLatitude()));
            arriveeLon.setText(String.valueOf(hike.getArrivee().getLongitude()));
        }
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
        if (hike.getDureeJours() > 0 && hike.getDureeJours() <= 10) {
            nbJours.setSelection(hike.getDureeJours() - 1);
        }

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

    private void setChampsEditables(boolean editable) {
        libelle.setEnabled(editable);
        departLat.setEnabled(editable);
        departLon.setEnabled(editable);
        arriveeLat.setEnabled(editable);
        arriveeLon.setEnabled(editable);
        nbJours.setEnabled(editable);
    }

    private void gererDialogPOI(int position, boolean isReadOnly) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ajout_poi, null);
        boolean isCreation = (position == -1);
        PointOfInterest poi = (!isCreation) ? listeTemporairePOI.get(position) : null;

        final EditText inputNom = view.findViewById(R.id.edit_poi_nom);
        final EditText inputLat = view.findViewById(R.id.edit_poi_lat);
        final EditText inputLon = view.findViewById(R.id.edit_poi_lon);
        final MapView mapPoi = view.findViewById(R.id.map_poi);

        // Configuration OSMDroid
        Configuration.getInstance().setUserAgentValue(getPackageName());
        mapPoi.setTileSource(TileSourceFactory.MAPNIK);
        mapPoi.setMultiTouchControls(true);

        double latInit = (poi != null) ? poi.getLatitude() : 46.2276;
        double lonInit = (poi != null) ? poi.getLongitude() : 2.2137;
        double zoomInit = (poi != null) ? 15.0 : 6.0;

        GeoPoint startPoint = new GeoPoint(latInit, lonInit);
        mapPoi.getController().setZoom(zoomInit);
        mapPoi.getController().setCenter(startPoint);

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
            mapPoi.getOverlays().add(0, eventsOverlay);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isCreation ? "Nouveau POI" : (isReadOnly ? "Détails POI" : "Modifier POI"));

        if (isReadOnly) {
            inputNom.setEnabled(false); inputLat.setEnabled(false); inputLon.setEnabled(false);
            builder.setPositiveButton("Fermer", (d, w) -> mapPoi.onDetach());
        } else {
            builder.setPositiveButton("Valider", null);
            builder.setNegativeButton("Annuler", (d, w) -> mapPoi.onDetach());
            if (!isCreation) {
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
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String nom = inputNom.getText().toString();
                String latStr = inputLat.getText().toString();
                String lonStr = inputLon.getText().toString();

                PoiValidator.ValidationResult result = PoiValidator.valider(nom, latStr, lonStr);
                if (result.isValid()) {
                    if (poi != null) {
                        poi.setNom(nom);
                        poi.setLatitude(result.getLatitude());
                        poi.setLongitude(result.getLongitude());
                    } else {
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

    private void lancerOptimisation() {
        if (currentHike == null || currentHike.getId() == 0) {
            Toast.makeText(this, "Impossible d'optimiser une randonnée non enregistrée.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Optimisation en cours...", Toast.LENGTH_SHORT).show();

        // Appel au service d'optimisation
        ServiceOptimisation.optimiserRandonnee(
                this,
                tokenManager.getToken(),
                currentHike.getId(),
                new ServiceOptimisation.OptimisationCallback() {

                    @Override
                    public void onSuccess(JSONObject result) {
                        Toast.makeText(ActiviteGestionRandonnee.this, "Randonnée optimisée avec succès !", Toast.LENGTH_SHORT).show();

                        // 1. On signale au Fragment parent que l'action a réussi
                        setResult(Activity.RESULT_OK);

                        // 2. On ferme l'activité pour retourner automatiquement sur le Fragment
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        // En cas d'erreur, on affiche simplement le message sans fermer l'activité
                        Toast.makeText(ActiviteGestionRandonnee.this, "Erreur d'optimisation : " + message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    // --- METHODES POUR LES PRODUITS ALIMENTAIRES ---

    private void afficherPopupSelectionFoodProduct() {
        ServiceFoodProduct.getAllFoodProducts(this, tokenManager.getToken(), new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                try {
                    List<FoodProduct> catalogueComplet = new ArrayList<>();
                    List<String> nomsProduits = new ArrayList<>();

                    for (int i = 0; i < result.length(); i++) {
                        FoodProduct fp = ServiceFoodProduct.constructFPFromJson(result.getJSONObject(i));
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
                    Toast.makeText(ActiviteGestionRandonnee.this, "Erreur de lecture des produits", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(ActiviteGestionRandonnee.this, "Impossible de charger le catalogue", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ajouterProduitLocal(FoodProduct produit) {
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

    private void afficherConfirmationSuppressionFoodProduct(FoodProduct produit, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Retirer le produit")
                .setMessage("Voulez-vous retirer " + produit.getNom() + " de la randonnée ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    listeTemporaireFoodProducts.remove(position);
                    adapterFoodProducts.notifyDataSetChanged();
                    Toast.makeText(ActiviteGestionRandonnee.this, "Produit retiré de la liste", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Non", null)
                .show();
    }


    // --- METHODES POUR LES EQUIPEMENTS ---

    private void afficherPopupSelectionEquipment() {
        ServiceEquipment.getAllEquipments(this, tokenManager.getToken(), new AppelAPI.VolleyCallback() {
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
                        // Attention: optDouble renvoie NaN si la clé existe mais est null, il vaut mieux faire ceci:
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
                    Toast.makeText(ActiviteGestionRandonnee.this, "Erreur de lecture des équipements", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(ActiviteGestionRandonnee.this, "Impossible de charger le catalogue", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Gère la logique d'ajout : vérifie le type et demande le propriétaire si nécessaire.
     */
    private void gererAjoutEquipment(EquipmentItem eq) {
        // 1. Vérifier s'il est déjà dans la liste
        for (EquipmentItem item : listeTemporaireEquipments) {
            if (item.getId() == eq.getId()) {
                Toast.makeText(this, "Cet équipement est déjà dans la randonnée", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 2. Si c'est un VETEMENT ou REPOS, demander le propriétaire
        if (eq.getType() == TypeEquipment.VETEMENT || eq.getType() == TypeEquipment.REPOS) {
            demanderProprietaireEtAjouter(eq);
        } else {
            // Sinon, l'ajouter directement
            ajouterEquipmentFinal(eq, null);
        }
    }

    /**
     * Affiche une popup pour sélectionner le participant propriétaire.
     */
    private void demanderProprietaireEtAjouter(EquipmentItem eq) {
        if (listeTemporaireParticipants.isEmpty()) {
            Toast.makeText(this, "Veuillez d'abord ajouter des participants à la randonnée.", Toast.LENGTH_LONG).show();
            return;
        }

        List<String> nomParticipants = new ArrayList<>();
        for (Participant p : listeTemporaireParticipants) {
            nomParticipants.add(p.getPrenom() + " " + p.getNom());
        }

        String[] tableauParticipants = nomParticipants.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("À qui appartient cet équipement ?")
                .setItems(tableauParticipants, (dialog, which) -> {
                    // On récupère l'ID du participant sélectionné
                    int idProprietaire = listeTemporaireParticipants.get(which).getId();
                    ajouterEquipmentFinal(eq, idProprietaire);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    /**
     * Ajoute l'équipement à la liste visuelle en sauvegardant l'ID du propriétaire.
     */
    private void ajouterEquipmentFinal(EquipmentItem eq, Integer ownerId) {
        // Si ce n'est pas le cas, ajoute un attribut "private Integer ownerId;" avec ses getters/setters
        // dans ta classe fr.iutrodez.a4awalk.modeles.entites.EquipmentItem

        eq.setOwnerId(ownerId);

        listeTemporaireEquipments.add(eq);
        adapterEquipments.notifyDataSetChanged();
        Toast.makeText(this, eq.getNom() + " ajouté à la liste !", Toast.LENGTH_SHORT).show();
    }

    private void ajouterEquipmentLocal(EquipmentItem eq) {
        for (EquipmentItem item : listeTemporaireEquipments) {
            if (item.getId() == eq.getId()) {
                Toast.makeText(this, "Cet équipement est déjà dans la randonnée", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        listeTemporaireEquipments.add(eq);
        adapterEquipments.notifyDataSetChanged();
        Toast.makeText(this, eq.getNom() + " ajouté à la liste !", Toast.LENGTH_SHORT).show();
    }

    private void afficherConfirmationSuppressionEquipment(EquipmentItem eq, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Retirer l'équipement")
                .setMessage("Voulez-vous retirer " + eq.getNom() + " de la randonnée ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    listeTemporaireEquipments.remove(position);
                    adapterEquipments.notifyDataSetChanged();
                    Toast.makeText(ActiviteGestionRandonnee.this, "Équipement retiré de la liste", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void lancerDemarrageCourse() {
        // Vérification des permissions GPS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        btnStartCourse.setEnabled(false);
        Toast.makeText(this, "Recherche de la position GPS...", Toast.LENGTH_SHORT).show();

        // Récupération de la dernière position connue
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                appelerAPIStartCourse(location.getLatitude(), location.getLongitude());
            } else {
                btnStartCourse.setEnabled(true);
                Toast.makeText(this, "Position introuvable. Veuillez vérifier que votre GPS est activé.", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            btnStartCourse.setEnabled(true);
            Toast.makeText(this, "Erreur lors de la récupération de la position.", Toast.LENGTH_SHORT).show();
        });
    }
    private void appelerAPIStartCourse(double lat, double lon) {
        if (currentHike == null || currentHike.getId() == 0) {
            btnStartCourse.setEnabled(true);
            Toast.makeText(this, "Erreur : ID Randonnée invalide.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Création de la course en cours...", Toast.LENGTH_SHORT).show();

        ServiceParcours.demarrerCourse(this, tokenManager.getToken(), currentHike.getId(), lat, lon, new ServiceParcours.CourseCreationCallback() {

            // Le callback reçoit maintenant l'objet Course hydraté par l'API
            @Override
            public void onSuccess(Course courseCreee) {
                btnStartCourse.setEnabled(true);
                Toast.makeText(ActiviteGestionRandonnee.this, "Course démarrée avec succès !", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ActiviteGestionRandonnee.this, SuiviParcours.class);

                intent.putExtra("COURSE_ID", courseCreee.getId());
                intent.putExtra("NOM_RANDONNEE", currentHike.getLibelle());
                intent.putExtra("NOM_PARCOURS", "Parcours - " + currentHike.getLibelle());

                // Formatage de la date renvoyée par l'API
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                if (courseCreee.getDateRealisation() != null) {
                    intent.putExtra("DATE_REALISATION", courseCreee.getDateRealisation().format(formatter));
                }

                List<GeoCoordinate> trajetsAPI = courseCreee.getTrajetsRealises();

                double[] latsArray = new double[trajetsAPI.size()];
                double[] lonsArray = new double[trajetsAPI.size()];

                for (int i = 0; i < trajetsAPI.size(); i++) {
                    latsArray[i] = trajetsAPI.get(i).getLatitude();
                    lonsArray[i] = trajetsAPI.get(i).getLongitude();
                }

                // Envoi des tableaux de coordonnées à l'Intent
                intent.putExtra("LATITUDES", latsArray);
                intent.putExtra("LONGITUDES", lonsArray);

                startActivity(intent);
            }

            @Override
            public void onError(VolleyError error) {
                btnStartCourse.setEnabled(true);
                Toast.makeText(ActiviteGestionRandonnee.this, "Erreur lors du démarrage : " + error.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Gestion du retour de la demande de permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée, on retente le démarrage
                lancerDemarrageCourse();
            } else {
                Toast.makeText(this, "Permission GPS requise pour démarrer la course.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fermerAvecResultat(int resultCode, String message) {
        Intent intent = new Intent();
        if (message != null) intent.putExtra("CHILD_MESSAGE", message);
        setResult(resultCode, intent);
        finish();
    }

    private void supprimerParticipantAPI(Participant participant) {
        ServiceParticipant.supprimerParticipantAPI(
                this, tokenManager.getToken(),
                currentHike.getId(), participant.getId(),
                new AppelAPI.VolleyObjectCallback() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        listeTemporaireParticipants.remove(participant);
                        adapterParticipants.notifyDataSetChanged();
                        updateBtnParticipantState();
                        Toast.makeText(ActiviteGestionRandonnee.this, "Participant supprimé", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(VolleyError error) {
                        Toast.makeText(ActiviteGestionRandonnee.this, "Erreur suppression participant", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private JSONObject buildParticipantBody(Participant p) {
        try {
            JSONObject body = new JSONObject();
            body.put("nom", p.getNom());
            body.put("prenom", p.getPrenom());
            body.put("age", p.getAge());
            body.put("niveau", p.getNiveau().toString());
            body.put("morphologie", p.getMorphologie().toString());
            body.put("besoinKcal", p.getBesoinKcal());
            body.put("besoinEauLitre", p.getBesoinEauLitre());
            body.put("capaciteEmportMaxKg", p.getCapaciteEmportMaxKg());
            return body;
        } catch (JSONException e) {
            return null;
        }
    }
}