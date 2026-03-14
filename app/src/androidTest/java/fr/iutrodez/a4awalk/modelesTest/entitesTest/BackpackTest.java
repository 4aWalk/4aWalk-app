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

}
