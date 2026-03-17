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

public class ActiviteGestionEquipment extends HeaderActivity {

    private RecyclerView recyclerEquipments;
    private EquipmentAdapter adapter;
    private Button btnAjouter;
    private SearchView searchView;                              // +
    private List<EquipmentItem> listeEquipements = new ArrayList<>();
    private List<EquipmentItem> listeEquipementsFiltree = new ArrayList<>(); // +
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_gestion_equipments);

        configurerToolbar();
        tokenManager = new TokenManager(this);

        recyclerEquipments = findViewById(R.id.recycler_equipments_catalog);
        btnAjouter        = findViewById(R.id.btn_afficher_popup_ajout_eq);
        searchView        = findViewById(R.id.search_equipments);           // +

        recyclerEquipments.setLayoutManager(new LinearLayoutManager(this));

        // L'adaptateur travaille désormais sur la liste filtrée
        adapter = new EquipmentAdapter(listeEquipementsFiltree, item ->
                PopUpEquipment.afficherPopupDetailsEquipment(
                        ActiviteGestionEquipment.this, item, new ArrayList<>())
        );
        recyclerEquipments.setAdapter(adapter);

        btnAjouter.setOnClickListener(v ->
                PopUpEquipment.afficherPopupAjoutEquipment(
                        ActiviteGestionEquipment.this,
                        tokenManager.getToken(),
                        this::chargerEquipementsDepuisAPI)
        );

        // Écoute les saisies dans la barre de recherche              // +
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filtrer(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrer(newText);
                return true;
            }
        });

        chargerEquipementsDepuisAPI();
    }

    // Filtre listeEquipements → listeEquipementsFiltree selon le nom  // +
    private void filtrer(String texte) {
        listeEquipementsFiltree.clear();
        if (texte == null || texte.trim().isEmpty()) {
            listeEquipementsFiltree.addAll(listeEquipements);
        } else {
            String recherche = texte.toLowerCase().trim();
            for (EquipmentItem item : listeEquipements) {
                if (item.getNom().toLowerCase().contains(recherche)) {
                    listeEquipementsFiltree.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void chargerEquipementsDepuisAPI() {
        ServiceEquipment.getAllEquipments(this, tokenManager.getToken(), new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                listeEquipements.clear();
                try {
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject obj = result.getJSONObject(i);
                        listeEquipements.add(ServiceEquipment.constructEqFromJson(obj));
                    }
                    // Rafraîchit la liste filtrée en respectant la recherche en cours  // +
                    filtrer(searchView.getQuery().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ActiviteGestionEquipment.this,
                            "Erreur de lecture des données", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(ActiviteGestionEquipment.this,
                        "Erreur de chargement", Toast.LENGTH_SHORT).show();
            }
        });
    }
}