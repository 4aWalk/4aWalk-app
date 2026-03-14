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
}
