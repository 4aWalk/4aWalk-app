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

/**
 * Classe utilitaire (sans état) regroupant les popups liées aux produits alimentaires.
 * Toutes les méthodes sont statiques. Le layout XML popup_ajout_food_product est réutilisé
 * pour les deux modes (ajout et consultation), à l'image de PopUpEquipment.
 */
public class PopUpFoodProduct {

    /**
     * Applique les dimensions correctes au Dialog :
     * - largeur : toute la largeur de l'écran
     * - hauteur : au maximum 90% de la hauteur de l'écran
     * Cela évite que les boutons tombent hors de l'écran sur les petits appareils.
     *
     * @param context Le contexte Android nécessaire pour accéder aux métriques d'écran.
     * @param dialog  Le Dialog dont on veut ajuster les dimensions.
     */
    private static void appliquerDimensionsDialog(Context context, Dialog dialog) {
        // Sortie anticipée si la fenêtre du dialog n'est pas encore créée
        if (dialog.getWindow() == null) return;

        // Récupère les dimensions réelles de l'écran en pixels
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        // Calcule 90% de la hauteur de l'écran pour éviter que le dialog déborde
        int maxHeight = (int) (metrics.heightPixels * 0.90);

        // Force la largeur en plein écran et une hauteur automatique (WRAP_CONTENT)
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Applique la contrainte de hauteur maximale via les LayoutParams de la fenêtre
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.height = maxHeight;
        dialog.getWindow().setAttributes(lp);
    }

    /**
     * Affiche une popup en lecture seule avec les détails d'un produit alimentaire.
     * Tous les champs sont pré-remplis et verrouillés. Le bouton "Valider" devient "Fermer".
     *
     * @param context Le contexte Android.
     * @param produit Le produit alimentaire dont on veut afficher les détails.
     */
    public static void afficherPopupDetailsFoodProduct(Context context, FoodProduct produit) {
        Dialog dialog = new Dialog(context);
        // Réutilise le même layout XML que pour l'ajout
        dialog.setContentView(R.layout.popup_ajout_food_product);

        // Personnalise le titre pour indiquer qu'on est en mode consultation
        TextView tvTitre = dialog.findViewById(R.id.tv_titre_popup_fp);
        if (tvTitre != null) tvTitre.setText("Détails du produit");

        // Liaison des vues du layout
        EditText etNom = dialog.findViewById(R.id.et_fp_nom);
        EditText etMasse = dialog.findViewById(R.id.et_fp_masse);
        EditText etAppellation = dialog.findViewById(R.id.et_fp_appellation);
        EditText etConditionnement = dialog.findViewById(R.id.et_fp_conditionnement);
        EditText etKcal = dialog.findViewById(R.id.et_fp_kcal);
        EditText etPrix = dialog.findViewById(R.id.et_fp_prix);
        Spinner spinnerNbItem = dialog.findViewById(R.id.spinner_fp_nb_item);
        Button btnAnnuler = dialog.findViewById(R.id.btn_fp_annuler);
        Button btnValider = dialog.findViewById(R.id.btn_fp_valider);

        // Pré-remplissage des champs texte avec les données du produit
        etNom.setText(produit.getNom());
        etMasse.setText(String.valueOf(produit.getMasseGrammes()) + " g");   // Affiche l'unité "g" pour la lisibilité
        etAppellation.setText(produit.getAppellationCourante());
        // Gestion du conditionnement optionnel (null → chaîne vide)
        etConditionnement.setText(produit.getConditionnement() != null ? produit.getConditionnement() : "");
        etKcal.setText(String.valueOf(produit.getApportNutritionnelKcal()) + "Kcal"); // Affiche l'unité "Kcal"
        etPrix.setText(String.valueOf(produit.getPrixEuro()));

        // Configuration du Spinner de quantité et sélection de la quantité actuelle
        // nbItem - 1 car les indices du spinner commencent à 0 et les quantités à 1
        Integer[] items = new Integer[]{1, 2, 3};
        ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNbItem.setAdapter(spinnerAdapter);
        spinnerNbItem.setSelection(produit.getNbItem() - 1);

        // Verrouillage de tous les champs : la popup est en lecture seule
        etNom.setEnabled(false);
        etMasse.setEnabled(false);
        etAppellation.setEnabled(false);
        etConditionnement.setEnabled(false);
        etKcal.setEnabled(false);
        etPrix.setEnabled(false);
        spinnerNbItem.setEnabled(false);

        // En mode consultation, on cache "Annuler" et on transforme "Valider" en "Fermer"
        btnAnnuler.setVisibility(View.GONE);
        btnValider.setText("Fermer");
        btnValider.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        appliquerDimensionsDialog(context, dialog);
    }

    /**
     * Affiche une popup pour ajouter un nouveau produit alimentaire au catalogue.
     * Les champs sont vides et modifiables. La validation et l'envoi à l'API sont gérés ici.
     *
     * @param context           Le contexte Android.
     * @param token             Le token JWT pour authentifier la requête API.
     * @param onSuccessCallback Callback exécuté après un ajout réussi (ex: recharger la liste).
     */
    public static void afficherPopupAjoutFoodProduct(Context context, String token, Runnable onSuccessCallback) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup_ajout_food_product);

        // Liaison des vues du layout
        EditText etNom = dialog.findViewById(R.id.et_fp_nom);
        EditText etMasse = dialog.findViewById(R.id.et_fp_masse);
        EditText etAppellation = dialog.findViewById(R.id.et_fp_appellation);
        EditText etConditionnement = dialog.findViewById(R.id.et_fp_conditionnement);
        EditText etKcal = dialog.findViewById(R.id.et_fp_kcal);
        EditText etPrix = dialog.findViewById(R.id.et_fp_prix);
        Spinner spinnerNbItem = dialog.findViewById(R.id.spinner_fp_nb_item);
        Button btnAnnuler = dialog.findViewById(R.id.btn_fp_annuler);
        Button btnValider = dialog.findViewById(R.id.btn_fp_valider);

        // Alimente le Spinner avec les quantités disponibles (1, 2 ou 3)
        Integer[] items = new Integer[]{1, 2, 3};
        ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNbItem.setAdapter(spinnerAdapter);

        // Ferme la popup sans effectuer d'action
        btnAnnuler.setOnClickListener(v -> dialog.dismiss());

        btnValider.setOnClickListener(v -> {
            // Récupération des valeurs saisies
            String nom = etNom.getText().toString().trim();
            String masseStr = etMasse.getText().toString().trim();
            String appellation = etAppellation.getText().toString().trim();
            String conditionnement = etConditionnement.getText().toString().trim();
            String kcalStr = etKcal.getText().toString().trim();
            String prixStr = etPrix.getText().toString().trim();

            // Étape 1 : Validation des champs obligatoires via le validateur centralisé
            String erreur = ValidateurFoodProduct.valider(nom, masseStr, appellation, kcalStr, prixStr);
            if (erreur != null) {
                Toast.makeText(context, erreur, Toast.LENGTH_SHORT).show();
                return; // Interrompt si les données sont invalides
            }

            // Étape 2 : Conversion des chaînes en types numériques
            // La virgule est remplacée par un point pour gérer les deux notations décimales
            double masse = Double.parseDouble(masseStr.replace(",", "."));
            double kcal = Double.parseDouble(kcalStr.replace(",", "."));
            double prix = Double.parseDouble(prixStr.replace(",", "."));
            // Cast explicite car getSelectedItem() retourne un Object (ici un Integer)
            int nbItem = (int) spinnerNbItem.getSelectedItem();

            // Étape 3 : Envoi au service métier qui appelle l'API
            ServiceFoodProduct.creerNouveauProduit(context, token, nom, masse, appellation, conditionnement, kcal, prix, nbItem, new AppelAPI.VolleyObjectCallback() {

                /** Appelé si la requête API réussit. */
                @Override
                public void onSuccess(JSONObject result) {
                    Toast.makeText(context, "Produit ajouté avec succès !", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    // Exécute le callback pour rafraîchir la liste dans l'activité parente
                    if (onSuccessCallback != null) {
                        onSuccessCallback.run();
                    }
                }

                /** Appelé si la requête API échoue (réseau, serveur, etc.). */
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