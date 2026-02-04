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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.entites.PointOfInterest;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceCreationRandonnee;

import fr.iutrodez.a4awalk.activites.GestionParticipant;

public class ActiviteGestionRandonnee extends AppCompatActivity {

    // Constantes
    private static final int MODE_CONSULTATION = 1;
    private static final int MODE_CREATION = 2;
    private static final int MODE_MODIFICATION = 3;
    private final String ERREUR = "ERREUR";

    // UI Elements
    private EditText libelle, departLat, departLon, arriveeLat, arriveeLon;
    private ListView listePoints, listeParticipants;
    private Spinner nbJours;
    private ImageButton btnAjouterPOI, btnAjouterParticipant;
    private Button validateButton;
    private LinearLayout containerPoi, containerParticipants;

    // Données locales
    private ArrayList<PointOfInterest> listeTemporairePOI = new ArrayList<>();
    private ArrayList<PointOfInterest> poiOriginaux = new ArrayList<>();
    private ArrayAdapter<PointOfInterest> adapterPOI;

    private ArrayList<Participant> listeTemporaireParticipants = new ArrayList<>();
    private ArrayList<Participant> participantsOriginaux = new ArrayList<>();
    private ArrayAdapter<Participant> adapterParticipants;

    // Métier
    private TokenManager tokenManager;
    private User currentUser;
    private Hike currentHike;

    private final String BASE_URL = "http://98.94.8.220:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_details_randonnee);

        tokenManager = new TokenManager(this);
        initElementsGraphiques();
        recupererDonneesUtilisateur();
        recupererHikeIntent();

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

    // --- METHODE UTILITAIRE POUR GERER L'ETAT DU BOUTON AJOUTER ---
    private void updateBtnParticipantState() {
        if (listeTemporaireParticipants.size() >= 3) {
            btnAjouterParticipant.setEnabled(false);
            btnAjouterParticipant.setAlpha(0.5f); // Rend le bouton semi-transparent pour indiquer qu'il est désactivé
        } else {
            btnAjouterParticipant.setEnabled(true);
            btnAjouterParticipant.setAlpha(1.0f);
        }
    }

    // =========================================================================
    // MODES
    // =========================================================================

    private void consultationRandonnee() {
        containerPoi.setVisibility(View.VISIBLE);
        containerParticipants.setVisibility(View.VISIBLE);
        setChampsEditables(false);
        btnAjouterPOI.setVisibility(View.GONE);
        btnAjouterParticipant.setVisibility(View.GONE);
        listePoints.setEnabled(false);
        listeParticipants.setEnabled(false);

        validateButton.setText("Modifier la randonnée");
        validateButton.setOnClickListener(v -> {
            participantsOriginaux.clear();
            participantsOriginaux.addAll(listeTemporaireParticipants);
            poiOriginaux.clear();
            poiOriginaux.addAll(listeTemporairePOI);
            modificationRandonnee();
        });
    }

    private void creationRandonnee() {
        containerPoi.setVisibility(View.GONE);
        containerParticipants.setVisibility(View.GONE);
        setChampsEditables(true);
        validateButton.setText("Ajouter la randonnée");

        // On vérifie l'état initial (normalement vide, donc activé)
        updateBtnParticipantState();

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

        // Mise à jour initiale de l'état du bouton selon le nombre de participants existants
        updateBtnParticipantState();

        btnAjouterPOI.setOnClickListener(v -> gererDialogPOI(-1));
        listePoints.setOnItemClickListener((parent, view, position, id) -> gererDialogPOI(position));
        listePoints.setOnItemLongClickListener((parent, view, position, id) -> {
            listeTemporairePOI.remove(position);
            adapterPOI.notifyDataSetChanged();
            Toast.makeText(this, "POI retiré", Toast.LENGTH_SHORT).show();
            return true;
        });

        // Gestion Participants
        btnAjouterParticipant.setOnClickListener(v -> {
            GestionParticipant.gererDialogParticipant(
                    ActiviteGestionRandonnee.this,
                    tokenManager.getToken(),
                    new GestionParticipant.ParticipantCallback() {
                        @Override
                        public void onParticipantCreated(Participant newParticipant) {
                            newParticipant.setId(0L);
                            listeTemporaireParticipants.add(newParticipant);
                            adapterParticipants.notifyDataSetChanged();

                            // Vérification après ajout
                            updateBtnParticipantState();
                        }
                    }
            );
        });

        listeParticipants.setOnItemClickListener((parent, view, position, id) -> {
            GestionParticipant.gererDialogParticipant(
                    ActiviteGestionRandonnee.this,
                    tokenManager.getToken(),
                    new GestionParticipant.ParticipantCallback() {
                        @Override
                        public void onParticipantCreated(Participant newParticipant) {
                            Participant oldParticipant = listeTemporaireParticipants.get(position);
                            newParticipant.setId(oldParticipant.getId());
                            listeTemporaireParticipants.set(position, newParticipant);
                            adapterParticipants.notifyDataSetChanged();
                            // Pas besoin d'updateBtnState ici car la taille ne change pas
                        }
                    }
            );
        });

        listeParticipants.setOnItemLongClickListener((parent, view, position, id) -> {
            listeTemporaireParticipants.remove(position);
            adapterParticipants.notifyDataSetChanged();
            Toast.makeText(this, "Participant retiré", Toast.LENGTH_SHORT).show();

            // Vérification après suppression (réactive le bouton si < 3)
            updateBtnParticipantState();
            return true;
        });

        validateButton.setText("Enregistrer les modifications");
        validateButton.setOnClickListener(v -> {
            try {
                traiterMiseAJour();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    // =========================================================================
    // API
    // =========================================================================

    private void traiterCreation() {
        String nom = libelle.getText().toString();
        if (nom.isEmpty()) {
            libelle.setError("Requis");
            return;
        }
        int duree = Integer.parseInt(nbJours.getSelectedItem().toString());

        ServiceCreationRandonnee.validerRandonneeComplete(
                this, tokenManager.getToken(), nom, duree,
                new ServiceCreationRandonnee.FullCreationCallback() {
                    @Override
                    public void onSuccess(long hikeId) {
                        Toast.makeText(ActiviteGestionRandonnee.this, "Randonnée créée !", Toast.LENGTH_SHORT).show();
                        fermerAvecResultat(Activity.RESULT_OK, null);
                    }
                    @Override
                    public void onError(String message) {
                        Toast.makeText(ActiviteGestionRandonnee.this, "Erreur API", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void traiterMiseAJour() throws JSONException {
        if (currentHike == null || currentHike.getId() == 0) {
            Toast.makeText(this, "Erreur ID Randonnée", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject bodyHike = new JSONObject();
        bodyHike.put("libelle", libelle.getText().toString());
        bodyHike.put("dureeJours", Integer.parseInt(nbJours.getSelectedItem().toString()));

        JSONObject departObj = new JSONObject();
        departObj.put("id", 1);
        bodyHike.put("depart", departObj);

        JSONObject arriveeObj = new JSONObject();
        arriveeObj.put("id", 2);
        bodyHike.put("arrivee", arriveeObj);

        String urlUpdateHike = BASE_URL + "/hikes/" + currentHike.getId();

        AppelAPI.put(urlUpdateHike, tokenManager.getToken(), bodyHike, this, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                traiterMiseAJourParticipants(currentHike.getId());
                traiterMiseAJourPOI(currentHike.getId());
                Toast.makeText(ActiviteGestionRandonnee.this, "Modifications enregistrées", Toast.LENGTH_SHORT).show();
                fermerAvecResultat(Activity.RESULT_OK, null);
            }
            @Override
            public void onError(VolleyError error) {
                Toast.makeText(ActiviteGestionRandonnee.this, "Erreur MAJ Rando", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void traiterMiseAJourParticipants(long hikeId) {
        AppelAPI.VolleyObjectCallback silentCallback = new AppelAPI.VolleyObjectCallback() {
            @Override public void onSuccess(JSONObject result) {}
            @Override public void onError(VolleyError error) {}
        };

        for (Participant p : listeTemporaireParticipants) {
            JSONObject body = new JSONObject();
            try {
                body.put("nom", p.getNom());
                body.put("prenom", p.getPrenom());
                body.put("age", p.getAge());
                body.put("niveau", p.getNiveau());
                body.put("morphologie", p.getMorphologie());
                body.put("besoinKcal", p.getBesoinKcal());
                body.put("besoinEauLitre", p.getBesoinEauLitre());
                double cap = (p.getCapaciteEmportMaxKg() != 0.0) ? p.getCapaciteEmportMaxKg() : 0.0;
                body.put("capaciteEmportMaxKg", cap);
            } catch (JSONException e) { continue; }

            Long id = p.getId();
            if (id == null || id == 0) {
                AppelAPI.post(BASE_URL + "/hikes/" + hikeId + "/participants", tokenManager.getToken(), body, this, silentCallback);
            } else {
                AppelAPI.put(BASE_URL + "/hikes/" + hikeId + "/participants/" + id, tokenManager.getToken(), body, this, silentCallback);
            }
        }

        for (Participant pOrigin : participantsOriginaux) {
            boolean present = false;
            for (Participant pTemp : listeTemporaireParticipants) {
                if (pTemp.getId() != null && pTemp.getId().equals(pOrigin.getId())) {
                    present = true;
                    break;
                }
            }
            if (!present) {
                AppelAPI.delete(BASE_URL + "/hikes/" + hikeId + "/participants/" + pOrigin.getId(), tokenManager.getToken(), this, null);
            }
        }
    }

    private void traiterMiseAJourPOI(long hikeId) {
        AppelAPI.VolleyObjectCallback silentCallback = new AppelAPI.VolleyObjectCallback() {
            @Override public void onSuccess(JSONObject result) {}
            @Override public void onError(VolleyError error) {}
        };

        for (PointOfInterest poi : listeTemporairePOI) {
            Long id = poi.getId();
            if (id == null || id == 0) {
                JSONObject body = new JSONObject();
                try {
                    body.put("name", poi.getName());
                    body.put("latitude", poi.getLatitude());
                    body.put("longitude", poi.getLongitude());
                    body.put("description", (poi.getName() != null) ? poi.getName() : "POI");
                } catch (JSONException e) { continue; }

                AppelAPI.post(BASE_URL + "/hikes/" + hikeId + "/poi", tokenManager.getToken(), body, this, silentCallback);
            }
        }

        for (PointOfInterest poiOrigin : poiOriginaux) {
            boolean present = false;
            for (PointOfInterest poiTemp : listeTemporairePOI) {
                if (poiTemp.getId() != null && poiTemp.getId().equals(poiOrigin.getId())) {
                    present = true;
                    break;
                }
            }
            if (!present) {
                AppelAPI.delete(BASE_URL + "/hikes/" + hikeId + "/poi/" + poiOrigin.getId(), tokenManager.getToken(), this, null);
            }
        }
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

        listeTemporaireParticipants.clear();
        if (hike.getParticipants() != null) listeTemporaireParticipants.addAll(hike.getParticipants());
        adapterParticipants.notifyDataSetChanged();

        // Vérification après remplissage
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

    private void gererDialogPOI(int position) {
        boolean isModification = (position >= 0);
        PointOfInterest poiAModifier = isModification ? listeTemporairePOI.get(position) : null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isModification ? "Détails POI" : "Nouveau POI");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ajout_poi, null);
        final EditText inputNom = view.findViewById(R.id.edit_poi_nom);
        final EditText inputLat = view.findViewById(R.id.edit_poi_lat);
        final EditText inputLon = view.findViewById(R.id.edit_poi_lon);
        if (isModification && poiAModifier != null) {
            inputNom.setText(poiAModifier.getName());
            inputLat.setText(String.valueOf(poiAModifier.getLatitude()));
            inputLon.setText(String.valueOf(poiAModifier.getLongitude()));
        }
        builder.setView(view);
        builder.setPositiveButton(isModification ? "Valider" : "Ajouter", (dialog, which) -> {
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
            } catch (Exception e) {}
        });
        if (isModification) {
            builder.setNeutralButton("Supprimer", (dialog, which) -> {
                listeTemporairePOI.remove(position);
                adapterPOI.notifyDataSetChanged();
            });
        }
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void fermerAvecResultat(int resultCode, String message) {
        Intent intent = new Intent();
        if (message != null) {
            intent.putExtra("CHILD_MESSAGE", message);
        }
        setResult(resultCode, intent);
        finish();
    }
}