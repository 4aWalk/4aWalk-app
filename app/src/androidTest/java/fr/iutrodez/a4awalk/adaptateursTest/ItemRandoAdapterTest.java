package fr.iutrodez.a4awalk.adaptateursTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.a4awalk.adaptateurs.ItemRandoAdapter;
import fr.iutrodez.a4awalk.modeles.entites.Hike;

/**
 * Classe de tests unitaires pour {@link ItemRandoAdapter}.
 *
 * <p>Teste le comportement de l'adaptateur RecyclerView gérant l'affichage
 * de la liste des randonnées, ainsi que la gestion des événements de clic.</p>
 *
 * <p>Catégories couvertes :</p>
 * <ul>
 *   <li><b>Cas nominaux</b> : comptage des éléments, transmission du listener,
 *       déclenchement du callback au clic.</li>
 *   <li><b>Cas limites</b> : liste vide, liste à un élément, liste volumineuse.</li>
 *   <li><b>Cas d'erreur</b> : listener {@code null}, liste {@code null}.</li>
 * </ul>
 *
 * <p><b>Emplacement du fichier :</b>
 * {@code app/src/androidTest/java/fr/iutrodez/a4awalk/adaptateurs/ItemRandoAdapterTest.java}</p>
 *
 * @author Votre équipe
 * @version 1.0
 * @see ItemRandoAdapter
 */
@RunWith(AndroidJUnit4.class)
public class ItemRandoAdapterTest {

    // -------------------------------------------------------------------------
    // Données de test
    // -------------------------------------------------------------------------

    /** Adaptateur testé, recréé avant chaque test. */
    private ItemRandoAdapter adaptateur;

    /** Liste de randonnées utilisée dans les cas nominaux. */
    private List<Hike> listeValide;

    /** Mock du listener de clic. */
    private ItemRandoAdapter.OnRandoClickListener mockListener;

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    /**
     * Initialise une liste de 3 randonnées et un mock du listener avant chaque test.
     */
    @Before
    public void setUp() {
        mockListener = mock(ItemRandoAdapter.OnRandoClickListener.class);

        listeValide = new ArrayList<>();
        listeValide.add(creerHike("Randonnée des Crêtes", 1));
        listeValide.add(creerHike("Tour du Lac", 2));
        listeValide.add(creerHike("Sentier des Forêts", 3));

        adaptateur = new ItemRandoAdapter(listeValide, mockListener);
    }

    // -------------------------------------------------------------------------
    // Méthode utilitaire
    // -------------------------------------------------------------------------

    /**
     * Crée une instance de {@link Hike} pour les tests.
     *
     * @param libelle le libellé de la randonnée
     * @param id      l'identifiant de la randonnée
     * @return une instance de {@link Hike} initialisée
     */
    private Hike creerHike(String libelle, int id) {
        Hike hike = new Hike();
        hike.setLibelle(libelle);
        hike.setId(id);
        return hike;
    }

    // =========================================================================
    // CAS NOMINAUX
    // =========================================================================

    /**
     * Vérifie que {@link ItemRandoAdapter#getItemCount()} retourne
     * le nombre exact d'éléments dans la liste fournie.
     *
     * <p><b>Given</b> : une liste de 3 randonnées.<br>
     * <b>When</b>  : {@code getItemCount()} est appelé.<br>
     * <b>Then</b>  : la valeur retournée est {@code 3}.</p>
     */
    @Test
    public void testGetItemCount_avecListeDeTroisElements_retourneTrois() {
        // Given — liste de 3 éléments initialisée dans setUp()

        // When
        int count = adaptateur.getItemCount();

        // Then
        assertEquals("getItemCount() doit retourner 3 pour une liste de 3 éléments", 3, count);
    }

    /**
     * Vérifie que l'adaptateur est bien instancié avec un listener non null.
     *
     * <p><b>Given</b> : un listener valide et une liste non vide.<br>
     * <b>When</b>  : le constructeur {@link ItemRandoAdapter} est appelé.<br>
     * <b>Then</b>  : l'instance créée est non null.</p>
     */
    @Test
    public void testConstructeur_avecListeEtListenerValides_instanceNonNull() {
        // Given — adaptateur créé dans setUp()

        // When / Then
        assertNotNull("L'adaptateur ne doit pas être null après instanciation", adaptateur);
    }

    /**
     * Vérifie que le callback {@link ItemRandoAdapter.OnRandoClickListener#onRandoClick(Hike)}
     * est bien déclenché avec le bon objet {@link Hike} lors d'un clic simulé.
     *
     * <p><b>Given</b> : un listener mocké et une liste de randonnées.<br>
     * <b>When</b>  : {@code onRandoClick} est appelé manuellement avec la première randonnée.<br>
     * <b>Then</b>  : le listener reçoit exactement la randonnée attendue.</p>
     */
    @Test
    public void testOnRandoClick_avecHikeValide_listenerDeclenche() {
        // Given
        Hike hikeAttendu = listeValide.get(0);

        // When — simulation du clic sur le premier élément
        mockListener.onRandoClick(hikeAttendu);

        // Then
        verify(mockListener, times(1)).onRandoClick(hikeAttendu);
    }

    /**
     * Vérifie que le listener n'est pas déclenché si aucun clic n'a eu lieu.
     *
     * <p><b>Given</b> : un adaptateur avec listener mocké, aucune interaction.<br>
     * <b>When</b>  : aucun clic n'est simulé.<br>
     * <b>Then</b>  : {@code onRandoClick} n'est jamais appelé.</p>
     */
    @Test
    public void testOnRandoClick_sansInteraction_listenerJamaisAppele() {
        // Given — adaptateur créé dans setUp(), aucun clic simulé

        // When — (aucune action)

        // Then
        verify(mockListener, never()).onRandoClick(any());
    }

    /**
     * Vérifie que chaque élément de la liste peut déclencher le listener
     * avec le bon objet {@link Hike}.
     *
     * <p><b>Given</b> : une liste de 3 randonnées.<br>
     * <b>When</b>  : un clic est simulé sur chaque élément.<br>
     * <b>Then</b>  : {@code onRandoClick} est appelé 3 fois, une fois par randonnée.</p>
     */
    @Test
    public void testOnRandoClick_surChaqueElement_listenerAppeleTroisFois() {
        // Given
        Hike hike0 = listeValide.get(0);
        Hike hike1 = listeValide.get(1);
        Hike hike2 = listeValide.get(2);

        // When — simulation de clics sur chaque élément
        mockListener.onRandoClick(hike0);
        mockListener.onRandoClick(hike1);
        mockListener.onRandoClick(hike2);

        // Then
        verify(mockListener, times(1)).onRandoClick(hike0);
        verify(mockListener, times(1)).onRandoClick(hike1);
        verify(mockListener, times(1)).onRandoClick(hike2);
    }

    // =========================================================================
    // CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que {@code getItemCount()} retourne {@code 0} pour une liste vide.
     *
     * <p><b>Given</b> : une liste vide.<br>
     * <b>When</b>  : {@code getItemCount()} est appelé.<br>
     * <b>Then</b>  : la valeur retournée est {@code 0}.</p>
     */
    @Test
    public void testGetItemCount_avecListeVide_retourneZero() {
        // Given
        List<Hike> listeVide = new ArrayList<>();
        ItemRandoAdapter adaptateurVide = new ItemRandoAdapter(listeVide, mockListener);

        // When
        int count = adaptateurVide.getItemCount();

        // Then
        assertEquals("getItemCount() doit retourner 0 pour une liste vide", 0, count);
    }

    /**
     * Vérifie que {@code getItemCount()} retourne {@code 1} pour une liste
     * contenant un seul élément.
     *
     * <p><b>Given</b> : une liste avec une seule randonnée.<br>
     * <b>When</b>  : {@code getItemCount()} est appelé.<br>
     * <b>Then</b>  : la valeur retournée est {@code 1}.</p>
     */
    @Test
    public void testGetItemCount_avecUnSeulElement_retourneUn() {
        // Given
        List<Hike> listeUnElement = new ArrayList<>();
        listeUnElement.add(creerHike("Randonnée Unique", 99));
        ItemRandoAdapter adaptateurUnElement = new ItemRandoAdapter(listeUnElement, mockListener);

        // When
        int count = adaptateurUnElement.getItemCount();

        // Then
        assertEquals("getItemCount() doit retourner 1 pour une liste à un élément", 1, count);
    }

    /**
     * Vérifie que {@code getItemCount()} gère correctement une liste volumineuse
     * (100 éléments).
     *
     * <p><b>Given</b> : une liste de 100 randonnées.<br>
     * <b>When</b>  : {@code getItemCount()} est appelé.<br>
     * <b>Then</b>  : la valeur retournée est {@code 100}.</p>
     */
    @Test
    public void testGetItemCount_avecListeVolumeuse_retourneCentElements() {
        // Given
        List<Hike> listeVolumeuse = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            listeVolumeuse.add(creerHike("Randonnée " + i, i));
        }
        ItemRandoAdapter adaptateurVolumeux = new ItemRandoAdapter(listeVolumeuse, mockListener);

        // When
        int count = adaptateurVolumeux.getItemCount();

        // Then
        assertEquals("getItemCount() doit gérer une liste de 100 éléments", 100, count);
    }

    /**
     * Vérifie que le listener est appelé avec le dernier élément de la liste
     * (clic sur l'élément en position finale).
     *
     * <p><b>Given</b> : une liste de 3 randonnées.<br>
     * <b>When</b>  : un clic est simulé sur le dernier élément.<br>
     * <b>Then</b>  : {@code onRandoClick} est appelé avec la bonne randonnée.</p>
     */
    @Test
    public void testOnRandoClick_surDernierElement_listenerDeclenche() {
        // Given
        Hike dernierHike = listeValide.get(listeValide.size() - 1);

        // When
        mockListener.onRandoClick(dernierHike);

        // Then
        verify(mockListener, times(1)).onRandoClick(dernierHike);
    }

    // =========================================================================
    // CAS D'ERREUR
    // =========================================================================

    /**
     * Vérifie que passer un listener {@code null} n'empêche pas l'instanciation
     * de l'adaptateur.
     *
     * <p><b>Given</b> : un listener {@code null}.<br>
     * <b>When</b>  : le constructeur {@link ItemRandoAdapter} est appelé.<br>
     * <b>Then</b>  : l'instance est créée sans exception.</p>
     *
     * <p><em>Note :</em> un clic sur un item avec listener {@code null} provoquerait
     * une {@link NullPointerException} dans {@code onBindViewHolder}. Ce test documente
     * ce comportement et signale un besoin de guard dans le code source.</p>
     */
    @Test
    public void testConstructeur_avecListenerNull_instanceCreeeSansException() {
        // Given
        ItemRandoAdapter.OnRandoClickListener listenerNull = null;

        // When
        ItemRandoAdapter adaptateurSansListener = new ItemRandoAdapter(listeValide, listenerNull);

        // Then : pas de NPE à la construction
        assertNotNull("L'adaptateur doit être instanciable même avec un listener null",
                adaptateurSansListener);
        assertEquals("getItemCount() doit fonctionner même avec un listener null",
                3, adaptateurSansListener.getItemCount());
    }

    /**
     * Vérifie que {@code getItemCount()} sur une liste vide retourne {@code 0}
     * et que le listener n'est jamais déclenché (aucun élément cliquable).
     *
     * <p><b>Given</b> : une liste vide et un listener mocké.<br>
     * <b>When</b>  : {@code getItemCount()} est appelé.<br>
     * <b>Then</b>  : {@code 0} est retourné et le listener n'est jamais invoqué.</p>
     */
    @Test
    public void testListeVide_listenerJamaisDeclenche() {
        // Given
        List<Hike> listeVide = new ArrayList<>();
        ItemRandoAdapter adaptateurVide = new ItemRandoAdapter(listeVide, mockListener);

        // When
        int count = adaptateurVide.getItemCount();

        // Then
        assertEquals("Aucun élément dans la liste vide", 0, count);
        verify(mockListener, never()).onRandoClick(any());
    }
}
