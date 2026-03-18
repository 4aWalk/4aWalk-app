package fr.iutrodez.a4awalk.activites;

import android.os.Bundle;
import android.widget.Button;
import android.widget.SearchView;
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
import fr.iutrodez.a4awalk.adaptateurs.EquipmentAdapter;
import fr.iutrodez.a4awalk.modeles.entites.EquipmentItem;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceEquipment;

/**
 * Activité de gestion du catalogue d'équipements.
 * Permet d'afficher, rechercher et ajouter des équipements via une API REST.
 */
public class ActiviteGestionEquipment extends HeaderActivity {

    private RecyclerView recyclerEquipments;
    private EquipmentAdapter adapter;
    private Button btnAjouter;
    private SearchView searchView;

    // Liste complète des équipements récupérés depuis l'API
    private List<EquipmentItem> listeEquipements = new ArrayList<>();

    // Liste affichée dans le RecyclerView (sous-ensemble filtré de listeEquipements)
    private List<EquipmentItem> listeEquipementsFiltree = new ArrayList<>();

    private TokenManager tokenManager;

    /**
     * Point d'entrée de l'activité.
     * Initialise l'interface, l'adaptateur, les listeners et charge les données.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_gestion_equipments);

        // Initialise la toolbar héritée de HeaderActivity
        configurerToolbar();

        // Récupère le token d'authentification stocké localement
        tokenManager = new TokenManager(this);

        // Liaison des vues avec leurs identifiants XML
        recyclerEquipments = findViewById(R.id.recycler_equipments_catalog);
        btnAjouter = findViewById(R.id.btn_afficher_popup_ajout_eq);
        searchView = findViewById(R.id.search_equipments);

        // Définit un LinearLayoutManager pour afficher les éléments en liste verticale
        recyclerEquipments.setLayoutManager(new LinearLayoutManager(this));

        // Crée l'adaptateur en lui passant la liste filtrée et un listener de clic.
        // Au clic sur un item, une popup de détails est affichée.
        adapter = new EquipmentAdapter(listeEquipementsFiltree, item ->
                PopUpEquipment.afficherPopupDetailsEquipment(
                        this, item, new ArrayList<>())
        );
        recyclerEquipments.setAdapter(adapter);

        // ouvre la popup d'ajout d'un nouvel équipement.
        // Une fois l'ajout effectué, recharge la liste depuis l'API (rappel via lambda).
        btnAjouter.setOnClickListener(v ->
                PopUpEquipment.afficherPopupAjoutEquipment(
                        this,
                        tokenManager.getToken(),
                        this::chargerEquipementsDepuisAPI) // Callback de rafraîchissement
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

        // Charge les équipements depuis l'API au démarrage de l'activité
        chargerEquipementsDepuisAPI();
    }

    /**
     * Filtre la liste des équipements selon le texte saisi.
     * Met à jour listeEquipementsFiltree et notifie l'adaptateur pour rafraîchir l'affichage.
     *
     * @param texte Le texte de recherche saisi par l'utilisateur.
     */
    private void filtrer(String texte) {
        // Vide la liste filtrée avant de la reconstruire
        listeEquipementsFiltree.clear();

        if (texte == null || texte.trim().isEmpty()) {
            // Si la recherche est vide, on affiche tous les équipements
            listeEquipementsFiltree.addAll(listeEquipements);
        } else {
            String recherche = texte.toLowerCase().trim();
            for (EquipmentItem item : listeEquipements) {
                // Ajoute l'item si son nom contient le texte recherché (insensible à la casse)
                if (item.getNom().toLowerCase().contains(recherche)) {
                    listeEquipementsFiltree.add(item);
                }
            }
        }

        // Notifie l'adaptateur que les données ont changé pour mettre à jour l'affichage
        adapter.notifyDataSetChanged();
    }

    /**
     * Appelle l'API pour récupérer tous les équipements disponibles.
     * En cas de succès, reconstruit la liste et applique le filtre actif.
     * En cas d'erreur, affiche un message Toast à l'utilisateur.
     */
    private void chargerEquipementsDepuisAPI() {
        ServiceEquipment.getAllEquipments(this, tokenManager.getToken(), new AppelAPI.VolleyCallback() {

            /**
             * Appelé quand la requête HTTP réussit.
             * @param result Le tableau JSON contenant les équipements renvoyés par l'API.
             */
            @Override
            public void onSuccess(JSONArray result) {
                // Vide la liste existante avant de la repeupler
                listeEquipements.clear();
                try {
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject obj = result.getJSONObject(i);
                        // Convertit chaque objet JSON en EquipmentItem et l'ajoute à la liste
                        listeEquipements.add(ServiceEquipment.constructEqFromJson(obj));
                    }
                    // Réapplique le filtre en cours pour ne pas perdre la recherche active
                    filtrer(searchView.getQuery().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ActiviteGestionEquipment.this,
                            "Erreur de lecture des données", Toast.LENGTH_SHORT).show();
                }
            }

            /**
             * Appelé en cas d'échec de la requête HTTP (réseau, serveur, etc.).
             */
            @Override
            public void onError(VolleyError error) {
                Toast.makeText(ActiviteGestionEquipment.this,
                        "Erreur de chargement", Toast.LENGTH_SHORT).show();
            }
        });
    }
}