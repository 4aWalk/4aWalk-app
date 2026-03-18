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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONObject;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.FoodProduct;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceFoodProduct;
import fr.iutrodez.a4awalk.utils.validators.ValidateurFoodProduct;

public class PopUpFoodProduct {

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
     * Affiche une popup en lecture seule avec les détails d'un produit alimentaire.
     */
    public static void afficherPopupDetailsFoodProduct(Context context, FoodProduct produit) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup_ajout_food_product);

        TextView tvTitre = dialog.findViewById(R.id.tv_titre_popup_fp);
        if (tvTitre != null) tvTitre.setText("Détails du produit");

        EditText etNom = dialog.findViewById(R.id.et_fp_nom);
        EditText etMasse = dialog.findViewById(R.id.et_fp_masse);
        EditText etAppellation = dialog.findViewById(R.id.et_fp_appellation);
        EditText etConditionnement = dialog.findViewById(R.id.et_fp_conditionnement);
        EditText etKcal = dialog.findViewById(R.id.et_fp_kcal);
        EditText etPrix = dialog.findViewById(R.id.et_fp_prix);
        Spinner spinnerNbItem = dialog.findViewById(R.id.spinner_fp_nb_item);
        Button btnAnnuler = dialog.findViewById(R.id.btn_fp_annuler);
        Button btnValider = dialog.findViewById(R.id.btn_fp_valider);

        // Remplissage des champs
        etNom.setText(produit.getNom());
        etMasse.setText(String.valueOf(produit.getMasseGrammes()) + " g");
        etAppellation.setText(produit.getAppellationCourante());
        etConditionnement.setText(produit.getConditionnement() != null ? produit.getConditionnement() : "");
        etKcal.setText(String.valueOf(produit.getApportNutritionnelKcal()) + "Kcal");
        etPrix.setText(String.valueOf(produit.getPrixEuro()));

        Integer[] items = new Integer[]{1, 2, 3};
        ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNbItem.setAdapter(spinnerAdapter);
        spinnerNbItem.setSelection(produit.getNbItem() - 1);

        // Verrouillage des champs (Mode Consultation)
        etNom.setEnabled(false);
        etMasse.setEnabled(false);
        etAppellation.setEnabled(false);
        etConditionnement.setEnabled(false);
        etKcal.setEnabled(false);
        etPrix.setEnabled(false);
        spinnerNbItem.setEnabled(false);

        // Configuration des boutons
        btnAnnuler.setVisibility(View.GONE);
        btnValider.setText("Fermer");
        btnValider.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        appliquerDimensionsDialog(context, dialog);
    }

    /**
     * Affiche une popup pour ajouter un nouveau FoodProduct.
     */
    public static void afficherPopupAjoutFoodProduct(Context context, String token, Runnable onSuccessCallback) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup_ajout_food_product);

        EditText etNom = dialog.findViewById(R.id.et_fp_nom);
        EditText etMasse = dialog.findViewById(R.id.et_fp_masse);
        EditText etAppellation = dialog.findViewById(R.id.et_fp_appellation);
        EditText etConditionnement = dialog.findViewById(R.id.et_fp_conditionnement);
        EditText etKcal = dialog.findViewById(R.id.et_fp_kcal);
        EditText etPrix = dialog.findViewById(R.id.et_fp_prix);
        Spinner spinnerNbItem = dialog.findViewById(R.id.spinner_fp_nb_item);
        Button btnAnnuler = dialog.findViewById(R.id.btn_fp_annuler);
        Button btnValider = dialog.findViewById(R.id.btn_fp_valider);

        Integer[] items = new Integer[]{1, 2, 3};
        ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNbItem.setAdapter(spinnerAdapter);

        btnAnnuler.setOnClickListener(v -> dialog.dismiss());

        btnValider.setOnClickListener(v -> {
            String nom = etNom.getText().toString().trim();
            String masseStr = etMasse.getText().toString().trim();
            String appellation = etAppellation.getText().toString().trim();
            String conditionnement = etConditionnement.getText().toString().trim();
            String kcalStr = etKcal.getText().toString().trim();
            String prixStr = etPrix.getText().toString().trim();

            // 1. Validation des champs
            String erreur = ValidateurFoodProduct.valider(nom, masseStr, appellation, kcalStr, prixStr);
            if (erreur != null) {
                Toast.makeText(context, erreur, Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Les données sont validées, la conversion est sûre
            double masse = Double.parseDouble(masseStr.replace(",", "."));
            double kcal = Double.parseDouble(kcalStr.replace(",", "."));
            double prix = Double.parseDouble(prixStr.replace(",", "."));
            int nbItem = (int) spinnerNbItem.getSelectedItem();

            // 3. Appel au service métier
            ServiceFoodProduct.creerNouveauProduit(context, token, nom, masse, appellation, conditionnement, kcal, prix, nbItem, new AppelAPI.VolleyObjectCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    Toast.makeText(context, "Produit ajouté avec succès !", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    if (onSuccessCallback != null) {
                        onSuccessCallback.run();
                    }
                }

                @Override
                public void onError(VolleyError error) {
                    Toast.makeText(context, "Erreur lors de l'ajout du produit", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
        appliquerDimensionsDialog(context, dialog);
    }
}