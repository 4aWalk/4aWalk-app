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

/**
 * Activité affichant le contenu du sac à dos d'un participant sous forme de tableau.
 * Les données (équipements et nourriture) sont reçues en JSON via l'Intent.
 * Le tableau est construit dynamiquement en code Java (pas de layout XML fixe pour les lignes).
 */
public class SacActivity extends AppCompatActivity {

    // Conteneur vertical dans lequel les lignes du tableau sont ajoutées dynamiquement
    private LinearLayout containerItemsSac;

    /**
     * Point d'entrée de l'activité.
     * Récupère les données JSON depuis l'Intent et déclenche le remplissage du tableau.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sac_a_dos);

        // Liaison des vues avec leurs identifiants XML
        ImageButton btnRetour = findViewById(R.id.btnRetour);
        containerItemsSac = findViewById(R.id.containerItemsSac);

        // Ferme l'activité et retourne à l'écran précédent
        btnRetour.setOnClickListener(v -> finish());

        // Récupération des chaînes JSON transmises par PopUpParticipant via l'Intent
        String equipementsJson = getIntent().getStringExtra("EQUIPEMENTS_JSON");
        String nourritureJson = getIntent().getStringExtra("NOURRITURE_JSON");

        // Lance le remplissage du tableau avec les deux sources de données
        remplirTableau(equipementsJson, nourritureJson);
    }

    /**
     * Lit les chaînes JSON d'équipements et de nourriture, puis génère les lignes du tableau.
     * Un index global est maintenu pour alterner les couleurs de fond (effet "zébré").
     *
     * @param equipementsStr JSON Array des équipements sous forme de chaîne.
     * @param nourritureStr  JSON Array de la nourriture sous forme de chaîne.
     */
    private void remplirTableau(String equipementsStr, String nourritureStr) {
        // Nettoie le conteneur pour éviter les doublons si la méthode est appelée plusieurs fois
        if (containerItemsSac != null) {
            containerItemsSac.removeAllViews();
        } else {
            Log.e("SacActivity", "Erreur : containerItemsSac est introuvable dans le layout.");
            return;
        }

        // Index global pour alterner les couleurs de fond entre les lignes (pair/impair)
        int indexLigne = 0;

        try {
            // 1. Traitement des équipements
            if (equipementsStr != null && !equipementsStr.isEmpty()) {
                JSONArray equipements = new JSONArray(equipementsStr);
                for (int i = 0; i < equipements.length(); i++) {
                    JSONObject eq = equipements.getJSONObject(i);
                    // optInt/optString : retourne la valeur par défaut si le champ est absent
                    int qte = eq.optInt("nbItem", 1);
                    String nom = eq.optString("nom", "Équipement inconnu");
                    double masse = eq.optDouble("masseGrammes", 0.0);

                    ajouterLigneTableau(qte, nom, formatPoids(masse), indexLigne % 2 == 0);
                    indexLigne++;
                }
            }

            // 2. Traitement de la nourriture (même structure que les équipements)
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

            // Si aucun item n'a été ajouté, on affiche un message "sac vide"
            if (indexLigne == 0) {
                ajouterLigneTableau(0, "Le sac à dos est vide", "-", true);
            }

        } catch (JSONException e) {
            // En cas d'erreur de parsing JSON, on affiche une ligne d'erreur dans le tableau
            Log.e("SacActivity", "Erreur lors du parsing JSON des items du sac", e);
            ajouterLigneTableau(0, "Erreur de lecture des données", "-", true);
        }
    }

    /**
     * Crée une ligne horizontale contenant 3 colonnes (Quantité, Nom, Poids)
     * et l'ajoute au conteneur parent du tableau.
     * Les dimensions sont exprimées en dp et converties en pixels via dpToPx().
     *
     * @param qte    La quantité de l'item (affiché en colonne 1).
     * @param nom    Le nom de l'item (affiché en colonne 2).
     * @param poids  Le poids formaté (affiché en colonne 3).
     * @param isPair Détermine la couleur de fond : clair si true, plus sombre si false.
     */
    private void ajouterLigneTableau(int qte, String nom, String poids, boolean isPair) {
        // Création du conteneur de ligne horizontal
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        row.setOrientation(LinearLayout.HORIZONTAL);

        // Alternance des couleurs pour une meilleure lisibilité (effet "zébré")
        if (isPair) {
            row.setBackgroundColor(Color.parseColor("#F5EBE1")); // Fond clair
        } else {
            row.setBackgroundColor(Color.parseColor("#E8D7C3")); // Fond légèrement plus sombre
        }

        // --- Colonne 1 : Quantité (largeur fixe 80dp, centrée) ---
        TextView tvQte = new TextView(this);
        tvQte.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(80), LinearLayout.LayoutParams.WRAP_CONTENT));
        // Affiche "-" si la quantité est 0 (ex: ligne de message "sac vide")
        tvQte.setText(qte > 0 ? String.valueOf(qte) : "-");
        tvQte.setGravity(Gravity.CENTER);
        tvQte.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        tvQte.setTextColor(Color.BLACK);
        tvQte.setTextSize(14f);

        // --- Colonne 2 : Nom (largeur fixe 250dp, aligné à gauche pour la lisibilité) ---
        TextView tvNom = new TextView(this);
        tvNom.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(250), LinearLayout.LayoutParams.WRAP_CONTENT));
        tvNom.setText(nom);
        tvNom.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        tvNom.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        tvNom.setTextColor(Color.BLACK);
        tvNom.setTextSize(14f);

        // --- Colonne 3 : Poids (largeur fixe 100dp, centré) ---
        TextView tvPoids = new TextView(this);
        tvPoids.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(100), LinearLayout.LayoutParams.WRAP_CONTENT));
        tvPoids.setText(poids);
        tvPoids.setGravity(Gravity.CENTER);
        tvPoids.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        tvPoids.setTextColor(Color.BLACK);
        tvPoids.setTextSize(14f);

        // Ajout des 3 colonnes dans la ligne
        row.addView(tvQte);
        row.addView(tvNom);
        row.addView(tvPoids);

        // Ajout de la ligne complète dans le conteneur principal du tableau
        containerItemsSac.addView(row);
    }

    /**
     * Formate le poids pour un affichage lisible :
     * - Au-dessus de 1000g → affiché en kilogrammes avec 2 décimales (ex: "1.50 kg")
     * - En dessous de 1000g → affiché en grammes sans décimales (ex: "250 g")
     *
     * @param masseGrammes La masse en grammes à formater.
     * @return La chaîne formatée avec l'unité appropriée.
     */
    private String formatPoids(double masseGrammes) {
        if (masseGrammes >= 1000) {
            // Conversion grammes → kilogrammes
            return String.format(java.util.Locale.getDefault(), "%.2f kg", masseGrammes / 1000.0);
        } else {
            return String.format(java.util.Locale.getDefault(), "%.0f g", masseGrammes);
        }
    }

    /**
     * Convertit une valeur en dp en pixels réels.
     * Nécessaire car les dimensions des vues créées en code doivent être en pixels.
     *
     * @param dp La valeur en dp à convertir.
     * @return La valeur équivalente en pixels pour la densité d'écran de l'appareil.
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}