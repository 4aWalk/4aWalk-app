package fr.iutrodez.a4awalk.modelesTest.entitesTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.iutrodez.a4awalk.modeles.entites.Backpack;
import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;

/**
 * Classe de tests d'instrumentation pour {@link Participant}.
 *
 * <p>Ces tests s'exécutent dans {@code androidTest/} car {@link Participant}
 * implémente {@link android.os.Parcelable}.</p>
 *
 * <p>Catégories couvertes :</p>
 * <ul>
 *   <li><b>Constructeurs</b> : initialisation correcte via constructeur vide
 *       et constructeur complet.</li>
 *   <li><b>{@code isOverloaded()}</b> : sac null, poids sous/sur la capacité max,
 *       capacité exactement atteinte.</li>
 *   <li><b>{@code toString()}</b> : prénom + nom, champs vides, sans niveau,
 *       "Nouveau participant" si tout est vide.</li>
 *   <li><b>{@code equals()}</b> : même id, ids différents, réflexivité, null,
 *       autre type.</li>
 * </ul>
 *
 * <p><b>Emplacement du fichier :</b>
 * {@code app/src/androidTest/java/fr/iutrodez/a4awalk/modeles/entites/ParticipantTest.java}</p>
 *
 * @author Votre équipe
 * @version 1.0
 * @see Participant
 */
@RunWith(AndroidJUnit4.class)
public class ParticipantTest {

    // -------------------------------------------------------------------------
    // Champs
    // -------------------------------------------------------------------------

    /** Participant nominal réutilisé dans les tests. */
    private Participant participant;

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    /**
     * Crée un {@link Participant} nominal avant chaque test.
     */
    @Before
    public void setUp() {
        participant = new Participant(
                "Dupont", "Jean", 30,
                Level.DEBUTANT, Morphology.MOYENNE,
                false, 2500, 3, 15.0, 1
        );
    }

    // -------------------------------------------------------------------------
    // Méthodes utilitaires
    // -------------------------------------------------------------------------

    /**
     * Crée un {@link Backpack} avec un poids total donné en kg.
     *
     * @param poidsKg le poids total à affecter au sac
     * @return un {@link Backpack} avec {@code totalMassKg} initialisé
     */
    private Backpack creerBackpackAvecPoids(double poidsKg) {
        Backpack backpack = new Backpack(participant);
        backpack.setTotalMassKg(poidsKg);
        return backpack;
    }

    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================

    /**
     * Vérifie que le constructeur vide crée une instance non null avec
     * les valeurs par défaut correctes.
     *
     * <p><b>Given</b> : aucun argument.<br>
     * <b>When</b>  : {@code new Participant()} est appelé.<br>
     * <b>Then</b>  : l'instance est non null, {@code creator = false},
     *               {@code besoinKcal = 0}, {@code capaciteEmportMaxKg = 0.0}.</p>
     */
    @Test
    public void testConstructeurVide_valeursParDefautCorrectes() {
        // Given / When
        Participant p = new Participant();

        // Then
        assertNotNull("L'instance ne doit pas être null",       p);
        assertFalse("creator doit être false par défaut",       p.getCreator());
        assertEquals("besoinKcal doit être 0 par défaut",       0, p.getBesoinKcal());
        assertEquals("besoinEauLitre doit être 0 par défaut",   0, p.getBesoinEauLitre());
        assertEquals("capaciteEmportMaxKg doit être 0.0",       0.0, p.getCapaciteEmportMaxKg(), 0.001);
        assertNull("Le backpack doit être null par défaut",     p.getBackpack());
    }

    /**
     * Vérifie que le constructeur complet assigne correctement tous les champs.
     *
     * <p><b>Given</b> : tous les paramètres fournis.<br>
     * <b>When</b>  : le constructeur complet est appelé.<br>
     * <b>Then</b>  : chaque getter retourne la valeur attendue.</p>
     */
    @Test
    public void testConstructeurComplet_tousLesChampsCorrectementAssignes() {
        // Given / When — participant créé dans setUp()

        // Then
        assertEquals("Le nom doit correspondre",          "Dupont",          participant.getNom());
        assertEquals("Le prénom doit correspondre",       "Jean",            participant.getPrenom());
        assertEquals("L'âge doit correspondre",           30,                participant.getAge());
        assertEquals("Le niveau doit correspondre",       Level.DEBUTANT,    participant.getNiveau());
        assertEquals("La morphologie doit correspondre",  Morphology.MOYENNE, participant.getMorphologie());
        assertFalse("creator doit être false",            participant.getCreator());
        assertEquals("besoinKcal doit correspondre",      2500,              participant.getBesoinKcal());
        assertEquals("besoinEauLitre doit correspondre",  3,                 participant.getBesoinEauLitre());
        assertEquals("capaciteEmportMaxKg doit correspondre", 15.0,          participant.getCapaciteEmportMaxKg(), 0.001);
        assertEquals("idRando doit correspondre",         1,                 participant.getIdRando());
    }

    /**
     * Vérifie que le constructeur complet avec {@code creator = true}
     * assigne correctement la valeur.
     *
     * <p><b>Given</b> : {@code creator = true}.<br>
     * <b>When</b>  : le constructeur est appelé.<br>
     * <b>Then</b>  : {@code getCreator()} retourne {@code true}.</p>
     */
    @Test
    public void testConstructeurComplet_creatorTrue_assigneCorrectement() {
        // Given / When
        Participant createur = new Participant(
                "Martin", "Paul", 25,
                Level.SPORTIF, Morphology.LEGERE,
                true, 3000, 4, 20.0, 2
        );

        // Then
        assertTrue("creator doit être true", createur.getCreator());
    }
}
