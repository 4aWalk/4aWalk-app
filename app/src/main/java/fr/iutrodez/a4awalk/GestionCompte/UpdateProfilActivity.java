package fr.iutrodez.a4awalk.GestionCompte;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.GestionCompte.Validator.Validator;
import fr.iutrodez.a4awalk.GestionCompte.Validator.ValidationResult;

public class UpdateProfilActivity extends AppCompatActivity {

    private Toolbar toolbar;

    // Champs texte
    private TextInputEditText etNom, etPrenom, etAge, etAdresse, etEmail;
    private TextInputEditText etMotDePasse, etConfirmerMotDePasse;

    // Spinners
    private Spinner spinnerNiveau, spinnerMorphologie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modification_compte);

        // ===== Toolbar =====
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        // ===== Initialisation des vues =====
        initViews();

        // ===== Pré-remplissage (données en dur) =====
        fillFormWithHardcodedData();

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
    // Pré-remplissage
    // ------------------------------------------------------------------

    private void fillFormWithHardcodedData() {

        etNom.setText("Dupont");
        etPrenom.setText("Jean");
        etAge.setText("23");
        etAdresse.setText("12 rue des Lilas, 12000 Rodez");
        etEmail.setText("jean.dupont@gmail.com");

        // ⚠️ Bonne pratique : pas de mot de passe pré-rempli
        etMotDePasse.setText("");
        etConfirmerMotDePasse.setText("");

        // Spinner Niveau
        setSpinnerSelection(
                spinnerNiveau,
                R.array.niveau_array,
                "Intermédiaire"
        );

        // Spinner Morphologie
        setSpinnerSelection(
                spinnerMorphologie,
                R.array.morphologie_array,
                "Athlétique"
        );
    }

    private void setSpinnerSelection(Spinner spinner, int arrayRes, String value) {
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this,
                        arrayRes,
                        android.R.layout.simple_spinner_item
                );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        int position = adapter.getPosition(value);
        if (position >= 0) {
            spinner.setSelection(position); // ← c'est ici que tu choisis l'item
        }
    }


    // ------------------------------------------------------------------
    // Validation
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
                nom,
                prenom,
                age,
                adresse,
                email,
                password,
                confirmPassword,
                niveau,
                morphologie
        );

        if (!result.valid) {
            showValidationError(result);
            return;
        }

        // ✅ Tout est valide
        Toast.makeText(this, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();

        int ageInt = result.age; // déjà parsé
        // TODO : appel API / sauvegarde
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString() : "";
    }

    // ------------------------------------------------------------------
    // Gestion des erreurs
    // ------------------------------------------------------------------

    private void showValidationError(ValidationResult result) {

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
                spinnerNiveau.requestFocus();
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                break;

            case "morphologie":
                spinnerMorphologie.requestFocus();
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void clearErrors() {
        etNom.setError(null);
        etPrenom.setError(null);
        etAge.setError(null);
        etAdresse.setError(null);
        etEmail.setError(null);
        etMotDePasse.setError(null);
        etConfirmerMotDePasse.setError(null);
    }

    // ------------------------------------------------------------------
    // Menu
    // ------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_header, menu);
        return true;
    }
}
