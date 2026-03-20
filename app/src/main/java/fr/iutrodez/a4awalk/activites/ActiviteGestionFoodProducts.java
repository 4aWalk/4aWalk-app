package fr.iutrodez.a4awalk.activites;

import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
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

/**
 * Activité de gestion du catalogue de produits alimentaires.
 * Permet d'afficher, rechercher et ajouter des produits alimentaires via une API REST.
 * Suit la même structure qu'ActiviteGestionEquipment.
 */
public class ActiviteGestionFoodProducts extends HeaderActivity {

    private RecyclerView recyclerFoodProducts;
    private FoodProductAdapter adapter;
    private Button btnAjouter;
    private SearchView searchView;

    // Liste complète des produits récupérés depuis l'API
    private List<FoodProduct> listeProduits = new ArrayList<>();

    // Liste affichée dans le RecyclerView (sous-ensemble filtré de listeProduits)
    private List<FoodProduct> listeProduitsFiltree = new ArrayList<>();

    private TokenManager tokenManager;

    /**
     * Point d'entrée de l'activité.
     * Initialise l'interface, l'adaptateur, les listeners et charge les données.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_gestion_foods);

        // Initialise la toolbar héritée de HeaderActivity
        configurerToolbar();

        // Récupère le token d'authentification stocké localement
        tokenManager = new TokenManager(this);

        // Liaison des vues avec leurs identifiants XML
        recyclerFoodProducts = findViewById(R.id.recycler_food_products_catalog);
        btnAjouter = findViewById(R.id.btn_afficher_popup_ajout);
        searchView = findViewById(R.id.search_food_products);

        // Définit un LinearLayoutManager pour afficher les éléments en liste verticale
        recyclerFoodProducts.setLayoutManager(new LinearLayoutManager(this));

        // Crée l'adaptateur en lui passant la liste filtrée et un listener de clic.
        // Au clic sur un item, une popup de détails est affichée.
        adapter = new FoodProductAdapter(listeProduitsFiltree, item ->
                PopUpFoodProduct.afficherPopupDetailsFoodProduct(
                        ActiviteGestionFoodProducts.this, item)
        );
        recyclerFoodProducts.setAdapter(adapter);

        // ouvre la popup d'ajout d'un nouveau produit.
        // Le callback (this::chargerProduitsDepuisAPI) recharge la liste après l'ajout.
        btnAjouter.setOnClickListener(v ->
                PopUpFoodProduct.afficherPopupAjoutFoodProduct(
                        ActiviteGestionFoodProducts.this,
                        tokenManager.getToken(),
                        this::chargerProduitsDepuisAPI) // Callback de rafraîchissement
        );

        // Écoute les saisies dans la barre de recherche pour filtrer en temps réel
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            // Déclenché quand l'utilisateur valide la recherche (appui sur Entrée)
            @Override
            public boolean onQueryTextSubmit(String query) {
                filtrer(query);
                return true;
            }

            // Déclenché à chaque modification du texte saisi
            @Override
            public boolean onQueryTextChange(String newText) {
                filtrer(newText);
                return true;
            }
        });

        // Charge les produits depuis l'API au démarrage de l'activité
        chargerProduitsDepuisAPI();
    }

    /**
     * Filtre la liste des produits alimentaires selon le texte saisi.
     * Met à jour listeProduitsFiltree et notifie l'adaptateur pour rafraîchir l'affichage.
     *
     * @param texte Le texte de recherche saisi par l'utilisateur.
     */
    private void filtrer(String texte) {
        // Vide la liste filtrée avant de la reconstruire
        listeProduitsFiltree.clear();

        if (texte == null || texte.trim().isEmpty()) {
            // Si la recherche est vide, on affiche tous les produits
            listeProduitsFiltree.addAll(listeProduits);
        } else {
            String recherche = texte.toLowerCase().trim();
            for (FoodProduct fp : listeProduits) {
                // Ajoute le produit si son nom contient le texte recherché (insensible à la casse)
                if (fp.getNom().toLowerCase().contains(recherche)) {
                    listeProduitsFiltree.add(fp);
                }
            }
        }

        // Notifie l'adaptateur que les données ont changé pour mettre à jour l'affichage
        adapter.notifyDataSetChanged();
    }

    /**
     * Appelle l'API pour récupérer tous les produits alimentaires disponibles.
     * En cas de succès, reconstruit la liste et applique le filtre actif.
     * En cas d'erreur, affiche un message Toast à l'utilisateur.
     */
    private void chargerProduitsDepuisAPI() {
        ServiceFoodProduct.getAllFoodProducts(this, tokenManager.getToken(), new AppelAPI.VolleyCallback() {

            /**
             * Appelé quand la requête HTTP réussit.
             * @param result Le tableau JSON contenant les produits renvoyés par l'API.
             */
            @Override
            public void onSuccess(JSONArray result) {
                // Vide la liste existante avant de la repeupler
                listeProduits.clear();
                try {
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject obj = result.getJSONObject(i);
                        // Convertit chaque objet JSON en FoodProduct et l'ajoute à la liste
                        listeProduits.add(ServiceFoodProduct.constructFPFromJson(obj));
                    }
                    // Réapplique le filtre en cours pour ne pas perdre la recherche active
                    filtrer(searchView.getQuery().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ActiviteGestionFoodProducts.this,
                            "Erreur de lecture des données", Toast.LENGTH_SHORT).show();
                }
            }

            /**
             * Appelé en cas d'échec de la requête HTTP (réseau, serveur, etc.).
             */
            @Override
            public void onError(VolleyError error) {
                Toast.makeText(ActiviteGestionFoodProducts.this,
                        "Erreur de chargement", Toast.LENGTH_SHORT).show();
            }
        });
    }
}