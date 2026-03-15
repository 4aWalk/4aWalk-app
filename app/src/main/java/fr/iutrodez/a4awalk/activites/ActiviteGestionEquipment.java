package fr.iutrodez.a4awalk.activites;

import android.app.Dialog;
import android.os.Bundle;
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
import fr.iutrodez.a4awalk.adaptateurs.EquipmentAdapter;
import fr.iutrodez.a4awalk.modeles.entites.EquipmentItem;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.modeles.enums.TypeEquipment;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceEquipment;

public class ActiviteGestionEquipment extends HeaderActivity {

    private RecyclerView recyclerEquipments;
    private EquipmentAdapter adapter;
    private Button btnAjouter;
    private List<EquipmentItem> listeEquipments = new ArrayList<>();
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_gestion_equipments);

        configurerToolbar();
        tokenManager = new TokenManager(this);

        recyclerEquipments = findViewById(R.id.recycler_equipments_catalog);
        btnAjouter = findViewById(R.id.btn_afficher_popup_ajout_eq);

        recyclerEquipments.setLayoutManager(new LinearLayoutManager(this));

        // Plus de listener de suppression
        adapter = new EquipmentAdapter(listeEquipments);
        recyclerEquipments.setAdapter(adapter);

        btnAjouter.setOnClickListener(v -> afficherPopupAjoutEquipment());

        chargerEquipmentsDepuisAPI();
    }

    private void chargerEquipmentsDepuisAPI() {
        ServiceEquipment.getAllEquipments(this, tokenManager.getToken(), new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                listeEquipments.clear();
                try {
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject obj = result.getJSONObject(i);
                        EquipmentItem eq = new EquipmentItem();
                        eq.setId(obj.getInt("id"));
                        eq.setNom(obj.getString("nom"));
                        eq.setDescription(obj.optString("description", ""));
                        eq.setMasseGrammes(obj.getDouble("masseGrammes"));
                        eq.setNbItem(obj.getInt("nbItem"));
                        eq.setType(TypeEquipment.valueOf(obj.getString("type")));
                        eq.setMasseAVide(obj.optDouble("masseAVide", 0.0));
                        listeEquipments.add(eq);
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(ActiviteGestionEquipment.this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void afficherPopupAjoutEquipment() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_ajout_equipment);

        EditText etNom = dialog.findViewById(R.id.et_eq_nom_create);
        EditText etDescription = dialog.findViewById(R.id.et_eq_desc_create);
        EditText etMasse = dialog.findViewById(R.id.et_eq_masse_create);
        EditText etMasseVide = dialog.findViewById(R.id.et_eq_masse_vide_create);
        Spinner spinnerNbItem = dialog.findViewById(R.id.spinner_eq_nb_item_create);
        Spinner spinnerType = dialog.findViewById(R.id.spinner_eq_type_create);

        Button btnAnnuler = dialog.findViewById(R.id.btn_eq_annuler_create);
        Button btnValider = dialog.findViewById(R.id.btn_eq_valider_create);

        // Initialisation Spinner Nb Item (1 à 3)
        ArrayAdapter<Integer> adapterNb = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new Integer[]{1, 2, 3});
        adapterNb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNbItem.setAdapter(adapterNb);

        // Initialisation Spinner Type Equipment
        ArrayAdapter<TypeEquipment> adapterType = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, TypeEquipment.values());
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapterType);

        btnAnnuler.setOnClickListener(v -> dialog.dismiss());

        btnValider.setOnClickListener(v -> {
            String nom = etNom.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();
            String masseStr = etMasse.getText().toString().trim();
            String masseVideStr = etMasseVide.getText().toString().trim();

            // --- VALIDATION DES BUSINESS RULES ---
            if (nom.isEmpty() || desc.isEmpty() || masseStr.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir les champs obligatoires", Toast.LENGTH_SHORT).show();
                return;
            }

            double masse = Double.parseDouble(masseStr);
            if (masse < 50 || masse > 5000) {
                Toast.makeText(this, "La masse doit être comprise entre 50g et 5000g", Toast.LENGTH_SHORT).show();
                return;
            }

            double masseVide = masseVideStr.isEmpty() ? 0.0 : Double.parseDouble(masseVideStr);

            EquipmentItem nouveauEq = new EquipmentItem();
            nouveauEq.setNom(nom);
            nouveauEq.setDescription(desc);
            nouveauEq.setMasseGrammes(masse);
            nouveauEq.setNbItem((Integer) spinnerNbItem.getSelectedItem());
            nouveauEq.setType((TypeEquipment) spinnerType.getSelectedItem());
            nouveauEq.setMasseAVide(masseVide);

            ServiceEquipment.creerEquipment(this, tokenManager.getToken(), nouveauEq, new AppelAPI.VolleyObjectCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    Toast.makeText(ActiviteGestionEquipment.this, "Équipement ajouté !", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    chargerEquipmentsDepuisAPI();
                }

                @Override
                public void onError(VolleyError error) {
                    Toast.makeText(ActiviteGestionEquipment.this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}