package fr.iutrodez.a4awalk.fragmentsTest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.fragments.FragmentListeParcours;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;

/**
 * Classe de tests d'instrumentation pour {@link FragmentListeParcours}.
 *
 * <p>Ces tests s'exécutent dans le dossier {@code androidTest/} car ils s'appuient
 * sur {@link FragmentScenario}, qui nécessite un environnement Android réel
 * (émulateur ou appareil physique).</p>
 *
 * <p>Catégories couvertes :</p>
 * <ul>
 *   <li><b>Cas nominaux</b> : création du fragment avec un {@link User} valide,
 *       récupération correcte des arguments, cycle de vie.</li>
 *   <li><b>Cas limites</b> : {@link User} sans données optionnelles, bundle avec
 *       clé inconnue.</li>
 *   <li><b>Cas d'erreur</b> : création sans arguments, {@link User} {@code null}
 *       dans le bundle.</li>
 * </ul>
 *
 * <p>Dépendances requises dans {@code build.gradle} (module app) :</p>
 * <pre>
 *   androidTestImplementation 'androidx.fragment:fragment-testing:1.8.+'
 *   androidTestImplementation 'androidx.test.ext:junit:1.2.+'
 *   androidTestImplementation 'androidx.test.espresso:espresso-core:3.6.+'
 *   debugImplementation 'androidx.fragment:fragment-testing-manifest:1.8.+'
 * </pre>
 *
 * <p><b>Emplacement du fichier :</b>
 * {@code app/src/androidTest/java/fr/iutrodez/a4awalk/fragments/FragmentListeParcoursTest.java}</p>
 *
 * @author Votre équipe
 * @version 1.0
 * @see FragmentListeParcours
 */
@RunWith(AndroidJUnit4.class)
public class FragmentListeParcoursTest {

    // -------------------------------------------------------------------------
    // Données de test
    // -------------------------------------------------------------------------

    /** Nom utilisé pour les tests nominaux. */
    private static final String NOM_VALIDE       = "Dupont";

    /** Prénom utilisé pour les tests nominaux. */
    private static final String PRENOM_VALIDE    = "Jean";

    /** Mail utilisé pour les tests nominaux. */
    private static final String MAIL_VALIDE      = "jean.dupont@test.fr";

    /** Adresse utilisée pour les tests nominaux. */
    private static final String ADRESSE_VALIDE   = "1 rue des Crêtes, Rodez";

    /** Âge utilisé pour les tests nominaux. */
    private static final int    AGE_VALIDE       = 30;

    /**
     * Crée un {@link User} valide complet pour les tests nominaux.
     * Utilise le constructeur à 7 paramètres (sans mot de passe).
     *
     * @return un {@link User} correctement initialisé
     */
    private User creerUserValide() {
        return new User(
                NOM_VALIDE,
                PRENOM_VALIDE,
                AGE_VALIDE,
                MAIL_VALIDE,
                ADRESSE_VALIDE,
                Level.DEBUTANT,
                Morphology.MOYENNE
        );
    }

    /**
     * Crée un {@link User} avec des champs minimaux (cas limite).
     * Utilise le constructeur vide puis les setters.
     *
     * @return un {@link User} aux champs partiellement renseignés
     */
    private User creerUserMinimal() {
        User user = new User();
        user.setNom("");
        user.setPrenom("");
        user.setMail("vide@test.fr");
        return user;
    }

    // =========================================================================
    // CAS NOMINAUX
    // =========================================================================

    /**
     * Vérifie que {@link FragmentListeParcours#newInstance(User)} crée bien
     * un fragment avec des arguments non null.
     *
     * <p><b>Given</b> : un objet {@link User} valide avec prénom et nom.<br>
     * <b>When</b>  : {@code newInstance(user)} est appelé.<br>
     * <b>Then</b>  : le fragment retourné possède un bundle d'arguments non null.</p>
     */
    @Test
    public void testNewInstance_avecUserValide_argumentsNonNull() {
        // Given
        User user = creerUserValide();

        // When
        FragmentListeParcours fragment = FragmentListeParcours.newInstance(user);

        // Then
        assertNotNull("Le fragment créé ne doit pas être null", fragment);
        assertNotNull("Les arguments du fragment ne doivent pas être null",
                fragment.getArguments());
    }

    /**
     * Vérifie que le bundle contient bien la clé {@code "USER_DATA"} après
     * un appel à {@link FragmentListeParcours#newInstance(User)}.
     *
     * <p><b>Given</b> : un objet {@link User} valide.<br>
     * <b>When</b>  : {@code newInstance(user)} est appelé.<br>
     * <b>Then</b>  : {@code getArguments().containsKey("USER_DATA")} retourne {@code true}.</p>
     */
    @Test
    public void testNewInstance_avecUserValide_bundleContientCleUserData() {
        // Given
        User user = creerUserValide();

        // When
        FragmentListeParcours fragment = FragmentListeParcours.newInstance(user);

        // Then
        assertNotNull(fragment.getArguments());
        assert fragment.getArguments().containsKey("USER_DATA")
                : "Le bundle doit contenir la clé USER_DATA";
    }

    /**
     * Vérifie que la vue du fragment s'inflate correctement et que le layout racine
     * est affiché lors du passage à l'état {@code RESUMED}.
     *
     * <p><b>Given</b> : un {@link User} valide passé via {@code newInstance}.<br>
     * <b>When</b>  : le fragment est lancé dans un conteneur via {@link FragmentScenario}.<br>
     * <b>Then</b>  : le layout {@code fragment_liste_parcours} est visible à l'écran.</p>
     */
    @Test
    public void testOnCreateView_avecUserValide_vueAffichee() {
        // Given
        User user = creerUserValide();
        Bundle args = new Bundle();
        args.putParcelable("USER_DATA", user);

        // When
        FragmentScenario<FragmentListeParcours> scenario =
                FragmentScenario.launchInContainer(
                        FragmentListeParcours.class,
                        args,
                        R.style.Theme__4aWalk
                );

        // Then : le fragment est visible dans le conteneur
        scenario.onFragment(fragment ->
                assertNotNull("La vue du fragment ne doit pas être null", fragment.getView())
        );
    }

    /**
     * Vérifie que le fragment atteint l'état {@code RESUMED} sans crash
     * lorsqu'un {@link User} valide est fourni.
     *
     * <p><b>Given</b> : un {@link User} valide dans le bundle.<br>
     * <b>When</b>  : le cycle de vie est amené jusqu'à {@code RESUMED}.<br>
     * <b>Then</b>  : aucune exception n'est levée et l'état est bien {@code RESUMED}.</p>
     */
    @Test
    public void testCycleDeVie_avecUserValide_atteintEtatResumed() {
        // Given
        User user = creerUserValide();
        Bundle args = new Bundle();
        args.putParcelable("USER_DATA", user);

        // When
        FragmentScenario<FragmentListeParcours> scenario =
                FragmentScenario.launchInContainer(
                        FragmentListeParcours.class,
                        args,
                        R.style.Theme__4aWalk
                );

        // Then : on force l'état RESUMED et on vérifie qu'il n'y a pas de crash
        scenario.moveToState(Lifecycle.State.RESUMED);
        scenario.onFragment(fragment ->
                assertNotNull("Le fragment doit exister après RESUMED", fragment)
        );
    }

    // =========================================================================
    // CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que le fragment supporte un {@link User} dont les champs optionnels
     * sont vides (prénom et nom = chaînes vides).
     *
     * <p><b>Given</b> : un {@link User} avec des champs vides.<br>
     * <b>When</b>  : {@code newInstance(user)} est appelé.<br>
     * <b>Then</b>  : le fragment est créé sans exception et les arguments sont présents.</p>
     */
    @Test
    public void testNewInstance_avecUserChampVides_fragmentCreesSansException() {
        // Given — User avec données vides via constructeur vide + setters (cas limite)
        User userVide = creerUserMinimal();

        // When
        FragmentListeParcours fragment = FragmentListeParcours.newInstance(userVide);

        // Then
        assertNotNull("Le fragment doit être créé même avec un User aux champs vides", fragment);
        assertNotNull("Les arguments doivent être présents même avec un User vide",
                fragment.getArguments());
    }

    /**
     * Vérifie que l'ajout d'une clé inconnue dans le bundle n'empêche pas
     * le fragment de fonctionner correctement.
     *
     * <p><b>Given</b> : un bundle contenant {@code "USER_DATA"} valide
     *   ET une clé parasite {@code "CLE_INCONNUE"}.<br>
     * <b>When</b>  : le fragment est lancé avec ce bundle.<br>
     * <b>Then</b>  : le fragment s'affiche normalement sans crash.</p>
     */
    @Test
    public void testOnCreateView_bundleAvecCleParasite_fragmentFonctionneNormalement() {
        // Given
        User user = creerUserValide();
        Bundle args = new Bundle();
        args.putParcelable("USER_DATA", user);
        args.putString("CLE_INCONNUE", "valeur_parasite"); // clé non attendue

        // When
        FragmentScenario<FragmentListeParcours> scenario =
                FragmentScenario.launchInContainer(
                        FragmentListeParcours.class,
                        args,
                        R.style.Theme__4aWalk
                );

        // Then : le fragment ne doit pas crasher à cause de la clé inconnue
        scenario.onFragment(fragment ->
                assertNotNull("Le fragment doit fonctionner malgré une clé parasite dans le bundle",
                        fragment.getView())
        );
    }

    /**
     * Vérifie le passage vers l'état {@code STARTED} puis retour à {@code CREATED}
     * (simulation d'une mise en arrière-plan).
     *
     * <p><b>Given</b> : un fragment lancé normalement avec un {@link User} valide.<br>
     * <b>When</b>  : le cycle de vie est amené à {@code STARTED} puis réduit à {@code CREATED}.<br>
     * <b>Then</b>  : aucune exception n'est levée (robustesse du cycle de vie).</p>
     */
    @Test
    public void testCycleDeVie_miseEnArrierePlan_sansException() {
        // Given
        User user = creerUserValide();
        Bundle args = new Bundle();
        args.putParcelable("USER_DATA", user);

        FragmentScenario<FragmentListeParcours> scenario =
                FragmentScenario.launchInContainer(
                        FragmentListeParcours.class,
                        args,
                        R.style.Theme__4aWalk
                );

        // When : simulation d'une mise en arrière-plan
        scenario.moveToState(Lifecycle.State.STARTED);
        scenario.moveToState(Lifecycle.State.CREATED);

        // Then : pas de crash = test réussi
        scenario.onFragment(fragment ->
                assertNotNull("Le fragment doit survivre au changement d'état du cycle de vie",
                        fragment)
        );
    }

    // =========================================================================
    // CAS D'ERREUR
    // =========================================================================

    /**
     * Vérifie que le fragment lancé <b>sans aucun argument</b> ne lève pas d'exception
     * et affiche quand même sa vue (car {@code getArguments()} est protégé par un {@code if}).
     *
     * <p><b>Given</b> : aucun argument n'est passé au fragment (bundle null).<br>
     * <b>When</b>  : le fragment est lancé via {@link FragmentScenario} sans bundle.<br>
     * <b>Then</b>  : la vue est inflatée sans crash ({@code user} reste {@code null}).</p>
     */
    @Test
    public void testOnCreateView_sansArguments_vueInflateeSansCrash() {
        // Given — aucun argument passé (bundle null)
        // When
        FragmentScenario<FragmentListeParcours> scenario =
                FragmentScenario.launchInContainer(
                        FragmentListeParcours.class,
                        null, // pas de bundle
                        R.style.Theme__4aWalk
                );

        // Then : le fragment doit s'afficher même sans user (user == null toléré)
        scenario.onFragment(fragment ->
                assertNotNull("La vue doit s'inflater même sans arguments", fragment.getView())
        );
    }

    /**
     * Vérifie que le fragment créé directement via {@code new} (sans {@code newInstance})
     * possède un bundle d'arguments null.
     *
     * <p><b>Given</b> : instanciation directe {@code new FragmentListeParcours()}.<br>
     * <b>When</b>  : {@code getArguments()} est appelé.<br>
     * <b>Then</b>  : le résultat est {@code null} (aucun argument défini).</p>
     */
    @Test
    public void testConstructeurDirect_sansNewInstance_argumentsNull() {
        // Given / When : instanciation directe, sans passer par newInstance
        FragmentListeParcours fragment = new FragmentListeParcours();

        // Then : pas d'arguments car newInstance n'a pas été utilisé
        assertNull("Les arguments doivent être null si newInstance n'est pas utilisé",
                fragment.getArguments());
    }

    /**
     * Vérifie que passer {@code null} comme {@link User} dans le bundle ne provoque
     * pas de crash lors de {@code onCreateView}.
     *
     * <p><b>Given</b> : bundle contenant {@code "USER_DATA" → null}.<br>
     * <b>When</b>  : le fragment est lancé via {@link FragmentScenario}.<br>
     * <b>Then</b>  : la vue est inflatée sans {@link NullPointerException}.</p>
     */
    @Test
    public void testOnCreateView_userNullDansBundle_vueInflateeSansCrash() {
        // Given — clé présente mais valeur null
        Bundle args = new Bundle();
        args.putParcelable("USER_DATA", null);

        // When
        FragmentScenario<FragmentListeParcours> scenario =
                FragmentScenario.launchInContainer(
                        FragmentListeParcours.class,
                        args,
                        R.style.Theme__4aWalk
                );

        // Then : pas de NPE, la vue doit s'inflater normalement
        scenario.onFragment(fragment ->
                assertNotNull("La vue doit s'inflater même si USER_DATA est null",
                        fragment.getView())
        );
    }
}
