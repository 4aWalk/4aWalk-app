package fr.iutrodez.a4awalk.activites;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.a4awalk.services.gestionAPI.ServiceInscription;
import fr.iutrodez.a4awalk.modeles.entites.ValidationResult;
import fr.iutrodez.a4awalk.utils.validators.Validator;
import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;

public class ActiviteInscription extends AppCompatActivity {

    private TextInputEditText etNom, etPrenom, etAge, etAdresse, etEmail, etMotDePasse, etConfirmerMotDePasse;
    private Spinner spinnerNiveau, spinnerMorphologie;
    private Button btnCreateAccount, btnRetour;
    ArrayAdapter<String> adapterNiveau;
    ArrayAdapter<String> adapterMorphologie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_inscription);

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
                    email,
                    password,
                    adresse,
                    Level.valueOf(niveau),
                    Morphology.valueOf(morphologie)
            );

            ServiceInscription.registerUser(this, user,
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

        List<String> niveauList = new ArrayList<>();
        niveauList.add("Choisissez votre niveau");
        for (Level level : Level.values()) {
            niveauList.add(level.name());
        }

        // Récupère toutes les valeurs de l'enum Level
        adapterNiveau = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                niveauList
        );
        adapterNiveau.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNiveau.setAdapter(adapterNiveau);

        List<String> morphoList = new ArrayList<>();
        morphoList.add("Choisissez votre morphologie");
        for (Morphology morph : Morphology.values()) {
            morphoList.add(morph.name());
        }

        // Récupère toutes les valeurs de l'enum Morphology
        adapterMorphologie = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                morphoList
        );
        adapterMorphologie.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMorphologie.setAdapter(adapterMorphologie);
    }
}
