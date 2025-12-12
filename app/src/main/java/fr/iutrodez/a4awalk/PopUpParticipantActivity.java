package fr.iutrodez.a4awalk;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class PopUpParticipantActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test); // ton layout principal

        Button btnOpen = findViewById(R.id.btnOpenPopup);

        btnOpen.setOnClickListener(v -> {

            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.popup_participant);
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );

            // --- Boutons du popup ---
            ImageButton btnClose = dialog.findViewById(R.id.btnClose);
            Button btnVoirSac = dialog.findViewById(R.id.btnVoirSac);
            Button btnAjouter = dialog.findViewById(R.id.btnAjouter);

            // --- Spinners du popup ---
            Spinner spinner1 = dialog.findViewById(R.id.spinnerNiveau);
            Spinner spinner2 = dialog.findViewById(R.id.spinnerMorphologie);

            // --- Champs supplémentaires ---
            EditText etAge = dialog.findViewById(R.id.etAge);
            EditText etBesoinKcal = dialog.findViewById(R.id.etBesoinKcal);
            EditText etBesoinEau = dialog.findViewById(R.id.etBesoinEau);
            EditText etCapacite = dialog.findViewById(R.id.etCapacite);
            CheckBox cbSacADos = dialog.findViewById(R.id.cbSacADos);

            // Désactiver capacité sac au départ
            etCapacite.setEnabled(false);

            cbSacADos.setOnCheckedChangeListener((buttonView, isChecked) -> {
                etCapacite.setEnabled(isChecked);
            });

            // --- Données des spinners ---
            String[] liste1 = getResources().getStringArray(R.array.niveaux);
            String[] liste2 = getResources().getStringArray(R.array.morphologies);

            // --- Adaptateur 1 avec texte noir ---
            ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, liste1) {
                @Override
                public TextView getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    view.setTextColor(Color.BLACK);
                    view.setBackgroundColor(Color.TRANSPARENT);
                    return view;
                }

                @Override
                public TextView getDropDownView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                    TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                    view.setTextColor(Color.BLACK);
                    return view;
                }
            };
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner1.setAdapter(adapter1);

            // --- Adaptateur 2 avec texte noir ---
            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, liste2) {
                @Override
                public TextView getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    view.setTextColor(Color.BLACK);
                    view.setBackgroundColor(Color.TRANSPARENT);
                    return view;
                }

                @Override
                public TextView getDropDownView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                    TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                    view.setTextColor(Color.BLACK);
                    return view;
                }
            };
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner2.setAdapter(adapter2);

            // --- Événements ---
            btnClose.setOnClickListener(view -> dialog.dismiss());

            btnVoirSac.setOnClickListener(view ->
                    Toast.makeText(this, "Ouverture du sac...", Toast.LENGTH_SHORT).show()
            );

            // --- Bouton valider avec vérifications ---
            btnAjouter.setOnClickListener(view -> {

                String age = etAge.getText().toString().trim();
                String kcal = etBesoinKcal.getText().toString().trim();
                String eau = etBesoinEau.getText().toString().trim();
                String capacite = etCapacite.getText().toString().trim();
                String choixNiveau = spinner1.getSelectedItem().toString();
                String choixMorpho = spinner2.getSelectedItem().toString();

                // Vérifications des EditText
                if (age.isEmpty()) {
                    etAge.setError("Veuillez entrer l'âge");
                    etAge.requestFocus();
                    return;
                }

                if (kcal.isEmpty()) {
                    etBesoinKcal.setError("Veuillez entrer le besoin calorique");
                    etBesoinKcal.requestFocus();
                    return;
                }

                if (eau.isEmpty()) {
                    etBesoinEau.setError("Veuillez entrer le besoin en eau");
                    etBesoinEau.requestFocus();
                    return;
                }

                if (cbSacADos.isChecked() && capacite.isEmpty()) {
                    etCapacite.setError("Veuillez entrer la capacité du sac");
                    etCapacite.requestFocus();
                    return;
                }

                // Vérifications des spinners
                if (spinner1.getSelectedItemPosition() == 0) {
                    Toast.makeText(this, "Veuillez choisir un niveau valide", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (spinner2.getSelectedItemPosition() == 0) {
                    Toast.makeText(this, "Veuillez choisir une morphologie valide", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Tout est correct
                Toast.makeText(this,
                        "Participant ajouté\nÂge : " + age +
                                "\nNiveau : " + choixNiveau +
                                "\nMorphologie : " + choixMorpho +
                                "\nKcal : " + kcal +
                                "\nEau : " + eau +
                                (cbSacADos.isChecked() ? "\nSac : " + capacite + " kg" : ""),
                        Toast.LENGTH_LONG).show();

                dialog.dismiss();
            });

            dialog.show();
        });
    }
}
