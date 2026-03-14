package fr.iutrodez.a4awalk.activites;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

public class ActiviteGestionFoodProducts extends AppCompatActivity {

    private RecyclerView recyclerFoodProducts;
    private FoodProductAdapter adapter;
    private Button btnAjouter;
    private List<FoodProduct> listeProduits = new ArrayList<>();
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_gestion_items);

        tokenManager = new TokenManager(this);

        recyclerFoodProducts = findViewById(R.id.recycler_food_products_catalog);
        btnAjouter = findViewById(R.id.btn_afficher_popup_ajout);

        recyclerFoodProducts.setLayoutManager(new LinearLayoutManager(this));


        adapter = new FoodProductAdapter(listeProduits, this::confirmerSuppression);
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
                        fp.setAppellationCourante(obj.getString("appelationCourante"));
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
        // TODO: Récupérer les autres EditText (masse, kcal...)

        Button btnAnnuler = dialog.findViewById(R.id.btn_fp_annuler);
        Button btnValider = dialog.findViewById(R.id.btn_fp_valider);

        btnAnnuler.setOnClickListener(v -> dialog.dismiss());

        btnValider.setOnClickListener(v -> {
            FoodProduct nouveauProduit = new FoodProduct();
            nouveauProduit.setNom(etNom.getText().toString());
            // TODO: Setter les autres attributs

            ServiceFoodProduct.creerFoodProduct(this, tokenManager.getToken(), nouveauProduit, new AppelAPI.VolleyObjectCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    Toast.makeText(ActiviteGestionFoodProducts.this, "Produit ajouté !", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    chargerProduitsDepuisAPI(); // On rafraîchit la liste complète
                }

                @Override
                public void onError(VolleyError error) {
                    Toast.makeText(ActiviteGestionFoodProducts.this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void confirmerSuppression(FoodProduct produit) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer le produit")
                .setMessage("Voulez-vous vraiment supprimer " + produit.getNom() + " du catalogue ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    ServiceFoodProduct.supprimerFoodProduct(this, tokenManager.getToken(), produit.getId(), new AppelAPI.VolleyObjectCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            Toast.makeText(ActiviteGestionFoodProducts.this, "Produit supprimé", Toast.LENGTH_SHORT).show();
                            chargerProduitsDepuisAPI(); // On rafraîchit la liste
                        }

                        @Override
                        public void onError(VolleyError error) {
                            Toast.makeText(ActiviteGestionFoodProducts.this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Non", null)
                .show();
    }
}