package fr.iutrodez.a4awalk.utils;

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
        // Vérification de l'âge
        String ageStr = etAge.getText().toString().trim();
        if (ageStr.isEmpty()) {
            etAge.setError("Veuillez entrer l'âge");
            etAge.requestFocus();
            return false;
        }
        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            etAge.setError("L'âge doit être un nombre");
            etAge.requestFocus();
            return false;
        }
        if (age <= 0 || age > 100) {
            etAge.setError("L'âge doit être entre 1 et 100");
            etAge.requestFocus();
            return false;
        }

        // Vérification du besoin calorique
        String kcalStr = etBesoinKcal.getText().toString().trim();
        if (kcalStr.isEmpty()) {
            etBesoinKcal.setError("Veuillez entrer le besoin calorique");
            etBesoinKcal.requestFocus();
            return false;
        }
        int kcal;
        try {
            kcal = Integer.parseInt(kcalStr);
        } catch (NumberFormatException e) {
            etBesoinKcal.setError("Le besoin calorique doit être un nombre");
            etBesoinKcal.requestFocus();
            return false;
        }
        if (kcal <= 0 || kcal > 10000) {
            etBesoinKcal.setError("Le besoin calorique doit être inférieur à 10000 kcal");
            etBesoinKcal.requestFocus();
            return false;
        }

        // Vérification du besoin en eau
        String eauStr = etBesoinEau.getText().toString().trim();
        if (eauStr.isEmpty()) {
            etBesoinEau.setError("Veuillez entrer le besoin en eau");
            etBesoinEau.requestFocus();
            return false;
        }
        double eau;
        try {
            eau = Double.parseDouble(eauStr);
        } catch (NumberFormatException e) {
            etBesoinEau.setError("Le besoin en eau doit être un nombre");
            etBesoinEau.requestFocus();
            return false;
        }
        if (eau <= 0 || eau > 8) {
            etBesoinEau.setError("Le besoin en eau doit être inférieur à 8 litres");
            etBesoinEau.requestFocus();
            return false;
        }

        // Vérification du sac à dos si coché
        if (sacChecked) {
            String capaciteStr = etCapacite.getText().toString().trim();
            if (capaciteStr.isEmpty()) {
                etCapacite.setError("Veuillez entrer la capacité du sac");
                etCapacite.requestFocus();
                return false;
            }
            double capacite;
            try {
                capacite = Double.parseDouble(capaciteStr);
            } catch (NumberFormatException e) {
                etCapacite.setError("La capacité du sac doit être un nombre");
                etCapacite.requestFocus();
                return false;
            }
            if (capacite <= 0 || capacite > 30) {
                etCapacite.setError("Le sac à dos doit peser moins de 30 kg");
                etCapacite.requestFocus();
                return false;
            }
        }

        // Vérification des spinners
        if (spinnerNiveau.getSelectedItemPosition() == 0) {
            return false;
        }

        if (spinnerMorphologie.getSelectedItemPosition() == 0) {
            return false;
        }

        return true;
    }
}
