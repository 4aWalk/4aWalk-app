package fr.iutrodez.a4awalk.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.iutrodez.a4awalk.R;

public class SacActivity extends AppCompatActivity {

    private LinearLayout containerItemsSac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sac_a_dos);

        // Récupération des éléments de la vue
        ImageButton btnRetour = findViewById(R.id.btnRetour);
        containerItemsSac = findViewById(R.id.containerItemsSac);

        // Gestion du bouton retour
        btnRetour.setOnClickListener(v -> finish());

        // Récupération des données passées par l'Intent depuis PopUpParticipant
        String equipementsJson = getIntent().getStringExtra("EQUIPEMENTS_JSON");
        String nourritureJson = getIntent().getStringExtra("NOURRITURE_JSON");

        // Remplissage dynamique du tableau
        remplirTableau(equipementsJson, nourritureJson);
    }

    /**
     * Lit les chaînes JSON d'équipements et de nourriture, puis génère les vues correspondantes.
     */
    private void remplirTableau(String equipementsStr, String nourritureStr) {
        // On s'assure que le conteneur est vide avant de commencer
        if (containerItemsSac != null) {
            containerItemsSac.removeAllViews();
        } else {
            Log.e("SacActivity", "Erreur : containerItemsSac est introuvable dans le layout.");
            return;
        }

        int indexLigne = 0; // Utilisé pour l'alternance des couleurs de fond

        try {
            // 1. Ajout des équipements
            if (equipementsStr != null && !equipementsStr.isEmpty()) {
                JSONArray equipements = new JSONArray(equipementsStr);
                for (int i = 0; i < equipements.length(); i++) {
                    JSONObject eq = equipements.getJSONObject(i);
                    int qte = eq.optInt("nbItem", 1);
                    String nom = eq.optString("nom", "Équipement inconnu");
                    double masse = eq.optDouble("masseGrammes", 0.0);

                    ajouterLigneTableau(qte, nom, formatPoids(masse), indexLigne % 2 == 0);
                    indexLigne++;
                }
            }

            // 2. Ajout de la nourriture
            if (nourritureStr != null && !nourritureStr.isEmpty()) {
                JSONArray nourritures = new JSONArray(nourritureStr);
                for (int i = 0; i < nourritures.length(); i++) {
                    JSONObject nour = nourritures.getJSONObject(i);
                    int qte = nour.optInt("nbItem", 1);
                    String nom = nour.optString("nom", "Nourriture inconnue");
                    double masse = nour.optDouble("masseGrammes", 0.0);

                    ajouterLigneTableau(qte, nom, formatPoids(masse), indexLigne % 2 == 0);
                    indexLigne++;
                }
            }

            // Si le sac est totalement vide (aucun équipement ni nourriture)
            if (indexLigne == 0) {
                ajouterLigneTableau(0, "Le sac à dos est vide", "-", true);
            }

        } catch (JSONException e) {
            Log.e("SacActivity", "Erreur lors du parsing JSON des items du sac", e);
            ajouterLigneTableau(0, "Erreur de lecture des données", "-", true);
        }
    }

    /**
     * Crée une nouvelle ligne (LinearLayout) contenant 3 colonnes (Quantité, Nom, Poids)
     * et l'ajoute au tableau.
     */
    private void ajouterLigneTableau(int qte, String nom, String poids, boolean isPair) {
        // Création de la ligne conteneur
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        row.setOrientation(LinearLayout.HORIZONTAL);

        // Alternance des couleurs de fond pour une meilleure lisibilité (#F5EBE1 et #E8D7C3)
        if (isPair) {
            row.setBackgroundColor(Color.parseColor("#F5EBE1"));
        } else {
            row.setBackgroundColor(Color.parseColor("#E8D7C3"));
        }

        // --- Colonne Quantité ---
        TextView tvQte = new TextView(this);
        tvQte.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(80), LinearLayout.LayoutParams.WRAP_CONTENT));
        tvQte.setText(qte > 0 ? String.valueOf(qte) : "-");
        tvQte.setGravity(Gravity.CENTER);
        tvQte.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        tvQte.setTextColor(Color.BLACK);
        tvQte.setTextSize(14f);

        // --- Colonne Nom ---
        TextView tvNom = new TextView(this);
        tvNom.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(250), LinearLayout.LayoutParams.WRAP_CONTENT));
        tvNom.setText(nom);
        tvNom.setGravity(Gravity.CENTER_VERTICAL | Gravity.START); // Aligné à gauche pour la lisibilité
        tvNom.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        tvNom.setTextColor(Color.BLACK);
        tvNom.setTextSize(14f);

        // --- Colonne Poids ---
        TextView tvPoids = new TextView(this);
        tvPoids.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(100), LinearLayout.LayoutParams.WRAP_CONTENT));
        tvPoids.setText(poids);
        tvPoids.setGravity(Gravity.CENTER);
        tvPoids.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        tvPoids.setTextColor(Color.BLACK);
        tvPoids.setTextSize(14f);

        // Ajout des vues dans la ligne
        row.addView(tvQte);
        row.addView(tvNom);
        row.addView(tvPoids);

        // Ajout de la ligne complète dans le conteneur parent
        containerItemsSac.addView(row);
    }

    /**
     * Formate le poids pour un affichage plus propre (ex: "1.2kg" ou "150g").
     */
    private String formatPoids(double masseGrammes) {
        if (masseGrammes >= 1000) {
            return String.format(java.util.Locale.getDefault(), "%.2f kg", masseGrammes / 1000.0);
        } else {
            return String.format(java.util.Locale.getDefault(), "%.0f g", masseGrammes);
        }
    }

    /**
     * Utilitaire pour convertir des valeurs dp (densité de pixels indépendante) en pixels réels.
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }


}