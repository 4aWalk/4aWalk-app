package fr.iutrodez.a4awalk.activites;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.EquipmentItem;
import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.enums.TypeEquipment;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceEquipment;
import fr.iutrodez.a4awalk.utils.validators.ValidateurEquipement;

public class PopUpEquipment {

    /**
     * Applique les dimensions correctes au Dialog :
     * - largeur : toute la largeur de l'écran
     * - hauteur : au maximum 90% de la hauteur de l'écran
     * Cela évite que les boutons tombent hors de l'écran sur les petits appareils.
     */
    private static void appliquerDimensionsDialog(Context context, Dialog dialog) {
        if (dialog.getWindow() == null) return;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int maxHeight = (int) (metrics.heightPixels * 0.90);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.height = maxHeight;
        dialog.getWindow().setAttributes(lp);
    }

    /**
     * Affiche une popup en lecture seule avec les détails d'un équipement.
     */
    public static void afficherPopupDetailsEquipment(Context context, EquipmentItem equipment, ArrayList<Participant> participants) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup_ajout_equipment);

        TextView tvTitre = dialog.findViewById(R.id.tv_titre_popup_eq);
        if (tvTitre != null) tvTitre.setText("Détails de l'équipement");

        EditText etNom = dialog.findViewById(R.id.et_eq_nom_create);
        EditText etMasse = dialog.findViewById(R.id.et_eq_masse_create);
        EditText etDescription = dialog.findViewById(R.id.et_eq_desc_create);
        EditText etMasseAVide = dialog.findViewById(R.id.et_eq_masse_vide_create);
        Spinner spinnerType = dialog.findViewById(R.id.spinner_eq_type_create);
        Spinner spinnerNbItem = dialog.findViewById(R.id.spinner_eq_nb_item_create);
        LinearLayout llOwnerSelection = dialog.findViewById(R.id.ll_owner_selection);
        Spinner spinnerOwner = dialog.findViewById(R.id.spinner_eq_owner_create);
        Button btnAnnuler = dialog.findViewById(R.id.btn_eq_annuler_create);
        Button btnValider = dialog.findViewById(R.id.btn_eq_valider_create);

        // Remplissage des champs
        etNom.setText(equipment.getNom());
        etMasse.setText(String.valueOf(equipment.getMasseGrammes()));
        etDescription.setText(equipment.getDescription() != null ? equipment.getDescription() : "");
        etMasseAVide.setText(String.valueOf(equipment.getMasseAVide()));

        // Spinner type d'équipement
        TypeEquipment[] types = TypeEquipment.values();
        ArrayAdapter<TypeEquipment> typeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        spinnerType.setSelection(typeAdapter.getPosition(equipment.getType()));

        // Spinner quantité
        Integer[] items = new Integer[]{1, 2, 3, 4, 5, 10};
        ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNbItem.setAdapter(spinnerAdapter);
        for (int i = 0; i < items.length; i++) {
            if (items[i] == equipment.getNbItem()) {
                spinnerNbItem.setSelection(i);
                break;
            }
        }

        // Gestion du spinner propriétaire
        if (equipment.getType() == TypeEquipment.VETEMENT || equipment.getType() == TypeEquipment.REPOS) {
            if (llOwnerSelection != null) llOwnerSelection.setVisibility(View.VISIBLE);

            List<String> nomParticipants = new ArrayList<>();
            nomParticipants.add("Aucun propriétaire défini");
            int positionSelectionnee = 0;
            for (int i = 0; i < participants.size(); i++) {
                Participant p = participants.get(i);
                nomParticipants.add(p.getPrenom() + " " + p.getNom());
                if (equipment.getOwnerId() != null && equipment.getOwnerId() == p.getId()) {
                    positionSelectionnee = i + 1;
                }
            }

            if (spinnerOwner != null) {
                ArrayAdapter<String> ownerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, nomParticipants);
                ownerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerOwner.setAdapter(ownerAdapter);
                spinnerOwner.setSelection(positionSelectionnee);
                spinnerOwner.setEnabled(false);
            }
        } else {
            if (llOwnerSelection != null) llOwnerSelection.setVisibility(View.GONE);
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
        appliquerDimensionsDialog(context, dialog);
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

            if (masseVideStr.isEmpty()) masseVideStr = "0";

            String nbItemStr = spinnerNbItem.getSelectedItem() != null
                    ? spinnerNbItem.getSelectedItem().toString() : "1";

            // 1. Validation
            String erreur = ValidateurEquipement.valider(nom, masseStr, nbItemStr);
            if (erreur != null) {
                Toast.makeText(context, erreur, Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Conversion
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
                        onSuccessCallback.run();
                    }
                }

                @Override
                public void onError(VolleyError error) {
                    Toast.makeText(context, "Erreur lors de l'ajout de l'équipement", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
        appliquerDimensionsDialog(context, dialog);
    }
}