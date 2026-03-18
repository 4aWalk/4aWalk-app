package fr.iutrodez.a4awalk.activites;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import fr.iutrodez.a4awalk.modeles.entites.LoginRequest;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceConnexion;
import fr.iutrodez.a4awalk.utils.validators.LoginValidator;
import fr.iutrodez.a4awalk.modeles.entites.ValidationResult;
import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;

/**
 * Activité de connexion (écran d'accueil de l'application).
 * Permet à l'utilisateur de se connecter avec son email et mot de passe,
 * ou de naviguer vers l'écran d'inscription.
 */
public class ActivitePrincipale extends AppCompatActivity {

    // Champs de saisie pour l'email et le mot de passe
    private EditText emailInput;
    private EditText passwordInput;

    // Bouton de connexion et bouton vers l'inscription
    private Button loginButton;
    private Button registerButton;

    /**
     * Point d'entrée de l'activité.
     * Initialise les vues et configure les listeners des deux boutons.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_connexion);

        // Liaison des vues avec leurs identifiants XML
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(v -> {

            // Récupération des valeurs saisies dans les champs
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // Validation des champs via le LoginValidator centralisé
            ValidationResult result = LoginValidator.validate(email, password);

            // Si la validation échoue, on affiche l'erreur sur le champ concerné
            if (!result.valid) {
                switch (result.field) {
                    case "email":
                        emailInput.setError(result.message);
                        emailInput.requestFocus();
                        break;

                    case "password":
                        passwordInput.setError(result.message);
                        passwordInput.requestFocus();
                        break;
                }
                // Interrompt le traitement si la validation a échoué
                return;
            }

            // Construit l'objet de requête de connexion avec les identifiants saisis
            LoginRequest loginRequest = new LoginRequest(email, password);

            // Envoie la requête de connexion à l'API via le service dédié
            ServiceConnexion.loginUser(
                    this,
                    loginRequest,
                    // Callback de succès : reçoit le token JWT et les données de l'utilisateur
                    (token, user) -> {
                        // Sauvegarde le token en local pour les requêtes suivantes
                        TokenManager tokenManager = new TokenManager(this);
                        tokenManager.saveToken(token);

                        // Navigue vers l'activité principale (liste des randonnées/parcours)
                        // en transmettant l'objet User pour éviter de le recharger
                        Intent intent = new Intent(ActivitePrincipale.this, ActiviteListes.class);
                        intent.putExtra("USER_DATA", user);
                        startActivity(intent);
                    },
                    // Callback d'erreur : affiche le message retourné par l'API
                    errorMsg -> Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            );
        });

        // Ouvre l'activité d'inscription sans passer de données supplémentaires
        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(this, ActiviteInscription.class));
        });
    }
}