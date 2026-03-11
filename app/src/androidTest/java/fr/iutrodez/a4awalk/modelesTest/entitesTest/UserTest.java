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

import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;

/**
 * Classe de tests d'instrumentation pour {@link User}.
 *
 * <p>Ces tests s'exécutent dans {@code androidTest/} car {@link User}
 * implémente {@link android.os.Parcelable}.</p>
 *
 * <p>Catégories couvertes :</p>
 * <ul>
 *   <li><b>Constructeurs</b> : initialisation correcte via constructeur vide,
 *       constructeur à 7 paramètres (sans password) et à 8 paramètres (avec password).</li>
 *   <li><b>{@code getFullName()}</b> : format "prénom NOM", cas limites et erreurs.</li>
 *   <li><b>{@code addCreatedHike()}</b> : ajout nominal, cohérence bidirectionnelle,
 *       plusieurs randonnées, doublon ignoré par {@link java.util.HashSet}.</li>
 *   <li><b>{@code equals()}</b> : égalité par mail, mails différents, réflexivité,
 *       null, autre type.</li>
 *   <li><b>{@code toString()}</b> : format correct.</li>
 * </ul>
 *
 * <p><b>Emplacement du fichier :</b>
 * {@code app/src/androidTest/java/fr/iutrodez/a4awalk/modeles/entites/UserTest.java}</p>
 *
 * @author Votre équipe
 * @version 1.0
 * @see User
 */
@RunWith(AndroidJUnit4.class)
public class UserTest {

    // -------------------------------------------------------------------------
    // Constantes de test
    // -------------------------------------------------------------------------

    private static final String NOM_NOMINAL      = "Dupont";
    private static final String PRENOM_NOMINAL   = "Jean";
    private static final int    AGE_NOMINAL      = 30;
    private static final String MAIL_NOMINAL     = "jean.dupont@test.fr";
    private static final String PASSWORD_NOMINAL = "motdepasse123";
    private static final String ADRESSE_NOMINALE = "1 rue des Crêtes, Rodez";

    // -------------------------------------------------------------------------
    // Champs
    // -------------------------------------------------------------------------

    /** Utilisateur nominal réutilisé dans les tests. */
    private User user;

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    /**
     * Crée un {@link User} nominal avant chaque test.
     */
    @Before
    public void setUp() {
        user = new User(
                NOM_NOMINAL, PRENOM_NOMINAL, AGE_NOMINAL,
                MAIL_NOMINAL, PASSWORD_NOMINAL, ADRESSE_NOMINALE,
                Level.DEBUTANT, Morphology.MOYENNE
        );
    }

    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================

    /**
     * Vérifie que le constructeur vide crée une instance non null avec
     * les champs à {@code null} et {@code createdHikes} vide.
     *
     * <p><b>Given</b> : aucun argument.<br>
     * <b>When</b>  : {@code new User()} est appelé.<br>
     * <b>Then</b>  : l'instance est non null, les champs String sont null,
     *               {@code createdHikes} est une collection vide.</p>
     */
    @Test
    public void testConstructeurVide_champsNullEtHikesVides() {
        // Given / When
        User u = new User();

        // Then
        assertNotNull("L'instance ne doit pas être null",          u);
        assertNull("Le nom doit être null",                        u.getNom());
        assertNull("Le mail doit être null",                       u.getMail());
        assertNotNull("createdHikes ne doit pas être null",        u.getCreatedHikes());
        assertTrue("createdHikes doit être vide",                  u.getCreatedHikes().isEmpty());
    }

    /**
     * Vérifie que le constructeur à 8 paramètres (avec password) assigne
     * correctement tous les champs.
     *
     * <p><b>Given</b> : tous les paramètres fournis dont le password.<br>
     * <b>When</b>  : le constructeur complet est appelé.<br>
     * <b>Then</b>  : chaque getter retourne la valeur attendue.</p>
     */
    @Test
    public void testConstructeurCompletAvecPassword_tousLesChampsAssignes() {
        // Given / When — user créé dans setUp()

        // Then
        assertEquals("Le nom doit correspondre",          NOM_NOMINAL,       user.getNom());
        assertEquals("Le prénom doit correspondre",       PRENOM_NOMINAL,    user.getPrenom());
        assertEquals("L'âge doit correspondre",           AGE_NOMINAL,       user.getAge());
        assertEquals("Le mail doit correspondre",         MAIL_NOMINAL,      user.getMail());
        assertEquals("Le password doit correspondre",     PASSWORD_NOMINAL,  user.getPassword());
        assertEquals("L'adresse doit correspondre",       ADRESSE_NOMINALE,  user.getAdresse());
        assertEquals("Le niveau doit correspondre",       Level.DEBUTANT,    user.getNiveau());
        assertEquals("La morphologie doit correspondre",  Morphology.MOYENNE, user.getMorphologie());
    }

    /**
     * Vérifie que le constructeur à 7 paramètres (sans password) assigne
     * correctement les champs et laisse password à {@code null}.
     *
     * <p><b>Given</b> : tous les paramètres sauf le password.<br>
     * <b>When</b>  : le constructeur sans password est appelé.<br>
     * <b>Then</b>  : tous les champs sont corrects et password est {@code null}.</p>
     */
    @Test
    public void testConstructeurSansPassword_passwordNull() {
        // Given / When
        User u = new User(
                NOM_NOMINAL, PRENOM_NOMINAL, AGE_NOMINAL,
                MAIL_NOMINAL, ADRESSE_NOMINALE,
                Level.SPORTIF, Morphology.LEGERE
        );

        // Then
        assertEquals("Le nom doit correspondre",   NOM_NOMINAL,    u.getNom());
        assertEquals("Le mail doit correspondre",  MAIL_NOMINAL,   u.getMail());
        assertNull("Le password doit être null avec ce constructeur", u.getPassword());
    }

    // =========================================================================
    // getFullName()
    // =========================================================================

    /**
     * Vérifie que {@code getFullName()} retourne "prénom NOM" avec le nom en majuscules.
     *
     * <p><b>Given</b> : prénom = "Jean", nom = "Dupont".<br>
     * <b>When</b>  : {@code getFullName()} est appelé.<br>
     * <b>Then</b>  : retourne "Jean DUPONT".</p>
     */
    @Test
    public void testGetFullName_prenomEtNomValides_retourneFormatCorrect() {
        // Given / When
        String fullName = user.getFullName();

        // Then
        assertEquals("getFullName() doit retourner 'Jean DUPONT'",
                "Jean DUPONT", fullName);
    }

    /**
     * Vérifie que {@code getFullName()} met bien le nom en majuscules.
     *
     * <p><b>Given</b> : nom = "martin" (minuscules).<br>
     * <b>When</b>  : {@code getFullName()} est appelé.<br>
     * <b>Then</b>  : le nom apparaît en majuscules dans le résultat.</p>
     */
    @Test
    public void testGetFullName_nomEnMinuscules_retourneNomEnMajuscules() {
        // Given
        user.setNom("martin");

        // When
        String fullName = user.getFullName();

        // Then
        assertTrue("Le nom doit être en majuscules dans getFullName()",
                fullName.contains("MARTIN"));
    }

    /**
     * Vérifie que {@code getFullName()} avec un prénom vide retourne " NOM".
     *
     * <p><b>Given</b> : prénom = "".<br>
     * <b>When</b>  : {@code getFullName()} est appelé.<br>
     * <b>Then</b>  : retourne " DUPONT" (espace + nom en majuscules).</p>
     */
    @Test
    public void testGetFullName_prenomVide_retourneEspaceNom() {
        // Given
        user.setPrenom("");

        // When
        String fullName = user.getFullName();

        // Then
        assertEquals("Un prénom vide doit donner ' DUPONT'",
                " DUPONT", fullName);
    }

    /**
     * Vérifie que {@code getFullName()} lève une {@link NullPointerException}
     * si le nom est {@code null} (appel de {@code toUpperCase()} sur null).
     *
     * <p><b>Given</b> : nom = {@code null}.<br>
     * <b>When</b>  : {@code getFullName()} est appelé.<br>
     * <b>Then</b>  : une {@link NullPointerException} est levée.</p>
     *
     * <p><em>Note :</em> ce test documente le comportement actuel et signale
     * un besoin de guard dans {@code getFullName()}.</p>
     */
    @Test(expected = NullPointerException.class)
    public void testGetFullName_nomNull_leveNullPointerException() {
        // Given
        user.setNom(null);

        // When — toUpperCase() sur null lève NPE
        user.getFullName();

        // Then : exception déclarée dans l'annotation
    }

    // =========================================================================
    // addCreatedHike()
    // =========================================================================

    /**
     * Vérifie qu'une randonnée est correctement ajoutée à {@code createdHikes}.
     *
     * <p><b>Given</b> : un user sans randonnée et une {@link Hike} valide.<br>
     * <b>When</b>  : {@code addCreatedHike(hike)} est appelé.<br>
     * <b>Then</b>  : {@code createdHikes} contient la randonnée.</p>
     */
    @Test
    public void testAddCreatedHike_hikeValide_ajoutDansCreatedHikes() {
        // Given
        Hike hike = new Hike();
        hike.setLibelle("Tour du Lac");
        hike.setDureeJours(1);

        // When
        user.addCreatedHike(hike);

        // Then
        assertEquals("createdHikes doit contenir 1 randonnée", 1, user.getCreatedHikes().size());
        assertTrue("La randonnée doit être dans createdHikes",  user.getCreatedHikes().contains(hike));
    }

    /**
     * Vérifie que {@code addCreatedHike()} assure la cohérence bidirectionnelle :
     * le créateur de la randonnée est bien mis à jour.
     *
     * <p><b>Given</b> : un user et une {@link Hike}.<br>
     * <b>When</b>  : {@code addCreatedHike(hike)} est appelé.<br>
     * <b>Then</b>  : {@code hike.getCreator()} retourne le même user.</p>
     */
    @Test
    public void testAddCreatedHike_coherenceBidirectionnelle_creatorAssigne() {
        // Given
        Hike hike = new Hike();
        hike.setLibelle("GR20");
        hike.setDureeJours(2);

        // When
        user.addCreatedHike(hike);

        // Then
        assertEquals("Le créateur de la randonnée doit être le user",
                user, hike.getCreator());
    }

    /**
     * Vérifie que plusieurs randonnées distinctes peuvent être ajoutées.
     *
     * <p><b>Given</b> : un user et 3 randonnées distinctes.<br>
     * <b>When</b>  : {@code addCreatedHike()} est appelé 3 fois.<br>
     * <b>Then</b>  : {@code createdHikes} contient 3 éléments.</p>
     */
    @Test
    public void testAddCreatedHike_troisRandonnees_tailleTrois() {
        // Given
        Hike h1 = new Hike(); h1.setLibelle("GR20");    h1.setId(1); h1.setDureeJours(1);
        Hike h2 = new Hike(); h2.setLibelle("GR10");    h2.setId(2); h2.setDureeJours(2);
        Hike h3 = new Hike(); h3.setLibelle("GR34");    h3.setId(3); h3.setDureeJours(3);

        // When
        user.addCreatedHike(h1);
        user.addCreatedHike(h2);
        user.addCreatedHike(h3);

        // Then
        assertEquals("createdHikes doit contenir 3 randonnées", 3, user.getCreatedHikes().size());
    }

    /**
     * Vérifie qu'ajouter deux fois la même randonnée ne crée pas de doublon
     * (comportement du {@link java.util.HashSet}).
     *
     * <p><b>Given</b> : un user et une même {@link Hike} ajoutée deux fois.<br>
     * <b>When</b>  : {@code addCreatedHike()} est appelé deux fois avec le même objet.<br>
     * <b>Then</b>  : {@code createdHikes} ne contient qu'un seul élément.</p>
     */
    @Test
    public void testAddCreatedHike_memeHikeDeuxFois_pasDeDateDoublon() {
        // Given
        Hike hike = new Hike();
        hike.setLibelle("Tour du Mont-Blanc");
        hike.setId(1);
        hike.setDureeJours(1);

        // When
        user.addCreatedHike(hike);
        user.addCreatedHike(hike); // Doublon

        // Then — HashSet déduplique automatiquement
        assertEquals("Un doublon ne doit pas être ajouté dans le HashSet",
                1, user.getCreatedHikes().size());
    }

    // =========================================================================
    // equals()
    // =========================================================================

    /**
     * Vérifie que deux users avec le même mail sont égaux.
     *
     * <p><b>Given</b> : deux instances avec le même mail.<br>
     * <b>When</b>  : {@code equals()} est appelé.<br>
     * <b>Then</b>  : retourne {@code true}.</p>
     */
    @Test
    public void testEquals_memeMail_retourneTrue() {
        // Given
        User u1 = new User(); u1.setMail(MAIL_NOMINAL);
        User u2 = new User(); u2.setMail(MAIL_NOMINAL);

        // When / Then
        assertTrue("Deux users avec le même mail doivent être égaux", u1.equals(u2));
    }

    /**
     * Vérifie que deux users avec des mails différents sont inégaux.
     *
     * <p><b>Given</b> : deux instances avec des mails distincts.<br>
     * <b>When</b>  : {@code equals()} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testEquals_mailsDifferents_retourneFalse() {
        // Given
        User u1 = new User(); u1.setMail("user1@test.fr");
        User u2 = new User(); u2.setMail("user2@test.fr");

        // When / Then
        assertFalse("Deux users avec des mails différents doivent être inégaux", u1.equals(u2));
    }

    /**
     * Vérifie qu'un user est égal à lui-même (réflexivité).
     *
     * <p><b>Given</b> : un même objet {@link User}.<br>
     * <b>When</b>  : {@code equals(user)} est appelé sur lui-même.<br>
     * <b>Then</b>  : retourne {@code true}.</p>
     */
    @Test
    public void testEquals_memeObjet_retourneTrue() {
        // Given / When / Then
        assertTrue("Un user doit être égal à lui-même", user.equals(user));
    }

    /**
     * Vérifie qu'un user n'est pas égal à {@code null}.
     *
     * <p><b>Given</b> : {@code null} comme argument.<br>
     * <b>When</b>  : {@code equals(null)} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testEquals_comparaisonAvecNull_retourneFalse() {
        // Given / When / Then
        assertFalse("Un user ne doit pas être égal à null", user.equals(null));
    }

    /**
     * Vérifie qu'un user n'est pas égal à un objet d'un autre type.
     *
     * <p><b>Given</b> : une {@code String} comme argument.<br>
     * <b>When</b>  : {@code equals(String)} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testEquals_comparaisonAvecAutreType_retourneFalse() {
        // Given / When / Then
        assertFalse("Un user ne doit pas être égal à un objet d'un autre type",
                user.equals(MAIL_NOMINAL));
    }

    /**
     * Vérifie que deux users avec un mail {@code null} sont considérés égaux
     * (comportement de {@link java.util.Objects#equals} avec deux null).
     *
     * <p><b>Given</b> : deux users avec mail = {@code null}.<br>
     * <b>When</b>  : {@code equals()} est appelé.<br>
     * <b>Then</b>  : retourne {@code true}.</p>
     */
    @Test
    public void testEquals_deuxMailsNull_retourneTrue() {
        // Given
        User u1 = new User();
        User u2 = new User();

        // When / Then — Objects.equals(null, null) == true
        assertTrue("Deux users avec mail null doivent être égaux", u1.equals(u2));
    }

    // =========================================================================
    // toString()
    // =========================================================================

    /**
     * Vérifie que {@code toString()} contient l'id, le mail et le nom.
     *
     * <p><b>Given</b> : un user avec id=1, mail et nom définis.<br>
     * <b>When</b>  : {@code toString()} est appelé.<br>
     * <b>Then</b>  : la chaîne contient le mail et le nom.</p>
     */
    @Test
    public void testToString_formatCorrect() {
        // Given
        user.setId(1);

        // When
        String resultat = user.toString();

        // Then
        assertTrue("toString() doit contenir le mail", resultat.contains(MAIL_NOMINAL));
        assertTrue("toString() doit contenir le nom",  resultat.contains(NOM_NOMINAL));
        assertTrue("toString() doit contenir l'id",    resultat.contains("1"));
    }
}
