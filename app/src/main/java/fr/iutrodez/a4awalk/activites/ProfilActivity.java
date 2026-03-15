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
import fr.iutrodez.a4awalk.modeles.entites.TokenManager; // <-- IMPORT IMPORTANT

public class ProfilActivity extends HeaderActivity {

    private static final String ME_URL = "http://98.94.8.220:8080/users/me";

    protected Toolbar toolbar;
    private RequestQueue requestQueue;

    // Vues
    private TextView userName, userAddress, userAge, userEmail, userLevel, userMorphology;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_compte);

        configurerToolbar();

        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        // Initialisation des vues
        userName = findViewById(R.id.user_name);
        userAddress = findViewById(R.id.user_address);
        userAge = findViewById(R.id.user_age);
        userEmail = findViewById(R.id.user_email);
        userLevel = findViewById(R.id.user_level);
        userMorphology = findViewById(R.id.user_morphology);
        ImageView profileImage = findViewById(R.id.profile_image);
        profileImage.setImageResource(R.drawable.user_icon);

        requestQueue = Volley.newRequestQueue(this);

        // Charger les infos depuis l'API
        chargerProfil();

        // Bouton Éditer
        ImageButton editButton = findViewById(R.id.edit_button);
        if (editButton != null) {
            editButton.setOnClickListener(v -> {
                Intent intent = new Intent(ProfilActivity.this, UpdateProfilActivity.class);
                startActivity(intent);
            });
        }
    }

    private void chargerProfil() {
        // === UTILISATION DE VOTRE TOKEN MANAGER ===
        TokenManager tokenManager = new TokenManager(this);
        final String token = tokenManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, ME_URL, null,
                response -> {
                    try {
                        String nom = response.optString("nom", "Inconnu");
                        String prenom = response.optString("prenom", "");
                        String age = String.valueOf(response.optInt("age", 0));
                        String adresse = response.optString("adresse", "Non renseignée");
                        String email = response.optString("mail", "Non renseigné");
                        String niveau = response.optString("niveau", "DEBUTANT");
                        String morphologie = response.optString("morphologie", "MOYENNE");

                        // Affichage dans l'interface
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
                error -> {
                    Log.e("ProfilActivity", "Erreur réseau: " + error.toString());
                    Toast.makeText(this, "Impossible de charger le profil", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        requestQueue.add(request);
    }
}