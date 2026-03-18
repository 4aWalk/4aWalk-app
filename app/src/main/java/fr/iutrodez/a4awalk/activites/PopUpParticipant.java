package fr.iutrodez.a4awalk.activites;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;

import fr.iutrodez.a4awalk.modeles.ParticipantCallback;
import fr.iutrodez.a4awalk.modeles.enums.ModeRandonnee;
import fr.iutrodez.a4awalk.utils.validators.ParticipantValidator;
import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;

public class PopUpParticipant {

    /**
     * Applique les dimensions correctes au Dialog :
     * - largeur : toute la largeur de l'écran
     * - hauteur : au maximum 90% de la hauteur de l'écran
     * Cela évite que les boutons tombent hors de l'écran sur les petits appareils.
     */
    private static void appliquerDimensionsDialog(Context context, Dialog dialog) {
        if (dialog.getWindow() == null) return;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int maxHeight = (int) (metrics.heightPixels * 0.90);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.height = maxHeight;
        dialog.getWindow().setAttributes(lp);
    }

    /**
     * Point d'entrée principal.
     */
    public static void afficherDialogParticipant(Context context, ModeRandonnee mode, String token,
                                                 int hikeId,
                                                 @Nullable Participant participant,
                                                 ParticipantCallback callback) {

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup_participant);

        Window window = dialog.getWindow();
        if (window != null) window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ParticipantViewHolder views = new ParticipantViewHolder(dialog);

        configurerComposantsCommuns(context, views);

        switch (mode) {
            case CREATION:
                setupModeCreation(context, dialog, views, token, hikeId, participant, callback);
                break;
            case CONSULTATION:
                setupModeConsultation(views, participant);
                break;
            case MODIFICATION:
                setupModeModification(context, dialog, views, token, hikeId, participant, callback);
                break;
        }

        // Actions communes
        views.btnClose.setOnClickListener(v -> dialog.dismiss());
        views.btnVoirSac.setOnClickListener(v -> {
            Intent intent = new Intent(context, SacActivity.class);
            context.startActivity(intent);
        });

        dialog.show();
        appliquerDimensionsDialog(context, dialog);
    }

    // --- CLASSE INTERNE POUR LA VUE ---
    private static class ParticipantViewHolder {
        final ImageButton btnClose;
        final Button btnVoirSac, btnAction, btnSupprimer;
        final EditText etNom, etPrenom, etAge, etBesoinKcal, etBesoinEau, etCapacite;
        final CheckBox cbSacADos;
        final Spinner spinnerNiveau, spinnerMorpho, spinnerMesParticipants;
        final TextView tvMesParticipants;
        final View separateurParticipants;

        ParticipantViewHolder(Dialog dialog) {
            btnClose = dialog.findViewById(R.id.btnClose);
            btnVoirSac = dialog.findViewById(R.id.btnVoirSac);
            btnAction = dialog.findViewById(R.id.btnAjouter);
            btnSupprimer = dialog.findViewById(R.id.btnSupprimer);
            etNom = dialog.findViewById(R.id.etNom);
            etPrenom = dialog.findViewById(R.id.etPrenom);
            etAge = dialog.findViewById(R.id.etAge);
            etBesoinKcal = dialog.findViewById(R.id.etBesoinKcal);
            etBesoinEau = dialog.findViewById(R.id.etBesoinEau);
            etCapacite = dialog.findViewById(R.id.etCapacite);
            cbSacADos = dialog.findViewById(R.id.cbSacADos);
            spinnerNiveau = dialog.findViewById(R.id.spinnerNiveau);
            spinnerMorpho = dialog.findViewById(R.id.spinnerMorphologie);
            spinnerMesParticipants = dialog.findViewById(R.id.spinner_mes_participants);
            tvMesParticipants = dialog.findViewById(R.id.tv_mes_participants);
            separateurParticipants = dialog.findViewById(R.id.separateur_participants);
        }
    }

    // --- METHODES PRIVEES DE LOGIQUE ET D'AFFICHAGE ---

    private static void configurerComposantsCommuns(Context context, ParticipantViewHolder views) {
        views.etCapacite.setEnabled(false);
        views.cbSacADos.setOnCheckedChangeListener((btn, isChecked) -> views.etCapacite.setEnabled(isChecked));
        configurerSpinner(context, views.spinnerNiveau, R.array.niveaux);
        configurerSpinner(context, views.spinnerMorpho, R.array.morphologies);
    }

    private static void setupModeCreation(Context context, Dialog dialog, ParticipantViewHolder views,
                                          String token, int hikeId,
                                          @Nullable Participant participant, ParticipantCallback callback) {
        if (participant != null) {
            remplirChamps(views, participant);
            views.btnAction.setText(R.string.btnModifier);
        } else {
            views.btnAction.setText(R.string.btnAjouter);
        }

        views.btnAction.setVisibility(View.VISIBLE);
        views.btnSupprimer.setVisibility(View.GONE);
        views.btnVoirSac.setVisibility(View.GONE);

        // Chargement des participants existants
        chargerEtAfficherMesParticipants(context, token, views);

        views.btnAction.setOnClickListener(v ->
                traiterSoumission(context, dialog, views, hikeId, 0, callback, false)
        );
    }

    private static void setupModeConsultation(ParticipantViewHolder views, Participant participant) {
        if (participant == null) return;
        remplirChamps(views, participant);
        verrouillerChamps(views);
        views.btnAction.setVisibility(View.GONE);
        views.btnSupprimer.setVisibility(View.GONE);
        views.cbSacADos.setEnabled(false);
    }

    private static void setupModeModification(Context context, Dialog dialog, ParticipantViewHolder views,
                                              String token, int hikeId,
                                              Participant participant, ParticipantCallback callback) {
        if (participant == null) return;

        remplirChamps(views, participant);
        views.btnAction.setText(R.string.btnModifier);
        views.btnAction.setVisibility(View.VISIBLE);
        views.btnVoirSac.setVisibility(View.GONE);
        views.btnSupprimer.setVisibility(View.VISIBLE);

        // Chargement des participants existants
        chargerEtAfficherMesParticipants(context, token, views);

        // Appel de notre nouvelle méthode (au lieu de views.btnVoirSac.setVisibility(View.GONE))
        configurerBoutonSacADos(context, views, participant);

        views.btnAction.setOnClickListener(v ->
                traiterSoumission(context, dialog, views, hikeId, participant.getId(), callback, true)
        );

        views.btnSupprimer.setOnClickListener(v -> {
            if (callback != null) {
                callback.onDeleteAction(participant);
                dialog.dismiss();
            }
        });
    }

    private static void traiterSoumission(Context context, Dialog dialog, ParticipantViewHolder views,
                                          int hikeId, int participantId, ParticipantCallback callback, boolean isUpdate) {

        boolean isValidForm = ParticipantValidator.validate(
                views.etAge, views.etBesoinKcal, views.etBesoinEau, views.etCapacite,
                views.spinnerNiveau, views.spinnerMorpho, views.cbSacADos.isChecked()
        );

        if (!isValidForm) return;

        try {
            Participant p = extraireDonneesVues(views);
            p.setIdRando(hikeId);

            if (isUpdate) {
                p.setId(participantId);
            }

            if (callback != null) {
                callback.onActionSuccess(p);
                dialog.dismiss();
            }

        } catch (NumberFormatException e) {
            Log.e("erreur", e.getMessage());
            Toast.makeText(context, "Erreur de format numérique", Toast.LENGTH_SHORT).show();
        }
    }

    private static Participant extraireDonneesVues(ParticipantViewHolder v) {
        String nom = v.etNom.getText().toString().trim();
        String prenom = v.etPrenom.getText().toString().trim();
        int age = v.etAge.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(v.etAge.getText().toString().trim());
        int kcal = v.etBesoinKcal.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(v.etBesoinKcal.getText().toString().trim());
        double eau = v.etBesoinEau.getText().toString().trim().isEmpty() ? 0 : Double.parseDouble(v.etBesoinEau.getText().toString().trim());
        double cap = v.cbSacADos.isChecked() && !v.etCapacite.getText().toString().trim().isEmpty()
                ? Double.parseDouble(v.etCapacite.getText().toString().trim().replace(",", "."))
                : 0.0;

        return new Participant(nom, prenom, age,
                Level.valueOf(v.spinnerNiveau.getSelectedItem().toString()),
                Morphology.valueOf(v.spinnerMorpho.getSelectedItem().toString()),
                false, kcal, eau, cap, 0);
    }

    private static void configurerSpinner(Context context, Spinner spinner, int arrayResId) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item,
                context.getResources().getStringArray(arrayResId));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private static void remplirChamps(ParticipantViewHolder v, Participant p) {
        if (v.etNom != null) v.etNom.setText(p.getNom() != null ? p.getNom() : "");
        if (v.etPrenom != null) v.etPrenom.setText(p.getPrenom() != null ? p.getPrenom() : "");

        v.etAge.setText(p.getAge() > 0 ? String.valueOf(p.getAge()) : "");
        v.etBesoinKcal.setText(p.getBesoinKcal() > 0 ? String.valueOf(p.getBesoinKcal()) : "");
        v.etBesoinEau.setText(p.getBesoinEauLitre() > 0 ? String.valueOf(p.getBesoinEauLitre()) : "");

        if (p.getCapaciteEmportMaxKg() > 0) {
            v.cbSacADos.setChecked(true);
            v.etCapacite.setEnabled(true);
            v.etCapacite.setText(String.valueOf(p.getCapaciteEmportMaxKg()));
        } else {
            v.cbSacADos.setChecked(false);
            v.etCapacite.setText("");
        }

        if (p.getNiveau() != null) {
            selectionnerSpinner(v.spinnerNiveau, p.getNiveau().toString());
        }
        if (p.getMorphologie() != null) {
            selectionnerSpinner(v.spinnerMorpho, p.getMorphologie().toString());
        }
    }

    private static void verrouillerChamps(ParticipantViewHolder v) {
        v.etNom.setEnabled(false); v.etPrenom.setEnabled(false); v.etAge.setEnabled(false);
        v.etBesoinKcal.setEnabled(false); v.etBesoinEau.setEnabled(false); v.etCapacite.setEnabled(false);
        v.spinnerNiveau.setEnabled(false); v.spinnerMorpho.setEnabled(false);
    }

    private static void selectionnerSpinner(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(value);
            if (position >= 0) spinner.setSelection(position);
        }
    }

    /**
     * Configure l'affichage et l'action du bouton "Voir le sac" si le participant en possède un.
     */
    private static void configurerBoutonSacADos(Context context, ParticipantViewHolder views, Participant participant) {
        if (participant.getBackpack() != null) {
            views.btnVoirSac.setVisibility(View.VISIBLE);

            views.btnVoirSac.setOnClickListener(view -> {
                Intent intent = new Intent(context, SacActivity.class);

                intent.putExtra("POIDS_ACTUEL", participant.getBackpack().getTotalMassKg());
                intent.putExtra("CAPACITE_MAX", participant.getCapaciteEmportMaxKg());

                // 1. Équipements
                JSONArray equipementsArray = new JSONArray();
                if (participant.getBackpack().getEquipmentItems() != null) {
                    for (EquipmentItem eq : participant.getBackpack().getEquipmentItems()) {
                        try {
                            JSONObject obj = new JSONObject();
                            obj.put("nom", eq.getNom());
                            obj.put("masseGrammes", eq.getMasseGrammes());
                            obj.put("nbItem", eq.getNbItem() > 0 ? eq.getNbItem() : 1);
                            equipementsArray.put(obj);
                        } catch (Exception ignored) {}
                    }
                }
                intent.putExtra("EQUIPEMENTS_JSON", equipementsArray.toString());

                // 2. Nourriture
                JSONArray nourritureArray = new JSONArray();
                if (participant.getBackpack().getFoodItems() != null) {
                    for (FoodProduct fp : participant.getBackpack().getFoodItems()) {
                        try {
                            JSONObject obj = new JSONObject();
                            obj.put("nom", fp.getNom());
                            obj.put("masseGrammes", fp.getMasseGrammes());
                            obj.put("nbItem", fp.getNbItem() > 0 ? fp.getNbItem() : 1);
                            nourritureArray.put(obj);
                        } catch (Exception ignored) {}
                    }
                }
                intent.putExtra("NOURRITURE_JSON", nourritureArray.toString());

                context.startActivity(intent);
            });
        } else {
            views.btnVoirSac.setVisibility(View.GONE);
        }
    }

    private static void chargerEtAfficherMesParticipants(Context context, String token,
                                                         ParticipantViewHolder views) {
        ServiceParticipant.getMyParticipants(context, token, new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                try {
                    List<Participant> mesParticipants = new ArrayList<>();
                    List<String> noms = new ArrayList<>();
                    noms.add("— Sélectionner un participant existant —");
                    mesParticipants.add(null); // placeholder

                    for (int i = 0; i < result.length(); i++) {
                        Participant p = ServiceParticipant.parseParticipant(result.getJSONObject(i));
                        mesParticipants.add(p);
                        noms.add(p.getPrenom() + " " + p.getNom() + " (" + p.getNiveau() + ")");
                    }

                    if (mesParticipants.size() <= 1) return; // Aucun participant, on n'affiche pas

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                            android.R.layout.simple_spinner_item, noms);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    views.spinnerMesParticipants.setAdapter(adapter);

                    views.tvMesParticipants.setVisibility(View.VISIBLE);
                    views.spinnerMesParticipants.setVisibility(View.VISIBLE);
                    views.separateurParticipants.setVisibility(View.VISIBLE);

                    views.spinnerMesParticipants.setOnItemSelectedListener(
                            new android.widget.AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(android.widget.AdapterView<?> parent,
                                                           View view, int position, long id) {
                                    if (position == 0) return; // placeholder
                                    Participant p = mesParticipants.get(position);
                                    if (p != null) remplirChamps(views, p);
                                }

                                @Override
                                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                            });

                } catch (JSONException e) {
                    Log.e("PopUpParticipant", "Erreur parsing mes participants", e);
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e("PopUpParticipant", "Erreur chargement mes participants");
            }
        });
    }
}