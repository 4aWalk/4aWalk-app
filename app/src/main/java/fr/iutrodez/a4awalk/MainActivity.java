package fr.iutrodez.a4awalk;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

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
            Button btnValider = dialog.findViewById(R.id.btnValider);

            // --- Spinners du popup ---
            Spinner spinner1 = dialog.findViewById(R.id.spinnerNiveau);
            Spinner spinner2 = dialog.findViewById(R.id.spinnerMorphologie);

            // --- Données des spinners ---
            String[] liste1 = getResources().getStringArray(R.array.niveaux);
            String[] liste2 = getResources().getStringArray(R.array.morphologies);

            // --- Adaptateur 1 avec texte noir ---
            ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, liste1) {
                @Override
                public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    view.setTextColor(Color.BLACK);       // texte affiché choisi
                    view.setBackgroundColor(Color.TRANSPARENT); // fond transparent pour ne pas écraser la zone
                    return view;
                }

                @Override
                public android.view.View getDropDownView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                    TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                    view.setTextColor(Color.BLACK);       // texte dans la liste déroulante
                    return view;
                }
            };
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner1.setAdapter(adapter1);


            // --- Adaptateur 2 avec texte noir ---
            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, liste2) {
                @Override
                public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    view.setTextColor(Color.BLACK);       // texte affiché choisi
                    view.setBackgroundColor(Color.TRANSPARENT); // fond transparent pour ne pas écraser la zone
                    return view;
                }

                @Override
                public android.view.View getDropDownView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                    TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                    view.setTextColor(Color.BLACK);       // texte dans la liste déroulante
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

            btnValider.setOnClickListener(view -> {
                String choix1 = spinner1.getSelectedItem().toString();
                String choix2 = spinner2.getSelectedItem().toString();

                Toast.makeText(this,
                        "Participant ajouté\nChoix : " + choix1 + " / " + choix2,
                        Toast.LENGTH_SHORT).show();
            });

            dialog.show();
        });
    }
}
