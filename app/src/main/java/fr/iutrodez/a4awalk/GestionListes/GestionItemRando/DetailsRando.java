package fr.iutrodez.a4awalk.GestionListes.GestionItemRando;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;


import java.util.ArrayList;
import java.util.Set;

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

    private ArrayAdapter adaptateurPoints;

    private Spinner nbJours;

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
        adaptateurPoints = new ArrayAdapter(this, android.R.layout.simple_list_item_1, pointsInterets );

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
        // String hikeID = intentionRecu.getStringExtra(FragmentListeRandonnees.HIKE_ID_KEY);
        // String url = String.format(URL_DETAILS_RANDO, hikeID);
    }

    private void creationRandonnee() {
    }

    private void modificationRandonnee() {
    }

    private void consultationRandonnee() {
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
}
