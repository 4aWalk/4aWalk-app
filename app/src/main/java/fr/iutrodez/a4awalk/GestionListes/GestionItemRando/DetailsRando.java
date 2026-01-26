package fr.iutrodez.a4awalk.GestionListes.GestionItemRando;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.android.volley.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import fr.iutrodez.a4awalk.AppelAPI;
import fr.iutrodez.a4awalk.entity.Hike;
import fr.iutrodez.a4awalk.entity.Participant;
import fr.iutrodez.a4awalk.entity.PointOfInterest;
import fr.iutrodez.a4awalk.GestionListes.FragmentListeRandonnees;
import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.entity.User;

public class DetailsRando extends AppCompatActivity {

    private final static String URL_CREATION = "http://98.94.8.220:8080/hikes";

    private final static String URL_AJOUT_POI = "http://IP:Port/hikes/%d/poi";

    private final String ERREUR = "ERREUR";

    private final String OK = "OK";

    private EditText libelle;

    private EditText departLat;

    private EditText departLon;

    private EditText arriveeLat;

    private EditText arriveeLon;

    private List<PointOfInterest> pointsInterets;

    private ListView listePoints;

    private TextView textePoints;

    private ListView listeParticipants;

    private TextView texteParticipant;

    private ArrayAdapter adaptateurPoints;

    private Spinner nbJours;

    private ArrayList<PointOfInterest> listeTemporairePOI = new ArrayList<>();
    private ArrayAdapter<PointOfInterest> adapterPOI;
    private ImageButton btnAjouterPOI; // Ajoute ce bouton dans ton layout principal XML

    private ImageButton btnAjouterParticipant;
    private List<Integer> jours;

    private Button validateButton;

    private Hike hike;

    private Intent intentionRecu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_rando);
        libelle = findViewById(R.id.nom_rando);
        departLat = findViewById(R.id.depart_rando_lat);
        departLon = findViewById(R.id.depart_rando_lon);
        arriveeLat = findViewById(R.id.arrivee_rando_lat);
        arriveeLon = findViewById(R.id.arrivee_rando_lon);
        listePoints = findViewById(R.id.points_list);
        nbJours = findViewById(R.id.spinner_jours);

        // on récupère l'intention qui a lancé cette activité
        intentionRecu = getIntent();
        int pageID = intentionRecu.getIntExtra("ID_PAGE",0);

        /* TODO branches conditionnelles permettant de configurer la page en fonction du besoin utilisateur
         * - 1 pour la consultation d'une randonnée
         * - 2 pour la création d'une randonnée
         */
        switch(pageID) {
            case 1:
                consultationRandonnee();
                break;
            case 2:
                creationRandonnee();
                break;
            default:
                // création d'une intention pour informer l'activté parente
                Intent intentionRetour = new Intent();
                intentionRetour.putExtra(FragmentListeRandonnees.CHILD_MESSAGE_KEY, ERREUR);
                // retour à l'activité parente et destruction de l'activité fille
                setResult(Activity.RESULT_CANCELED, intentionRetour);
                finish(); // destruction de l'activité courante
        }
    }

    private void creationRandonnee() {
        // 1. On rend les champs modifiables
        libelle.setClickable(true);
        departLat.setClickable(true);
        departLon.setClickable(true);
        arriveeLat.setClickable(true);
        arriveeLon.setClickable(true);
        validateButton = findViewById(R.id.validate_button);
        validateButton.setVisibility(View.VISIBLE);
        btnAjouterPOI = findViewById(R.id.btn_add_poi);
        btnAjouterPOI.setVisibility(View.VISIBLE);
        btnAjouterParticipant = findViewById(R.id.btn_add_participant);
        btnAjouterParticipant.setVisibility(View.VISIBLE);

        // 1. Préparer les données
        jours = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            jours.add(i);
        }

        // 2. Créer l'adaptateur
        // On utilise un layout standard d'Android pour l'apparence
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                jours
        );

        // 3. Spécifier le layout de la liste déroulante
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 4. Appliquer l'adapter au Spinner
        nbJours.setAdapter(adapter);

        // 1. Initialiser l'adapter pour la ListView
        // Note: Assure-toi que ta classe PointOfInterest a une méthode toString() qui retourne le nom !
        adapterPOI = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listeTemporairePOI);
        listePoints.setAdapter(adapterPOI);

        // 2. Bouton pour ouvrir la popup
        btnAjouterPOI.setOnClickListener(v -> afficherDialogAjoutPOI());

        // 2. Action au clic sur Valider
        validateButton.setOnClickListener(v -> {
            try {
                // Construction du JSON à envoyer
                JSONObject randoAEnvoyer = new JSONObject();
                randoAEnvoyer.put("libelle", libelle.getText().toString());
                randoAEnvoyer.put("dureeJours", Integer.parseInt(nbJours.getSelectedItem().toString()));
                JSONObject depart = new JSONObject();
                depart.put("id",1);
                randoAEnvoyer.put("depart",depart);
                JSONObject arrivee = new JSONObject();
                arrivee.put("id",2);
                randoAEnvoyer.put("arrivee",arrivee);

                // Appel de la nouvelle méthode POST
                AppelAPI.postAPI(URL_CREATION, randoAEnvoyer, this, new AppelAPI.VolleyObjectCallback() {
                    @Override
                    public void onSuccess(JSONObject result) throws JSONException {
                        Log.i("création rando", "randonnée créée");
                        ajoutPOIRando(result);
                    }

                    @Override
                    public void onError(String message) {
                        // L'erreur est déjà affichée par le Toast dans AppelAPI
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur dans les données", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ajoutPOIRando(JSONObject result) throws JSONException {
        long randoID = result.getLong("id");
        User user = getIntent().getParcelableExtra("USER_DATA");
        String urlAjoutPOI = String.format(URL_AJOUT_POI, randoID);
        for (int i = 0; i < listeTemporairePOI.size(); i++) {
            PointOfInterest pointActuel = listeTemporairePOI.get(i);
            JSONObject poi = new JSONObject();
            poi.put("latitude",pointActuel.getLatitude());
            poi.put("longitude",pointActuel.getLongitude());
            poi.put("name",pointActuel.getName());
            AppelAPI.postAPI(urlAjoutPOI, poi, this, new AppelAPI.VolleyObjectCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    Log.i("création rando", "point d'intérêt crée: ");
                }

                @Override
                public void onError(String message) {
                    // L'erreur est déjà affichée par le Toast dans AppelAPI
                }
            });
        }
        // création d'une intention pour informer l'activté parente
        Intent intentionRetour = new Intent();
        // retour à l'activité parente et destruction de l'activité fille
        setResult(Activity.RESULT_OK, intentionRetour);
        finish(); // destruction de l'activité courante
    }

    private void modificationRandonnee() {
    }

    private void consultationRandonnee() {
        // 1. Récupération sécurisée de l'objet Hike
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            hike = getIntent().getParcelableExtra("HIKE_OBJECT", Hike.class);
        } else {
            hike = getIntent().getParcelableExtra("HIKE_OBJECT");
        }

        if (hike != null) {
            // 2. Gestion des POI Optionnels
            pointsInterets = new ArrayList<>(hike.getOptionalPoints());
            adaptateurPoints = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1,
                    pointsInterets);
            listePoints.setAdapter(adaptateurPoints);

            // 3. Gestion des Participants (si vous avez une ListView dédiée)
            listeParticipants = findViewById(R.id.participants_list); // Vérifiez l'ID dans votre XML
            if (listeParticipants != null) {
                List<Participant> participants = new ArrayList<>(hike.getParticipants());
                ArrayAdapter<Participant> adapterPart = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        participants);
                listeParticipants.setAdapter(adapterPart);
            }

            // 5. Remplir les champs textes
            showData();
        }
    }

    private void affichageInfosRando() {
        // TODO
    }

    private void showData() {
        if (hike == null) return;

        // Remplissage des champs
        libelle.setText(hike.getLibelle());
        libelle.setEnabled(false);

        if (hike.getDepart() != null) {
            departLat.setText(String.valueOf(hike.getDepart().getLatitude()));
            departLon.setText(String.valueOf(hike.getDepart().getLongitude()));
            departLat.setEnabled(false);
            departLon.setEnabled(false);
        }

        if (hike.getArrivee() != null) {
            arriveeLat.setText(String.valueOf(hike.getArrivee().getLatitude()));
            arriveeLon.setText(String.valueOf(hike.getArrivee().getLongitude()));
            arriveeLat.setEnabled(false);
            arriveeLon.setEnabled(false);
        }
    }

    private void afficherDialogAjoutPOI() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nouveau point d'intérêt");

        // On charge le layout XML créé à l'étape 1
        View viewInflated = getLayoutInflater().inflate(R.layout.dialog_ajout_poi, null);

        final EditText inputNom = viewInflated.findViewById(R.id.edit_poi_nom);
        final EditText inputLat = viewInflated.findViewById(R.id.edit_poi_lat);
        final EditText inputLon = viewInflated.findViewById(R.id.edit_poi_lon);

        builder.setView(viewInflated);

        // Bouton "Ajouter" de la popup
        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String nom = inputNom.getText().toString();
            double lat = Double.parseDouble(inputLat.getText().toString());
            double lon = Double.parseDouble(inputLon.getText().toString());

            PointOfInterest nouveauPoint = new PointOfInterest(1L, nom, lat, lon);

            // Ajout à la liste et mise à jour de l'affichage
            listeTemporairePOI.add(nouveauPoint);
            adapterPOI.notifyDataSetChanged();
        });

        // Bouton "Annuler"
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
