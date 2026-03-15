package fr.iutrodez.a4awalk.activites;

import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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

        adapter = new EquipmentAdapter(listeEquipments, item -> afficherPopupDetailsEquipment(item));
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

        ArrayAdapter<Integer> adapterNb = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new Integer[]{1, 2, 3});
        adapterNb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNbItem.setAdapter(adapterNb);

        ArrayAdapter<TypeEquipment> adapterType = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, TypeEquipment.values());
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapterType);

        btnAnnuler.setOnClickListener(v -> dialog.dismiss());

        btnValider.setOnClickListener(v -> {
            String nom = etNom.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();
            String masseStr = etMasse.getText().toString().trim();
            String masseVideStr = etMasseVide.getText().toString().trim();

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

        // Force la largeur
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    // VOICI LA METHODE MANQUANTE QUE J'AI AJOUTEE
    private void afficherPopupDetailsEquipment(EquipmentItem equipement) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_ajout_equipment);

        TextView tvTitre = dialog.findViewById(R.id.tv_titre_popup_eq);
        if (tvTitre != null) tvTitre.setText("Détails de l'équipement");

        EditText etNom = dialog.findViewById(R.id.et_eq_nom_create);
        EditText etDesc = dialog.findViewById(R.id.et_eq_desc_create);
        EditText etMasse = dialog.findViewById(R.id.et_eq_masse_create);
        Spinner spinnerNbItem = dialog.findViewById(R.id.spinner_eq_nb_item_create);
        Spinner spinnerType = dialog.findViewById(R.id.spinner_eq_type_create);
        EditText etMasseVide = dialog.findViewById(R.id.et_eq_masse_vide_create);

        Button btnAnnuler = dialog.findViewById(R.id.btn_eq_annuler_create);
        Button btnValider = dialog.findViewById(R.id.btn_eq_valider_create);

        etNom.setText(equipement.getNom());
        etDesc.setText(equipement.getDescription());
        etMasse.setText(String.valueOf(equipement.getMasseGrammes()));

        if (equipement.getMasseAVide() > 0) {
            etMasseVide.setText(String.valueOf(equipement.getMasseAVide()));
        }

        Integer[] items = new Integer[]{1, 2, 3};
        ArrayAdapter<Integer> adapterNbItem = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        spinnerNbItem.setAdapter(adapterNbItem);
        spinnerNbItem.setSelection(equipement.getNbItem() - 1);

        String[] types = new String[]{"Sac", "Vêtement", "Bivouac", "Autre"};
        ArrayAdapter<String> adapterTypeStr = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        spinnerType.setAdapter(adapterTypeStr);

        if (equipement.getType() != null) {
            for (int i = 0; i < types.length; i++) {
                if (types[i].equalsIgnoreCase(equipement.getType().name())) {
                    spinnerType.setSelection(i);
                    break;
                }
            }
        }

        // Verrouillage
        etNom.setEnabled(false);
        etDesc.setEnabled(false);
        etMasse.setEnabled(false);
        etMasseVide.setEnabled(false);
        spinnerNbItem.setEnabled(false);
        spinnerType.setEnabled(false);

        // Configuration des boutons
        btnAnnuler.setVisibility(android.view.View.GONE);
        btnValider.setText("Fermer");
        btnValider.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}