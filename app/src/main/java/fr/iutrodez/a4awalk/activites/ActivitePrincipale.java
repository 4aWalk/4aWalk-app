package fr.iutrodez.a4awalk.activites;

import static android.os.Build.VERSION_CODES.R;

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

public class ActivitePrincipale extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_connexion);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(v -> {

            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            ValidationResult result = LoginValidator.validate(email, password);

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
                return;
            }

            LoginRequest loginRequest = new LoginRequest(email, password);

            ServiceConnexion.loginUser(
                    this,
                    loginRequest,
                    (token, user) -> {
                        TokenManager tokenManager = new TokenManager(this);
                        tokenManager.saveToken(token);
                        Intent intent = new Intent(ActivitePrincipale.this, ActiviteListes.class);
                        intent.putExtra("USER_DATA",user);
                        startActivity(intent);
                    },
                    errorMsg -> Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            );

        });

        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(this, ActiviteInscription.class));
        });
    }
}
