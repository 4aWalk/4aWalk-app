package fr.iutrodez.a4awalk.modelesTest.entitesTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.entites.PointOfInterest;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;
import fr.iutrodez.a4awalk.modeles.erreurs.HikeException;

/**
 * Classe de tests d'instrumentation pour {@link Hike}.
 *
 * <p>Ces tests s'exécutent dans {@code androidTest/} car {@link Hike}
 * implémente {@link android.os.Parcelable}, qui nécessite un environnement Android.
 * Cependant, les tests se concentrent sur la <b>logique métier pure</b> (sans réseau
 * ni UI) : gestion des participants, validation de la durée, points d'intérêt,
 * et égalité.</p>
 *
 * <p>Catégories couvertes :</p>
 * <ul>
 *   <li><b>Constructeurs</b> : initialisation correcte des champs.</li>
 *   <li><b>{@code addParticipant()}</b> : ajout nominal, doublon, participant null.</li>
 *   <li><b>{@code removeParticipant()}</b> : suppression nominale, participant absent.</li>
 *   <li><b>{@code setDureeJours()}</b> : bornes valides (1 et 3), valeurs invalides.</li>
 *   <li><b>{@code addPointOfInterest()}</b> : ajout nominal et multiple.</li>
 *   <li><b>{@code equals()}</b> : égalité par id, par libellé, réflexivité, null.</li>
 *   <li><b>{@code toString()}</b> : format correct.</li>
 * </ul>
 *
 * <p><b>Emplacement du fichier :</b>
 * {@code app/src/androidTest/java/fr/iutrodez/a4awalk/modeles/entites/HikeTest.java}</p>
 *
 * @author Votre équipe
 * @version 1.0
 * @see Hike
 */
@RunWith(AndroidJUnit4.class)
public class HikeTest {

    // -------------------------------------------------------------------------
    // Champs
    // -------------------------------------------------------------------------

    /** Randonnée nominale réutilisée dans les tests. */
    private Hike hike;

    /** Participant de référence pour les tests. */
    private Participant participant;

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    /**
     * Crée une {@link Hike} et un {@link Participant} de référence avant chaque test.
     */
    @Before
    public void setUp() {
        hike        = new Hike();
        hike.setLibelle("Tour du Mont-Blanc");
        hike.setDureeJours(2);

        participant = new Participant();
    }

    // -------------------------------------------------------------------------
    // Méthode utilitaire
    // -------------------------------------------------------------------------

    /**
     * Crée un {@link User} valide pour les tests nécessitant un créateur.
     *
     * @return un {@link User} correctement initialisé
     */
    private User creerUserValide() {
        return new User(
                "Dupont", "Jean", 30,
                "jean.dupont@test.fr",
                "1 rue des Crêtes",
                Level.DEBUTANT,
                Morphology.MOYENNE
        );
    }

    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================

    /**
     * Vérifie que le constructeur vide crée une instance non null avec
     * une liste de participants vide.
     *
     * <p><b>Given</b> : aucun argument.<br>
     * <b>When</b>  : {@code new Hike()} est appelé.<br>
     * <b>Then</b>  : l'instance est non null et {@code participantSize()} = 0.</p>
     */
    @Test
    public void testConstructeurVide_instanceNonNullEtParticipantsVides() {
        // Given / When
        Hike hikeVide = new Hike();

        // Then
        assertNotNull("Le constructeur vide doit créer une instance non null", hikeVide);
        assertEquals("La liste de participants doit être vide", 0, hikeVide.participantSize());
    }

    /**
     * Vérifie que le constructeur complet assigne correctement tous les champs.
     *
     * <p><b>Given</b> : tous les paramètres du constructeur fournis.<br>
     * <b>When</b>  : {@code new Hike(id, libelle, depart, arrivee, duree, creator)} est appelé.<br>
     * <b>Then</b>  : chaque getter retourne la valeur attendue.</p>
     */
    @Test
    public void testConstructeurComplet_tousLesChampsCorrectementAssignes() {
        // Given
        User creator = creerUserValide();
        Hike hikeComplet = new Hike(42, "GR20 Corse", null, null, 3, creator);

        // When / Then
        assertEquals("L'id doit correspondre",      42,          hikeComplet.getId());
        assertEquals("Le libellé doit correspondre", "GR20 Corse", hikeComplet.getLibelle());
        assertEquals("La durée doit correspondre",  3,           hikeComplet.getDureeJours());
        assertEquals("Le créateur doit correspondre", creator,   hikeComplet.getCreator());
    }

    // =========================================================================
    // addParticipant()
    // =========================================================================

    /**
     * Vérifie qu'un participant valide est correctement ajouté à la randonnée.
     *
     * <p><b>Given</b> : une randonnée vide et un participant non null.<br>
     * <b>When</b>  : {@code addParticipant(participant)} est appelé.<br>
     * <b>Then</b>  : {@code participantSize()} vaut {@code 1}.</p>
     */
    @Test
    public void testAddParticipant_participantValide_ajouteCorrectorement() throws HikeException {
        // Given — hike vide et participant créés dans setUp()

        // When
        hike.addParticipant(participant);

        // Then
        assertEquals("Un participant doit être ajouté", 1, hike.participantSize());
    }

    /**
     * Vérifie que plusieurs participants distincts peuvent être ajoutés.
     *
     * <p><b>Given</b> : une randonnée vide et 3 participants distincts.<br>
     * <b>When</b>  : {@code addParticipant()} est appelé 3 fois.<br>
     * <b>Then</b>  : {@code participantSize()} vaut {@code 3}.</p>
     */
    @Test
    public void testAddParticipant_troisParticipantsDistincts_tailleTrois() throws HikeException {
        // Given
        Participant p1 = new Participant();
        Participant p2 = new Participant();
        Participant p3 = new Participant();

        // When
        hike.addParticipant(p1);
        hike.addParticipant(p2);
        hike.addParticipant(p3);

        // Then
        assertEquals("Trois participants distincts doivent être ajoutés", 3, hike.participantSize());
    }

    /**
     * Vérifie qu'ajouter un participant {@code null} lève une {@link HikeException}.
     *
     * <p><b>Given</b> : {@code null} comme participant.<br>
     * <b>When</b>  : {@code addParticipant(null)} est appelé.<br>
     * <b>Then</b>  : une {@link HikeException} est levée.</p>
     */
    @Test(expected = HikeException.class)
    public void testAddParticipant_participantNull_leveHikeException() throws HikeException {
        // Given
        Participant participantNull = null;

        // When — doit lever HikeException
        hike.addParticipant(participantNull);

        // Then : exception déclarée dans l'annotation
    }

    /**
     * Vérifie qu'ajouter un participant déjà inscrit lève une {@link HikeException}.
     *
     * <p><b>Given</b> : un participant déjà ajouté à la randonnée.<br>
     * <b>When</b>  : {@code addParticipant()} est appelé une seconde fois avec le même participant.<br>
     * <b>Then</b>  : une {@link HikeException} est levée.</p>
     */
    @Test(expected = HikeException.class)
    public void testAddParticipant_participantDoublon_leveHikeException() throws HikeException {
        // Given
        hike.addParticipant(participant); // Premier ajout

        // When — second ajout du même participant → doit lever HikeException
        hike.addParticipant(participant);

        // Then : exception déclarée dans l'annotation
    }

    /**
     * Vérifie que le message de l'exception pour participant null est correct.
     *
     * <p><b>Given</b> : {@code null} comme participant.<br>
     * <b>When</b>  : {@code addParticipant(null)} est appelé.<br>
     * <b>Then</b>  : le message de l'exception contient "nul".</p>
     */
    @Test
    public void testAddParticipant_participantNull_messageExceptionCorrect() {
        // Given
        try {
            // When
            hike.addParticipant(null);
        } catch (HikeException e) {
            // Then
            assertTrue("Le message doit mentionner 'nul'",
                    e.getMessage().contains("nul"));
        }
    }

    /**
     * Vérifie que le message de l'exception pour participant doublon est correct.
     *
     * <p><b>Given</b> : un participant déjà inscrit.<br>
     * <b>When</b>  : {@code addParticipant()} est rappelé avec le même participant.<br>
     * <b>Then</b>  : le message de l'exception contient "déjà inscrit".</p>
     */
    @Test
    public void testAddParticipant_participantDoublon_messageExceptionCorrect() throws HikeException {
        // Given
        hike.addParticipant(participant);

        // When
        try {
            hike.addParticipant(participant);
        } catch (HikeException e) {
            // Then
            assertTrue("Le message doit mentionner 'déjà inscrit'",
                    e.getMessage().contains("déjà inscrit"));
        }
    }

    // =========================================================================
    // removeParticipant()
    // =========================================================================

    /**
     * Vérifie qu'un participant inscrit est correctement retiré de la randonnée.
     *
     * <p><b>Given</b> : une randonnée avec un participant.<br>
     * <b>When</b>  : {@code removeParticipant(participant)} est appelé.<br>
     * <b>Then</b>  : {@code participantSize()} vaut {@code 0}.</p>
     */
    @Test
    public void testRemoveParticipant_participantExistant_supprimeCorrectorement() throws HikeException {
        // Given
        hike.addParticipant(participant);
        assertEquals(1, hike.participantSize());

        // When
        hike.removeParticipant(participant);

        // Then
        assertEquals("La liste doit être vide après suppression", 0, hike.participantSize());
    }

    /**
     * Vérifie que retirer un participant non inscrit lève une {@link HikeException}.
     *
     * <p><b>Given</b> : une randonnée sans participants.<br>
     * <b>When</b>  : {@code removeParticipant(participant)} est appelé.<br>
     * <b>Then</b>  : une {@link HikeException} est levée.</p>
     */
    @Test(expected = HikeException.class)
    public void testRemoveParticipant_participantAbsent_leveHikeException() throws HikeException {
        // Given — hike sans participants

        // When — doit lever HikeException
        hike.removeParticipant(participant);

        // Then : exception déclarée dans l'annotation
    }

    /**
     * Vérifie que le bon participant est retiré quand plusieurs sont inscrits.
     *
     * <p><b>Given</b> : une randonnée avec 2 participants.<br>
     * <b>When</b>  : un seul participant est retiré.<br>
     * <b>Then</b>  : {@code participantSize()} vaut {@code 1} et le bon reste.</p>
     */
    @Test
    public void testRemoveParticipant_deuxParticipants_retirerUnSeul() throws HikeException {
        // Given
        Participant autreParticipant = new Participant();
        hike.addParticipant(participant);
        hike.addParticipant(autreParticipant);

        // When
        hike.removeParticipant(participant);

        // Then
        assertEquals("Il doit rester 1 participant", 1, hike.participantSize());
        assertTrue("L'autre participant doit toujours être présent",
                hike.getParticipants().contains(autreParticipant));
    }

    // =========================================================================
    // setDureeJours()
    // =========================================================================

    /**
     * Vérifie que la durée minimale autorisée (1 jour) est acceptée.
     *
     * <p><b>Given</b> : durée = {@code 1}.<br>
     * <b>When</b>  : {@code setDureeJours(1)} est appelé.<br>
     * <b>Then</b>  : {@code getDureeJours()} retourne {@code 1}.</p>
     */
    @Test
    public void testSetDureeJours_unJour_accepte() {
        // Given / When
        hike.setDureeJours(1);

        // Then
        assertEquals("La durée de 1 jour doit être acceptée", 1, hike.getDureeJours());
    }

    /**
     * Vérifie que la durée maximale autorisée (3 jours) est acceptée.
     *
     * <p><b>Given</b> : durée = {@code 3}.<br>
     * <b>When</b>  : {@code setDureeJours(3)} est appelé.<br>
     * <b>Then</b>  : {@code getDureeJours()} retourne {@code 3}.</p>
     */
    @Test
    public void testSetDureeJours_troisJours_accepte() {
        // Given / When
        hike.setDureeJours(3);

        // Then
        assertEquals("La durée de 3 jours doit être acceptée", 3, hike.getDureeJours());
    }

    /**
     * Vérifie que la durée {@code 0} lève une {@link IllegalArgumentException}.
     *
     * <p><b>Given</b> : durée = {@code 0}.<br>
     * <b>When</b>  : {@code setDureeJours(0)} est appelé.<br>
     * <b>Then</b>  : une {@link IllegalArgumentException} est levée.</p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetDureeJours_zero_leveIllegalArgumentException() {
        // Given / When — doit lever IllegalArgumentException
        hike.setDureeJours(0);

        // Then : exception déclarée dans l'annotation
    }

    /**
     * Vérifie que la durée {@code 4} (hors borne supérieure) lève une
     * {@link IllegalArgumentException}.
     *
     * <p><b>Given</b> : durée = {@code 4}.<br>
     * <b>When</b>  : {@code setDureeJours(4)} est appelé.<br>
     * <b>Then</b>  : une {@link IllegalArgumentException} est levée.</p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetDureeJours_quatre_leveIllegalArgumentException() {
        // Given / When — doit lever IllegalArgumentException
        hike.setDureeJours(4);

        // Then : exception déclarée dans l'annotation
    }

    /**
     * Vérifie que la durée négative lève une {@link IllegalArgumentException}.
     *
     * <p><b>Given</b> : durée = {@code -1}.<br>
     * <b>When</b>  : {@code setDureeJours(-1)} est appelé.<br>
     * <b>Then</b>  : une {@link IllegalArgumentException} est levée.</p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetDureeJours_negatif_leveIllegalArgumentException() {
        // Given / When — doit lever IllegalArgumentException
        hike.setDureeJours(-1);

        // Then : exception déclarée dans l'annotation
    }

    // =========================================================================
    // addPointOfInterest()
    // =========================================================================

    /**
     * Vérifie qu'un point d'intérêt est correctement ajouté à la liste.
     *
     * <p><b>Given</b> : une randonnée sans points d'intérêt.<br>
     * <b>When</b>  : {@code addPointOfInterest(poi)} est appelé.<br>
     * <b>Then</b>  : la liste contient 1 élément.</p>
     */
    @Test
    public void testAddPointOfInterest_poiValide_ajouteCorrectement() {
        // Given
        PointOfInterest poi = new PointOfInterest();

        // When
        hike.addPointOfInterest(poi);

        // Then
        assertEquals("Un POI doit être ajouté à la liste", 1, hike.getOptionalPoints().size());
        assertTrue("Le POI ajouté doit être dans la liste", hike.getOptionalPoints().contains(poi));
    }

    /**
     * Vérifie que plusieurs points d'intérêt peuvent être ajoutés successivement.
     *
     * <p><b>Given</b> : une randonnée sans points d'intérêt.<br>
     * <b>When</b>  : {@code addPointOfInterest()} est appelé 3 fois.<br>
     * <b>Then</b>  : la liste contient 3 éléments.</p>
     */
    @Test
    public void testAddPointOfInterest_troisPois_tailleTrois() {
        // Given
        PointOfInterest poi1 = new PointOfInterest();
        PointOfInterest poi2 = new PointOfInterest();
        PointOfInterest poi3 = new PointOfInterest();

        // When
        hike.addPointOfInterest(poi1);
        hike.addPointOfInterest(poi2);
        hike.addPointOfInterest(poi3);

        // Then
        assertEquals("Trois POIs doivent être dans la liste", 3, hike.getOptionalPoints().size());
    }

    // =========================================================================
    // equals()
    // =========================================================================

    /**
     * Vérifie que deux randonnées avec le même id sont égales.
     *
     * <p><b>Given</b> : deux randonnées avec {@code id = 1}.<br>
     * <b>When</b>  : {@code equals()} est appelé.<br>
     * <b>Then</b>  : retourne {@code true}.</p>
     */
    @Test
    public void testEquals_memeId_retourneTrue() {
        // Given
        Hike h1 = new Hike(); h1.setId(1);
        Hike h2 = new Hike(); h2.setId(1);

        // When / Then
        assertTrue("Deux randonnées avec le même id doivent être égales", h1.equals(h2));
    }

    /**
     * Vérifie que deux randonnées avec le même libellé sont égales (clé naturelle).
     *
     * <p><b>Given</b> : deux randonnées avec le même libellé mais des ids différents.<br>
     * <b>When</b>  : {@code equals()} est appelé.<br>
     * <b>Then</b>  : retourne {@code true}.</p>
     */
    @Test
    public void testEquals_memeLibelle_retourneTrue() {
        // Given
        Hike h1 = new Hike(); h1.setId(1); h1.setLibelle("GR20");
        Hike h2 = new Hike(); h2.setId(2); h2.setLibelle("GR20");

        // When / Then
        assertTrue("Deux randonnées avec le même libellé doivent être égales", h1.equals(h2));
    }

    /**
     * Vérifie que deux randonnées avec des ids et libellés différents sont inégales.
     *
     * <p><b>Given</b> : deux randonnées complètement distinctes.<br>
     * <b>When</b>  : {@code equals()} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testEquals_idEtLibelleDifferents_retourneFalse() {
        // Given
        Hike h1 = new Hike(); h1.setId(1); h1.setLibelle("GR20");
        Hike h2 = new Hike(); h2.setId(2); h2.setLibelle("Tour du Mont-Blanc");

        // When / Then
        assertFalse("Deux randonnées distinctes ne doivent pas être égales", h1.equals(h2));
    }

    /**
     * Vérifie qu'une randonnée est égale à elle-même (réflexivité).
     *
     * <p><b>Given</b> : un même objet {@link Hike}.<br>
     * <b>When</b>  : {@code equals(hike)} est appelé sur lui-même.<br>
     * <b>Then</b>  : retourne {@code true}.</p>
     */
    @Test
    public void testEquals_memeObjet_retourneTrue() {
        // Given / When / Then
        assertTrue("Une randonnée doit être égale à elle-même", hike.equals(hike));
    }

    /**
     * Vérifie qu'une randonnée n'est pas égale à {@code null}.
     *
     * <p><b>Given</b> : {@code null} comme argument.<br>
     * <b>When</b>  : {@code equals(null)} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testEquals_comparaisonAvecNull_retourneFalse() {
        // Given / When / Then
        assertFalse("Une randonnée ne doit pas être égale à null", hike.equals(null));
    }

    // =========================================================================
    // toString()
    // =========================================================================

    /**
     * Vérifie que {@code toString()} contient l'id, le libellé et le nombre
     * de participants dans le bon format.
     *
     * <p><b>Given</b> : une randonnée avec id=1, libellé et 0 participants.<br>
     * <b>When</b>  : {@code toString()} est appelé.<br>
     * <b>Then</b>  : la chaîne contient l'id, le libellé et "participants=0".</p>
     */
    @Test
    public void testToString_formatCorrect() {
        // Given
        hike.setId(1);

        // When
        String resultat = hike.toString();

        // Then
        assertTrue("toString() doit contenir l'id",       resultat.contains("1"));
        assertTrue("toString() doit contenir le libellé", resultat.contains("Tour du Mont-Blanc"));
        assertTrue("toString() doit contenir participants=0", resultat.contains("participants=0"));
    }
}
