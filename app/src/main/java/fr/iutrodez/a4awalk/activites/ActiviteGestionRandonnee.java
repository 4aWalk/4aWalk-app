package fr.iutrodez.a4awalk.activites;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.ParticipantCallback;
import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.entites.PointOfInterest;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.modeles.enums.ModeRandonnee;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServicePOI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceParticipant;
import fr.iutrodez.a4awalk.services.gestionAPI.randonnee.ServiceCreationRandonnee;
import fr.iutrodez.a4awalk.services.gestionAPI.randonnee.ServiceModificationRandonnee;
import fr.iutrodez.a4awalk.services.gestionAPI.randonnee.ServiceRandonnee;
import fr.iutrodez.a4awalk.utils.validators.PoiValidator;
import fr.iutrodez.a4awalk.utils.validators.ValidateurRandonnee;

public class ActiviteGestionRandonnee extends HeaderActivity {

    private static final int MODE_CONSULTATION = 1;
    private static final int MODE_CREATION = 2;
    private static final int MODE_MODIFICATION = 3;
    private final String ERREUR = "ERREUR";

    private EditText libelle, departLat, departLon, arriveeLat, arriveeLon;
    private ListView listePoints, listeParticipants;
    private Spinner nbJours;
    private ImageButton btnAjouterPOI, btnAjouterParticipant;
    private Button validateButton, btnSupprimer;
    private LinearLayout containerPoi, containerParticipants;

    private ArrayList<PointOfInterest> listeTemporairePOI = new ArrayList<>();
    private ArrayList<PointOfInterest> poiOriginaux = new ArrayList<>();
    private ArrayAdapter<PointOfInterest> adapterPOI;

    private ArrayList<Participant> listeTemporaireParticipants = new ArrayList<>();
    private ArrayList<Participant> participantsOriginaux = new ArrayList<>();
    private ArrayAdapter<Participant> adapterParticipants;

    private TokenManager tokenManager;
    private User currentUser;
    private Hike currentHike;

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

        listePoints = findViewById(R.id.points_list);
        listeParticipants = findViewById(R.id.participants_list);
        nbJours = findViewById(R.id.spinner_jours);

        containerPoi = findViewById(R.id.container_poi);
        containerParticipants = findViewById(R.id.container_participants);

        btnAjouterPOI = findViewById(R.id.btn_add_poi);
        btnAjouterParticipant = findViewById(R.id.btn_add_participant);
        validateButton = findViewById(R.id.validate_button);
        btnSupprimer = findViewById(R.id.btn_delete_hike);

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
        containerPoi.setVisibility(View.VISIBLE);
        containerParticipants.setVisibility(View.VISIBLE);
        setChampsEditables(false);
        btnAjouterPOI.setVisibility(View.GONE);
        btnAjouterParticipant.setVisibility(View.GONE);
        listePoints.setEnabled(true);
        listeParticipants.setEnabled(true);

        listeParticipants.setOnItemClickListener((parent, view, position, id) -> {
            Participant p = listeTemporaireParticipants.get(position);
            GestionParticipant.afficherDialogParticipant(this, ModeRandonnee.CONSULTATION, tokenManager.getToken(), currentHike.getId(), p, null);
        });

        listePoints.setOnItemClickListener((parent, view, position, id) -> gererDialogPOI(position, true));

        validateButton.setText(R.string.bouton_modifier_randonnee);
        validateButton.setOnClickListener(v -> {
            participantsOriginaux.clear();
            participantsOriginaux.addAll(listeTemporaireParticipants);
            poiOriginaux.clear();
            poiOriginaux.addAll(listeTemporairePOI);
            modificationRandonnee();
        });
        btnSupprimer.setVisibility(View.VISIBLE);
        btnSupprimer.setOnClickListener(v -> afficherConfirmationSuppression());
    }

    private void creationRandonnee() {
        containerPoi.setVisibility(View.GONE);
        containerParticipants.setVisibility(View.GONE);
        setChampsEditables(true);
        validateButton.setText(R.string.bouton_ajouter_randonnee);
        validateButton.setOnClickListener(v -> traiterCreation());
    }

    private void modificationRandonnee() {
        containerPoi.setVisibility(View.VISIBLE);
        containerParticipants.setVisibility(View.VISIBLE);
        setChampsEditables(true);
        btnAjouterPOI.setVisibility(View.VISIBLE);
        btnAjouterParticipant.setVisibility(View.VISIBLE);
        listePoints.setEnabled(true);
        listeParticipants.setEnabled(true);

        updateBtnParticipantState();

        btnAjouterPOI.setOnClickListener(v -> gererDialogPOI(-1, false));

        listePoints.setOnItemClickListener((parent, view, position, id) -> gererDialogPOI(position, false));

        listePoints.setOnItemLongClickListener((parent, view, position, id) -> {
            listeTemporairePOI.remove(position);
            adapterPOI.notifyDataSetChanged();
            Toast.makeText(this, "POI retiré", Toast.LENGTH_SHORT).show();
            return true;
        });

        btnAjouterParticipant.setOnClickListener(v -> {
            GestionParticipant.afficherDialogParticipant(this, ModeRandonnee.CREATION, tokenManager.getToken(), currentHike.getId(), null, new ParticipantCallback() {
                @Override
                public void onActionSuccess(Participant newParticipant) {
                    newParticipant.setId(0);
                    newParticipant.setIdRando(currentHike.getId());
                    listeTemporaireParticipants.add(newParticipant);
                    adapterParticipants.notifyDataSetChanged();
                    updateBtnParticipantState();
                }
                public void onDeleteAction(Participant participantToDelete) {}
            });
        });

        listeParticipants.setOnItemClickListener((parent, view, position, id) -> {
            Participant pToEdit = listeTemporaireParticipants.get(position);
            if (pToEdit.getIdRando() == 0) pToEdit.setIdRando(currentHike.getId());

            GestionParticipant.afficherDialogParticipant(this, ModeRandonnee.MODIFICATION, tokenManager.getToken(), currentHike.getId(), pToEdit, new ParticipantCallback() {
                @Override
                public void onActionSuccess(Participant updatedParticipant) {
                    if (pToEdit.getId() != 0) updatedParticipant.setId(pToEdit.getId());
                    updatedParticipant.setIdRando(currentHike.getId());
                    listeTemporaireParticipants.set(position, updatedParticipant);
                    adapterParticipants.notifyDataSetChanged();
                }
                @Override
                public void onDeleteAction(Participant participantToDelete) {
                    listeTemporaireParticipants.remove(participantToDelete);
                    adapterParticipants.notifyDataSetChanged();
                    Toast.makeText(ActiviteGestionRandonnee.this, "Participant retiré (validez pour enregistrer)", Toast.LENGTH_SHORT).show();
                    updateBtnParticipantState();
                }
            });
        });

        listeParticipants.setOnItemLongClickListener((parent, view, position, id) -> {
            listeTemporaireParticipants.remove(position);
            adapterParticipants.notifyDataSetChanged();
            Toast.makeText(this, "Participant retiré (validez pour enregistrer)", Toast.LENGTH_SHORT).show();
            updateBtnParticipantState();
            return true;
        });

        validateButton.setText("Enregistrer les modifications");
        validateButton.setOnClickListener(v -> traiterMiseAJour());
    }

    private void traiterCreation() {
        // 1. Récupération des données brutes de l'interface
        String nom = libelle.getText().toString().trim();
        String latDStr = departLat.getText().toString().trim();
        String lonDStr = departLon.getText().toString().trim();
        String latAStr = arriveeLat.getText().toString().trim();
        String lonAStr = arriveeLon.getText().toString().trim();

        int duree = 1; // Valeur par défaut
        if (nbJours.getSelectedItem() != null) {
            try {
                duree = Integer.parseInt(nbJours.getSelectedItem().toString());
            } catch (NumberFormatException e) {
                // empty body
            }
        }

        // 2. Appel de TON validateur
        String erreurValidation = ValidateurRandonnee.verifierDonnees(nom, latDStr, lonDStr, latAStr, lonAStr, duree);

        // 3. Gestion des erreurs de validation
        if (erreurValidation != null) {
            // S'il y a une erreur (c'est-à-dire si le retour n'est pas null), on l'affiche et on stoppe le processus
            Toast.makeText(this, erreurValidation, Toast.LENGTH_LONG).show();
            return;
        }

        // 4. Conversion sécurisée des coordonnées
        // À ce stade, on sait que la conversion ne plantera pas car ton validateur a déjà fait le travail de vérification
        double latD = Double.parseDouble(latDStr.replace(",", "."));
        double lonD = Double.parseDouble(lonDStr.replace(",", "."));
        double latA = Double.parseDouble(latAStr.replace(",", "."));
        double lonA = Double.parseDouble(lonAStr.replace(",", "."));

        // 5. Génération des valeurs par défaut pour les champs requis par l'API
        String nomDepart = "Départ : " + nom;
        String descDepart = "Point de départ de la randonnée";
        String nomArrivee = "Arrivée : " + nom;
        String descArrivee = "Point d'arrivée de la randonnée";

        // Appel de l'API avec les données formatées
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
                        Toast.makeText(ActiviteGestionRandonnee.this, "Erreur lors de la création : " + message, Toast.LENGTH_LONG).show();
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

        // Utilisation de la nouvelle classe ServiceModificationRandonnee
        ServiceModificationRandonnee.modifierRandonneeAPI(this, tokenManager.getToken(), currentHike.getId(), libelleTexte, dureeJours, new ServiceModificationRandonnee.UpdateHikeCallback() {
            @Override
            public void onSuccess() {
                traiterMiseAJourParticipants(currentHike.getId());
                traiterMiseAJourPOI(currentHike.getId());
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
        if (hike.getDureeJours() > 0 && hike.getDureeJours() <= 10) {
            nbJours.setSelection(hike.getDureeJours() - 1);
        }
        listeTemporairePOI.clear();
        if (hike.getOptionalPoints() != null) listeTemporairePOI.addAll(hike.getOptionalPoints());
        adapterPOI.notifyDataSetChanged();
        Log.i("idRando", hike.getId() + "");
        Log.i("POIS", hike.getOptionalPoints().toString());

        listeTemporaireParticipants.clear();
        if (hike.getParticipants() != null) listeTemporaireParticipants.addAll(hike.getParticipants());
        adapterParticipants.notifyDataSetChanged();
        Log.i("Participants", hike.getParticipants().toString());

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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isCreation ? "Nouveau POI" : (isReadOnly ? "Détails POI" : "Modifier POI"));

        final EditText inputNom = view.findViewById(R.id.edit_poi_nom);
        final EditText inputLat = view.findViewById(R.id.edit_poi_lat);
        final EditText inputLon = view.findViewById(R.id.edit_poi_lon);

        if (poi != null) {
            inputNom.setText(poi.getNom());
            inputLat.setText(String.valueOf(poi.getLatitude()));
            inputLon.setText(String.valueOf(poi.getLongitude()));
        }

        if (isReadOnly) {
            inputNom.setEnabled(false); inputLat.setEnabled(false); inputLon.setEnabled(false);
            builder.setPositiveButton("Fermer", null);
        } else {
            // On définit le bouton Valider à null ici pour éviter la fermeture automatique
            builder.setPositiveButton("Valider", null);
            builder.setNegativeButton("Annuler", null);

            if (!isCreation) {
                builder.setNeutralButton("Supprimer", (dialog, which) -> {
                    listeTemporairePOI.remove(position);
                    adapterPOI.notifyDataSetChanged();
                });
            }
        }

        builder.setView(view);

        // On crée et on affiche l'AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // On surcharge le comportement du bouton "Valider" APRÈS l'affichage (seulement si on n'est pas en lecture seule)
        if (!isReadOnly) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String nom = inputNom.getText().toString();
                String latStr = inputLat.getText().toString();
                String lonStr = inputLon.getText().toString();

                // Appel de notre nouvelle classe de validation
                PoiValidator.ValidationResult result = PoiValidator.valider(nom, latStr, lonStr);

                if (result.isValid()) {
                    // Succès : on met à jour ou on ajoute le POI
                    if (poi != null) {
                        poi.setNom(nom);
                        poi.setLatitude(result.getLatitude());
                        poi.setLongitude(result.getLongitude());
                    } else {
                        listeTemporairePOI.add(new PointOfInterest(0, nom, result.getLatitude(), result.getLongitude(),null,0));
                    }
                    adapterPOI.notifyDataSetChanged();

                    // On ferme la boîte de dialogue manuellement puisque tout est bon
                    dialog.dismiss();
                } else {
                    // Échec : on affiche le message d'erreur et on laisse la boîte de dialogue ouverte
                    Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void afficherConfirmationSuppression() {
        String nomRando = currentHike != null && currentHike.getLibelle() != null ? currentHike.getLibelle() : "cette randonnée";

        new AlertDialog.Builder(this)
                .setTitle("Confirmation de suppression")
                .setMessage("Etes-vous sur de supprimer la randonnée " + nomRando + " ?")
                .setPositiveButton("Oui", (dialog, which) -> supprimerRandonnee())
                .setNegativeButton("Non", null)
                .show();
    }

    private void supprimerRandonnee() {
        ServiceRandonnee.supprimerRandonnee(this, tokenManager.getToken(), currentHike.getId(), new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                Toast.makeText(ActiviteGestionRandonnee.this, "Randonnée supprimée avec succès", Toast.LENGTH_SHORT).show();
                fermerAvecResultat(Activity.RESULT_OK, null);
            }
            @Override
            public void onError(VolleyError error) {
                // AJOUT DES VÉRIFICATIONS DE SÉCURITÉ ICI (error != null && error.networkResponse != null)
                if (error.networkResponse != null && error.networkResponse.statusCode == 204) {
                    Toast.makeText(ActiviteGestionRandonnee.this, "Randonnée supprimée avec succès", Toast.LENGTH_SHORT).show();
                    fermerAvecResultat(Activity.RESULT_OK, null);
                } else {
                    Toast.makeText(ActiviteGestionRandonnee.this, "Erreur lors de la suppression de la randonnée", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fermerAvecResultat(int resultCode, String message) {
        Intent intent = new Intent();
        if (message != null) intent.putExtra("CHILD_MESSAGE", message);
        setResult(resultCode, intent);
        finish();
    }
}