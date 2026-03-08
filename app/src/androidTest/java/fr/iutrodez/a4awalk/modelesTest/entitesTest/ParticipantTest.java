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

    // =========================================================================
    // isOverloaded()
    // =========================================================================

    /**
     * Vérifie que {@code isOverloaded()} retourne {@code false} quand
     * le backpack est {@code null}.
     *
     * <p><b>Given</b> : un participant sans backpack assigné.<br>
     * <b>When</b>  : {@code isOverloaded()} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testIsOverloaded_backpackNull_retourneFalse() {
        // Given — participant sans backpack (null par défaut)
        participant.setBackpack(null);

        // When
        boolean surcharge = participant.isOverloaded();

        // Then
        assertFalse("Sans backpack, le participant ne peut pas être surchargé", surcharge);
    }

    /**
     * Vérifie que {@code isOverloaded()} retourne {@code false} quand le poids
     * du sac est inférieur à la capacité maximale.
     *
     * <p><b>Given</b> : capacité max = 15 kg, poids du sac = 10 kg.<br>
     * <b>When</b>  : {@code isOverloaded()} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testIsOverloaded_poidsSousCapaciteMax_retourneFalse() {
        // Given
        participant.setCapaciteEmportMaxKg(15.0);
        participant.setBackpack(creerBackpackAvecPoids(10.0));

        // When
        boolean surcharge = participant.isOverloaded();

        // Then
        assertFalse("10 kg < 15 kg max → pas de surcharge", surcharge);
    }

    /**
     * Vérifie que {@code isOverloaded()} retourne {@code true} quand le poids
     * du sac dépasse la capacité maximale.
     *
     * <p><b>Given</b> : capacité max = 15 kg, poids du sac = 20 kg.<br>
     * <b>When</b>  : {@code isOverloaded()} est appelé.<br>
     * <b>Then</b>  : retourne {@code true}.</p>
     */
    @Test
    public void testIsOverloaded_poidsSuperieurCapaciteMax_retourneTrue() {
        // Given
        participant.setCapaciteEmportMaxKg(15.0);
        participant.setBackpack(creerBackpackAvecPoids(20.0));

        // When
        boolean surcharge = participant.isOverloaded();

        // Then
        assertTrue("20 kg > 15 kg max → surcharge détectée", surcharge);
    }

    /**
     * Vérifie que {@code isOverloaded()} retourne {@code false} quand le poids
     * est exactement égal à la capacité maximale (borne exacte).
     *
     * <p><b>Given</b> : capacité max = 15 kg, poids du sac = 15 kg.<br>
     * <b>When</b>  : {@code isOverloaded()} est appelé.<br>
     * <b>Then</b>  : retourne {@code false} (égalité n'est pas une surcharge).</p>
     */
    @Test
    public void testIsOverloaded_poidsEgalCapaciteMax_retourneFalse() {
        // Given — poids exactement à la limite
        participant.setCapaciteEmportMaxKg(15.0);
        participant.setBackpack(creerBackpackAvecPoids(15.0));

        // When
        boolean surcharge = participant.isOverloaded();

        // Then
        assertFalse("15 kg == 15 kg max → pas de surcharge (borne exacte)", surcharge);
    }

    /**
     * Vérifie que {@code isOverloaded()} retourne {@code false} quand
     * le sac est vide (poids = 0).
     *
     * <p><b>Given</b> : capacité max = 15 kg, poids du sac = 0 kg.<br>
     * <b>When</b>  : {@code isOverloaded()} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testIsOverloaded_sacVide_retourneFalse() {
        // Given
        participant.setCapaciteEmportMaxKg(15.0);
        participant.setBackpack(creerBackpackAvecPoids(0.0));

        // When
        boolean surcharge = participant.isOverloaded();

        // Then
        assertFalse("Un sac vide ne peut pas provoquer de surcharge", surcharge);
    }

    /**
     * Vérifie que {@code isOverloaded()} retourne {@code true} quand
     * la capacité max est 0 et le sac a du poids.
     *
     * <p><b>Given</b> : capacité max = 0 kg, poids du sac = 0.1 kg.<br>
     * <b>When</b>  : {@code isOverloaded()} est appelé.<br>
     * <b>Then</b>  : retourne {@code true}.</p>
     */
    @Test
    public void testIsOverloaded_capaciteMaxZero_toutPoidsEstSurcharge() {
        // Given
        participant.setCapaciteEmportMaxKg(0.0);
        participant.setBackpack(creerBackpackAvecPoids(0.1));

        // When
        boolean surcharge = participant.isOverloaded();

        // Then
        assertTrue("Avec capacité max = 0, tout poids > 0 est une surcharge", surcharge);
    }

    // =========================================================================
    // toString()
    // =========================================================================

    /**
     * Vérifie que {@code toString()} affiche prénom et nom correctement.
     *
     * <p><b>Given</b> : prénom = "Jean", nom = "Dupont", âge = 30.<br>
     * <b>When</b>  : {@code toString()} est appelé.<br>
     * <b>Then</b>  : la chaîne contient "Jean Dupont".</p>
     */
    @Test
    public void testToString_prenomEtNomPresents_afficheNomComplet() {
        // Given / When
        String resultat = participant.toString();

        // Then
        assertTrue("toString() doit contenir le prénom et le nom",
                resultat.contains("Jean Dupont"));
    }

    /**
     * Vérifie que {@code toString()} affiche "Nouveau participant" quand
     * prénom et nom sont tous les deux vides.
     *
     * <p><b>Given</b> : prénom = "", nom = "".<br>
     * <b>When</b>  : {@code toString()} est appelé.<br>
     * <b>Then</b>  : la chaîne contient "Nouveau participant".</p>
     */
    @Test
    public void testToString_prenomEtNomVides_afficheNouveauParticipant() {
        // Given
        Participant p = new Participant();
        p.setNom("");
        p.setPrenom("");

        // When
        String resultat = p.toString();

        // Then
        assertTrue("toString() doit afficher 'Nouveau participant' si nom et prénom sont vides",
                resultat.contains("Nouveau participant"));
    }

    /**
     * Vérifie que {@code toString()} affiche "Nouveau participant" quand
     * prénom et nom sont {@code null}.
     *
     * <p><b>Given</b> : prénom = {@code null}, nom = {@code null}.<br>
     * <b>When</b>  : {@code toString()} est appelé.<br>
     * <b>Then</b>  : la chaîne contient "Nouveau participant".</p>
     */
    @Test
    public void testToString_prenomEtNomNull_afficheNouveauParticipant() {
        // Given
        Participant p = new Participant();

        // When
        String resultat = p.toString();

        // Then
        assertTrue("toString() doit afficher 'Nouveau participant' si nom et prénom sont null",
                resultat.contains("Nouveau participant"));
    }

    /**
     * Vérifie que {@code toString()} affiche l'âge correctement.
     *
     * <p><b>Given</b> : âge = 30.<br>
     * <b>When</b>  : {@code toString()} est appelé.<br>
     * <b>Then</b>  : la chaîne contient "30 ans".</p>
     */
    @Test
    public void testToString_agePresent_afficheAge() {
        // Given / When
        String resultat = participant.toString();

        // Then
        assertTrue("toString() doit contenir l'âge", resultat.contains("30 ans"));
    }

    /**
     * Vérifie que {@code toString()} affiche le niveau quand il est défini.
     *
     * <p><b>Given</b> : niveau = {@link Level#DEBUTANT}.<br>
     * <b>When</b>  : {@code toString()} est appelé.<br>
     * <b>Then</b>  : la chaîne contient le libellé du niveau.</p>
     */
    @Test
    public void testToString_niveauPresent_afficheNiveau() {
        // Given / When
        String resultat = participant.toString();

        // Then
        assertTrue("toString() doit contenir le niveau",
                resultat.contains(Level.DEBUTANT.toString()));
    }

    /**
     * Vérifie que {@code toString()} n'affiche pas de niveau quand il est {@code null}.
     *
     * <p><b>Given</b> : niveau = {@code null}.<br>
     * <b>When</b>  : {@code toString()} est appelé.<br>
     * <b>Then</b>  : la chaîne ne contient pas "null".</p>
     */
    @Test
    public void testToString_niveauNull_nAffichePasNull() {
        // Given
        Participant p = new Participant();
        p.setNom("Martin");
        p.setPrenom("Paul");
        p.setAge(25);
        p.setNiveau(null);

        // When
        String resultat = p.toString();

        // Then
        assertFalse("toString() ne doit pas afficher 'null' pour un niveau null",
                resultat.contains("null"));
    }

    // =========================================================================
    // equals()
    // =========================================================================

    /**
     * Vérifie que deux participants avec le même id sont égaux.
     *
     * <p><b>Given</b> : deux instances avec {@code id = 1}.<br>
     * <b>When</b>  : {@code equals()} est appelé.<br>
     * <b>Then</b>  : retourne {@code true}.</p>
     */
    @Test
    public void testEquals_memeId_retourneTrue() {
        // Given
        Participant p1 = new Participant(); p1.setPId(1);
        Participant p2 = new Participant(); p2.setPId(1);

        // When / Then
        assertTrue("Deux participants avec le même id doivent être égaux", p1.equals(p2));
    }

    /**
     * Vérifie que deux participants avec des ids différents sont inégaux.
     *
     * <p><b>Given</b> : deux instances avec des ids distincts.<br>
     * <b>When</b>  : {@code equals()} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testEquals_idsDifferents_retourneFalse() {
        // Given
        Participant p1 = new Participant(); p1.setPId(1);
        Participant p2 = new Participant(); p2.setPId(2);

        // When / Then
        assertFalse("Deux participants avec des ids différents doivent être inégaux",
                p1.equals(p2));
    }

    /**
     * Vérifie qu'un participant est égal à lui-même (réflexivité).
     *
     * <p><b>Given</b> : un même objet {@link Participant}.<br>
     * <b>When</b>  : {@code equals(participant)} est appelé sur lui-même.<br>
     * <b>Then</b>  : retourne {@code true}.</p>
     */
    @Test
    public void testEquals_memeObjet_retourneTrue() {
        // Given / When / Then
        assertTrue("Un participant doit être égal à lui-même",
                participant.equals(participant));
    }

    /**
     * Vérifie qu'un participant n'est pas égal à {@code null}.
     *
     * <p><b>Given</b> : {@code null} comme argument.<br>
     * <b>When</b>  : {@code equals(null)} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testEquals_comparaisonAvecNull_retourneFalse() {
        // Given / When / Then
        assertFalse("Un participant ne doit pas être égal à null",
                participant.equals(null));
    }

    /**
     * Vérifie qu'un participant n'est pas égal à un objet d'un autre type.
     *
     * <p><b>Given</b> : une {@code String} comme argument.<br>
     * <b>When</b>  : {@code equals(String)} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testEquals_comparaisonAvecAutreType_retourneFalse() {
        // Given / When / Then
        assertFalse("Un participant ne doit pas être égal à un objet d'un autre type",
                participant.equals("Dupont Jean"));
    }

    /**
     * Vérifie que deux participants créés avec le constructeur vide (id = 0)
     * sont considérés égaux.
     *
     * <p><b>Given</b> : deux instances créées avec {@code new Participant()}.<br>
     * <b>When</b>  : {@code equals()} est appelé.<br>
     * <b>Then</b>  : retourne {@code true} (id primitif = 0 pour les deux).</p>
     */
    @Test
    public void testEquals_deuxConstructeursVidesIdZero_retourneTrue() {
        // Given
        Participant p1 = new Participant();
        Participant p2 = new Participant();

        // When / Then — les deux ont id = 0 (valeur par défaut du int primitif)
        assertTrue("Deux participants avec id=0 par défaut doivent être égaux", p1.equals(p2));
    }
}
