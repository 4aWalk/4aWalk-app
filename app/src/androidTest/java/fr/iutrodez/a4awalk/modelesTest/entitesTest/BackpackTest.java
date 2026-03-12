package fr.iutrodez.a4awalk.modelesTest.entitesTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import fr.iutrodez.a4awalk.modeles.entites.Backpack;
import fr.iutrodez.a4awalk.modeles.entites.EquipmentItem;
import fr.iutrodez.a4awalk.modeles.entites.FoodProduct;
import fr.iutrodez.a4awalk.modeles.entites.Participant;

/**
 * Classe de tests unitaires pour {@link Backpack}.
 *
 * <p>Ces tests s'exécutent dans {@code test/} (JVM pure, sans émulateur)
 * car {@link Backpack} ne dépend d'aucune classe Android.</p>
 *
 * <p>Catégories couvertes :</p>
 * <ul>
 *   <li><b>Constructeurs</b> : initialisation correcte des valeurs par défaut.</li>
 *   <li><b>{@code updateAndGetTotalMass()}</b> : calcul du poids total avec
 *       équipements, aliments, combinaisons, et collections vides.</li>
 *   <li><b>{@code clearContent()}</b> : vidage complet du sac et remise à zéro
 *       du poids.</li>
 *   <li><b>{@code equals()}</b> : égalité basée sur l'id ou le propriétaire.</li>
 *   <li><b>Cas limites</b> : sac vide, un seul item, très grand nombre d'items.</li>
 *   <li><b>Cas d'erreur</b> : collections {@code null}, items à masse nulle.</li>
 * </ul>
 *
 * <p><b>Emplacement du fichier :</b>
 * {@code app/src/test/java/fr/iutrodez/a4awalk/modeles/entites/BackpackTest.java}</p>
 *
 * @author Votre équipe
 * @version 1.0
 * @see Backpack
 */
public class BackpackTest {

    // -------------------------------------------------------------------------
    // Champs
    // -------------------------------------------------------------------------

    /** Sac à dos testé, recréé avant chaque test. */
    private Backpack backpack;

    /** Participant propriétaire utilisé dans les tests. */
    private Participant owner;

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    /**
     * Crée un {@link Backpack} vide et un {@link Participant} propriétaire
     * avant chaque test.
     */
    @Before
    public void setUp() {
        owner   = new Participant();
        backpack = new Backpack(owner);
    }

    // -------------------------------------------------------------------------
    // Méthodes utilitaires
    // -------------------------------------------------------------------------

    /**
     * Crée un {@link FoodProduct} avec une masse donnée en grammes.
     *
     * @param nom          le nom du produit
     * @param masseGrammes la masse en grammes
     * @return une instance de {@link FoodProduct} prête pour les tests
     */
    private FoodProduct creerFoodProduct(String nom, double masseGrammes) {
        return new FoodProduct(nom, "desc", masseGrammes, "appellation", "cond", 100.0, 2.0);
    }

    /**
     * Crée un {@link EquipmentItem} avec une masse donnée en grammes.
     *
     * @param nom          le nom de l'équipement
     * @param masseGrammes la masse en grammes
     * @return une instance de {@link EquipmentItem} prête pour les tests
     */
    private EquipmentItem creerEquipmentItem(String nom, double masseGrammes) {
        return new EquipmentItem(nom, "desc", masseGrammes, false);
    }

    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================

    /**
     * Vérifie que le constructeur vide initialise {@code totalMassKg} à {@code 0.0}.
     *
     * <p><b>Given</b> : aucun argument.<br>
     * <b>When</b>  : {@code new Backpack()} est appelé.<br>
     * <b>Then</b>  : {@code getTotalMassKg()} retourne {@code 0.0}.</p>
     */
    @Test
    public void testConstructeurVide_totalMassInitialiseeAZero() {
        // Given / When
        Backpack backpackVide = new Backpack();

        // Then
        assertEquals("Le constructeur vide doit initialiser totalMassKg à 0.0",
                0.0, backpackVide.getTotalMassKg(), 0.001);
    }

    /**
     * Vérifie que le constructeur avec propriétaire assigne correctement le owner.
     *
     * <p><b>Given</b> : un {@link Participant} valide.<br>
     * <b>When</b>  : {@code new Backpack(owner)} est appelé.<br>
     * <b>Then</b>  : {@code getOwner()} retourne le même participant.</p>
     */
    @Test
    public void testConstructeurAvecOwner_ownerCorrectementAssigne() {
        // Given / When — backpack créé dans setUp()

        // Then
        assertEquals("Le owner doit être celui passé au constructeur", owner, backpack.getOwner());
    }

    /**
     * Vérifie que le constructeur avec propriétaire initialise aussi {@code totalMassKg} à 0.
     *
     * <p><b>Given</b> : un {@link Participant} valide.<br>
     * <b>When</b>  : {@code new Backpack(owner)} est appelé.<br>
     * <b>Then</b>  : {@code getTotalMassKg()} retourne {@code 0.0}.</p>
     */
    @Test
    public void testConstructeurAvecOwner_totalMassInitialiseeAZero() {
        // Given / When — backpack créé dans setUp()

        // Then
        assertEquals("totalMassKg doit être 0.0 même avec le constructeur owner",
                0.0, backpack.getTotalMassKg(), 0.001);
    }

    // =========================================================================
    // updateAndGetTotalMass()
    // =========================================================================

    /**
     * Vérifie que {@code updateAndGetTotalMass()} retourne {@code 0.0}
     * pour un sac vide.
     *
     * <p><b>Given</b> : un sac sans équipement ni nourriture.<br>
     * <b>When</b>  : {@code updateAndGetTotalMass()} est appelé.<br>
     * <b>Then</b>  : {@code getTotalMassKg()} vaut {@code 0.0}.</p>
     */
    @Test
    public void testUpdateTotalMass_sacVide_retourneZero() {
        // Given — sac vide

        // When
        backpack.updateAndGetTotalMass();

        // Then
        assertEquals("Un sac vide doit avoir un poids de 0.0 kg",
                0.0, backpack.getTotalMassKg(), 0.001);
    }

    /**
     * Vérifie que {@code updateAndGetTotalMass()} calcule correctement
     * le poids d'un seul équipement.
     *
     * <p><b>Given</b> : un équipement de 2000g (= 2.0 kg).<br>
     * <b>When</b>  : {@code updateAndGetTotalMass()} est appelé.<br>
     * <b>Then</b>  : {@code getTotalMassKg()} vaut {@code 2.0}.</p>
     */
    @Test
    public void testUpdateTotalMass_unEquipement_calculePoidsCorrectorement() {
        // Given
        backpack.getEquipmentItems().add(creerEquipmentItem("Tente", 2000.0));

        // When
        backpack.updateAndGetTotalMass();

        // Then
        assertEquals("2000g doit donner 2.0 kg",
                2.0, backpack.getTotalMassKg(), 0.001);
    }

    /**
     * Vérifie que {@code updateAndGetTotalMass()} calcule correctement
     * le poids d'un seul aliment.
     *
     * <p><b>Given</b> : un aliment de 500g (= 0.5 kg).<br>
     * <b>When</b>  : {@code updateAndGetTotalMass()} est appelé.<br>
     * <b>Then</b>  : {@code getTotalMassKg()} vaut {@code 0.5}.</p>
     */
    @Test
    public void testUpdateTotalMass_unAliment_calculePoidsCorrectorement() {
        // Given
        backpack.getFoodItems().add(creerFoodProduct("Barre de céréales", 500.0));

        // When
        backpack.updateAndGetTotalMass();

        // Then
        assertEquals("500g doit donner 0.5 kg",
                0.5, backpack.getTotalMassKg(), 0.001);
    }

    /**
     * Vérifie que {@code updateAndGetTotalMass()} additionne correctement
     * les équipements et les aliments ensemble.
     *
     * <p><b>Given</b> : un équipement de 1500g et un aliment de 500g.<br>
     * <b>When</b>  : {@code updateAndGetTotalMass()} est appelé.<br>
     * <b>Then</b>  : {@code getTotalMassKg()} vaut {@code 2.0}.</p>
     */
    @Test
    public void testUpdateTotalMass_equipementEtAliment_additionneCorrectement() {
        // Given
        backpack.getEquipmentItems().add(creerEquipmentItem("Duvet", 1500.0));
        backpack.getFoodItems().add(creerFoodProduct("Noix", 500.0));

        // When
        backpack.updateAndGetTotalMass();

        // Then
        assertEquals("1500g + 500g doit donner 2.0 kg",
                2.0, backpack.getTotalMassKg(), 0.001);
    }

    /**
     * Vérifie que {@code updateAndGetTotalMass()} additionne correctement
     * plusieurs équipements et plusieurs aliments.
     *
     * <p><b>Given</b> : 2 équipements (1000g + 500g) et 2 aliments (300g + 200g).<br>
     * <b>When</b>  : {@code updateAndGetTotalMass()} est appelé.<br>
     * <b>Then</b>  : {@code getTotalMassKg()} vaut {@code 2.0}.</p>
     */
    @Test
    public void testUpdateTotalMass_multiplesItems_sommeCorrecte() {
        // Given
        backpack.getEquipmentItems().add(creerEquipmentItem("Tente", 1000.0));
        backpack.getEquipmentItems().add(creerEquipmentItem("Réchaud", 500.0));
        backpack.getFoodItems().add(creerFoodProduct("Pain", 300.0));
        backpack.getFoodItems().add(creerFoodProduct("Fromage", 200.0));

        // When
        backpack.updateAndGetTotalMass();

        // Then
        assertEquals("1000 + 500 + 300 + 200 = 2000g = 2.0 kg",
                2.0, backpack.getTotalMassKg(), 0.001);
    }

    /**
     * Vérifie que {@code updateAndGetTotalMass()} recalcule correctement
     * après l'ajout d'un item supplémentaire.
     *
     * <p><b>Given</b> : un sac avec un équipement, puis un second ajouté après.<br>
     * <b>When</b>  : {@code updateAndGetTotalMass()} est appelé deux fois.<br>
     * <b>Then</b>  : le second appel reflète le nouveau contenu.</p>
     */
    @Test
    public void testUpdateTotalMass_appelDouble_recalculeCorrectement() {
        // Given
        backpack.getEquipmentItems().add(creerEquipmentItem("Tente", 1000.0));
        backpack.updateAndGetTotalMass();
        assertEquals(1.0, backpack.getTotalMassKg(), 0.001);

        // When — ajout d'un second item puis recalcul
        backpack.getEquipmentItems().add(creerEquipmentItem("Duvet", 1000.0));
        backpack.updateAndGetTotalMass();

        // Then
        assertEquals("Après ajout d'un second item, le poids doit être recalculé à 2.0 kg",
                2.0, backpack.getTotalMassKg(), 0.001);
    }

    // =========================================================================
    // clearContent()
    // =========================================================================

    /**
     * Vérifie que {@code clearContent()} vide la liste des équipements.
     *
     * <p><b>Given</b> : un sac contenant un équipement.<br>
     * <b>When</b>  : {@code clearContent()} est appelé.<br>
     * <b>Then</b>  : {@code getEquipmentItems()} est vide.</p>
     */
    @Test
    public void testClearContent_avecEquipement_videEquipements() {
        // Given
        backpack.getEquipmentItems().add(creerEquipmentItem("Tente", 2000.0));

        // When
        backpack.clearContent();

        // Then
        assertTrue("La liste des équipements doit être vide après clearContent()",
                backpack.getEquipmentItems().isEmpty());
    }

    /**
     * Vérifie que {@code clearContent()} vide la liste des aliments.
     *
     * <p><b>Given</b> : un sac contenant un aliment.<br>
     * <b>When</b>  : {@code clearContent()} est appelé.<br>
     * <b>Then</b>  : {@code getFoodItems()} est vide.</p>
     */
    @Test
    public void testClearContent_avecAliment_videAliments() {
        // Given
        backpack.getFoodItems().add(creerFoodProduct("Pain", 500.0));

        // When
        backpack.clearContent();

        // Then
        assertTrue("La liste des aliments doit être vide après clearContent()",
                backpack.getFoodItems().isEmpty());
    }

    /**
     * Vérifie que {@code clearContent()} remet {@code totalMassKg} à {@code 0.0}.
     *
     * <p><b>Given</b> : un sac avec des items et un poids calculé.<br>
     * <b>When</b>  : {@code clearContent()} est appelé.<br>
     * <b>Then</b>  : {@code getTotalMassKg()} vaut {@code 0.0}.</p>
     */
    @Test
    public void testClearContent_avecPoids_remiseAZeroDuPoids() {
        // Given
        backpack.getEquipmentItems().add(creerEquipmentItem("Tente", 2000.0));
        backpack.getFoodItems().add(creerFoodProduct("Pain", 500.0));
        backpack.updateAndGetTotalMass();
        assertTrue("Le poids doit être > 0 avant clearContent()",
                backpack.getTotalMassKg() > 0);

        // When
        backpack.clearContent();

        // Then
        assertEquals("totalMassKg doit être remis à 0.0 après clearContent()",
                0.0, backpack.getTotalMassKg(), 0.001);
    }

    /**
     * Vérifie que {@code clearContent()} sur un sac déjà vide ne lève pas d'exception.
     *
     * <p><b>Given</b> : un sac vide.<br>
     * <b>When</b>  : {@code clearContent()} est appelé.<br>
     * <b>Then</b>  : aucune exception, poids toujours à {@code 0.0}.</p>
     */
    @Test
    public void testClearContent_sacDejaVide_pasDException() {
        // Given — sac vide

        // When
        backpack.clearContent();

        // Then
        assertEquals("clearContent() sur un sac vide ne doit pas crasher",
                0.0, backpack.getTotalMassKg(), 0.001);
        assertTrue(backpack.getEquipmentItems().isEmpty());
        assertTrue(backpack.getFoodItems().isEmpty());
    }

    // =========================================================================
    // equals()
    // =========================================================================

    /**
     * Vérifie que deux sacs avec le même id sont considérés égaux.
     *
     * <p><b>Given</b> : deux sacs avec le même id {@code 1L}.<br>
     * <b>When</b>  : {@code equals()} est appelé.<br>
     * <b>Then</b>  : retourne {@code true}.</p>
     */
    @Test
    public void testEquals_memeId_retourneTrue() {
        // Given
        Backpack b1 = new Backpack();
        Backpack b2 = new Backpack();
        b1.setId(1L);
        b2.setId(1L);

        // When / Then
        assertTrue("Deux sacs avec le même id doivent être égaux", b1.equals(b2));
    }

    /**
     * Vérifie que deux sacs avec des ids différents mais le même owner sont égaux.
     *
     * <p><b>Given</b> : deux sacs avec des ids différents mais le même {@link Participant}.<br>
     * <b>When</b>  : {@code equals()} est appelé.<br>
     * <b>Then</b>  : retourne {@code true} (égalité par owner).</p>
     */
    @Test
    public void testEquals_memeOwner_retourneTrue() {
        // Given
        Backpack b1 = new Backpack(owner);
        Backpack b2 = new Backpack(owner);
        b1.setId(1L);
        b2.setId(2L);

        // When / Then
        assertTrue("Deux sacs avec le même owner doivent être égaux", b1.equals(b2));
    }

    /**
     * Vérifie que deux sacs avec des ids et des owners différents sont inégaux.
     *
     * <p><b>Given</b> : deux sacs complètement distincts.<br>
     * <b>When</b>  : {@code equals()} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testEquals_idEtOwnerDifferents_retourneFalse() {
        // Given
        Participant autreOwner = new Participant();
        Backpack b1 = new Backpack(owner);
        Backpack b2 = new Backpack(autreOwner);
        b1.setId(1L);
        b2.setId(2L);

        // When / Then
        assertFalse("Deux sacs avec id et owner différents doivent être inégaux",
                b1.equals(b2));
    }

    /**
     * Vérifie qu'un sac est égal à lui-même (réflexivité).
     *
     * <p><b>Given</b> : un même objet {@link Backpack}.<br>
     * <b>When</b>  : {@code equals(backpack)} est appelé sur lui-même.<br>
     * <b>Then</b>  : retourne {@code true}.</p>
     */
    @Test
    public void testEquals_memeObjet_retourneTrue() {
        // Given / When / Then
        assertTrue("Un sac doit être égal à lui-même", backpack.equals(backpack));
    }

    /**
     * Vérifie qu'un sac n'est pas égal à {@code null}.
     *
     * <p><b>Given</b> : {@code null} comme argument.<br>
     * <b>When</b>  : {@code equals(null)} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testEquals_comparaisonAvecNull_retourneFalse() {
        // Given / When / Then
        assertFalse("Un sac ne doit pas être égal à null", backpack.equals(null));
    }

    // =========================================================================
    // CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que {@code updateAndGetTotalMass()} gère correctement un item
     * à masse nulle (0g).
     *
     * <p><b>Given</b> : un équipement de 0g.<br>
     * <b>When</b>  : {@code updateAndGetTotalMass()} est appelé.<br>
     * <b>Then</b>  : {@code getTotalMassKg()} vaut {@code 0.0}.</p>
     */
    @Test
    public void testUpdateTotalMass_itemMasseNulle_retourneZero() {
        // Given
        backpack.getEquipmentItems().add(creerEquipmentItem("ItemLéger", 0.0));

        // When
        backpack.updateAndGetTotalMass();

        // Then
        assertEquals("Un item à 0g ne doit pas modifier le poids total",
                0.0, backpack.getTotalMassKg(), 0.001);
    }

    /**
     * Vérifie que {@code updateAndGetTotalMass()} gère un grand nombre d'items
     * sans perte de précision significative.
     *
     * <p><b>Given</b> : 100 équipements de 100g chacun (= 10 000g = 10 kg).<br>
     * <b>When</b>  : {@code updateAndGetTotalMass()} est appelé.<br>
     * <b>Then</b>  : {@code getTotalMassKg()} vaut {@code 10.0}.</p>
     */
    @Test
    public void testUpdateTotalMass_grandNombreItems_sommeCorrecte() {
        // Given — 100 équipements de 100g chacun
        for (int i = 0; i < 100; i++) {
            backpack.getEquipmentItems().add(creerEquipmentItem("Item" + i, 100.0));
        }

        // When
        backpack.updateAndGetTotalMass();

        // Then
        assertEquals("100 items de 100g doivent donner 10.0 kg",
                10.0, backpack.getTotalMassKg(), 0.01);
    }
}
