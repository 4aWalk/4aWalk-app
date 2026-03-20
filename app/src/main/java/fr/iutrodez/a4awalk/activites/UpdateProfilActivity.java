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
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;

/**
 * Activité de modification du profil de l'utilisateur connecté.
 * Charge les données actuelles depuis l'API (/users/me), pré-remplit le formulaire,
 * puis envoie les modifications via une requête PUT authentifiée à /users/{id}.
 */
public class UpdateProfilActivity extends HeaderActivity {

    // URL de base pour les requêtes utilisateur (le PUT sera sur BASE_URL + id)
    private static final String BASE_URL = "http://98.94.8.220:8080/users/";

    // URL pour récupérer les données de l'utilisateur connecté (utilisé pour le pré-remplissage)
    private static final String ME_URL   = "http://98.94.8.220:8080/users/me";

    // Champs de saisie du formulaire de modification
    private TextInputEditText etNom, etPrenom, etAge, etAdresse, etEmail, etMotDePasse, etConfirmerMotDePasse;

    // Menus déroulants pour le niveau et la morphologie
    private Spinner spinnerNiveau, spinnerMorphologie;

    // File d'attente Volley pour les requêtes HTTP
    private RequestQueue requestQueue;

    // ID de l'utilisateur connecté, nécessaire pour construire l'URL du PUT (/users/{id})
    // Initialisé à null et renseigné après le chargement du profil
    private Long currentUserId = null;

    /**
     * Point d'entrée de l'activité.
     * Initialise les vues, charge le profil actuel et configure les boutons.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modification_compte);

        // Initialise la toolbar héritée de HeaderActivity
        configurerToolbar();

        // Initialise et configure les vues du formulaire
        initViews();

        // Initialise la file de requêtes Volley
        requestQueue = Volley.newRequestQueue(this);

        // Charge les données de l'utilisateur depuis l'API pour pré-remplir le formulaire
        chargerDonneesUtilisateur();

        // Bouton retour : navigue vers ProfilActivity sans sauvegarder
        Button btnRetour = findViewById(R.id.btn_retour);
        btnRetour.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfilActivity.class));
            finish();
        });

        // Bouton valider : déclenche la validation puis l'envoi des modifications à l'API
        Button btnValider = findViewById(R.id.btn_update_account);
        btnValider.setOnClickListener(v -> validateForm());
    }

    /**
     * Initialise les vues du formulaire et configure les Spinners
     * avec des valeurs statiques (les enums ne sont pas utilisés directement ici).
     */
    private void initViews() {
        // Liaison des champs texte avec leurs identifiants XML
        etNom = findViewById(R.id.up_nom);
        etPrenom = findViewById(R.id.up_prenom);
        etAge = findViewById(R.id.up_age);
        etAdresse = findViewById(R.id.up_adresse);
        etEmail = findViewById(R.id.up_email);
        etMotDePasse = findViewById(R.id.up_mot_de_passe);
        etConfirmerMotDePasse = findViewById(R.id.up_confirmer_mot_de_passe);

        spinnerNiveau = findViewById(R.id.spinner_update_niveau);
        spinnerMorphologie = findViewById(R.id.spinner_update_morphologie);

        // Alimente le Spinner niveau avec les 3 valeurs possibles
        ArrayAdapter<CharSequence> adapterNiveau = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"DEBUTANT", "ENTRAINE", "SPORTIF"});
        adapterNiveau.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNiveau.setAdapter(adapterNiveau);

        // Alimente le Spinner morphologie avec les 3 valeurs possibles
        ArrayAdapter<CharSequence> adapterMorpho = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"LEGERE", "MOYENNE", "FORTE"});
        adapterMorpho.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMorphologie.setAdapter(adapterMorpho);
    }

    /**
     * Envoie une requête GET authentifiée à /users/me pour récupérer les données actuelles
     * de l'utilisateur et pré-remplir tous les champs du formulaire.
     * Sauvegarde également l'ID utilisateur dans currentUserId pour le PUT ultérieur.
     */
    private void chargerDonneesUtilisateur() {
        TokenManager tokenManager = new TokenManager(this);
        final String token = tokenManager.getToken();

        // Ne fait rien si l'utilisateur n'est pas connecté
        if (token == null || token.isEmpty()) return;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, ME_URL, null,
                response -> {
                    // Sauvegarde l'ID pour pouvoir construire l'URL du PUT (/users/{id})
                    currentUserId = response.optLong("id", -1);

                    // Pré-remplissage des champs texte avec les données actuelles
                    etNom.setText(response.optString("nom", ""));
                    etPrenom.setText(response.optString("prenom", ""));
                    int age = response.optInt("age", 0);
                    // N'affiche pas "0" si l'âge est absent
                    etAge.setText(age > 0 ? String.valueOf(age) : "");
                    etAdresse.setText(response.optString("adresse", ""));
                    etEmail.setText(response.optString("mail", ""));

                    // Sélectionne la bonne valeur dans chaque Spinner
                    setSpinnerSelection(spinnerNiveau, response.optString("niveau", "DEBUTANT"));
                    setSpinnerSelection(spinnerMorphologie, response.optString("morphologie", "MOYENNE"));
                },
                error -> Toast.makeText(this, "Erreur chargement", Toast.LENGTH_SHORT).show()
        ) {
            /**
             * Injecte le token JWT dans le header Authorization de chaque requête.
             */
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    /**
     * Sélectionne dans un Spinner l'item dont la valeur textuelle correspond à 'value'.
     * La comparaison est insensible à la casse pour éviter les problèmes de format.
     *
     * @param spinner Le Spinner dans lequel effectuer la sélection.
     * @param value   La valeur à sélectionner (ex: "DEBUTANT").
     */
    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break; // Inutile de continuer une fois la valeur trouvée
            }
        }
    }

    /**
     * Valide les champs du formulaire avant d'envoyer les modifications.
     * Les contrôles effectués : champs obligatoires, concordance des mots de passe, plage d'âge.
     * Si tout est valide, délègue l'envoi à envoyerMiseAJourAPI().
     */
    private void validateForm() {
        // Récupération des valeurs saisies
        String nom = etNom.getText().toString().trim();
        String prenom = etPrenom.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String adresse = etAdresse.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etMotDePasse.getText().toString().trim();
        String confirmPassword = etConfirmerMotDePasse.getText().toString().trim();

        // Vérification que tous les champs obligatoires sont remplis (mot de passe inclus)
        if (nom.isEmpty() || prenom.isEmpty() || adresse.isEmpty() || ageStr.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs (mot de passe inclus)", Toast.LENGTH_LONG).show();
            return;
        }

        // Vérification de la concordance des deux champs mot de passe
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation de la plage d'âge (3 à 99 ans)
        int age = Integer.parseInt(ageStr);
        if (age < 3 || age > 99) {
            Toast.makeText(this, "L'âge doit être compris entre 3 et 99 ans", Toast.LENGTH_SHORT).show();
            return;
        }

        String niveau = spinnerNiveau.getSelectedItem().toString();
        String morphologie = spinnerMorphologie.getSelectedItem().toString();

        // Toutes les validations sont passées, on envoie les données à l'API
        envoyerMiseAJourAPI(nom, prenom, age, adresse, email, password, niveau, morphologie);
    }

    /**
     * Construit le corps JSON de la requête PUT et l'envoie à /users/{currentUserId}.
     * Gère les codes d'erreur HTTP spécifiques (400 = mot de passe, 409 = email déjà pris).
     *
     * @param nom        Nouveau nom.
     * @param prenom     Nouveau prénom.
     * @param age        Nouvel âge.
     * @param adresse    Nouvelle adresse.
     * @param email      Nouvel email.
     * @param password   Nouveau mot de passe.
     * @param niveau     Nouveau niveau (DEBUTANT, ENTRAINE, SPORTIF).
     * @param morphologie Nouvelle morphologie (LEGERE, MOYENNE, FORTE).
     */
    private void envoyerMiseAJourAPI(String nom, String prenom, int age, String adresse, String email, String password, String niveau, String morphologie) {
        TokenManager tokenManager = new TokenManager(this);
        final String token = tokenManager.getToken();

        if (token == null || token.isEmpty()) return;

        // Sécurité : on vérifie que l'ID a bien été récupéré lors du chargement du profil
        // currentUserId peut être null si l'API n'a pas encore répondu
        if (currentUserId == null || currentUserId == -1) {
            Toast.makeText(this, "Erreur interne: ID utilisateur inconnu. Attendez la fin du chargement.", Toast.LENGTH_LONG).show();
            return;
        }

        // Construction de l'URL cible : /users/{id} (ex: /users/2)
        String url = BASE_URL + currentUserId;

        // Construction du corps JSON de la requête PUT
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

        // Sérialise le JSONObject en chaîne pour l'envoyer dans le corps de la requête
        final String requestBody = body.toString();

        // StringRequest utilisé car l'API renvoie une réponse texte simple (pas JSON)
        StringRequest request = new StringRequest(Request.Method.PUT, url,
                // Succès : retour à la page profil après mise à jour
                response -> {
                    Toast.makeText(this, "Profil mis à jour ✔", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, ProfilActivity.class));
                    finish();
                },
                // Erreur : message adapté selon le code HTTP retourné par l'API
                error -> {
                    String message = "Erreur mise à jour";
                    if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                        // 400 : données invalides (ex: mot de passe trop simple)
                        message = "Format invalide (Vérifiez la complexité du mot de passe)";
                    } else if (error.networkResponse != null && error.networkResponse.statusCode == 409) {
                        // 409 : conflit (l'email est déjà associé à un autre compte)
                        message = "Cette adresse email est déjà utilisée !";
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
        ) {
            /**
             * Fournit le corps de la requête PUT en bytes.
             * Obligatoire car StringRequest ne supporte pas setBody() directement.
             */
            @Override
            public byte[] getBody() {
                return requestBody.getBytes();
            }

            /**
             * Déclare le type MIME du corps : JSON.
             * Indique à l'API comment interpréter les données reçues.
             */
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            /**
             * Injecte le token JWT et le Content-Type dans les headers de la requête.
             */
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Ajoute la requête à la file Volley pour exécution asynchrone
        requestQueue.add(request);
    }
}