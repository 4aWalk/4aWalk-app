package fr.iutrodez.a4awalk.activites;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import fr.iutrodez.a4awalk.GestionP.Activity.SacActivity;
import fr.iutrodez.a4awalk.GestionP.Activity.Validator.ParticipantValidator;
import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;

public class GestionParticipant {

    public interface ParticipantCallback {
        void onParticipantCreated(Participant participant);
    }

    /**
     * Gère l'affichage du dialogue pour les Participants.
     */
    public static void gererDialogParticipant(Context context, String token, ParticipantCallback callback) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup_participant);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Récupération des vues
        ImageButton btnClose = dialog.findViewById(R.id.btnClose);
        Button btnVoirSac = dialog.findViewById(R.id.btnVoirSac);
        Button btnAjouter = dialog.findViewById(R.id.btnAjouter);

        Spinner spinner1 = dialog.findViewById(R.id.spinnerNiveau);
        Spinner spinner2 = dialog.findViewById(R.id.spinnerMorphologie);

        // --- NOUVEAUX CHAMPS NOM / PRENOM ---
        // Assurez-vous que ces ID existent dans popup_participant.xml
        EditText etNom = dialog.findViewById(R.id.etNom);
        EditText etPrenom = dialog.findViewById(R.id.etPrenom);
        // ------------------------------------

        EditText etAge = dialog.findViewById(R.id.etAge);
        EditText etBesoinKcal = dialog.findViewById(R.id.etBesoinKcal);
        EditText etBesoinEau = dialog.findViewById(R.id.etBesoinEau);
        EditText etCapacite = dialog.findViewById(R.id.etCapacite);
        CheckBox cbSacADos = dialog.findViewById(R.id.cbSacADos);

        etCapacite.setEnabled(false);

        cbSacADos.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etCapacite.setEnabled(isChecked);
        });

        // Configuration des Spinners (Code inchangé)
        String[] liste1 = context.getResources().getStringArray(R.array.niveaux);
        String[] liste2 = context.getResources().getStringArray(R.array.morphologies);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, liste1) {
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

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, liste2) {
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

        // Listeners
        btnClose.setOnClickListener(view -> dialog.dismiss());

        btnVoirSac.setOnClickListener(view -> {
            Intent intent = new Intent(context, SacActivity.class);
            context.startActivity(intent);
        });

        btnAjouter.setOnClickListener(view -> {
            // Récupération des valeurs String
            String nom = (etNom != null) ? etNom.getText().toString().trim() : "";
            String prenom = (etPrenom != null) ? etPrenom.getText().toString().trim() : "";

            String age = etAge.getText().toString().trim();
            String kcal = etBesoinKcal.getText().toString().trim();
            String eau = etBesoinEau.getText().toString().trim();
            String capacite = etCapacite.getText().toString().trim();
            String choixNiveau = spinner1.getSelectedItem().toString();
            String choixMorpho = spinner2.getSelectedItem().toString();

            // Validation (Ajouter la validation Nom/Prénom si nécessaire dans votre Validator)
            boolean isValid = ParticipantValidator.validate(
                    etAge,
                    etBesoinKcal,
                    etBesoinEau,
                    etCapacite,
                    spinner1,
                    spinner2,
                    cbSacADos.isChecked()
            );

            if (!isValid) return;

            // Validation sommaire pour Nom/Prénom (si non géré par le validator)
            if (nom.isEmpty() || prenom.isEmpty()) {
                Toast.makeText(context, "Nom et Prénom requis", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.i("verif", "Vérification faite");

            int ageInt = Integer.parseInt(age);
            Integer kcalInt = Integer.parseInt(kcal);
            Integer eauInt = Integer.parseInt(eau);
            double capaciteDouble = 0.0;
            if (cbSacADos.isChecked() && !capacite.isEmpty()) {
                capaciteDouble = Double.parseDouble(capacite);
            }

            // Création directe via le constructeur (plus sûr si ServiceParticipant n'est pas à jour)
            Participant nouveauParticipant = new Participant(
                    nom,
                    prenom,
                    ageInt,
                    Level.valueOf(choixNiveau),
                    Morphology.valueOf(choixMorpho),
                    false, // isCreator par défaut false ici
                    kcalInt,
                    eauInt,
                    capaciteDouble
            );

            if (callback != null) {
                callback.onParticipantCreated(nouveauParticipant);
            }
            dialog.dismiss();
        });

        dialog.show();
    }
}