package fr.iutrodez.a4awalk.modelesTest.entitesTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import fr.iutrodez.a4awalk.modeles.entites.EquipmentItem;

/**
 * Classe de tests unitaires pour {@link EquipmentItem}.
 *
 * <p>Ces tests s'exécutent dans {@code test/} (JVM pure, sans émulateur)
 * car {@link EquipmentItem} ne dépend d'aucune classe Android.</p>
 *
 * <p>Catégories couvertes :</p>
 * <ul>
 *   <li><b>Constructeurs</b> : initialisation correcte des champs via le
 *       constructeur vide et le constructeur complet.</li>
 *   <li><b>{@code getWeightKg()}</b> : conversion grammes → kilogrammes,
 *       cas nominaux, limites et erreurs.</li>
 *   <li><b>{@code equals()}</b> : égalité par id, par nom, réflexivité,
 *       comparaison avec null et objet différent.</li>
 *   <li><b>{@code toString()}</b> : format correct selon {@code permetRepos}.</li>
 *   <li><b>Cas limites</b> : masse nulle, masse très grande, nom vide.</li>
 * </ul>
 *
 * <p><b>Emplacement du fichier :</b>
 * {@code app/src/test/java/fr/iutrodez/a4awalk/modeles/entites/EquipmentItemTest.java}</p>
 *
 * @author Votre équipe
 * @version 1.0
 * @see EquipmentItem
 */
public class EquipmentItemTest {

    // -------------------------------------------------------------------------
    // Champs
    // -------------------------------------------------------------------------

    /** Équipement nominal réutilisé dans les tests. */
    private EquipmentItem equipmentNominal;

    // -------------------------------------------------------------------------
    // Constantes de test
    // -------------------------------------------------------------------------

    private static final String NOM_NOMINAL         = "Tente";
    private static final String DESC_NOMINALE       = "Tente 2 places légère";
    private static final double MASSE_NOMINALE_G    = 2000.0;
    private static final double MASSE_NOMINALE_KG   = 2.0;
    private static final boolean PERMET_REPOS       = true;

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    /**
     * Crée un {@link EquipmentItem} nominal avant chaque test.
     */
    @Before
    public void setUp() {
        equipmentNominal = new EquipmentItem(
                NOM_NOMINAL,
                DESC_NOMINALE,
                MASSE_NOMINALE_G,
                PERMET_REPOS
        );
    }

    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================

    /**
     * Vérifie que le constructeur vide crée une instance sans crash
     * et laisse tous les champs à {@code null} / {@code false}.
     *
     * <p><b>Given</b> : aucun argument.<br>
     * <b>When</b>  : {@code new EquipmentItem()} est appelé.<br>
     * <b>Then</b>  : nom, description et masse sont {@code null},
     *               {@code permetRepos} vaut {@code false}.</p>
     */
    @Test
    public void testConstructeurVide_champsNullEtPermetReposFalse() {
        // Given / When
        EquipmentItem item = new EquipmentItem();

        // Then
        assertNull("Le nom doit être null avec le constructeur vide",        item.getNom());
        assertNull("La description doit être null avec le constructeur vide", item.getDescription());
        assertNull("La masse doit être null avec le constructeur vide",       item.getMasseGrammes());
        assertFalse("permetRepos doit être false par défaut",                 item.isPermetRepos());
    }

    /**
     * Vérifie que le constructeur complet assigne correctement tous les champs.
     *
     * <p><b>Given</b> : nom, description, masse et permetRepos fournis.<br>
     * <b>When</b>  : {@code new EquipmentItem(nom, desc, masse, permetRepos)} est appelé.<br>
     * <b>Then</b>  : chaque getter retourne la valeur passée au constructeur.</p>
     */
    @Test
    public void testConstructeurComplet_tousLesChampsCorrectementAssignes() {
        // Given / When — equipmentNominal créé dans setUp()

        // Then
        assertEquals("Le nom doit correspondre",        NOM_NOMINAL,       equipmentNominal.getNom());
        assertEquals("La description doit correspondre", DESC_NOMINALE,    equipmentNominal.getDescription());
        assertEquals("La masse doit correspondre",       MASSE_NOMINALE_G, equipmentNominal.getMasseGrammes(), 0.001);
        assertTrue("permetRepos doit être true",         equipmentNominal.isPermetRepos());
    }

    /**
     * Vérifie que le constructeur complet avec {@code permetRepos = false}
     * assigne correctement la valeur.
     *
     * <p><b>Given</b> : {@code permetRepos = false}.<br>
     * <b>When</b>  : le constructeur est appelé.<br>
     * <b>Then</b>  : {@code isPermetRepos()} retourne {@code false}.</p>
     */
    @Test
    public void testConstructeurComplet_permetReposFalse_assigneCorrectement() {
        // Given / When
        EquipmentItem itemUtilitaire = new EquipmentItem("Réchaud", "Réchaud gaz", 300.0, false);

        // Then
        assertFalse("permetRepos doit être false", itemUtilitaire.isPermetRepos());
    }

    // =========================================================================
    // getWeightKg()
    // =========================================================================

    /**
     * Vérifie que {@code getWeightKg()} convertit correctement 2000g en 2.0 kg.
     *
     * <p><b>Given</b> : un équipement de {@code 2000g}.<br>
     * <b>When</b>  : {@code getWeightKg()} est appelé.<br>
     * <b>Then</b>  : retourne {@code 2.0}.</p>
     */
    @Test
    public void testGetWeightKg_deuxMilleGrammes_retourneDeuxKg() {
        // Given — equipmentNominal avec 2000g

        // When
        double poids = equipmentNominal.getWeightKg();

        // Then
        assertEquals("2000g doit être converti en 2.0 kg", MASSE_NOMINALE_KG, poids, 0.001);
    }

    /**
     * Vérifie que {@code getWeightKg()} convertit correctement 500g en 0.5 kg.
     *
     * <p><b>Given</b> : un équipement de {@code 500g}.<br>
     * <b>When</b>  : {@code getWeightKg()} est appelé.<br>
     * <b>Then</b>  : retourne {@code 0.5}.</p>
     */
    @Test
    public void testGetWeightKg_cinqCentsGrammes_retourneDemiKg() {
        // Given
        EquipmentItem item = new EquipmentItem("Gourde", "Gourde 0.5L", 500.0, false);

        // When
        double poids = item.getWeightKg();

        // Then
        assertEquals("500g doit être converti en 0.5 kg", 0.5, poids, 0.001);
    }

    /**
     * Vérifie que {@code getWeightKg()} retourne {@code 0.0} pour une masse nulle.
     *
     * <p><b>Given</b> : un équipement de {@code 0g}.<br>
     * <b>When</b>  : {@code getWeightKg()} est appelé.<br>
     * <b>Then</b>  : retourne {@code 0.0}.</p>
     */
    @Test
    public void testGetWeightKg_masseNulle_retourneZero() {
        // Given
        EquipmentItem item = new EquipmentItem("ItemVirtuel", "Sans masse", 0.0, false);

        // When
        double poids = item.getWeightKg();

        // Then
        assertEquals("0g doit être converti en 0.0 kg", 0.0, poids, 0.001);
    }

    /**
     * Vérifie que {@code getWeightKg()} gère correctement une masse très grande.
     *
     * <p><b>Given</b> : un équipement de {@code 10 000g} (10 kg).<br>
     * <b>When</b>  : {@code getWeightKg()} est appelé.<br>
     * <b>Then</b>  : retourne {@code 10.0}.</p>
     */
    @Test
    public void testGetWeightKg_dixMilleGrammes_retourneDixKg() {
        // Given
        EquipmentItem itemLourd = new EquipmentItem("Kayak", "Équipement lourd", 10000.0, false);

        // When
        double poids = itemLourd.getWeightKg();

        // Then
        assertEquals("10000g doit être converti en 10.0 kg", 10.0, poids, 0.001);
    }

    /**
     * Vérifie que {@code getWeightKg()} retourne une valeur correcte
     * pour une masse non ronde (précision décimale).
     *
     * <p><b>Given</b> : un équipement de {@code 1500g}.<br>
     * <b>When</b>  : {@code getWeightKg()} est appelé.<br>
     * <b>Then</b>  : retourne {@code 1.5}.</p>
     */
    @Test
    public void testGetWeightKg_millecinqCentsGrammes_retourneUnVirguleCinqKg() {
        // Given
        EquipmentItem item = new EquipmentItem("Sac de couchage", "Duvet léger", 1500.0, true);

        // When
        double poids = item.getWeightKg();

        // Then
        assertEquals("1500g doit être converti en 1.5 kg", 1.5, poids, 0.001);
    }

    // =========================================================================
    // equals()
    // =========================================================================

    /**
     * Vérifie que deux équipements avec le même id sont égaux.
     *
     * <p><b>Given</b> : deux instances avec {@code id = 1L}.<br>
     * <b>When</b>  : {@code equals()} est appelé.<br>
     * <b>Then</b>  : retourne {@code true}.</p>
     */
    @Test
    public void testEquals_memeId_retourneTrue() {
        // Given
        EquipmentItem item1 = new EquipmentItem("Tente", "desc", 2000.0, true);
        EquipmentItem item2 = new EquipmentItem("Duvet", "desc", 1000.0, true);
        item1.setId(1L);
        item2.setId(1L);

        // When / Then
        assertTrue("Deux équipements avec le même id doivent être égaux", item1.equals(item2));
    }

    /**
     * Vérifie que deux équipements avec le même nom sont égaux (clé naturelle).
     *
     * <p><b>Given</b> : deux instances avec le même nom mais des ids différents.<br>
     * <b>When</b>  : {@code equals()} est appelé.<br>
     * <b>Then</b>  : retourne {@code true}.</p>
     */
    @Test
    public void testEquals_memeNom_retourneTrue() {
        // Given
        EquipmentItem item1 = new EquipmentItem("Tente", "desc1", 2000.0, true);
        EquipmentItem item2 = new EquipmentItem("Tente", "desc2", 1500.0, false);
        item1.setId(1L);
        item2.setId(2L);

        // When / Then
        assertTrue("Deux équipements avec le même nom doivent être égaux", item1.equals(item2));
    }

    /**
     * Vérifie que deux équipements avec des ids et des noms différents sont inégaux.
     *
     * <p><b>Given</b> : deux instances complètement distinctes.<br>
     * <b>When</b>  : {@code equals()} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testEquals_idEtNomDifferents_retourneFalse() {
        // Given
        EquipmentItem item1 = new EquipmentItem("Tente", "desc", 2000.0, true);
        EquipmentItem item2 = new EquipmentItem("Duvet", "desc", 1000.0, false);
        item1.setId(1L);
        item2.setId(2L);

        // When / Then
        assertFalse("Deux équipements distincts ne doivent pas être égaux", item1.equals(item2));
    }

    /**
     * Vérifie qu'un équipement est égal à lui-même (réflexivité).
     *
     * <p><b>Given</b> : un même objet {@link EquipmentItem}.<br>
     * <b>When</b>  : {@code equals(item)} est appelé sur lui-même.<br>
     * <b>Then</b>  : retourne {@code true}.</p>
     */
    @Test
    public void testEquals_memeObjet_retourneTrue() {
        // Given / When / Then
        assertTrue("Un équipement doit être égal à lui-même",
                equipmentNominal.equals(equipmentNominal));
    }

    /**
     * Vérifie qu'un équipement n'est pas égal à {@code null}.
     *
     * <p><b>Given</b> : {@code null} comme argument.<br>
     * <b>When</b>  : {@code equals(null)} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testEquals_comparaisonAvecNull_retourneFalse() {
        // Given / When / Then
        assertFalse("Un équipement ne doit pas être égal à null",
                equipmentNominal.equals(null));
    }

    /**
     * Vérifie qu'un équipement n'est pas égal à un objet d'un autre type.
     *
     * <p><b>Given</b> : une {@code String} comme argument.<br>
     * <b>When</b>  : {@code equals(String)} est appelé.<br>
     * <b>Then</b>  : retourne {@code false}.</p>
     */
    @Test
    public void testEquals_comparaisonAvecAutreType_retourneFalse() {
        // Given / When / Then
        assertFalse("Un équipement ne doit pas être égal à un objet d'un autre type",
                equipmentNominal.equals("Tente"));
    }

    // =========================================================================
    // toString()
    // =========================================================================

    /**
     * Vérifie que {@code toString()} affiche "Repos" quand {@code permetRepos = true}.
     *
     * <p><b>Given</b> : un équipement avec {@code permetRepos = true}.<br>
     * <b>When</b>  : {@code toString()} est appelé.<br>
     * <b>Then</b>  : la chaîne contient {@code "Repos"}.</p>
     */
    @Test
    public void testToString_permetReposTrue_contientRepos() {
        // Given — equipmentNominal avec permetRepos = true

        // When
        String resultat = equipmentNominal.toString();

        // Then
        assertTrue("toString() doit contenir 'Repos' quand permetRepos est true",
                resultat.contains("Repos"));
    }

    /**
     * Vérifie que {@code toString()} affiche "Utilitaire" quand {@code permetRepos = false}.
     *
     * <p><b>Given</b> : un équipement avec {@code permetRepos = false}.<br>
     * <b>When</b>  : {@code toString()} est appelé.<br>
     * <b>Then</b>  : la chaîne contient {@code "Utilitaire"}.</p>
     */
    @Test
    public void testToString_permetReposFalse_contientUtilitaire() {
        // Given
        EquipmentItem itemUtilitaire = new EquipmentItem("Réchaud", "desc", 300.0, false);

        // When
        String resultat = itemUtilitaire.toString();

        // Then
        assertTrue("toString() doit contenir 'Utilitaire' quand permetRepos est false",
                resultat.contains("Utilitaire"));
    }

    /**
     * Vérifie que {@code toString()} contient le nom de l'équipement.
     *
     * <p><b>Given</b> : un équipement avec le nom "Tente".<br>
     * <b>When</b>  : {@code toString()} est appelé.<br>
     * <b>Then</b>  : la chaîne contient {@code "Tente"}.</p>
     */
    @Test
    public void testToString_contientLeNom() {
        // Given / When
        String resultat = equipmentNominal.toString();

        // Then
        assertTrue("toString() doit contenir le nom de l'équipement",
                resultat.contains(NOM_NOMINAL));
    }
}
