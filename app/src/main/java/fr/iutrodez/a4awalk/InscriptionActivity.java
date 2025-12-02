package fr.iutrodez.a4awalk;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

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
            }
        });

        // Listener bouton retour
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

        StringBuilder champsManquants = new StringBuilder();

        if (nom.isEmpty()) champsManquants.append("Nom, ");
        if (prenom.isEmpty()) champsManquants.append("Prénom, ");
        if (age.isEmpty()) champsManquants.append("Âge, ");
        if (adresse.isEmpty()) champsManquants.append("Adresse, ");
        if (email.isEmpty()) champsManquants.append("Email, ");
        if (motDePasse.isEmpty()) champsManquants.append("Mot de passe, ");
        if (confirmerMotDePasse.isEmpty()) champsManquants.append("Confirmation mot de passe, ");

        // Si certains champs sont manquants
        if (champsManquants.length() > 0) {
            // Supprime la dernière virgule et espace
            champsManquants.setLength(champsManquants.length() - 2);
            Toast.makeText(this, "Veuillez remplir les champs suivants : " + champsManquants.toString(), Toast.LENGTH_LONG).show();
            return false;
        }

        // Vérification email valide
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Veuillez entrer un email valide", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return false;
        }

        // Vérification mot de passe
        if (motDePasse.length() < 6) {
            Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show();
            etMotDePasse.requestFocus();
            return false;
        }

        if (!motDePasse.equals(confirmerMotDePasse)) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            etConfirmerMotDePasse.requestFocus();
            return false;
        }

        return true;
    }

}
