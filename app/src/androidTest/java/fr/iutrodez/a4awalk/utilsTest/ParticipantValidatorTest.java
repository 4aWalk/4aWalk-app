package fr.iutrodez.a4awalk.utilsTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import fr.iutrodez.a4awalk.utils.ParticipantValidator;

/**
 * Classe de test instrumentée pour {@link ParticipantValidator}.
 *
 * <p>Cette classe vérifie le comportement du validateur de formulaire participant,
 * notamment :</p>
 * <ul>
 *     <li>La validation de l'âge (vide, hors bornes, format invalide)</li>
 *     <li>La validation du besoin calorique</li>
 *     <li>La validation du besoin en eau</li>
 *     <li>La validation de la capacité du sac à dos</li>
 *     <li>La validation des spinners niveau et morphologie</li>
 * </ul>
 *
 * <p>Les tests couvrent trois catégories :</p>
 * <ul>
 *     <li><b>Nominaux</b> : formulaire valide dans des conditions normales</li>
 *     <li><b>Limites</b> : valeurs aux bornes de chaque champ</li>
 *     <li><b>Erreurs</b> : champs vides, format invalide, valeurs hors bornes</li>
 * </ul>
 *
 * @author Votre nom
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class ParticipantValidatorTest {

    // =========================================================================
    // ATTRIBUTS
    // =========================================================================

    /** Contexte Android fourni par le runner de test */
    private Context contexte;

    /** Champ âge */
    private EditText etAge;

    /** Champ besoin calorique */
    private EditText etBesoinKcal;

    /** Champ besoin en eau */
    private EditText etBesoinEau;

    /** Champ capacité du sac */
    private EditText etCapacite;

    /** Spinner niveau */
    private Spinner spinnerNiveau;

    /** Spinner morphologie */
    private Spinner spinnerMorphologie;

    // =========================================================================
    // INITIALISATION
    // =========================================================================

    /**
     * Initialise tous les composants graphiques avant chaque test
     * avec des valeurs valides par défaut.
     */
    @Before
    public void setUp() {
        contexte = ApplicationProvider.getApplicationContext();

        // Initialisation des EditText
        etAge = new EditText(contexte);
        etBesoinKcal = new EditText(contexte);
        etBesoinEau = new EditText(contexte);
        etCapacite = new EditText(contexte);

        // Valeurs valides par défaut
        etAge.setText("30");
        etBesoinKcal.setText("2000");
        etBesoinEau.setText("2.5");
        etCapacite.setText("10");

        // Initialisation des spinners avec position 1 (valide) par défaut
        spinnerNiveau = construireSpinner(
                Arrays.asList("-- Choisir --", "DEBUTANT", "ENTRAINE", "SPORTIF")
        );
        spinnerMorphologie = construireSpinner(
                Arrays.asList("-- Choisir --", "LEGERE", "MOYENNE", "FORTE")
        );

        // Sélection de la position 1 (valide) pour les deux spinners
        spinnerNiveau.setSelection(1);
        spinnerMorphologie.setSelection(1);
    }

    // =========================================================================
    // MÉTHODES UTILITAIRES
    // =========================================================================

    /**
     * Construit un {@link Spinner} avec la liste d'éléments fournie.
     *
     * @param elements Liste des éléments du spinner
     * @return {@link Spinner} configuré avec les éléments
     */
    private Spinner construireSpinner(List<String> elements) {
        Spinner spinner = new Spinner(contexte);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                contexte,
                android.R.layout.simple_spinner_item,
                elements
        );
        spinner.setAdapter(adapter);
        return spinner;
    }

    /**
     * Appelle la méthode {@code validate} avec les champs courants
     * et le paramètre sacChecked fourni.
     *
     * @param sacChecked true si le sac à dos est coché
     * @return Résultat de la validation
     */
    private boolean valider(boolean sacChecked) {
        return ParticipantValidator.validate(
                etAge, etBesoinKcal, etBesoinEau, etCapacite,
                spinnerNiveau, spinnerMorphologie, sacChecked
        );
    }

    // =========================================================================
    // TESTS — CAS NOMINAUX
    // =========================================================================

    /**
     * Vérifie que la validation retourne true avec tous les champs valides
     * et le sac à dos non coché.
     *
     * <p><b>Cas nominal</b></p>
     */
    @Test
    public void testValidate_TousChampValidesSansSac_RetourneTrue() {
        // Given — tous les champs valides, sac non coché

        // When — on valide le formulaire sans sac
        boolean resultat = valider(false);

        // Then — la validation retourne true
        assertTrue("La validation doit retourner true avec des champs valides", resultat);
    }

    /**
     * Vérifie que la validation retourne true avec tous les champs valides
     * et le sac à dos coché avec une capacité valide.
     *
     * <p><b>Cas nominal</b></p>
     */
    @Test
    public void testValidate_TousChampValidesAvecSac_RetourneTrue() {
        // Given — tous les champs valides, sac coché avec capacité valide
        etCapacite.setText("15");

        // When — on valide le formulaire avec sac
        boolean resultat = valider(true);

        // Then — la validation retourne true
        assertTrue("La validation doit retourner true avec sac et capacité valide",
                resultat);
    }

    // =========================================================================
    // TESTS — AGE — CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que la validation accepte l'âge minimal (1 an).
     *
     * <p><b>Cas limite</b> : âge = 1</p>
     */
    @Test
    public void testValidate_AgeMinimal_RetourneTrue() {
        // Given — âge minimal de 1 an
        etAge.setText("1");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne true
        assertTrue("La validation doit accepter l'âge minimal de 1 an", resultat);
    }

    /**
     * Vérifie que la validation accepte l'âge maximal (100 ans).
     *
     * <p><b>Cas limite</b> : âge = 100</p>
     */
    @Test
    public void testValidate_AgeMaximal_RetourneTrue() {
        // Given — âge maximal de 100 ans
        etAge.setText("100");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne true
        assertTrue("La validation doit accepter l'âge maximal de 100 ans", resultat);
    }

    // =========================================================================
    // TESTS — AGE — CAS ERREURS
    // =========================================================================

    /**
     * Vérifie que la validation rejette un âge vide.
     *
     * <p><b>Cas erreur</b> : âge vide</p>
     */
    @Test
    public void testValidate_AgeVide_RetourneFalse() {
        // Given — champ âge vide
        etAge.setText("");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter un âge vide", resultat);
    }

    /**
     * Vérifie que la validation rejette un âge de 0.
     *
     * <p><b>Cas erreur</b> : âge = 0</p>
     */
    @Test
    public void testValidate_AgeZero_RetourneFalse() {
        // Given — âge à 0
        etAge.setText("0");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter un âge de 0", resultat);
    }

    /**
     * Vérifie que la validation rejette un âge supérieur à 100.
     *
     * <p><b>Cas erreur</b> : âge = 101</p>
     */
    @Test
    public void testValidate_AgeSuperieur100_RetourneFalse() {
        // Given — âge supérieur à 100
        etAge.setText("101");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter un âge supérieur à 100", resultat);
    }

    /**
     * Vérifie que la validation rejette un âge négatif.
     *
     * <p><b>Cas erreur</b> : âge négatif</p>
     */
    @Test
    public void testValidate_AgeNegatif_RetourneFalse() {
        // Given — âge négatif
        etAge.setText("-1");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter un âge négatif", resultat);
    }

    /**
     * Vérifie que la validation rejette un âge avec format invalide (texte).
     *
     * <p><b>Cas erreur</b> : âge = "abc"</p>
     */
    @Test
    public void testValidate_AgeFormatInvalide_RetourneFalse() {
        // Given — âge au format invalide
        etAge.setText("abc");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter un âge non numérique", resultat);
    }

    // =========================================================================
    // TESTS — BESOIN CALORIQUE — CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que la validation accepte le besoin calorique minimal (1 kcal).
     *
     * <p><b>Cas limite</b> : kcal = 1</p>
     */
    @Test
    public void testValidate_KcalMinimal_RetourneTrue() {
        // Given — besoin calorique minimal de 1
        etBesoinKcal.setText("1");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne true
        assertTrue("La validation doit accepter un besoin calorique de 1", resultat);
    }

    /**
     * Vérifie que la validation accepte le besoin calorique maximal (10000 kcal).
     *
     * <p><b>Cas limite</b> : kcal = 10000</p>
     */
    @Test
    public void testValidate_KcalMaximal_RetourneTrue() {
        // Given — besoin calorique maximal de 10000
        etBesoinKcal.setText("10000");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne true
        assertTrue("La validation doit accepter un besoin calorique de 10000", resultat);
    }

    // =========================================================================
    // TESTS — BESOIN CALORIQUE — CAS ERREURS
    // =========================================================================

    /**
     * Vérifie que la validation rejette un besoin calorique vide.
     *
     * <p><b>Cas erreur</b> : kcal vide</p>
     */
    @Test
    public void testValidate_KcalVide_RetourneFalse() {
        // Given — champ besoin calorique vide
        etBesoinKcal.setText("");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter un besoin calorique vide", resultat);
    }

    /**
     * Vérifie que la validation rejette un besoin calorique supérieur à 10000.
     *
     * <p><b>Cas erreur</b> : kcal = 10001</p>
     */
    @Test
    public void testValidate_KcalSuperieur10000_RetourneFalse() {
        // Given — besoin calorique supérieur à 10000
        etBesoinKcal.setText("10001");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter un besoin calorique > 10000", resultat);
    }

    /**
     * Vérifie que la validation rejette un besoin calorique à 0.
     *
     * <p><b>Cas erreur</b> : kcal = 0</p>
     */
    @Test
    public void testValidate_KcalZero_RetourneFalse() {
        // Given — besoin calorique à 0
        etBesoinKcal.setText("0");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter un besoin calorique de 0", resultat);
    }

    /**
     * Vérifie que la validation rejette un besoin calorique au format invalide.
     *
     * <p><b>Cas erreur</b> : kcal = "abc"</p>
     */
    @Test
    public void testValidate_KcalFormatInvalide_RetourneFalse() {
        // Given — besoin calorique au format invalide
        etBesoinKcal.setText("abc");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter un besoin calorique non numérique",
                resultat);
    }

    // =========================================================================
    // TESTS — BESOIN EN EAU — CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que la validation accepte le besoin en eau minimal (0.1 litre).
     *
     * <p><b>Cas limite</b> : eau = 0.1</p>
     */
    @Test
    public void testValidate_EauMinimale_RetourneTrue() {
        // Given — besoin en eau minimal de 0.1 litre
        etBesoinEau.setText("0.1");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne true
        assertTrue("La validation doit accepter un besoin en eau de 0.1 litre", resultat);
    }

    /**
     * Vérifie que la validation accepte le besoin en eau maximal (8 litres).
     *
     * <p><b>Cas limite</b> : eau = 8</p>
     */
    @Test
    public void testValidate_EauMaximale_RetourneTrue() {
        // Given — besoin en eau maximal de 8 litres
        etBesoinEau.setText("8");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne true
        assertTrue("La validation doit accepter un besoin en eau de 8 litres", resultat);
    }

    // =========================================================================
    // TESTS — BESOIN EN EAU — CAS ERREURS
    // =========================================================================

    /**
     * Vérifie que la validation rejette un besoin en eau vide.
     *
     * <p><b>Cas erreur</b> : eau vide</p>
     */
    @Test
    public void testValidate_EauVide_RetourneFalse() {
        // Given — champ besoin en eau vide
        etBesoinEau.setText("");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter un besoin en eau vide", resultat);
    }

    /**
     * Vérifie que la validation rejette un besoin en eau supérieur à 8 litres.
     *
     * <p><b>Cas erreur</b> : eau = 8.1</p>
     */
    @Test
    public void testValidate_EauSuperieure8_RetourneFalse() {
        // Given — besoin en eau supérieur à 8 litres
        etBesoinEau.setText("8.1");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter un besoin en eau > 8 litres", resultat);
    }

    /**
     * Vérifie que la validation rejette un besoin en eau à 0.
     *
     * <p><b>Cas erreur</b> : eau = 0</p>
     */
    @Test
    public void testValidate_EauZero_RetourneFalse() {
        // Given — besoin en eau à 0
        etBesoinEau.setText("0");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter un besoin en eau de 0", resultat);
    }

    /**
     * Vérifie que la validation rejette un besoin en eau au format invalide.
     *
     * <p><b>Cas erreur</b> : eau = "abc"</p>
     */
    @Test
    public void testValidate_EauFormatInvalide_RetourneFalse() {
        // Given — besoin en eau au format invalide
        etBesoinEau.setText("abc");

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter un besoin en eau non numérique",
                resultat);
    }

    // =========================================================================
    // TESTS — CAPACITE SAC — CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que la validation accepte la capacité minimale du sac (0.1 kg).
     *
     * <p><b>Cas limite</b> : capacité = 0.1 kg</p>
     */
    @Test
    public void testValidate_CapaciteMinimale_RetourneTrue() {
        // Given — capacité minimale de 0.1 kg avec sac coché
        etCapacite.setText("0.1");

        // When — on valide le formulaire avec sac coché
        boolean resultat = valider(true);

        // Then — la validation retourne true
        assertTrue("La validation doit accepter une capacité de 0.1 kg", resultat);
    }

    /**
     * Vérifie que la validation accepte la capacité maximale du sac (30 kg).
     *
     * <p><b>Cas limite</b> : capacité = 30 kg</p>
     */
    @Test
    public void testValidate_CapaciteMaximale_RetourneTrue() {
        // Given — capacité maximale de 30 kg avec sac coché
        etCapacite.setText("30");

        // When — on valide le formulaire avec sac coché
        boolean resultat = valider(true);

        // Then — la validation retourne true
        assertTrue("La validation doit accepter une capacité de 30 kg", resultat);
    }

    // =========================================================================
    // TESTS — CAPACITE SAC — CAS ERREURS
    // =========================================================================

    /**
     * Vérifie que la validation rejette une capacité vide quand le sac est coché.
     *
     * <p><b>Cas erreur</b> : capacité vide avec sac coché</p>
     */
    @Test
    public void testValidate_CapaciteVideAvecSacCoche_RetourneFalse() {
        // Given — capacité vide avec sac coché
        etCapacite.setText("");

        // When — on valide le formulaire avec sac coché
        boolean resultat = valider(true);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter une capacité vide avec sac coché",
                resultat);
    }

    /**
     * Vérifie que la validation accepte une capacité vide quand le sac n'est pas coché.
     *
     * <p><b>Cas limite</b> : capacité vide avec sac non coché</p>
     */
    @Test
    public void testValidate_CapaciteVideSansSac_RetourneTrue() {
        // Given — capacité vide mais sac non coché
        etCapacite.setText("");

        // When — on valide le formulaire sans sac
        boolean resultat = valider(false);

        // Then — la validation retourne true car la capacité n'est pas requise
        assertTrue("La validation doit accepter une capacité vide si sac non coché",
                resultat);
    }

    /**
     * Vérifie que la validation rejette une capacité supérieure à 30 kg.
     *
     * <p><b>Cas erreur</b> : capacité = 30.1 kg</p>
     */
    @Test
    public void testValidate_CapaciteSuperieure30_RetourneFalse() {
        // Given — capacité supérieure à 30 kg avec sac coché
        etCapacite.setText("30.1");

        // When — on valide le formulaire avec sac coché
        boolean resultat = valider(true);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter une capacité > 30 kg", resultat);
    }

    /**
     * Vérifie que la validation rejette une capacité à 0 avec sac coché.
     *
     * <p><b>Cas erreur</b> : capacité = 0</p>
     */
    @Test
    public void testValidate_CapaciteZeroAvecSacCoche_RetourneFalse() {
        // Given — capacité à 0 avec sac coché
        etCapacite.setText("0");

        // When — on valide le formulaire avec sac coché
        boolean resultat = valider(true);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter une capacité de 0 avec sac coché",
                resultat);
    }

    /**
     * Vérifie que la validation rejette une capacité au format invalide.
     *
     * <p><b>Cas erreur</b> : capacité = "abc"</p>
     */
    @Test
    public void testValidate_CapaciteFormatInvalide_RetourneFalse() {
        // Given — capacité au format invalide avec sac coché
        etCapacite.setText("abc");

        // When — on valide le formulaire avec sac coché
        boolean resultat = valider(true);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter une capacité non numérique", resultat);
    }

    // =========================================================================
    // TESTS — SPINNERS — CAS ERREURS
    // =========================================================================

    /**
     * Vérifie que la validation rejette la sélection par défaut du spinner niveau
     * (position 0 = "-- Choisir --").
     *
     * <p><b>Cas erreur</b> : spinner niveau à la position 0</p>
     */
    @Test
    public void testValidate_SpinnerNiveauPosition0_RetourneFalse() {
        // Given — spinner niveau à la position 0 (non sélectionné)
        spinnerNiveau.setSelection(0);

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter le spinner niveau à la position 0",
                resultat);
    }

    /**
     * Vérifie que la validation rejette la sélection par défaut du spinner morphologie
     * (position 0 = "-- Choisir --").
     *
     * <p><b>Cas erreur</b> : spinner morphologie à la position 0</p>
     */
    @Test
    public void testValidate_SpinnerMorphologiePosition0_RetourneFalse() {
        // Given — spinner morphologie à la position 0 (non sélectionné)
        spinnerMorphologie.setSelection(0);

        // When — on valide le formulaire
        boolean resultat = valider(false);

        // Then — la validation retourne false
        assertFalse("La validation doit rejeter le spinner morphologie à la position 0",
                resultat);
    }

    /**
     * Vérifie que la validation accepte toutes les positions valides du spinner niveau.
     *
     * <p><b>Cas nominal</b> : positions 1, 2, 3</p>
     */
    @Test
    public void testValidate_SpinnerNiveauToutesPositionsValides_RetourneTrue() {
        // Given / When / Then — toutes les positions valides du spinner niveau
        for (int i = 1; i <= 3; i++) {
            spinnerNiveau.setSelection(i);
            assertTrue("La validation doit accepter le spinner niveau à la position " + i,
                    valider(false));
        }
    }

    /**
     * Vérifie que la validation accepte toutes les positions valides du spinner morphologie.
     *
     * <p><b>Cas nominal</b> : positions 1, 2, 3</p>
     */
    @Test
    public void testValidate_SpinnerMorphologieToutesPositionsValides_RetourneTrue() {
        // Given / When / Then — toutes les positions valides du spinner morphologie
        for (int i = 1; i <= 3; i++) {
            spinnerMorphologie.setSelection(i);
            assertTrue("La validation doit accepter le spinner morphologie à la position " + i,
                    valider(false));
        }
    }
}
