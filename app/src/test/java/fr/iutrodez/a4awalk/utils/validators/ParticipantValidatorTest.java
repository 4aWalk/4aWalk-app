package fr.iutrodez.a4awalk.utils.validators;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.widget.EditText;
import android.widget.Spinner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests unitaires pour {@link ParticipantValidator}.
 *
 * Chaque EditText et Spinner est mocké car ce sont des vues Android
 * non instanciables dans un contexte JVM pur.
 */
@RunWith(MockitoJUnitRunner.class)
public class ParticipantValidatorTest {

    @Mock EditText etAge;
    @Mock EditText etBesoinKcal;
    @Mock EditText etBesoinEau;
    @Mock EditText etCapacite;
    @Mock Spinner  spinnerNiveau;
    @Mock Spinner  spinnerMorphologie;

    // -----------------------------------------------------------------------
    // Configuration par défaut : toutes les valeurs correctes
    // -----------------------------------------------------------------------

    @Before
    public void setUp() {
        // Valeurs par défaut valides
        mockText(etAge,        "25");
        mockText(etBesoinKcal, "2000");
        mockText(etBesoinEau,  "2.5");
        mockText(etCapacite,   "10");
        when(spinnerNiveau.getSelectedItemPosition()).thenReturn(1);
        when(spinnerMorphologie.getSelectedItemPosition()).thenReturn(1);
    }

    private void mockText(EditText et, String value) {
        android.text.Editable editable = mock(android.text.Editable.class);
        when(editable.toString()).thenReturn(value);
        when(et.getText()).thenReturn(editable);
    }

    // -----------------------------------------------------------------------
    // Tests sur l'âge
    // -----------------------------------------------------------------------

    @Test
    public void validate_ageVide_retourneFalse() {
        mockText(etAge, "");
        assertFalse(callValidate(false));
        verify(etAge).setError("Veuillez entrer l'âge");
    }

    @Test
    public void validate_ageNonNumerique_retourneFalse() {
        mockText(etAge, "abc");
        assertFalse(callValidate(false));
        verify(etAge).setError("L'âge doit être un nombre");
    }

    @Test
    public void validate_ageTropBas_retourneFalse() {
        mockText(etAge, "9");
        assertFalse(callValidate(false));
        verify(etAge).setError("L'âge doit être entre 10 et 99");
    }

    @Test
    public void validate_ageTropHaut_retourneFalse() {
        mockText(etAge, "100");
        assertFalse(callValidate(false));
        verify(etAge).setError("L'âge doit être entre 10 et 99");
    }

    @Test
    public void validate_ageLimiteBasseValide_retourneTrue() {
        mockText(etAge, "10");
        assertTrue(callValidate(false));
    }

    @Test
    public void validate_ageLimiteHauteValide_retourneTrue() {
        mockText(etAge, "99");
        assertTrue(callValidate(false));
    }

    // -----------------------------------------------------------------------
    // Tests sur le besoin calorique
    // -----------------------------------------------------------------------

    @Test
    public void validate_kcalVide_retourneFalse() {
        mockText(etBesoinKcal, "");
        assertFalse(callValidate(false));
        verify(etBesoinKcal).setError("Veuillez entrer le besoin calorique");
    }

    @Test
    public void validate_kcalNonNumerique_retourneFalse() {
        mockText(etBesoinKcal, "abc");
        assertFalse(callValidate(false));
        verify(etBesoinKcal).setError("Le besoin calorique doit être un nombre");
    }

    @Test
    public void validate_kcalTropBas_retourneFalse() {
        mockText(etBesoinKcal, "1700");
        assertFalse(callValidate(false));
        verify(etBesoinKcal).setError("Le besoin calorique doit être entre 1700 et 10000 kcal");
    }

    @Test
    public void validate_kcalTropHaut_retourneFalse() {
        mockText(etBesoinKcal, "10001");
        assertFalse(callValidate(false));
        verify(etBesoinKcal).setError("Le besoin calorique doit être entre 1700 et 10000 kcal");
    }

    @Test
    public void validate_kcalLimiteHauteValide_retourneTrue() {
        mockText(etBesoinKcal, "10000");
        assertTrue(callValidate(false));
    }

    // -----------------------------------------------------------------------
    // Tests sur le besoin en eau
    // -----------------------------------------------------------------------

    @Test
    public void validate_eauVide_retourneFalse() {
        mockText(etBesoinEau, "");
        assertFalse(callValidate(false));
        verify(etBesoinEau).setError("Veuillez entrer le besoin en eau");
    }

    @Test
    public void validate_eauNonNumerique_retourneFalse() {
        mockText(etBesoinEau, "abc");
        assertFalse(callValidate(false));
        verify(etBesoinEau).setError("Le besoin en eau doit être un nombre");
    }

    @Test
    public void validate_eauTropBasse_retourneFalse() {
        mockText(etBesoinEau, "1");
        assertFalse(callValidate(false));
        verify(etBesoinEau).setError("Le besoin en eau doit être entre 1 et 8 litres");
    }

    @Test
    public void validate_eauTropHaute_retourneFalse() {
        mockText(etBesoinEau, "8.1");
        assertFalse(callValidate(false));
        verify(etBesoinEau).setError("Le besoin en eau doit être entre 1 et 8 litres");
    }

    @Test
    public void validate_eauLimiteHauteValide_retourneTrue() {
        mockText(etBesoinEau, "8");
        assertTrue(callValidate(false));
    }

    // -----------------------------------------------------------------------
    // Tests sur le sac à dos (sacChecked = true)
    // -----------------------------------------------------------------------

    @Test
    public void validate_sacChecked_capaciteVide_retourneFalse() {
        mockText(etCapacite, "");
        assertFalse(callValidate(true));
        verify(etCapacite).setError("Veuillez entrer la capacité du sac");
    }

    @Test
    public void validate_sacChecked_capaciteNonNumerique_retourneFalse() {
        mockText(etCapacite, "xyz");
        assertFalse(callValidate(true));
        verify(etCapacite).setError("La capacité du sac doit être un nombre");
    }

    @Test
    public void validate_sacChecked_capaciteNegative_retourneFalse() {
        mockText(etCapacite, "0");
        assertFalse(callValidate(true));
        verify(etCapacite).setError("Le sac à dos doit peser entre 0 et 35 kg");
    }

    @Test
    public void validate_sacChecked_capaciteTropHaute_retourneFalse() {
        mockText(etCapacite, "31");
        assertFalse(callValidate(true));
        verify(etCapacite).setError("Le sac à dos doit peser entre 0 et 35 kg");
    }

    @Test
    public void validate_sacChecked_capaciteValide_retourneTrue() {
        mockText(etCapacite, "15");
        assertTrue(callValidate(true));
    }

    @Test
    public void validate_sacNonChecked_capaciteIgnoree_retourneTrue() {
        // Même si capacite est vide, sacChecked=false donc pas de vérification
        mockText(etCapacite, "");
        assertTrue(callValidate(false));
    }

    // -----------------------------------------------------------------------
    // Tests sur les Spinners
    // -----------------------------------------------------------------------

    @Test
    public void validate_spinnerNiveauPosition0_retourneFalse() {
        when(spinnerNiveau.getSelectedItemPosition()).thenReturn(0);
        assertFalse(callValidate(false));
    }

    @Test
    public void validate_spinnerMorphologiePosition0_retourneFalse() {
        when(spinnerMorphologie.getSelectedItemPosition()).thenReturn(0);
        assertFalse(callValidate(false));
    }

    // -----------------------------------------------------------------------
    // Test du cas entièrement valide
    // -----------------------------------------------------------------------

    @Test
    public void validate_toutesValeursValides_retourneTrue() {
        assertTrue(callValidate(false));
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private boolean callValidate(boolean sacChecked) {
        return ParticipantValidator.validate(
                etAge, etBesoinKcal, etBesoinEau, etCapacite,
                spinnerNiveau, spinnerMorphologie, sacChecked);
    }
}
