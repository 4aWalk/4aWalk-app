package fr.iutrodez.a4awalk.activites;

import fr.iutrodez.a4awalk.activites.GestionParticipant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.fragments.FragmentListeRandonnees;
import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.entites.PointOfInterest;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceCreationRandonnee;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceParticipant;

public class ActiviteGestionRandonnee extends AppCompatActivity {

    private final String ERREUR = "ERREUR";

    // UI
    private EditText libelle, departLat, departLon, arriveeLat, arriveeLon;
    private ListView listePoints, listeParticipants;
    private Spinner nbJours;
    private ImageButton btnAjouterPOI, btnAjouterParticipant;
    private Button validateButton;

    // Données locales pour l'affichage
    private ArrayList<PointOfInterest> listeTemporairePOI = new ArrayList<>();
    private ArrayAdapter<PointOfInterest> adapterPOI;
    private ArrayList<Participant> listeTemporaireParticipants = new ArrayList<>();
    private ArrayAdapter<Participant> adapterParticipants;

    // Métier
    private TokenManager tokenManager;
    private ServiceCreationRandonnee creationService;
    private User currentUser;
    private List<Participant> participants;
    private Hike hike;

    // id de la randonnée créée pour l'ajout des points d'intérêts et des participants
    private Long idRandonnee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_details_randonnee);

        tokenManager = new TokenManager(this);
        //TODO vérifier validité token

        creationService = new ServiceCreationRandonnee(this);

        recupererDonneesIntent();
        initElementsGraphiques();

        int pageID = getIntent().getIntExtra("ID_PAGE", 0);
        if (pageID == 1) {
            consultationRandonnee();
        } else if (pageID == 2) {
            creationRandonnee();
        } else {
            fermerAvecResultat(Activity.RESULT_CANCELED, ERREUR);
        }
    }

    private void recupererDonneesIntent() {
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
        listePoints = findViewById(R.id.points_list);
        listeParticipants = findViewById(R.id.participants_list);
        nbJours = findViewById(R.id.spinner_jours);
        btnAjouterPOI = findViewById(R.id.btn_add_poi);
        btnAjouterParticipant = findViewById(R.id.btn_add_participant);
        validateButton = findViewById(R.id.validate_button);
    }

    private void creationRandonnee() {
        activeModeCreation();
        ajouterUtilisateurCourant();

        btnAjouterPOI.setOnClickListener(v -> gererDialogPOI(-1));
        btnAjouterParticipant.setOnClickListener(v -> GestionParticipant.gererDialogParticipant(this, tokenManager.getToken()));

        // Ajout des écouteurs sur les listes pour modification/suppression
        listePoints.setOnItemClickListener((parent, view, position, id) -> {
            gererDialogPOI(position);
        });

        listeParticipants.setOnItemClickListener((parent, view, position, id) -> {
            GestionParticipant.gererDialogParticipant(this, tokenManager.getToken());
        });

        validateButton.setOnClickListener(v -> traiterClicValidation());
    }

    private void traiterClicValidation() {
        validateButton.setEnabled(false);

        String nom = libelle.getText().toString();
        int duree = Integer.parseInt(nbJours.getSelectedItem().toString());

        creationService.creerRandonnee(
                nom,
                duree,
                departLat.getText().toString(),
                departLon.getText().toString(),
                arriveeLat.getText().toString(),
                arriveeLon.getText().toString(),
                listeTemporairePOI,
                listeTemporaireParticipants,
                new ServiceCreationRandonnee.CreationCallback() {
                    @Override
                    public void onSuccess(long hikeId) {
                        Toast.makeText(ActiviteGestionRandonnee.this,
                                "Randonnée n°" + hikeId + " créée avec succès !",
                                Toast.LENGTH_SHORT).show();

                        // On stocke l'ID récupéré
                        idRandonnee = hikeId;

                        // Vous pouvez maintenant naviguer ou mettre à jour l'UI
                        validateButton.setEnabled(true);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(ActiviteGestionRandonnee.this, message, Toast.LENGTH_LONG).show();
                        validateButton.setEnabled(true);
                    }
                }
        );
        for (Participant participant : participants) {
            ServiceParticipant.ajoutParticipantAPI(this, tokenManager.getToken(), participant, idRandonnee);
        }

        fermerAvecResultat(Activity.RESULT_OK, null);
    }

    private void ajouterUtilisateurCourant() {
        if (currentUser != null) {
            // Création d'un participant par défaut pour l'utilisateur courant
            Participant moi = new Participant();
            listeTemporaireParticipants.add(moi);
            if (adapterParticipants != null) adapterParticipants.notifyDataSetChanged();
        }
    }

    private void fermerAvecResultat(int resultCode, String messageErreur) {
        Intent intent = new Intent();
        if (messageErreur != null) {
            intent.putExtra(FragmentListeRandonnees.CHILD_MESSAGE_KEY, messageErreur);
        }
        setResult(resultCode, intent);
        finish();
    }

    private void activeModeCreation() {
        libelle.setClickable(true); libelle.setFocusableInTouchMode(true);
        departLat.setClickable(true); departLat.setFocusableInTouchMode(true);
        departLon.setClickable(true); departLon.setFocusableInTouchMode(true);
        arriveeLat.setClickable(true); arriveeLat.setFocusableInTouchMode(true);
        arriveeLon.setClickable(true); arriveeLon.setFocusableInTouchMode(true);

        validateButton.setVisibility(View.VISIBLE);
        btnAjouterPOI.setVisibility(View.VISIBLE);
        btnAjouterParticipant.setVisibility(View.VISIBLE);

        List<Integer> jours = new ArrayList<>();
        for (int i = 1; i <= 3; i++) jours.add(i);
        ArrayAdapter<Integer> adapterJours = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, jours);
        adapterJours.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nbJours.setAdapter(adapterJours);

        adapterPOI = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listeTemporairePOI);
        listePoints.setAdapter(adapterPOI);

        adapterParticipants = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listeTemporaireParticipants);
        listeParticipants.setAdapter(adapterParticipants);
    }

    // --- GESTION DES DIALOGUES (Création, Modification, Suppression) ---

    /**
     * Gère l'affichage du dialogue pour les POI.
     * @param position L'index dans la liste. Si -1, c'est un nouvel ajout.
     */
    private void gererDialogPOI(int position) {
        boolean isModification = (position >= 0);
        PointOfInterest poiAModifier = isModification ? listeTemporairePOI.get(position) : null;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isModification ? "Détails du POI" : "Nouveau POI");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ajout_poi, null);
        final EditText inputNom = view.findViewById(R.id.edit_poi_nom);
        final EditText inputLat = view.findViewById(R.id.edit_poi_lat);
        final EditText inputLon = view.findViewById(R.id.edit_poi_lon);

        // Pré-remplissage si modification
        if (isModification && poiAModifier != null) {
            inputNom.setText(poiAModifier.getName());
            inputLat.setText(String.valueOf(poiAModifier.getLatitude()));
            inputLon.setText(String.valueOf(poiAModifier.getLongitude()));
        }

        builder.setView(view);

        // Bouton Positif : Ajouter ou Modifier
        builder.setPositiveButton(isModification ? "Modifier" : "Ajouter", (dialog, which) -> {
            try {
                String nom = inputNom.getText().toString();
                double lat = Double.parseDouble(inputLat.getText().toString());
                double lon = Double.parseDouble(inputLon.getText().toString());

                if (isModification) {
                    poiAModifier.setName(nom);
                    poiAModifier.setLatitude(lat);
                    poiAModifier.setLongitude(lon);
                } else {
                    listeTemporairePOI.add(new PointOfInterest(0L, nom, lat, lon));
                }
                adapterPOI.notifyDataSetChanged();
            } catch (Exception e) {
                Toast.makeText(this, "Erreur saisie", Toast.LENGTH_SHORT).show();
            }
        });

        // Bouton Neutre : Supprimer (seulement si on modifie un existant)
        if (isModification) {
            builder.setNeutralButton("Supprimer", (dialog, which) -> {
                listeTemporairePOI.remove(position);
                adapterPOI.notifyDataSetChanged();
            });
        }

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void consultationRandonnee() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            hike = getIntent().getParcelableExtra("HIKE_OBJECT", Hike.class);
        } else {
            hike = getIntent().getParcelableExtra("HIKE_OBJECT");
        }
        if (hike != null) {
            libelle.setText(hike.getLibelle());
            libelle.setEnabled(false);
            if(hike.getOptionalPoints() != null) {
                adapterPOI = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, hike.getOptionalPoints());
                listePoints.setAdapter(adapterPOI);
            }
        }
    }
}