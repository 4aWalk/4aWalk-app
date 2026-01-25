package fr.iutrodez.a4awalk.GestionListes.GestionItemRando;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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
import fr.iutrodez.a4awalk.entity.PointOfInterest;
import fr.iutrodez.a4awalk.GestionListes.FragmentListeRandonnees;
import fr.iutrodez.a4awalk.R;

public class DetailsRando extends AppCompatActivity {

    private EditText libelle;

    private EditText depart;

    private EditText arrivee;

    private String[] pointsInterets;

    private ListView listePoints;

    private TextView textePoints;

    private ListView listeParticipants;

    private TextView texteParticipant;

    private TextView texteProduit;

    private TextView texteEquipement;

    private ArrayAdapter adaptateurPoints;

    private Spinner nbJours;

    private ArrayList<PointOfInterest> listeTemporairePOI = new ArrayList<>();
    private ArrayAdapter<PointOfInterest> adapterPOI;
    private ImageButton btnAjouterPOI; // Ajoute ce bouton dans ton layout principal XML

    private List<Integer> jours;

    private Button validateButton;

    private Hike hike;

    private Intent intentionRecu;

    private final String ERREUR = "ERREUR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_rando);
        libelle = findViewById(R.id.nom_rando);
        depart = findViewById(R.id.depart_rando);
        arrivee = findViewById(R.id.arrivee_rando);
        listePoints = findViewById(R.id.points_list);
        nbJours = findViewById(R.id.spinner_jours);
        validateButton = findViewById(R.id.validate_button);

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
        libelle.setFocusableInTouchMode(true);
        depart.setFocusableInTouchMode(true);
        arrivee.setFocusableInTouchMode(true);
        validateButton.setVisibility(View.VISIBLE);

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
        btnAjouterPOI = findViewById(R.id.btn_add_poi); // Pense à créer ce bouton dans ton layout principal
        btnAjouterPOI.setOnClickListener(v -> afficherDialogAjoutPOI());

        // 2. Action au clic sur Valider
        validateButton.setOnClickListener(v -> {
            try {
                // Construction du JSON à envoyer
                JSONObject randoAEnvoyer = new JSONObject();
                randoAEnvoyer.put("libelle", libelle.getText().toString());
                randoAEnvoyer.put("depart", depart.getText().toString());
                randoAEnvoyer.put("arrivee", arrivee.getText().toString());
                // Exemple : on récupère la valeur du Spinner
                randoAEnvoyer.put("dureeJours", Integer.parseInt(nbJours.getSelectedItem().toString()));

                String urlCreation = "http://98.94.8.220:8080/hikes";

                // Appel de la nouvelle méthode POST
                AppelAPI.postAPI(urlCreation, randoAEnvoyer, this, new AppelAPI.VolleyObjectCallback() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        Toast.makeText(DetailsRando.this, "Randonnée créée avec succès !", Toast.LENGTH_SHORT).show();
                        finish(); // Ferme la page et revient à la liste
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

    private void modificationRandonnee() {
    }

    private void consultationRandonnee() {
        adaptateurPoints = new ArrayAdapter(this, android.R.layout.simple_list_item_1, pointsInterets);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            hike = getIntent().getParcelableExtra("PRODUCT_KEY", Hike.class);
        } else {
            hike = (Hike) getIntent().getParcelableExtra("PRODUCT_KEY");
        }
        showData();
    }

    private void affichageInfosRando() {
        // TODO
    }

    private void showData() {
        libelle.setText(hike.getLibelle());
        depart.setText(hike.getDepart());
        arrivee.setText(hike.getArrivee());
        listePoints.setAdapter(adaptateurPoints);
    }

    private void afficherDialogAjoutPOI() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nouveau point d'intérêt");

        // On charge le layout XML créé à l'étape 1
        View viewInflated = getLayoutInflater().inflate(R.layout.dialog_ajout_poi, null);

        final EditText inputNom = viewInflated.findViewById(R.id.edit_poi_nom);
        final EditText inputDesc = viewInflated.findViewById(R.id.edit_poi_desc);
        final EditText inputLat = viewInflated.findViewById(R.id.edit_poi_lat);
        final EditText inputLon = viewInflated.findViewById(R.id.edit_poi_lon);

        builder.setView(viewInflated);

        // Bouton "Ajouter" de la popup
        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String nom = inputNom.getText().toString();
            String desc = inputDesc.getText().toString();
            double lat = Double.parseDouble(inputLat.getText().toString());
            double lon = Double.parseDouble(inputLon.getText().toString());

            if (!nom.isEmpty()) {
                // Création de l'objet (Adapte le constructeur selon ta classe)
                PointOfInterest nouveauPoint = new PointOfInterest(nom, lat, lon, desc, null);

                // Ajout à la liste et mise à jour de l'affichage
                listeTemporairePOI.add(nouveauPoint);
                adapterPOI.notifyDataSetChanged();
            }
        });

        // Bouton "Annuler"
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
