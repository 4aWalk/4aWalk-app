package fr.iutrodez.a4awalk.GestionCompte;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import fr.iutrodez.a4awalk.R;

public class InscriptionActivity extends AppCompatActivity {

    private TextInputEditText etNom;
    private TextInputEditText etPrenom;
    private TextInputEditText etAge;
    private TextInputEditText etAdresse;
    private TextInputEditText etEmail;
    private TextInputEditText etMotDePasse;
    private TextInputEditText etConfirmerMotDePasse;
    private Spinner spinnerNiveau;
    private Spinner spinnerMorphologie;
    private Button btnCreateAccount;
    private Button btnRetour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inscription);

        // Initialisation des vues
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

        // Listener bouton créer compte
        btnCreateAccount.setOnClickListener(v -> {
            if (validateForm()) {
                Toast.makeText(this, "Compte créé avec succès !", Toast.LENGTH_SHORT).show();

                // Retour à la connexion
                finish();
            }
        });

        // Bouton Retour
        btnRetour.setOnClickListener(v -> finish());
    }

    private boolean validateForm() {

        String nom = etNom.getText().toString().trim();
        String prenom = etPrenom.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String adresse = etAdresse.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String motDePasse = etMotDePasse.getText().toString().trim();
        String confirmerMotDePasse = etConfirmerMotDePasse.getText().toString().trim();

        //Nom
        if (nom.isEmpty()) {
            etNom.setError("Veuillez entrer votre nom");
            etNom.requestFocus();
            return false;
        }

        //Prénom
        if (prenom.isEmpty()) {
            etPrenom.setError("Veuillez entrer votre prénom");
            etPrenom.requestFocus();
            return false;
        }

        //Âge
        if (age.isEmpty()) {
            etAge.setError("Veuillez entrer votre âge");
            etAge.requestFocus();
            return false;
        }

        //Adresse
        if (adresse.isEmpty()) {
            etAdresse.setError("Veuillez entrer votre adresse");
            etAdresse.requestFocus();
            return false;
        }

        // Email
        if (email.isEmpty()) {
            etEmail.setError("Veuillez entrer votre email");
            etEmail.requestFocus();
            return false;
        }

        //Mot de passe
        if (motDePasse.isEmpty()) {
            etMotDePasse.setError("Veuillez entrer un mot de passe");
            etMotDePasse.requestFocus();
            return false;
        }

        //Confirmation mot de passe
        if (confirmerMotDePasse.isEmpty()) {
            etConfirmerMotDePasse.setError("Veuillez confirmer le mot de passe");
            etConfirmerMotDePasse.requestFocus();
            return false;
        }

        //Email déjà existant
        if (email.equalsIgnoreCase("neo.becogne@iut-rodez.fr")) {
            etEmail.setError("Un compte avec cet email existe déjà");
            etEmail.requestFocus();
            return false;
        }

        // Vérification âge numérique
        try {
            int ageNum = Integer.parseInt(age);
            if (ageNum < 1 || ageNum > 120) {
                etAge.setError("Âge invalide");
                etAge.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etAge.setError("L'âge doit être un nombre");
            etAge.requestFocus();
            return false;
        }

        //Email invalide
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Veuillez entrer un email valide");
            etEmail.requestFocus();
            return false;
        }

        // Mot de passe trop court
        if (motDePasse.length() < 6) {
            etMotDePasse.setError("Au moins 6 caractères requis");
            etMotDePasse.requestFocus();
            return false;
        }

        // Mots de passe différents
        if (!motDePasse.equals(confirmerMotDePasse)) {
            etConfirmerMotDePasse.setError("Les mots de passe ne correspondent pas");
            etConfirmerMotDePasse.requestFocus();
            return false;
        }

        // Spinner niveau
        if (spinnerNiveau.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Veuillez choisir un niveau", Toast.LENGTH_SHORT).show();
            return false;
        }

        //Spinner morphologie
        if (spinnerMorphologie.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Veuillez choisir une morphologie", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


}
