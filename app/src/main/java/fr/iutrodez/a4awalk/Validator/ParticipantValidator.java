package fr.iutrodez.a4awalk.Validator;

import android.widget.EditText;
import android.widget.Spinner;

public class ParticipantValidator {

    public static boolean validate(
            EditText etAge,
            EditText etBesoinKcal,
            EditText etBesoinEau,
            EditText etCapacite,
            Spinner spinnerNiveau,
            Spinner spinnerMorphologie,
            boolean sacChecked
    ) {
        if (etAge.getText().toString().trim().isEmpty()) {
            etAge.setError("Veuillez entrer l'âge");
            etAge.requestFocus();
            return false;
        }

        if (spinnerNiveau.getSelectedItemPosition() == 0) {
            return false;
        }

        if (spinnerMorphologie.getSelectedItemPosition() == 0) {
            return false;
        }

        if (sacChecked && etCapacite.getText().toString().trim().isEmpty()) {
            etCapacite.setError("Veuillez entrer la capacité du sac");
            etCapacite.requestFocus();
            return false;
        }

        return true;
    }
}
