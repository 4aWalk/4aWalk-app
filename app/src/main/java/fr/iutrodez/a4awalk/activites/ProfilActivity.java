package fr.iutrodez.a4awalk.activites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;

/**
 * Activité d'affichage du profil de l'utilisateur connecté.
 * Charge les données depuis l'API REST (/users/me) et les affiche en lecture seule.
 * Un bouton d'édition permet de naviguer vers UpdateProfilActivity.
 */
public class ProfilActivity extends HeaderActivity {

    // URL de l'endpoint qui retourne les informations de l'utilisateur connecté
    private static final String ME_URL = "http://98.94.8.220:8080/users/me";

    protected Toolbar toolbar;

    // File d'attente Volley pour les requêtes HTTP
    private RequestQueue requestQueue;

    // Vues affichant les informations du profil
    private TextView userName, userAddress, userAge, userEmail, userLevel, userMorphology;

    /**
     * Point d'entrée de l'activité.
     * Initialise les vues, charge le profil depuis l'API et configure le bouton d'édition.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_compte);

        // Initialise la toolbar héritée de HeaderActivity
        configurerToolbar();

        // Configuration optionnelle de la toolbar locale (masque le titre par défaut)
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        // Liaison des TextViews avec leurs identifiants XML
        userName = findViewById(R.id.user_name);
        userAddress = findViewById(R.id.user_address);
        userAge = findViewById(R.id.user_age);
        userEmail = findViewById(R.id.user_email);
        userLevel = findViewById(R.id.user_level);
        userMorphology = findViewById(R.id.user_morphology);

        // Définit l'icône de profil par défaut (pas de photo personnalisée)
        ImageView profileImage = findViewById(R.id.profile_image);
        profileImage.setImageResource(R.drawable.user_icon);

        // Initialise la file de requêtes Volley pour les appels HTTP
        requestQueue = Volley.newRequestQueue(this);

        // Déclenche le chargement du profil depuis l'API
        chargerProfil();

        // Bouton d'édition : navigue vers l'écran de modification du profil
        ImageButton editButton = findViewById(R.id.edit_button);
        if (editButton != null) {
            editButton.setOnClickListener(v -> {
                Intent intent = new Intent(ProfilActivity.this, UpdateProfilActivity.class);
                startActivity(intent);
            });
        }
    }

    /**
     * Envoie une requête GET authentifiée à /users/me pour récupérer les données
     * de l'utilisateur connecté et les afficher dans les TextViews.
     * Utilise optString/optInt pour gérer les champs absents sans lever d'exception.
     */
    private void chargerProfil() {
        // Récupère le token JWT sauvegardé localement
        TokenManager tokenManager = new TokenManager(this);
        final String token = tokenManager.getToken();

        // Vérifie que l'utilisateur est bien connecté avant d'appeler l'API
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        // Requête GET qui retourne un JSONObject (pas un tableau)
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, ME_URL, null,
                response -> {
                    try {
                        // Extraction des champs JSON avec des valeurs par défaut si absents
                        String nom = response.optString("nom", "Inconnu");
                        String prenom = response.optString("prenom", "");
                        String age = String.valueOf(response.optInt("age", 0));
                        String adresse = response.optString("adresse", "Non renseignée");
                        String email = response.optString("mail", "Non renseigné");
                        String niveau = response.optString("niveau", "DEBUTANT");
                        String morphologie = response.optString("morphologie", "MOYENNE");

                        // Mise à jour de l'interface avec les données récupérées
                        userName.setText(nom + " " + prenom);
                        userAddress.setText(adresse);
                        userAge.setText(age + " ans");
                        userEmail.setText(email);
                        userLevel.setText(niveau);
                        userMorphology.setText(morphologie);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                // Callback d'erreur : log pour le debug + message utilisateur
                error -> {
                    Log.e("ProfilActivity", "Erreur réseau: " + error.toString());
                    Toast.makeText(this, "Impossible de charger le profil", Toast.LENGTH_SHORT).show();
                }
        ) {
            /**
             * Surcharge getHeaders() pour injecter le token JWT dans chaque requête.
             * Le header "Authorization: Bearer <token>" est requis par l'API.
             */
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        // Ajoute la requête à la file Volley pour exécution asynchrone
        requestQueue.add(request);
    }
}