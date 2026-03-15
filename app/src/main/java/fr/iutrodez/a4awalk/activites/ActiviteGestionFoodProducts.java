package fr.iutrodez.a4awalk.activites;

import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.adaptateurs.FoodProductAdapter;
import fr.iutrodez.a4awalk.modeles.entites.FoodProduct;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceFoodProduct;

public class ActiviteGestionFoodProducts extends HeaderActivity {

    private RecyclerView recyclerFoodProducts;
    private FoodProductAdapter adapter;
    private Button btnAjouter;
    private List<FoodProduct> listeProduits = new ArrayList<>();
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_gestion_foods);

        configurerToolbar();

        tokenManager = new TokenManager(this);

        recyclerFoodProducts = findViewById(R.id.recycler_food_products_catalog);
        btnAjouter = findViewById(R.id.btn_afficher_popup_ajout);

        recyclerFoodProducts.setLayoutManager(new LinearLayoutManager(this));

        // Suppression de l'écouteur de suppression
        adapter = new FoodProductAdapter(listeProduits);
        recyclerFoodProducts.setAdapter(adapter);

        btnAjouter.setOnClickListener(v -> afficherPopupAjoutFoodProduct());

        chargerProduitsDepuisAPI();
    }

    private void chargerProduitsDepuisAPI() {
        ServiceFoodProduct.getAllFoodProducts(this, tokenManager.getToken(), new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                listeProduits.clear();
                try {
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject obj = result.getJSONObject(i);
                        FoodProduct fp = new FoodProduct();
                        fp.setId(obj.getInt("id"));
                        fp.setNom(obj.getString("nom"));
                        fp.setMasseGrammes(obj.getDouble("masseGrammes"));
                        fp.setAppellationCourante(obj.getString("appelationCourante")); // Attention à l'orthographe exacte du JSON
                        fp.setConditionnement(obj.getString("conditionnement"));
                        fp.setApportNutritionnelKcal(obj.getDouble("apportNutritionnelKcal"));
                        fp.setPrixEuro(obj.getDouble("prixEuro"));
                        fp.setNbItem(obj.getInt("nbItem"));
                        listeProduits.add(fp);
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(ActiviteGestionFoodProducts.this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void afficherPopupAjoutFoodProduct() {
        Dialog dialog = new Dialog(this);
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

        // Configuration du Spinner (1 à 3)
        Integer[] items = new Integer[]{1, 2, 3};
        ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
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

            // VÉRIFICATION DES RÈGLES MÉTIER
            if (nom.isEmpty() || appellation.isEmpty()) {
                Toast.makeText(this, "Le nom et l'appellation sont obligatoires.", Toast.LENGTH_SHORT).show();
                return;
            }

            double masse = 0;
            double kcal = 0;
            double prix = 0;

            try {
                masse = Double.parseDouble(masseStr);
                kcal = Double.parseDouble(kcalStr);
                prix = Double.parseDouble(prixStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Veuillez remplir correctement les champs numériques.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (masse < 50 || masse > 5000) {
                Toast.makeText(this, "La masse doit être entre 50g et 5000g.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (kcal < 50 || kcal > 3000) {
                Toast.makeText(this, "L'apport nutritionnel doit être entre 50 et 3000 Kcal.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (prix < 0) {
                Toast.makeText(this, "Le prix ne peut pas être négatif.", Toast.LENGTH_SHORT).show();
                return;
            }

            int nbItem = (int) spinnerNbItem.getSelectedItem();

            // Création de l'objet
            FoodProduct nouveauProduit = new FoodProduct();
            nouveauProduit.setNom(nom);
            nouveauProduit.setMasseGrammes(masse);
            nouveauProduit.setAppellationCourante(appellation);
            nouveauProduit.setConditionnement(conditionnement.isEmpty() ? null : conditionnement);
            nouveauProduit.setApportNutritionnelKcal(kcal);
            nouveauProduit.setPrixEuro(prix);
            nouveauProduit.setNbItem(nbItem);

            // Note: Si FoodProduct demande un setDescription() qui correspond à l'appellation, n'oublie pas de le setter aussi.

            ServiceFoodProduct.creerFoodProduct(this, tokenManager.getToken(), nouveauProduit, new AppelAPI.VolleyObjectCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    Toast.makeText(ActiviteGestionFoodProducts.this, "Produit ajouté !", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    chargerProduitsDepuisAPI(); // On rafraîchit la liste
                }

                @Override
                public void onError(VolleyError error) {
                    Toast.makeText(ActiviteGestionFoodProducts.this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Affichage de la popup
        dialog.show();

        // FORCER LA LARGEUR DE LA POPUP (Règle le problème de la popup trop étroite)
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}