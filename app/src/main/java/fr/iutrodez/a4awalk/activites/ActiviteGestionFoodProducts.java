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

        adapter = new FoodProductAdapter(listeProduits, item ->
                PopUpFoodProduct.afficherPopupDetailsFoodProduct(ActiviteGestionFoodProducts.this, item)
        );
        recyclerFoodProducts.setAdapter(adapter);

        btnAjouter.setOnClickListener(v ->
                PopUpFoodProduct.afficherPopupAjoutFoodProduct(ActiviteGestionFoodProducts.this, tokenManager.getToken(), this::chargerProduitsDepuisAPI)
        );

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
                        FoodProduct fp = ServiceFoodProduct.constructFPFromJson(obj);
                        listeProduits.add(fp);
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ActiviteGestionFoodProducts.this, "Erreur de lecture des données", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(ActiviteGestionFoodProducts.this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
            }
        });
    }
}