package fr.iutrodez.a4awalk;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connexion); // Assure-toi que le layout est correct

        // Initialisation des vues
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        // Listener bouton Se connecter
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            } else if (!email.equals("neo.becogne@iut-rodez.fr") || !password.equals("12345")) {
                Toast.makeText(MainActivity.this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Connexion réussie !", Toast.LENGTH_SHORT).show();
                // Ici tu peux ajouter la logique après connexion réussie
            }
        });

        // Listener bouton S'inscrire
        registerButton.setOnClickListener(v -> {
            // Rediriger vers l'activité d'inscription
            Toast.makeText(MainActivity.this, "Bouton S'inscrire cliqué", Toast.LENGTH_SHORT).show();
        });
    }
}
