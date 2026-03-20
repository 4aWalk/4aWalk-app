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

/**
 * Classe utilitaire (sans état) regroupant les popups liées aux équipements.
 * Toutes les méthodes sont statiques car elles ne nécessitent pas d'instance propre.
 * Le layout XML popup_ajout_equipment est réutilisé pour les deux modes (ajout et consultation).
 */
public class PopUpEquipment {

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
     * Affiche une popup en lecture seule avec les détails d'un équipement.
     * Tous les champs sont pré-remplis et verrouillés. Le bouton "Valider" devient "Fermer".
     *
     * @param context      Le contexte Android.
     * @param equipment    L'équipement dont on veut afficher les détails.
     * @param participants La liste des participants, utilisée pour afficher le propriétaire
     *                     si l'équipement est de type VETEMENT ou REPOS.
     */
    public static void afficherPopupDetailsEquipment(Context context, EquipmentItem equipment, ArrayList<Participant> participants) {
        Dialog dialog = new Dialog(context);
        // Réutilise le même layout XML que pour l'ajout
        dialog.setContentView(R.layout.popup_ajout_equipment);

        // Personnalise le titre de la popup pour indiquer qu'on est en mode consultation
        TextView tvTitre = dialog.findViewById(R.id.tv_titre_popup_eq);
        if (tvTitre != null) tvTitre.setText("Détails de l'équipement");

        // Liaison des vues du layout
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

        // Pré-remplissage des champs texte avec les données de l'équipement
        etNom.setText(equipment.getNom());
        etMasse.setText(String.valueOf(equipment.getMasseGrammes()));
        // Gestion de la description optionnelle (null → chaîne vide)
        etDescription.setText(equipment.getDescription() != null ? equipment.getDescription() : "");
        etMasseAVide.setText(String.valueOf(equipment.getMasseAVide()));

        // Configuration du Spinner des types d'équipement et sélection du type actuel
        TypeEquipment[] types = TypeEquipment.values();
        ArrayAdapter<TypeEquipment> typeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        spinnerType.setSelection(typeAdapter.getPosition(equipment.getType()));

        // Configuration du Spinner de quantité et sélection de la quantité actuelle
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

        // Le spinner propriétaire n'est affiché que pour les types VETEMENT et REPOS
        if (equipment.getType() == TypeEquipment.VETEMENT || equipment.getType() == TypeEquipment.REPOS) {
            if (llOwnerSelection != null) llOwnerSelection.setVisibility(View.VISIBLE);

            // Construction de la liste des noms de participants pour le Spinner
            List<String> nomParticipants = new ArrayList<>();
            nomParticipants.add("Aucun propriétaire défini"); // Option par défaut
            int positionSelectionnee = 0;
            for (int i = 0; i < participants.size(); i++) {
                Participant p = participants.get(i);
                nomParticipants.add(p.getPrenom() + " " + p.getNom());
                // Détermine la position à sélectionner si un propriétaire est défini
                if (equipment.getOwnerId() != null && equipment.getOwnerId() == p.getId()) {
                    positionSelectionnee = i + 1; // +1 car l'option "Aucun" est à l'index 0
                }
            }

            if (spinnerOwner != null) {
                ArrayAdapter<String> ownerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, nomParticipants);
                ownerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerOwner.setAdapter(ownerAdapter);
                spinnerOwner.setSelection(positionSelectionnee);
                // Désactivé en mode consultation
                spinnerOwner.setEnabled(false);
            }
        } else {
            // Cache la section propriétaire pour les types qui n'en ont pas besoin
            if (llOwnerSelection != null) llOwnerSelection.setVisibility(View.GONE);
        }

        // Verrouillage de tous les champs : la popup est en lecture seule
        etNom.setEnabled(false);
        etMasse.setEnabled(false);
        etDescription.setEnabled(false);
        etMasseAVide.setEnabled(false);
        spinnerType.setEnabled(false);
        spinnerNbItem.setEnabled(false);

        // En mode consultation, on cache "Annuler" et on transforme "Valider" en "Fermer"
        btnAnnuler.setVisibility(View.GONE);
        btnValider.setText("Fermer");
        btnValider.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        // Applique les contraintes de taille après l'affichage du dialog
        appliquerDimensionsDialog(context, dialog);
    }

    /**
     * Affiche une popup pour ajouter un nouvel équipement au catalogue.
     * Les champs sont vides et modifiables. La validation et l'envoi à l'API sont gérés ici.
     *
     * @param context           Le contexte Android.
     * @param token             Le token JWT pour authentifier la requête API.
     * @param onSuccessCallback Callback exécuté après un ajout réussi (ex: recharger la liste).
     */
    public static void afficherPopupAjoutEquipment(Context context, String token, Runnable onSuccessCallback) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup_ajout_equipment);

        // Liaison des vues du layout
        EditText etNom = dialog.findViewById(R.id.et_eq_nom_create);
        EditText etMasse = dialog.findViewById(R.id.et_eq_masse_create);
        EditText etDescription = dialog.findViewById(R.id.et_eq_desc_create);
        EditText etMasseAVide = dialog.findViewById(R.id.et_eq_masse_vide_create);
        Spinner spinnerType = dialog.findViewById(R.id.spinner_eq_type_create);
        Spinner spinnerNbItem = dialog.findViewById(R.id.spinner_eq_nb_item_create);
        Button btnAnnuler = dialog.findViewById(R.id.btn_eq_annuler_create);
        Button btnValider = dialog.findViewById(R.id.btn_eq_valider_create);

        // Alimente le Spinner avec toutes les valeurs de l'enum TypeEquipment
        TypeEquipment[] types = TypeEquipment.values();
        ArrayAdapter<TypeEquipment> typeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Alimente le Spinner avec les quantités disponibles
        Integer[] items = new Integer[]{1, 2, 3, 4, 5, 10};
        ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNbItem.setAdapter(spinnerAdapter);

        // Ferme la popup sans effectuer d'action
        btnAnnuler.setOnClickListener(v -> dialog.dismiss());

        btnValider.setOnClickListener(v -> {
            // Récupération des valeurs saisies
            String nom = etNom.getText().toString().trim();
            String masseStr = etMasse.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String masseVideStr = etMasseAVide.getText().toString().trim();

            // Si la masse à vide n'est pas renseignée, on utilise 0 par défaut
            if (masseVideStr.isEmpty()) masseVideStr = "0";

            String nbItemStr = spinnerNbItem.getSelectedItem() != null
                    ? spinnerNbItem.getSelectedItem().toString() : "1";

            // Étape 1 : Validation des champs obligatoires
            String erreur = ValidateurEquipement.valider(nom, masseStr, nbItemStr);
            if (erreur != null) {
                Toast.makeText(context, erreur, Toast.LENGTH_SHORT).show();
                return; // Interrompt si les données sont invalides
            }

            // Étape 2 : Conversion des chaînes en types numériques
            // La virgule est remplacée par un point pour gérer les deux notations décimales
            double masse = Double.parseDouble(masseStr.replace(",", "."));
            double masseAVide = Double.parseDouble(masseVideStr.replace(",", "."));
            int nbItem = Integer.parseInt(nbItemStr);
            TypeEquipment type = (TypeEquipment) spinnerType.getSelectedItem();

            // Étape 3 : Envoi au service métier qui appelle l'API
            ServiceEquipment.creerNouveauEquipement(context, token, nom, masse, description, type, masseAVide, nbItem, new AppelAPI.VolleyObjectCallback() {

                /** Appelé si la requête API réussit. */
                @Override
                public void onSuccess(JSONObject result) {
                    Toast.makeText(context, "Équipement ajouté avec succès !", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    // Exécute le callback pour rafraîchir la liste dans l'activité parente
                    if (onSuccessCallback != null) {
                        onSuccessCallback.run();
                    }
                }

                /** Appelé si la requête API échoue (réseau, serveur, etc.). */
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