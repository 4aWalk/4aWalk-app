package fr.iutrodez.a4awalk.activites;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONObject;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.EquipmentItem;
import fr.iutrodez.a4awalk.modeles.enums.TypeEquipment;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceEquipment;
import fr.iutrodez.a4awalk.utils.validators.ValidateurEquipement;

public class PopUpEquipment {

    /**
     * Affiche une popup en lecture seule avec les détails d'un équipement.
     */
    public static void afficherPopupDetailsEquipment(Context context, EquipmentItem equipment) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup_ajout_equipment); // Assure-toi d'avoir ce layout !

        TextView tvTitre = dialog.findViewById(R.id.tv_titre_popup_eq);
        if (tvTitre != null) tvTitre.setText("Détails de l'équipement");

        EditText etNom = dialog.findViewById(R.id.et_eq_nom_create);
        EditText etMasse = dialog.findViewById(R.id.et_eq_masse_create);
        EditText etDescription = dialog.findViewById(R.id.et_eq_desc_create);
        EditText etMasseAVide = dialog.findViewById(R.id.et_eq_masse_vide_create);
        Spinner spinnerType = dialog.findViewById(R.id.spinner_eq_type_create);
        Spinner spinnerNbItem = dialog.findViewById(R.id.spinner_eq_nb_item_create);

        Button btnAnnuler = dialog.findViewById(R.id.btn_eq_annuler_create);
        Button btnValider = dialog.findViewById(R.id.btn_eq_valider_create);

        // Remplissage des champs
        etNom.setText(equipment.getNom());
        etMasse.setText(String.valueOf(equipment.getMasseGrammes()));
        etDescription.setText(equipment.getDescription() != null ? equipment.getDescription() : "");
        etMasseAVide.setText(String.valueOf(equipment.getMasseAVide()));

        // Spinner pour le type d'équipement
        TypeEquipment[] types = TypeEquipment.values();
        ArrayAdapter<TypeEquipment> typeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        int spinnerPosition = typeAdapter.getPosition(equipment.getType());
        spinnerType.setSelection(spinnerPosition);

        // Spinner pour la quantité
        Integer[] items = new Integer[]{1, 2, 3, 4, 5, 10}; // Adapté pour les équipements
        ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNbItem.setAdapter(spinnerAdapter);

        // Sélection simple de la quantité
        for (int i = 0; i < items.length; i++) {
            if (items[i] == equipment.getNbItem()) {
                spinnerNbItem.setSelection(i);
                break;
            }
        }

        // Verrouillage des champs (Mode Consultation)
        etNom.setEnabled(false);
        etMasse.setEnabled(false);
        etDescription.setEnabled(false);
        etMasseAVide.setEnabled(false);
        spinnerType.setEnabled(false);
        spinnerNbItem.setEnabled(false);

        // Configuration des boutons
        btnAnnuler.setVisibility(View.GONE);
        btnValider.setText("Fermer");
        btnValider.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    /**
     * Affiche une popup pour ajouter un nouvel équipement au catalogue.
     */
    public static void afficherPopupAjoutEquipment(Context context, String token, Runnable onSuccessCallback) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup_ajout_equipment);

        EditText etNom = dialog.findViewById(R.id.et_eq_nom_create);
        EditText etMasse = dialog.findViewById(R.id.et_eq_masse_create);
        EditText etDescription = dialog.findViewById(R.id.et_eq_desc_create);
        EditText etMasseAVide = dialog.findViewById(R.id.et_eq_masse_vide_create);
        Spinner spinnerType = dialog.findViewById(R.id.spinner_eq_type_create);
        Spinner spinnerNbItem = dialog.findViewById(R.id.spinner_eq_nb_item_create);

        Button btnAnnuler = dialog.findViewById(R.id.btn_eq_annuler_create);
        Button btnValider = dialog.findViewById(R.id.btn_eq_valider_create);

        // Remplissage Spinner Types
        TypeEquipment[] types = TypeEquipment.values();
        ArrayAdapter<TypeEquipment> typeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Remplissage Spinner Quantités
        Integer[] items = new Integer[]{1, 2, 3, 4, 5, 10};
        ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNbItem.setAdapter(spinnerAdapter);

        btnAnnuler.setOnClickListener(v -> dialog.dismiss());

        btnValider.setOnClickListener(v -> {
            String nom = etNom.getText().toString().trim();
            String masseStr = etMasse.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String masseVideStr = etMasseAVide.getText().toString().trim();

            // Si la masse à vide est vide, on la met à 0
            if (masseVideStr.isEmpty()) masseVideStr = "0";

            // On utilise la valeur brute du Spinner pour nbItemStr (pour passer dans le validateur)
            String nbItemStr = spinnerNbItem.getSelectedItem() != null ? spinnerNbItem.getSelectedItem().toString() : "1";

            // 1. Validation des champs via ValidateurEquipement
            String erreur = ValidateurEquipement.valider(nom, masseStr, nbItemStr);

            if (erreur != null) {
                Toast.makeText(context, erreur, Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Les données sont validées
            double masse = Double.parseDouble(masseStr.replace(",", "."));
            double masseAVide = Double.parseDouble(masseVideStr.replace(",", "."));
            int nbItem = Integer.parseInt(nbItemStr);
            TypeEquipment type = (TypeEquipment) spinnerType.getSelectedItem();

            // 3. Appel au service métier
            ServiceEquipment.creerNouveauEquipement(context, token, nom, masse, description, type, masseAVide, nbItem, new AppelAPI.VolleyObjectCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    Toast.makeText(context, "Équipement ajouté avec succès !", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    if (onSuccessCallback != null) {
                        onSuccessCallback.run(); // Rafraîchit la liste dans l'activité
                    }
                }

                @Override
                public void onError(VolleyError error) {
                    Toast.makeText(context, "Erreur lors de l'ajout de l'équipement", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}