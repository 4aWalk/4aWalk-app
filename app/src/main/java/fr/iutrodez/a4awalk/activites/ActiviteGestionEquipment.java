package fr.iutrodez.a4awalk.activites;

import android.os.Bundle;
import android.widget.Button;
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
    private List<EquipmentItem> listeEquipements = new ArrayList<>();
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Assure-toi de créer le layout correspondant (ex: activite_gestion_equipments.xml)
        setContentView(R.layout.activite_gestion_equipments);

        configurerToolbar();

        tokenManager = new TokenManager(this);

        recyclerEquipments = findViewById(R.id.recycler_equipments_catalog);
        btnAjouter = findViewById(R.id.btn_afficher_popup_ajout);

        recyclerEquipments.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EquipmentAdapter(listeEquipements, item ->
                PopUpEquipment.afficherPopupDetailsEquipment(ActiviteGestionEquipment.this, item)
        );
        recyclerEquipments.setAdapter(adapter);

        btnAjouter.setOnClickListener(v ->
                PopUpEquipment.afficherPopupAjoutEquipment(ActiviteGestionEquipment.this, tokenManager.getToken(), this::chargerEquipementsDepuisAPI)
        );

        chargerEquipementsDepuisAPI();
    }

    private void chargerEquipementsDepuisAPI() {
        ServiceEquipment.getAllEquipments(this, tokenManager.getToken(), new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                listeEquipements.clear();
                try {
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject obj = result.getJSONObject(i);
                        // Assure-toi d'avoir cette méthode dans ServiceEquipment
                        EquipmentItem eq = ServiceEquipment.constructEqFromJson(obj);
                        listeEquipements.add(eq);
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ActiviteGestionEquipment.this, "Erreur de lecture des données", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(ActiviteGestionEquipment.this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
            }
        });
    }
}