package fr.iutrodez.a4awalk.modelesTest.entitesTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import fr.iutrodez.a4awalk.modeles.entites.FoodProduct;

/**
 * Classe de tests unitaires pour {@link FoodProduct}.
 *
 * <p>Ces tests s'exécutent dans {@code test/} (JVM pure, sans émulateur)
 * car {@link FoodProduct} ne dépend d'aucune classe Android.</p>
 *
 * <p>Catégories couvertes :</p>
 * <ul>
 *   <li><b>Constructeurs</b> : initialisation correcte des champs via le
 *       constructeur vide et le constructeur complet.</li>
 *   <li><b>{@code getWeightKg()}</b> : conversion grammes → kilogrammes.</li>
 *   <li><b>{@code getEnergyDensity()}</b> : calcul du ratio Kcal/gramme,
 *       cas nominaux, limites (masse nulle, masse négative) et erreurs.</li>
 *   <li><b>{@code equals()}</b> : égalité par id, par nom, réflexivité,
 *       comparaison avec null et objet d'un autre type.</li>
 *   <li><b>{@code toString()}</b> : format correct avec nom, conditionnement,
 *       apport calorique et prix.</li>
 * </ul>
 *
 * <p><b>Emplacement du fichier :</b>
 * {@code app/src/test/java/fr/iutrodez/a4awalk/modeles/entites/FoodProductTest.java}</p>
 *
 * @author Votre équipe
 * @version 1.0
 * @see FoodProduct
 */
public class FoodProductTest {

    // -------------------------------------------------------------------------
    // Constantes de test
    // -------------------------------------------------------------------------

    private static final String NOM_NOMINAL              = "Barre de céréales";
    private static final String DESC_NOMINALE            = "Barre énergétique";
    private static final double MASSE_NOMINALE_G         = 500.0;
    private static final String APPELLATION_NOMINALE     = "Barre";
    private static final String CONDITIONNEMENT_NOMINAL  = "Sachet 5 barres";
    private static final double KCAL_NOMINALE            = 250.0;
    private static final double PRIX_NOMINAL             = 3.50;

    // -------------------------------------------------------------------------
    // Champs
    // -------------------------------------------------------------------------

    /** Produit nominal réutilisé dans les tests. */
    private FoodProduct produitNominal;

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    /**
     * Crée un {@link FoodProduct} nominal avant chaque test.
     */
    @Before
    public void setUp() {
        produitNominal = new FoodProduct(
                NOM_NOMINAL,
                DESC_NOMINALE,
                MASSE_NOMINALE_G,
                APPELLATION_NOMINALE,
                CONDITIONNEMENT_NOMINAL,
                KCAL_NOMINALE,
                PRIX_NOMINAL
        );
    }

    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================

    /**
     * Vérifie que le constructeur vide crée une instance sans crash
     * et laisse tous les champs à {@code null}.
     *
     * <p><b>Given</b> : aucun argument.<br>
     * <b>When</b>  : {@code new FoodProduct()} est appelé.<br>
     * <b>Then</b>  : tous les champs sont {@code null}.</p>
     */
    @Test
    public void testConstructeurVide_tousLesChampsNull() {
        // Given / When
        FoodProduct produit = new FoodProduct();

        // Then
        assertNull("Le nom doit être null",               produit.getNom());
        assertNull("La description doit être null",       produit.getDescription());
        assertNull("La masse doit être null",             produit.getMasseGrammes());
        assertNull("L'appellation doit être null",        produit.getAppellationCourante());
        assertNull("Le conditionnement doit être null",   produit.getConditionnement());
    }

    /**
     * Vérifie que le constructeur complet assigne correctement tous les champs.
     *
     * <p><b>Given</b> : tous les paramètres fournis.<br>
     * <b>When</b>  : le constructeur complet est appelé.<br>
     * <b>Then</b>  : chaque getter retourne la valeur passée au constructeur.</p>
     */
    @Test
    public void testConstructeurComplet_tousLesChampsCorrectementAssignes() {
        // Given / When — produitNominal créé dans setUp()

        // Then
        assertEquals("Le nom doit correspondre",             NOM_NOMINAL,             produitNominal.getNom());
        assertEquals("La description doit correspondre",     DESC_NOMINALE,           produitNominal.getDescription());
        assertEquals("La masse doit correspondre",           MASSE_NOMINALE_G,        produitNominal.getMasseGrammes(), 0.001);
        assertEquals("L'appellation doit correspondre",      APPELLATION_NOMINALE,    produitNominal.getAppellationCourante());
        assertEquals("Le conditionnement doit correspondre", CONDITIONNEMENT_NOMINAL, produitNominal.getConditionnement());
        assertEquals("Les kcal doivent correspondre",        KCAL_NOMINALE,           produitNominal.getApportNutritionnelKcal(), 0.001);
        assertEquals("Le prix doit correspondre",            PRIX_NOMINAL,            produitNominal.getPrixEuro(), 0.001);
    }

    // =========================================================================
    // getWeightKg()
    // =========================================================================

    /**
     * Vérifie que {@code getWeightKg()} convertit correctement 500g en 0.5 kg.
     *
     * <p><b>Given</b> : un produit de {@code 500g}.<br>
     * <b>When</b>  : {@code getWeightKg()} est appelé.<br>
     * <b>Then</b>  : retourne {@code 0.5}.</p>
     */
    @Test
    public void testGetWeightKg_cinqCentsGrammes_retourneDemiKg() {
        // Given — produitNominal avec 500g

        // When
        double poids = produitNominal.getWeightKg();

        // Then
        assertEquals("500g doit être converti en 0.5 kg", 0.5, poids, 0.001);
    }

    /**
     * Vérifie que {@code getWeightKg()} retourne {@code 0.0} pour une masse nulle.
     *
     * <p><b>Given</b> : un produit de {@code 0g}.<br>
     * <b>When</b>  : {@code getWeightKg()} est appelé.<br>
     * <b>Then</b>  : retourne {@code 0.0}.</p>
     */
    @Test
    public void testGetWeightKg_masseNulle_retourneZero() {
        // Given
        FoodProduct produit = new FoodProduct("Eau", "desc", 0.0, "app", "cond", 0.0, 0.0);

        // When
        double poids = produit.getWeightKg();

        // Then
        assertEquals("0g doit être converti en 0.0 kg", 0.0, poids, 0.001);
    }

    /**
     * Vérifie que {@code getWeightKg()} gère correctement 1000g (= 1 kg exact).
     *
     * <p><b>Given</b> : un produit de {@code 1000g}.<br>
     * <b>When</b>  : {@code getWeightKg()} est appelé.<br>
     * <b>Then</b>  : retourne exactement {@code 1.0}.</p>
     */
    @Test
    public void testGetWeightKg_milleGrammes_retourneUnKg() {
        // Given
        FoodProduct produit = new FoodProduct("Conserve", "desc", 1000.0, "app", "cond", 150.0, 1.5);

        // When
        double poids = produit.getWeightKg();

        // Then
        assertEquals("1000g doit être converti en 1.0 kg", 1.0, poids, 0.001);
    }

    // =========================================================================
    // getEnergyDensity()
    // =========================================================================

    /**
     * Vérifie que {@code getEnergyDensity()} calcule correctement le ratio Kcal/gramme.
     *
     * <p><b>Given</b> : 250 Kcal pour 500g.<br>
     * <b>When</b>  : {@code getEnergyDensity()} est appelé.<br>
     * <b>Then</b>  : retourne {@code 0.5} Kcal/g.</p>
     */
    @Test
    public void testGetEnergyDensity_deuxCentCinquanteKcalCinqCentsG_retourneDemiKcalParG() {
        // Given — produitNominal : 250 Kcal / 500g

        // When
        double densite = produitNominal.getEnergyDensity();

        // Then
        assertEquals("250 Kcal / 500g doit donner 0.5 Kcal/g", 0.5, densite, 0.001);
    }

    /**
     * Vérifie que {@code getEnergyDensity()} retourne {@code 0} quand la masse est nulle.
     *
     * <p><b>Given</b> : un produit avec {@code masseGrammes = 0}.<br>
     * <b>When</b>  : {@code getEnergyDensity()} est appelé.<br>
     * <b>Then</b>  : retourne {@code 0} (protection contre la division par zéro).</p>
     */
    @Test
    public void testGetEnergyDensity_masseNulle_retourneZero() {
        // Given
        FoodProduct produit = new FoodProduct("Produit", "desc", 0.0, "app", "cond", 100.0, 1.0);

        // When
        double densite = produit.getEnergyDensity();

        // Then
        assertEquals("Une masse de 0g doit retourner une densité de 0 (pas de division par zéro)",
                0.0, densite, 0.001);
    }

    /**
     * Vérifie que {@code getEnergyDensity()} retourne {@code 0} quand la masse est négative.
     *
     * <p><b>Given</b> : un produit avec {@code masseGrammes = -100}.<br>
     * <b>When</b>  : {@code getEnergyDensity()} est appelé.<br>
     * <b>Then</b>  : retourne {@code 0} (guard {@code masseGrammes <= 0}).</p>
     */
    @Test
    public void testGetEnergyDensity_masseNegative_retourneZero() {
        // Given
        FoodProduct produit = new FoodProduct();
        produit.setMasseGrammes(-100.0);
        produit.setApportNutritionnelKcal(200.0);

        // When
        double densite = produit.getEnergyDensity();

        // Then
        assertEquals("Une masse négative doit retourner une densité de 0",
                0.0, densite, 0.001);
    }

    /**
     * Vérifie que {@code getEnergyDensity()} retourne {@code 0} quand l'apport
     * calorique est nul (produit sans calories).
     *
     * <p><b>Given</b> : {@code apportNutritionnelKcal = 0} et masse positive.<br>
     * <b>When</b>  : {@code getEnergyDensity()} est appelé.<br>
     * <b>Then</b>  : retourne {@code 0.0}.</p>
     */
    @Test
    public void testGetEnergyDensity_kcalNulle_retourneZero() {
        // Given
        FoodProduct produit = new FoodProduct("Eau plate", "desc", 500.0, "app", "cond", 0.0, 0.5);

        // When
        double densite = produit.getEnergyDensity();

        // Then
        assertEquals("Un apport calorique de 0 doit donner une densité de 0",
                0.0, densite, 0.001);
    }

    /**
     * Vérifie que {@code getEnergyDensity()} retourne une valeur élevée
     * pour un aliment très calorique et léger (aliment idéal randonnée).
     *
     * <p><b>Given</b> : 600 Kcal pour 100g (chocolat noir).<br>
     * <b>When</b>  : {@code getEnergyDensity()} est appelé.<br>
     * <b>Then</b>  : retourne {@code 6.0} Kcal/g.</p>
     */
    @Test
    public void testGetEnergyDensity_alimentTresCalorique_retourneRatioEleve() {
        // Given
        FoodProduct chocolat = new FoodProduct("Chocolat noir", "desc", 100.0, "app", "tablette", 600.0, 2.0);

        // When
        double densite = chocolat.getEnergyDensity();

        // Then
        assertEquals("600 Kcal / 100g doit donner 6.0 Kcal/g", 6.0, densite, 0.001);
    }

    // =========================================================================
    // equals()
    // =========================================================================

    /**
     * Vérifie que deux produits avec le même id sont égaux.
     *
     * <p><b>Given</b> : deux instances avec {@code id = 1L}.<br>
     * <b>When</b>  : {@code equals()} est appelé.<br>
     * <b>Then</b>  : retourne {@code true}.</p>
     */
    @Test
    public void testEquals_memeId_retourneTrue() {
        // Given
        FoodProduct p1 = new FoodProduct("Barre", "desc", 100.0, "app", "cond", 200.0, 1.0);
        FoodProduct p2 = new FoodProduct("Noix",  "desc", 200.0, "app", "cond", 300.0, 2.0);
        p1.setId(1L);
        p2.setId(1L);

        // When / Then
        assertTrue("Deux produits avec le même id doivent être égaux", p1.equals(p2));
    }

    /**
     * Vérifie que deux produits avec le même nom sont égaux (clé naturelle).
     *
     * <p><b>Given</b> : deux instances avec le même nom mais des ids différents.<br>
     * <b>When</b>  : {@code equals()} est appelé.<br>
     * <b>Then</b>  : retourne {@code true}.</p>
     */
    @Test
    public void testEquals_memeNom_retourneTrue() {
        // Given
        FoodProduct p1 = new FoodProduct("Barre", "desc1", 100.0, "app", "cond", 200.0, 1.0);
        FoodProduct p2 = new FoodProduct("Barre", "desc2", 150.0, "app", "cond", 250.0, 2.0);
        p1.setId(1L);
        p2.setId(2L);

        // When / Then
        assertTrue("Deux produits avec le même nom doivent être égaux", p1.equals(p2));
    }

    /**
     * Vérifie que deux produits avec des ids et des noms différents sont inégaux.
     *
     * <p><b>Given</b> : deux instances complètement distinctes.<br>
     * <b>When</b>  : {@code equals()} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testEquals_idEtNomDifferents_retourneFalse() {
        // Given
        FoodProduct p1 = new FoodProduct("Barre", "desc", 100.0, "app", "cond", 200.0, 1.0);
        FoodProduct p2 = new FoodProduct("Noix",  "desc", 200.0, "app", "cond", 300.0, 2.0);
        p1.setId(1L);
        p2.setId(2L);

        // When / Then
        assertFalse("Deux produits distincts ne doivent pas être égaux", p1.equals(p2));
    }

    /**
     * Vérifie qu'un produit est égal à lui-même (réflexivité).
     *
     * <p><b>Given</b> : un même objet {@link FoodProduct}.<br>
     * <b>When</b>  : {@code equals(produit)} est appelé sur lui-même.<br>
     * <b>Then</b>  : retourne {@code true}.</p>
     */
    @Test
    public void testEquals_memeObjet_retourneTrue() {
        // Given / When / Then
        assertTrue("Un produit doit être égal à lui-même",
                produitNominal.equals(produitNominal));
    }

    /**
     * Vérifie qu'un produit n'est pas égal à {@code null}.
     *
     * <p><b>Given</b> : {@code null} comme argument.<br>
     * <b>When</b>  : {@code equals(null)} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testEquals_comparaisonAvecNull_retourneFalse() {
        // Given / When / Then
        assertFalse("Un produit ne doit pas être égal à null",
                produitNominal.equals(null));
    }

    /**
     * Vérifie qu'un produit n'est pas égal à un objet d'un autre type.
     *
     * <p><b>Given</b> : une {@code String} comme argument.<br>
     * <b>When</b>  : {@code equals(String)} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testEquals_comparaisonAvecAutreType_retourneFalse() {
        // Given / When / Then
        assertFalse("Un produit ne doit pas être égal à un objet d'un autre type",
                produitNominal.equals("Barre de céréales"));
    }

    // =========================================================================
    // toString()
    // =========================================================================

    /**
     * Vérifie que {@code toString()} contient le nom du produit.
     *
     * <p><b>Given</b> : un produit avec le nom "Barre de céréales".<br>
     * <b>When</b>  : {@code toString()} est appelé.<br>
     * <b>Then</b>  : la chaîne contient {@code "Barre de céréales"}.</p>
     */
    @Test
    public void testToString_contientLeNom() {
        // Given / When
        String resultat = produitNominal.toString();

        // Then
        assertTrue("toString() doit contenir le nom du produit",
                resultat.contains(NOM_NOMINAL));
    }

    /**
     * Vérifie que {@code toString()} contient le conditionnement.
     *
     * <p><b>Given</b> : un produit avec le conditionnement "Sachet 5 barres".<br>
     * <b>When</b>  : {@code toString()} est appelé.<br>
     * <b>Then</b>  : la chaîne contient {@code "Sachet 5 barres"}.</p>
     */
    @Test
    public void testToString_contientLeConditionnement() {
        // Given / When
        String resultat = produitNominal.toString();

        // Then
        assertTrue("toString() doit contenir le conditionnement",
                resultat.contains(CONDITIONNEMENT_NOMINAL));
    }

    /**
     * Vérifie que {@code toString()} contient l'apport calorique formaté.
     *
     * <p><b>Given</b> : un produit avec 250 Kcal.<br>
     * <b>When</b>  : {@code toString()} est appelé.<br>
     * <b>Then</b>  : la chaîne contient {@code "250"}.</p>
     */
    @Test
    public void testToString_contientLesKcal() {
        // Given / When
        String resultat = produitNominal.toString();

        // Then
        assertTrue("toString() doit contenir la valeur calorique",
                resultat.contains("250"));
    }
}
