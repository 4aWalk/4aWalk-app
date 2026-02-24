package fr.iutrodez.a4awalk.activites;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;

import fr.iutrodez.a4awalk.GestionP.Activity.SacActivity;
import fr.iutrodez.a4awalk.GestionP.Activity.Validator.ParticipantValidator;
import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceParticipant;

public class GestionParticipant {

    public static final int ETAT_CREATION = 1;
    public static final int ETAT_CONSULTATION = 2;
    public static final int ETAT_MODIFICATION = 3;

    public interface ParticipantCallback {
        void onActionSuccess(Participant participant);
    }

    /**
     * Point d'entrée principal.
     * @param hikeId L'ID de la randonnée (nécessaire pour modification/création contextuelle)
     */
    public static void afficherDialogParticipant(Context context, int etat, String token,
                                                 int hikeId,
                                                 @Nullable Participant participant,
                                                 ParticipantCallback callback) {

        if (etat < 1 || etat > 3) throw new IllegalArgumentException("État invalide");

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup_participant);

        Window window = dialog.getWindow();
        if (window != null) window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ViewHolder views = new ViewHolder(dialog);

        // Configuration Commune
        views.etCapacite.setEnabled(false);
        views.cbSacADos.setOnCheckedChangeListener((btn, isChecked) -> views.etCapacite.setEnabled(isChecked));
        configurerSpinner(context, views.spinnerNiveau, R.array.niveaux);
        configurerSpinner(context, views.spinnerMorpho, R.array.morphologies);

        // --- Gestion par État ---
        switch (etat) {
            case ETAT_CREATION:
                setupModeCreation(context, dialog, views, token, hikeId, callback);
                break;
            case ETAT_CONSULTATION:
                setupModeConsultation(views, participant);
                break;
            case ETAT_MODIFICATION:
                setupModeModification(context, dialog, views, token, hikeId, participant, callback);
                break;
        }

        views.btnClose.setOnClickListener(v -> dialog.dismiss());
        views.btnVoirSac.setOnClickListener(v -> {
            Intent intent = new Intent(context, SacActivity.class);
            context.startActivity(intent);
        });

        dialog.show();
    }

    private static void setupModeCreation(Context context, Dialog dialog, ViewHolder views, String token, int hikeId, ParticipantCallback callback) {
        views.btnAction.setText("Ajouter");
        views.btnAction.setVisibility(View.VISIBLE);
        // On passe 0 pour le participantId car c'est une création
        views.btnAction.setOnClickListener(v ->
                traiterSoumission(context, dialog, views, token, hikeId, 0, callback, false)
        );
    }

    private static void setupModeConsultation(ViewHolder views, Participant participant) {
        if (participant == null) return;
        remplirChamps(views, participant);
        verrouillerChamps(views);
        views.btnAction.setVisibility(View.GONE);
        views.cbSacADos.setEnabled(false);
    }

    private static void setupModeModification(Context context, Dialog dialog, ViewHolder views, String token, int hikeId, Participant participant, ParticipantCallback callback) {
        if (participant == null) return;

        remplirChamps(views, participant);
        views.btnAction.setText("Modifier");
        views.btnAction.setVisibility(View.VISIBLE);

        views.btnAction.setOnClickListener(v ->
                traiterSoumission(context, dialog, views, token, hikeId, participant.getId(), callback, true)
        );
    }

    private static void traiterSoumission(Context context, Dialog dialog, ViewHolder views, String token,
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
                p.setPId(participantId);
                // Appel API Modification avec gestion d'erreur intégrée dans le service
                ServiceParticipant.modifierParticipantAPI(context, token, p, () -> {
                    if (callback != null) callback.onActionSuccess(p);
                    dialog.dismiss();
                });
            } else {
                // Création locale (l'appel API se fera lors de la validation globale de la rando si c'est une création de rando)
                if (callback != null) {
                    callback.onActionSuccess(p);
                    dialog.dismiss();
                }
            }
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Erreur de format numérique", Toast.LENGTH_SHORT).show();
        }
    }

    private static Participant extraireDonneesVues(ViewHolder v) {
        String nom = v.etNom.getText().toString().trim();
        String prenom = v.etPrenom.getText().toString().trim();
        int age = Integer.parseInt(v.etAge.getText().toString().trim());
        int kcal = Integer.parseInt(v.etBesoinKcal.getText().toString().trim());
        int eau = Integer.parseInt(v.etBesoinEau.getText().toString().trim());
        double cap = v.cbSacADos.isChecked() && !v.etCapacite.getText().toString().isEmpty()
                ? Double.parseDouble(v.etCapacite.getText().toString().replace(",", "."))
                : 0.0;

        // MODIFICATION : On passe 0 au lieu de null à idRando ici, il est set correctement dans traiterSoumission
        return new Participant(nom, prenom, age,
                Level.valueOf(v.spinnerNiveau.getSelectedItem().toString()),
                Morphology.valueOf(v.spinnerMorpho.getSelectedItem().toString()),
                false, kcal, eau, cap, 0);
    }

    private static class ViewHolder {
        ImageButton btnClose;
        Button btnVoirSac, btnAction;
        EditText etNom, etPrenom, etAge, etBesoinKcal, etBesoinEau, etCapacite;
        CheckBox cbSacADos;
        Spinner spinnerNiveau, spinnerMorpho;

        ViewHolder(Dialog d) {
            btnClose = d.findViewById(R.id.btnClose);
            btnVoirSac = d.findViewById(R.id.btnVoirSac);
            btnAction = d.findViewById(R.id.btnAjouter);
            etNom = d.findViewById(R.id.etNom);
            etPrenom = d.findViewById(R.id.etPrenom);
            etAge = d.findViewById(R.id.etAge);
            etBesoinKcal = d.findViewById(R.id.etBesoinKcal);
            etBesoinEau = d.findViewById(R.id.etBesoinEau);
            etCapacite = d.findViewById(R.id.etCapacite);
            cbSacADos = d.findViewById(R.id.cbSacADos);
            spinnerNiveau = d.findViewById(R.id.spinnerNiveau);
            spinnerMorpho = d.findViewById(R.id.spinnerMorphologie);
        }
    }

    private static void configurerSpinner(Context context, Spinner spinner, int arrayResId) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,
                context.getResources().getStringArray(arrayResId));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private static void remplirChamps(ViewHolder v, Participant p) {
        if (v.etNom != null) v.etNom.setText(p.getNom());
        if (v.etPrenom != null) v.etPrenom.setText(p.getPrenom());
        v.etAge.setText(String.valueOf(p.getAge()));
        v.etBesoinKcal.setText(String.valueOf(p.getBesoinKcal()));
        v.etBesoinEau.setText(String.valueOf(p.getBesoinEauLitre()));

        if (p.getCapaciteEmportMaxKg() > 0) {
            v.cbSacADos.setChecked(true);
            v.etCapacite.setEnabled(true);
            v.etCapacite.setText(String.valueOf(p.getCapaciteEmportMaxKg()));
        } else {
            v.cbSacADos.setChecked(false);
            v.etCapacite.setText("");
        }
        selectionnerSpinner(v.spinnerNiveau, p.getNiveau().toString());
        selectionnerSpinner(v.spinnerMorpho, p.getMorphologie().toString());
    }

    private static void verrouillerChamps(ViewHolder v) {
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
}