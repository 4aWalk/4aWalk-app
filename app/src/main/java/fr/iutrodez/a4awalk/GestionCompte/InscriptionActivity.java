package fr.iutrodez.a4awalk.GestionCompte;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import fr.iutrodez.a4awalk.GestionCompte.Service.RegisterService;
import fr.iutrodez.a4awalk.GestionCompte.Validator.ValidationResult;
import fr.iutrodez.a4awalk.GestionCompte.Validator.Validator;
import fr.iutrodez.a4awalk.R;

public class InscriptionActivity extends AppCompatActivity {

    private TextInputEditText etNom, etPrenom, etAge, etAdresse, etEmail, etMotDePasse, etConfirmerMotDePasse;
    private Spinner spinnerNiveau, spinnerMorphologie;
    private Button btnCreateAccount, btnRetour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inscription);

        initViews();

        btnCreateAccount.setOnClickListener(v -> {

            String nom = etNom.getText().toString().trim();
            String prenom = etPrenom.getText().toString().trim();
            String ageStr = etAge.getText().toString().trim();
            String adresse = etAdresse.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etMotDePasse.getText().toString().trim();
            String confirmPassword = etConfirmerMotDePasse.getText().toString().trim();
            String niveau = spinnerNiveau.getSelectedItem().toString();
            String morphologie = spinnerMorphologie.getSelectedItem().toString();

            ValidationResult result = Validator.validate(
                    nom, prenom, ageStr, adresse, email, password, confirmPassword, niveau, morphologie
            );

            if (!result.valid) {
                switch (result.field) {
                    case "nom":
                        etNom.setError(result.message);
                        etNom.requestFocus();
                        break;

                    case "prenom":
                        etPrenom.setError(result.message);
                        etPrenom.requestFocus();
                        break;

                    case "age":
                        etAge.setError(result.message);
                        etAge.requestFocus();
                        break;

                    case "adresse":
                        etAdresse.setError(result.message);
                        etAdresse.requestFocus();
                        break;

                    case "email":
                        etEmail.setError(result.message);
                        etEmail.requestFocus();
                        break;

                    case "password":
                        etMotDePasse.setError(result.message);
                        etMotDePasse.requestFocus();
                        break;

                    case "confirmPassword":
                        etConfirmerMotDePasse.setError(result.message);
                        etConfirmerMotDePasse.requestFocus();
                        break;

                    case "niveau":
                    case "morphologie":
                        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                        break;
                }
                return;
            }

            User user = new User(
                    nom,
                    prenom,
                    result.age,
                    adresse,
                    email,
                    password,
                    niveau,
                    morphologie
            );

            RegisterService.registerUser(this, user,
                    () -> finish(),
                    msg -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            );
        });

        btnRetour.setOnClickListener(v -> finish());
    }

    private void initViews() {
        etNom = findViewById(R.id.et_nom);
        etPrenom = findViewById(R.id.et_prenom);
        etAge = findViewById(R.id.et_age);
        etAdresse = findViewById(R.id.et_adresse);
        etEmail = findViewById(R.id.et_email);
        etMotDePasse = findViewById(R.id.et_mot_de_passe);
        etConfirmerMotDePasse = findViewById(R.id.et_confirmer_mot_de_passe);
        spinnerNiveau = findViewById(R.id.spinner_niveau);
        spinnerMorphologie = findViewById(R.id.spinner_morphologie);
        btnCreateAccount = findViewById(R.id.btn_create_account);
        btnRetour = findViewById(R.id.btn_retour);
    }
}
