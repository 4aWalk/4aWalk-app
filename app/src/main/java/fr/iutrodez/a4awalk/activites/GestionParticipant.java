package fr.iutrodez.a4awalk.activites;

import static fr.iutrodez.a4awalk.services.gestionAPI.ServiceParticipant.creationParticipant;

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
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT)
        );

        ImageButton btnClose = dialog.findViewById(R.id.btnClose);
        Button btnVoirSac = dialog.findViewById(R.id.btnVoirSac);
        Button btnAjouter = dialog.findViewById(R.id.btnAjouter);

        Spinner spinner1 = dialog.findViewById(R.id.spinnerNiveau);
        Spinner spinner2 = dialog.findViewById(R.id.spinnerMorphologie);

        EditText etAge = dialog.findViewById(R.id.etAge);
        EditText etBesoinKcal = dialog.findViewById(R.id.etBesoinKcal);
        EditText etBesoinEau = dialog.findViewById(R.id.etBesoinEau);
        EditText etCapacite = dialog.findViewById(R.id.etCapacite);
        CheckBox cbSacADos = dialog.findViewById(R.id.cbSacADos);

        etCapacite.setEnabled(false);

        cbSacADos.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etCapacite.setEnabled(isChecked);
        });

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

        btnClose.setOnClickListener(view -> dialog.dismiss());

        btnVoirSac.setOnClickListener(view -> {
            Intent intent = new Intent(context, SacActivity.class);
            context.startActivity(intent);
        });

        btnAjouter.setOnClickListener(view -> {

            String age = etAge.getText().toString().trim();
            String kcal = etBesoinKcal.getText().toString().trim();
            String eau = etBesoinEau.getText().toString().trim();
            String capacite = etCapacite.getText().toString().trim();
            String choixNiveau = spinner1.getSelectedItem().toString();
            String choixMorpho = spinner2.getSelectedItem().toString();

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

            Log.i("verif", "Vérification faite");

            int ageInt = Integer.parseInt(age);
            Integer kcalInt = Integer.parseInt(kcal);
            Integer eauInt = Integer.parseInt(eau);
            double capaciteDouble;
            capaciteDouble = 0.0;
            if (cbSacADos.isChecked()) {
                capaciteDouble = capacite.isEmpty() ? null : Double.parseDouble(capacite);
            }

            Participant nouveauParticipant = creationParticipant(ageInt, Level.valueOf(choixNiveau), Morphology.valueOf(choixMorpho), kcalInt, eauInt, capaciteDouble);

            if (callback != null) {
                callback.onParticipantCreated(nouveauParticipant);
            }
            dialog.dismiss();
        });

        dialog.show();
    }
}
