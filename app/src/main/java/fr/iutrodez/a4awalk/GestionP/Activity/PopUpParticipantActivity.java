package fr.iutrodez.a4awalk.GestionP.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
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

import org.json.JSONObject;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.GestionP.Activity.Request.ParticipantRequest;
import fr.iutrodez.a4awalk.GestionP.Activity.Service.ParticipantService;
import fr.iutrodez.a4awalk.GestionP.Activity.Util.PopupUtil;
import fr.iutrodez.a4awalk.GestionP.Activity.Validator.ParticipantValidator;

public class PopUpParticipantActivity extends AppCompatActivity {

    // 🟢 HikeId en dur
    private static final int HIKE_ID = 2;

    // 🟢 Token en dur
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZW8uYmVjb2duZUBpdXQtcm9kZXouZnIiLCJ1c2VySWQiOjIsImlhdCI6MTc2OTQzMzY3MSwiZXhwIjoxNzY5NTIwMDcxfQ.URJXD_DJlK458jXPrRhsiZCe8wqArVmqDpWGHfuGfjQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_details_randonnee); // 🔴 Change ici le nom du layout si nécessaire

        ImageButton btnAddParticipant = findViewById(R.id.btn_add_participant);

        // 🟢 Rendre le bouton visible
        btnAddParticipant.setVisibility(android.view.View.VISIBLE);

        //Button btnSuppParticipant = findViewById(R.id.btnOpenPopup3);
        //Button btnSuppRandonnee = findViewById(R.id.btnOpenPopup2);
        //Button btnSuppParcour = findViewById(R.id.btnOpenPopup4);

        //btnSuppParticipant.setOnClickListener(v -> {
            //PopupUtil.showDeletePopup(
                    //PopUpParticipantActivity.this,
                    //"Êtes-vous sûr de supprimer le participant :",
                    //"Participant 1",
                    //null
            //);
        //});
/*
        btnSuppRandonnee.setOnClickListener(v -> {
            PopupUtil.showDeletePopup(
                    PopUpParticipantActivity.this,
                    "Êtes-vous sûr de vouloir supprimer cette randonnée ?",
                    "Randonnée 3",
                    new PopupUtil.PopupCallback() {
                        @Override
                        public void onValidate() {
                            PopupUtil.showDeletePopup(
                                    PopUpParticipantActivity.this,
                                    "Êtes-vous sûr de vouloir supprimer ce parcours ?",
                                    "Parcours A",
                                    null
                            );
                        }
                    }
            );
        });
/*
        btnSuppParcour.setOnClickListener(v -> {
            PopupUtil.showDeletePopup(
                    PopUpParticipantActivity.this,
                    "Êtes-vous sûr de vouloir supprimer ce parcour",
                    "Parcour A",
                    null
            );
        });

 */

        // 🟢 Ouverture de la popup depuis le ImageButton btn_add_participant
        btnAddParticipant.setOnClickListener(v -> {
            openParticipantPopup();
        });
    }

    // 🟢 Méthode extraite pour ouvrir la popup
    private void openParticipantPopup() {
        Dialog dialog = new Dialog(this);
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

        String[] liste1 = getResources().getStringArray(R.array.niveaux);
        String[] liste2 = getResources().getStringArray(R.array.morphologies);

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

        btnClose.setOnClickListener(view -> dialog.dismiss());

        btnVoirSac.setOnClickListener(view -> {
            Intent intent = new Intent(PopUpParticipantActivity.this, SacActivity.class);
            startActivity(intent);
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

            int ageInt = Integer.parseInt(age);
            Integer kcalInt = kcal.isEmpty() ? null : Integer.parseInt(kcal);
            Integer eauInt = eau.isEmpty() ? null : Integer.parseInt(eau);
            Double capaciteDouble = capacite.isEmpty() ? null : Double.parseDouble(capacite);

            try {
                JSONObject body = ParticipantRequest.build(
                        ageInt,
                        choixNiveau,
                        choixMorpho,
                        kcalInt,
                        eauInt,
                        capaciteDouble
                );

                ParticipantService service = new ParticipantService(PopUpParticipantActivity.this);
                service.addParticipant(
                        HIKE_ID,      // 🟢 ID en dur
                        body,
                        TOKEN,        // 🟢 TOKEN en dur
                        response -> {
                            Toast.makeText(PopUpParticipantActivity.this, "Participant ajouté !", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        },
                        error -> {
                            Toast.makeText(PopUpParticipantActivity.this, "Erreur : " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                );

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(PopUpParticipantActivity.this, "Erreur JSON", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}