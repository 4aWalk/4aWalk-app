package fr.iutrodez.a4awalk.activites;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.ValidationResult;
import fr.iutrodez.a4awalk.utils.validators.Validator;;

public class UpdateProfilActivity extends HeaderActivity {

    // ===== Constantes API =====
    private static final String BASE_URL = "http://98.94.8.220:8080/users/";
    private static final String ME_URL   = "http://98.94.8.220:8080/users/me";
    private static final String TOKEN_KEY = "auth_token";
    private static final String USER_ID_KEY = "user_id";

    private Toolbar toolbar;

    // Champs texte
    private TextInputEditText etNom, etPrenom, etAge, etAdresse, etEmail;
    private TextInputEditText etMotDePasse, etConfirmerMotDePasse;

    // Spinners
    private Spinner spinnerNiveau, spinnerMorphologie;

    // Volley
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modification_compte);

        configurerToolbar();

        // ===== Initialisation =====
        initViews();

        requestQueue = Volley.newRequestQueue(this);

        // On charge les données depuis l'API, puis on remplit le formulaire
        chargerDonneesUtilisateur();

        // ===== Bouton Retour =====
        Button btnRetour = findViewById(R.id.btn_retour);
        btnRetour.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfilActivity.class));
            finish();
        });

        // ===== Bouton Valider =====
        Button btnValider = findViewById(R.id.btn_update_account);
        btnValider.setOnClickListener(v -> validateForm());
    }

    // ------------------------------------------------------------------
    // Initialisation
    // ------------------------------------------------------------------

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
    }

    // ------------------------------------------------------------------
    // Récupération des données utilisateur via GET /users/me
    // ------------------------------------------------------------------

    private void chargerDonneesUtilisateur() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String token = prefs.getString(TOKEN_KEY, null);

        if (token == null) {
            Toast.makeText(this, "Session expirée, veuillez vous reconnecter", Toast.LENGTH_LONG).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.GET, ME_URL,
                response -> {
                    try {
                        JSONObject user = new JSONObject(response);
                        remplirFormulaire(user);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur lors de la lecture des données", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String message = "Impossible de charger le profil";
                    if (error.networkResponse != null) {
                        switch (error.networkResponse.statusCode) {
                            case 401: message = "Non autorisé, veuillez vous reconnecter"; break;
                            case 404: message = "Utilisateur introuvable"; break;
                        }
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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

    // ------------------------------------------------------------------
    // Remplissage du formulaire à partir de la réponse JSON
    // ------------------------------------------------------------------

    private void remplirFormulaire(JSONObject user) throws JSONException {
        etNom.setText(user.optString("nom", ""));
        etPrenom.setText(user.optString("prenom", ""));
        etAge.setText(user.optInt("age", 0) != 0 ? String.valueOf(user.optInt("age")) : "");
        etAdresse.setText(user.optString("adresse", ""));
        etEmail.setText(user.optString("mail", ""));
        etMotDePasse.setText("");
        etConfirmerMotDePasse.setText("");

        setSpinnerSelection(spinnerNiveau, user.optString("niveau", ""), "niveau");
        setSpinnerSelection(spinnerMorphologie, user.optString("morphologie", ""), "morphologie");
    }

    // ------------------------------------------------------------------
    // Configuration des spinners
    // ------------------------------------------------------------------

    private void setSpinnerSelection(Spinner spinner, String value, String type) {
        ArrayAdapter<CharSequence> adapter;
        if (type.equals("niveau")) {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                    new String[]{"DEBUTANT", "ENTRAINE", "SPORTIF"});
        } else if (type.equals("morphologie")) {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                    new String[]{"LEGERE", "MOYENNE", "FORTE"});
        } else {
            return;
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        int position = -1;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                position = i;
                break;
            }
        }
        if (position >= 0) spinner.setSelection(position);
    }

    // ------------------------------------------------------------------
    // Validation du formulaire
    // ------------------------------------------------------------------

    private void validateForm() {
        clearErrors();

        String nom = getText(etNom);
        String prenom = getText(etPrenom);
        String age = getText(etAge);
        String adresse = getText(etAdresse);
        String email = getText(etEmail);
        String password = getText(etMotDePasse);
        String confirmPassword = getText(etConfirmerMotDePasse);

        String niveau = spinnerNiveau.getSelectedItem().toString();
        String morphologie = spinnerMorphologie.getSelectedItem().toString();

        ValidationResult result = Validator.validate(
                nom, prenom, age, adresse, email, password, confirmPassword, niveau, morphologie
        );

        if (!result.valid) {
            showValidationError(result);
            return;
        }

        envoyerMiseAJourAPI(nom, prenom, age, adresse, email, password, niveau, morphologie);
    }

    // ------------------------------------------------------------------
    // Appel API PUT /users/{id}
    // ------------------------------------------------------------------

    private void envoyerMiseAJourAPI(String nom, String prenom, String age, String adresse,
                                     String email, String password,
                                     String niveau, String morphologie) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userId = prefs.getString(USER_ID_KEY, null);
        final String token = prefs.getString(TOKEN_KEY, null);

        if (userId == null || token == null) {
            Toast.makeText(this, "Session expirée, veuillez vous reconnecter", Toast.LENGTH_LONG).show();
            return;
        }

        String url = BASE_URL + userId;

        JSONObject body = new JSONObject();
        try {
            body.put("mail", email);
            body.put("password", password);
            body.put("nom", nom);
            body.put("prenom", prenom);
            body.put("adresse", adresse);
            body.put("age", Integer.parseInt(age));
            body.put("niveau", niveau);
            body.put("morphologie", morphologie);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de la préparation des données", Toast.LENGTH_SHORT).show();
            return;
        }

        final String requestBody = body.toString();

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> {
                    Toast.makeText(this, "Profil mis à jour avec succès ✔", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, ProfilActivity.class));
                    finish();
                },
                error -> {
                    String message = "Erreur lors de la mise à jour";
                    if (error.networkResponse != null) {
                        switch (error.networkResponse.statusCode) {
                            case 400: message = "Données invalides"; break;
                            case 401: message = "Non autorisé, veuillez vous reconnecter"; break;
                            case 409: message = "Cette adresse email est déjà utilisée"; break;
                        }
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

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString() : "";
    }

    // ------------------------------------------------------------------
    // Gestion des erreurs de validation
    // ------------------------------------------------------------------

    private void showValidationError(ValidationResult result) {
        switch (result.field) {
            case "nom": etNom.setError(result.message); etNom.requestFocus(); break;
            case "prenom": etPrenom.setError(result.message); etPrenom.requestFocus(); break;
            case "age": etAge.setError(result.message); etAge.requestFocus(); break;
            case "adresse": etAdresse.setError(result.message); etAdresse.requestFocus(); break;
            case "email": etEmail.setError(result.message); etEmail.requestFocus(); break;
            case "password": etMotDePasse.setError(result.message); etMotDePasse.requestFocus(); break;
            case "confirmPassword": etConfirmerMotDePasse.setError(result.message); etConfirmerMotDePasse.requestFocus(); break;
            case "niveau": spinnerNiveau.requestFocus(); Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show(); break;
            case "morphologie": spinnerMorphologie.requestFocus(); Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show(); break;
        }
    }

    private void clearErrors() {
        etNom.setError(null); etPrenom.setError(null); etAge.setError(null);
        etAdresse.setError(null); etEmail.setError(null);
        etMotDePasse.setError(null); etConfirmerMotDePasse.setError(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_header, menu);
        return true;
    }
}