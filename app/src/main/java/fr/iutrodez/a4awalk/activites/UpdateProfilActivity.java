package fr.iutrodez.a4awalk.activites;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager; // <-- IMPORT IMPORTANT

public class UpdateProfilActivity extends HeaderActivity {

    private static final String BASE_URL = "http://98.94.8.220:8080/users/";
    private static final String ME_URL   = "http://98.94.8.220:8080/users/me";

    private TextInputEditText etNom, etPrenom, etAge, etAdresse, etEmail, etMotDePasse, etConfirmerMotDePasse;
    private Spinner spinnerNiveau, spinnerMorphologie;
    private RequestQueue requestQueue;

    // Pour stocker l'ID de l'utilisateur nécessaire pour le PUT
    private Long currentUserId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modification_compte);

        configurerToolbar();
        initViews();

        requestQueue = Volley.newRequestQueue(this);

        chargerDonneesUtilisateur();

        Button btnRetour = findViewById(R.id.btn_retour);
        btnRetour.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfilActivity.class));
            finish();
        });

        Button btnValider = findViewById(R.id.btn_update_account);
        btnValider.setOnClickListener(v -> validateForm());
    }

    private void initViews() {
        etNom = findViewById(R.id.up_nom);
        etPrenom = findViewById(R.id.up_prenom);
        etAge = findViewById(R.id.up_age);
        etAdresse = findViewById(R.id.up_adresse);
        etEmail = findViewById(R.id.up_email);
        etMotDePasse = findViewById(R.id.up_mot_de_passe);
        etConfirmerMotDePasse = findViewById(R.id.up_confirmer_mot_de_passe);

        spinnerNiveau = findViewById(R.id.spinner_update_niveau);
        spinnerMorphologie = findViewById(R.id.spinner_update_morphologie);

        ArrayAdapter<CharSequence> adapterNiveau = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"DEBUTANT", "ENTRAINE", "SPORTIF"});
        adapterNiveau.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNiveau.setAdapter(adapterNiveau);

        ArrayAdapter<CharSequence> adapterMorpho = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"LEGERE", "MOYENNE", "FORTE"});
        adapterMorpho.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMorphologie.setAdapter(adapterMorpho);
    }

    private void chargerDonneesUtilisateur() {
        TokenManager tokenManager = new TokenManager(this);
        final String token = tokenManager.getToken();

        if (token == null || token.isEmpty()) return;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, ME_URL, null,
                response -> {
                    // ON SAUVEGARDE L'ID ICI POUR POUVOIR FAIRE LE PUT PLUS TARD
                    currentUserId = response.optLong("id", -1);

                    etNom.setText(response.optString("nom", ""));
                    etPrenom.setText(response.optString("prenom", ""));
                    int age = response.optInt("age", 0);
                    etAge.setText(age > 0 ? String.valueOf(age) : "");
                    etAdresse.setText(response.optString("adresse", ""));
                    etEmail.setText(response.optString("mail", ""));

                    setSpinnerSelection(spinnerNiveau, response.optString("niveau", "DEBUTANT"));
                    setSpinnerSelection(spinnerMorphologie, response.optString("morphologie", "MOYENNE"));
                },
                error -> Toast.makeText(this, "Erreur chargement", Toast.LENGTH_SHORT).show()
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

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void validateForm() {
        String nom = etNom.getText().toString().trim();
        String prenom = etPrenom.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String adresse = etAdresse.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etMotDePasse.getText().toString().trim();
        String confirmPassword = etConfirmerMotDePasse.getText().toString().trim();

        if (nom.isEmpty() || prenom.isEmpty() || adresse.isEmpty() || ageStr.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs (mot de passe inclus)", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);
        if (age < 3 || age > 99) {
            Toast.makeText(this, "L'âge doit être compris entre 3 et 99 ans", Toast.LENGTH_SHORT).show();
            return;
        }

        String niveau = spinnerNiveau.getSelectedItem().toString();
        String morphologie = spinnerMorphologie.getSelectedItem().toString();

        envoyerMiseAJourAPI(nom, prenom, age, adresse, email, password, niveau, morphologie);
    }

    private void envoyerMiseAJourAPI(String nom, String prenom, int age, String adresse, String email, String password, String niveau, String morphologie) {
        TokenManager tokenManager = new TokenManager(this);
        final String token = tokenManager.getToken();

        if (token == null || token.isEmpty()) return;

        // Sécurité : Vérifier qu'on a bien récupéré l'ID
        if (currentUserId == null || currentUserId == -1) {
            Toast.makeText(this, "Erreur interne: ID utilisateur inconnu. Attendez la fin du chargement.", Toast.LENGTH_LONG).show();
            return;
        }

        String url = BASE_URL + currentUserId; // Format: /users/2

        JSONObject body = new JSONObject();
        try {
            body.put("nom", nom);
            body.put("prenom", prenom);
            body.put("adresse", adresse);
            body.put("age", age);
            body.put("mail", email);
            body.put("password", password);
            body.put("niveau", niveau);
            body.put("morphologie", morphologie);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        final String requestBody = body.toString();

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> {
                    Toast.makeText(this, "Profil mis à jour ✔", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, ProfilActivity.class));
                    finish();
                },
                error -> {
                    String message = "Erreur mise à jour";
                    if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                        message = "Format invalide (Vérifiez la complexité du mot de passe)";
                    } else if (error.networkResponse != null && error.networkResponse.statusCode == 409) {
                        message = "Cette adresse email est déjà utilisée !";
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public byte[] getBody() {
                return requestBody.getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }
}