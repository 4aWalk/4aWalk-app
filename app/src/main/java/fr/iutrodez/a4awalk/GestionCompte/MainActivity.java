package fr.iutrodez.a4awalk.GestionCompte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import fr.iutrodez.a4awalk.R;

public class MainActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connexion);

        // Initialisation des vues
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        // Bouton SE CONNECTER
        loginButton.setOnClickListener(v -> {

            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            //Vérification email
            if (email.isEmpty()) {
                emailInput.setError("Veuillez entrer votre email");
                emailInput.requestFocus();
                return;
            }

            //Vérification mot de passe
            if (password.isEmpty()) {
                passwordInput.setError("Veuillez entrer votre mot de passe");
                passwordInput.requestFocus();
                return;
            }

            //Identifiants incorrects
            if (!email.equals("neo.becogne@iut-rodez.fr") || !password.equals("12345")) {
                Toast.makeText(
                        MainActivity.this,
                        "Email ou mot de passe incorrect",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // ✅ Connexion réussie
            Toast.makeText(
                    MainActivity.this,
                    "Connexion réussie !",
                    Toast.LENGTH_SHORT
            ).show();

            // TODO : redirection vers une autre activité
        });

        // Bouton S'INSCRIRE
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InscriptionActivity.class);
            startActivity(intent);
        });
    }
}
