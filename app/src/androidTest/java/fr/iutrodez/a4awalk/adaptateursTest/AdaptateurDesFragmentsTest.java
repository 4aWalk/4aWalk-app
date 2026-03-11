package fr.iutrodez.a4awalk.adaptateursTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.iutrodez.a4awalk.activites.ActivitePrincipale; // Adaptez selon votre activité hôte
import fr.iutrodez.a4awalk.adaptateurs.AdaptateurDesFragments;
import fr.iutrodez.a4awalk.fragments.FragmentListeParcours;
import fr.iutrodez.a4awalk.fragments.FragmentListeRandonnees;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;

/**
 * Classe de tests d'instrumentation pour {@link AdaptateurDesFragments}.
 *
 * <p>Vérifie le comportement de l'adaptateur ViewPager2 qui gère les deux
 * fragments principaux de l'application ({@link FragmentListeRandonnees}
 * et {@link FragmentListeParcours}).</p>
 *
 * <p>Catégories couvertes :</p>
 * <ul>
 *   <li><b>Cas nominaux</b> : nombre de fragments correct, types de fragments
 *       aux positions 0 et 1, transmission du {@link User} dans les bundles.</li>
 *   <li><b>Cas limites</b> : position aux bornes exactes (0 et 1),
 *       {@link User} avec champs vides.</li>
 *   <li><b>Cas d'erreur</b> : position négative, position hors bornes,
 *       {@link User} {@code null}.</li>
 * </ul>
 *
 * <p><b>Emplacement du fichier :</b>
 * {@code app/src/androidTest/java/fr/iutrodez/a4awalk/adaptateurs/AdaptateurDesFragmentsTest.java}</p>
 *
 * <p>Dépendances requises dans {@code build.gradle} :</p>
 * <pre>
 *   androidTestImplementation 'androidx.test.ext:junit:1.1.5'
 *   androidTestImplementation 'androidx.test.core:core:1.5.0'
 *   debugImplementation 'androidx.fragment:fragment-testing:1.6.2'
 *   debugImplementation 'androidx.fragment:fragment-testing-manifest:1.6.2'
 * </pre>
 *
 * @author Votre équipe
 * @version 1.0
 * @see AdaptateurDesFragments
 */
@RunWith(AndroidJUnit4.class)
public class AdaptateurDesFragmentsTest {

    // -------------------------------------------------------------------------
    // Constantes
    // -------------------------------------------------------------------------

    /** Nombre de fragments attendus dans le ViewPager2. */
    private static final int NB_FRAGMENTS_ATTENDU = 2;

    /** Position du fragment liste des randonnées. */
    private static final int POSITION_RANDONNEES = 0;

    /** Position du fragment liste des parcours. */
    private static final int POSITION_PARCOURS = 1;

    // -------------------------------------------------------------------------
    // Données de test
    // -------------------------------------------------------------------------

    /** Adaptateur testé, recréé avant chaque test. */
    private AdaptateurDesFragments adaptateur;

    /** User valide utilisé dans les cas nominaux. */
    private User userValide;

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    /**
     * Initialise le {@link User} valide et l'adaptateur avant chaque test.
     * L'adaptateur est instancié dans le contexte d'une vraie {@code FragmentActivity}
     * via {@link ActivityScenario} pour satisfaire le constructeur de
     * {@link androidx.viewpager2.adapter.FragmentStateAdapter}.
     */
    @Before
    public void setUp() {
        userValide = new User(
                "Dupont",
                "Jean",
                30,
                "jean.dupont@test.fr",
                "1 rue des Crêtes, Rodez",
                Level.DEBUTANT,
                Morphology.MOYENNE
        );
    }

    // =========================================================================
    // CAS NOMINAUX
    // =========================================================================

    /**
     * Vérifie que {@link AdaptateurDesFragments#getItemCount()} retourne
     * exactement 2 fragments.
     *
     * <p><b>Given</b> : un adaptateur initialisé avec un {@link User} valide.<br>
     * <b>When</b>  : {@code getItemCount()} est appelé.<br>
     * <b>Then</b>  : la valeur retournée est {@code 2}.</p>
     */
    @Test
    public void testGetItemCount_retourneToujoursDeuxFragments() {
        // Given / When / Then
        ActivityScenario.launch(ActivitePrincipale.class).onActivity(activite -> {
            // Given
            AdaptateurDesFragments adaptateur = new AdaptateurDesFragments(activite, userValide);

            // When
            int count = adaptateur.getItemCount();

            // Then
            assertEquals(
                    "L'adaptateur doit toujours gérer exactement 2 fragments",
                    NB_FRAGMENTS_ATTENDU,
                    count);
        });
    }

    /**
     * Vérifie que la position {@code 0} retourne bien une instance de
     * {@link FragmentListeRandonnees}.
     *
     * <p><b>Given</b> : un adaptateur initialisé avec un {@link User} valide.<br>
     * <b>When</b>  : {@code createFragment(0)} est appelé.<br>
     * <b>Then</b>  : le fragment retourné est une instance de {@link FragmentListeRandonnees}.</p>
     */
    @Test
    public void testCreateFragment_position0_retourneFragmentListeRandonnees() {
        // Given / When / Then
        ActivityScenario.launch(ActivitePrincipale.class).onActivity(activite -> {
            // Given
            AdaptateurDesFragments adaptateur = new AdaptateurDesFragments(activite, userValide);

            // When
            Fragment fragment = adaptateur.createFragment(POSITION_RANDONNEES);

            // Then
            assertNotNull("Le fragment à la position 0 ne doit pas être null", fragment);
            assertTrue(
                    "La position 0 doit retourner un FragmentListeRandonnees",
                    fragment instanceof FragmentListeRandonnees);
        });
    }

    /**
     * Vérifie que la position {@code 1} retourne bien une instance de
     * {@link FragmentListeParcours}.
     *
     * <p><b>Given</b> : un adaptateur initialisé avec un {@link User} valide.<br>
     * <b>When</b>  : {@code createFragment(1)} est appelé.<br>
     * <b>Then</b>  : le fragment retourné est une instance de {@link FragmentListeParcours}.</p>
     */
    @Test
    public void testCreateFragment_position1_retourneFragmentListeParcours() {
        // Given / When / Then
        ActivityScenario.launch(ActivitePrincipale.class).onActivity(activite -> {
            // Given
            AdaptateurDesFragments adaptateur = new AdaptateurDesFragments(activite, userValide);

            // When
            Fragment fragment = adaptateur.createFragment(POSITION_PARCOURS);

            // Then
            assertNotNull("Le fragment à la position 1 ne doit pas être null", fragment);
            assertTrue(
                    "La position 1 doit retourner un FragmentListeParcours",
                    fragment instanceof FragmentListeParcours);
        });
    }

    /**
     * Vérifie que le {@link User} est bien transmis dans le bundle du fragment
     * à la position {@code 0}.
     *
     * <p><b>Given</b> : un adaptateur initialisé avec un {@link User} valide.<br>
     * <b>When</b>  : {@code createFragment(0)} est appelé.<br>
     * <b>Then</b>  : le bundle du fragment contient la clé {@code "USER_DATA"}.</p>
     */
    @Test
    public void testCreateFragment_position0_userTransmisDansBundle() {
        // Given / When / Then
        ActivityScenario.launch(ActivitePrincipale.class).onActivity(activite -> {
            // Given
            AdaptateurDesFragments adaptateur = new AdaptateurDesFragments(activite, userValide);

            // When
            Fragment fragment = adaptateur.createFragment(POSITION_RANDONNEES);

            // Then
            assertNotNull("Les arguments du fragment position 0 ne doivent pas être null",
                    fragment.getArguments());
            assertTrue(
                    "Le bundle du fragment position 0 doit contenir USER_DATA",
                    fragment.getArguments().containsKey("USER_DATA"));
        });
    }

    /**
     * Vérifie que le {@link User} est bien transmis dans le bundle du fragment
     * à la position {@code 1}.
     *
     * <p><b>Given</b> : un adaptateur initialisé avec un {@link User} valide.<br>
     * <b>When</b>  : {@code createFragment(1)} est appelé.<br>
     * <b>Then</b>  : le bundle du fragment contient la clé {@code "USER_DATA"}.</p>
     */
    @Test
    public void testCreateFragment_position1_userTransmisDansBundle() {
        // Given / When / Then
        ActivityScenario.launch(ActivitePrincipale.class).onActivity(activite -> {
            // Given
            AdaptateurDesFragments adaptateur = new AdaptateurDesFragments(activite, userValide);

            // When
            Fragment fragment = adaptateur.createFragment(POSITION_PARCOURS);

            // Then
            assertNotNull("Les arguments du fragment position 1 ne doivent pas être null",
                    fragment.getArguments());
            assertTrue(
                    "Le bundle du fragment position 1 doit contenir USER_DATA",
                    fragment.getArguments().containsKey("USER_DATA"));
        });
    }

    // =========================================================================
    // CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que {@code getItemCount()} reste {@code 2} même si le {@link User}
     * passé a des champs vides.
     *
     * <p><b>Given</b> : un {@link User} initialisé avec le constructeur vide.<br>
     * <b>When</b>  : {@code getItemCount()} est appelé.<br>
     * <b>Then</b>  : la valeur retournée est toujours {@code 2}.</p>
     */
    @Test
    public void testGetItemCount_avecUserVide_retourneDeuxFragments() {
        // Given / When / Then
        ActivityScenario.launch(ActivitePrincipale.class).onActivity(activite -> {
            // Given — User minimal sans données
            User userVide = new User();
            AdaptateurDesFragments adaptateur = new AdaptateurDesFragments(activite, userVide);

            // When
            int count = adaptateur.getItemCount();

            // Then
            assertEquals(
                    "getItemCount() doit retourner 2 même avec un User vide",
                    NB_FRAGMENTS_ATTENDU,
                    count);
        });
    }

    /**
     * Vérifie que les deux positions limites valides ({@code 0} et {@code 1})
     * retournent toutes les deux des fragments non null.
     *
     * <p><b>Given</b> : un adaptateur initialisé avec un {@link User} valide.<br>
     * <b>When</b>  : {@code createFragment(0)} et {@code createFragment(1)} sont appelés.<br>
     * <b>Then</b>  : les deux fragments retournés sont non null.</p>
     */
    @Test
    public void testCreateFragment_auxDeuxBornesValides_fragmentsNonNull() {
        // Given / When / Then
        ActivityScenario.launch(ActivitePrincipale.class).onActivity(activite -> {
            // Given
            AdaptateurDesFragments adaptateur = new AdaptateurDesFragments(activite, userValide);

            // When
            Fragment fragmentBorneMin = adaptateur.createFragment(0);
            Fragment fragmentBorneMax = adaptateur.createFragment(1);

            // Then
            assertNotNull("Le fragment à la position 0 (borne min) ne doit pas être null",
                    fragmentBorneMin);
            assertNotNull("Le fragment à la position 1 (borne max) ne doit pas être null",
                    fragmentBorneMax);
        });
    }

    // =========================================================================
    // CAS D'ERREUR
    // =========================================================================

    /**
     * Vérifie que {@code createFragment(-1)} retourne {@code null}
     * (position négative non gérée par le {@code switch}).
     *
     * <p><b>Given</b> : un adaptateur initialisé avec un {@link User} valide.<br>
     * <b>When</b>  : {@code createFragment(-1)} est appelé.<br>
     * <b>Then</b>  : {@code null} est retourné (case {@code default} du switch).</p>
     */
    @Test
    public void testCreateFragment_positionNegative_retourneNull() {
        // Given / When / Then
        ActivityScenario.launch(ActivitePrincipale.class).onActivity(activite -> {
            // Given
            AdaptateurDesFragments adaptateur = new AdaptateurDesFragments(activite, userValide);

            // When
            Fragment fragment = adaptateur.createFragment(-1);

            // Then
            assertNull(
                    "Une position négative doit retourner null (case default du switch)",
                    fragment);
        });
    }

    /**
     * Vérifie que {@code createFragment(2)} retourne {@code null}
     * (position hors des bornes valides).
     *
     * <p><b>Given</b> : un adaptateur initialisé avec un {@link User} valide.<br>
     * <b>When</b>  : {@code createFragment(2)} est appelé.<br>
     * <b>Then</b>  : {@code null} est retourné (case {@code default} du switch).</p>
     */
    @Test
    public void testCreateFragment_positionHorsBornes_retourneNull() {
        // Given / When / Then
        ActivityScenario.launch(ActivitePrincipale.class).onActivity(activite -> {
            // Given
            AdaptateurDesFragments adaptateur = new AdaptateurDesFragments(activite, userValide);

            // When
            Fragment fragment = adaptateur.createFragment(NB_FRAGMENTS_ATTENDU);

            // Then
            assertNull(
                    "Une position hors bornes (>= 2) doit retourner null",
                    fragment);
        });
    }

    /**
     * Vérifie que l'adaptateur est correctement instancié même si le {@link User}
     * passé est {@code null}, et que {@code getItemCount()} retourne toujours {@code 2}.
     *
     * <p><b>Given</b> : {@code null} passé comme {@link User}.<br>
     * <b>When</b>  : {@code getItemCount()} est appelé.<br>
     * <b>Then</b>  : {@code 2} est retourné sans {@link NullPointerException}.</p>
     */
    @Test
    public void testGetItemCount_avecUserNull_retourneDeuxSansCrash() {
        // Given / When / Then
        ActivityScenario.launch(ActivitePrincipale.class).onActivity(activite -> {
            // Given — user null (cas dégradé)
            AdaptateurDesFragments adaptateur = new AdaptateurDesFragments(activite, null);

            // When
            int count = adaptateur.getItemCount();

            // Then : getItemCount() ne dépend pas du user → pas de crash
            assertEquals(
                    "getItemCount() doit retourner 2 même avec un User null",
                    NB_FRAGMENTS_ATTENDU,
                    count);
        });
    }
}