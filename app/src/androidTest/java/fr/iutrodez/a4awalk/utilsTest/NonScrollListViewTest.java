package fr.iutrodez.a4awalk.utilsTest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.iutrodez.a4awalk.utils.NonScrollListView;

/**
 * Classe de test instrumentée pour {@link NonScrollListView}.
 *
 * <p>Cette classe vérifie le comportement de la ListView personnalisée
 * qui s'étend sur toute sa hauteur pour afficher tous ses éléments,
 * notamment :</p>
 * <ul>
 *     <li>La création via les différents constructeurs</li>
 *     <li>Le calcul correct de la hauteur dans {@code onMeasure}</li>
 *     <li>La mise à jour du {@code LayoutParams.height}</li>
 * </ul>
 *
 * <p>Les tests couvrent trois catégories :</p>
 * <ul>
 *     <li><b>Nominaux</b> : création et mesure dans des conditions normales</li>
 *     <li><b>Limites</b> : largeur nulle, hauteur nulle, taille maximale</li>
 *     <li><b>Erreurs</b> : specs invalides</li>
 * </ul>
 *
 * @author Votre nom
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class NonScrollListViewTest {

    // =========================================================================
    // ATTRIBUTS
    // =========================================================================

    /** Contexte Android fourni par le runner de test */
    private Context contexte;

    // =========================================================================
    // INITIALISATION
    // =========================================================================

    /**
     * Initialise le contexte Android avant chaque test.
     */
    @Before
    public void setUp() {
        contexte = ApplicationProvider.getApplicationContext();
    }

    // =========================================================================
    // TESTS — Constructeurs — CAS NOMINAUX
    // =========================================================================

    /**
     * Vérifie que le constructeur avec Context seul crée bien l'objet.
     *
     * <p><b>Cas nominal</b></p>
     */
    @Test
    public void testConstructeur_AvecContextSeul_ObjetNonNul() {
        // Given — un contexte valide

        // When — on crée une NonScrollListView avec le constructeur simple
        NonScrollListView vue = new NonScrollListView(contexte);

        // Then — l'objet est correctement instancié
        assertNotNull("La vue ne doit pas être nulle avec un Context valide", vue);
    }

    /**
     * Vérifie que le constructeur avec Context et AttributeSet null
     * crée bien l'objet sans exception.
     *
     * <p><b>Cas nominal</b></p>
     */
    @Test
    public void testConstructeur_AvecContextEtAttributeSetNull_ObjetNonNul() {
        // Given — un contexte valide et un AttributeSet null

        // When — on crée une NonScrollListView avec AttributeSet null
        NonScrollListView vue = new NonScrollListView(contexte, null);

        // Then — l'objet est correctement instancié
        assertNotNull("La vue ne doit pas être nulle avec AttributeSet null", vue);
    }

    /**
     * Vérifie que le constructeur complet avec style par défaut
     * crée bien l'objet sans exception.
     *
     * <p><b>Cas nominal</b></p>
     */
    @Test
    public void testConstructeur_AvecContextAttributeSetEtStyle_ObjetNonNul() {
        // Given — un contexte valide, AttributeSet null et style 0

        // When — on crée une NonScrollListView avec tous les paramètres
        NonScrollListView vue = new NonScrollListView(contexte, null, 0);

        // Then — l'objet est correctement instancié
        assertNotNull("La vue ne doit pas être nulle avec style 0", vue);
    }

    // =========================================================================
    // TESTS — onMeasure — CAS NOMINAUX
    // =========================================================================

    /**
     * Vérifie que {@code onMeasure} met bien à jour la hauteur du LayoutParams
     * après un appel avec des specs valides.
     *
     * <p><b>Cas nominal</b></p>
     */
    @Test
    public void testOnMeasure_SpecsValides_LayoutParamsHauteurMiseAJour() {
        // Given — une NonScrollListView avec un LayoutParams initialisé
        NonScrollListView vue = new NonScrollListView(contexte);
        vue.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        int widthSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.AT_MOST);

        // When — on appelle onMeasure avec des specs valides
        vue.measure(widthSpec, heightSpec);

        // Then — le LayoutParams.height est mis à jour avec la hauteur mesurée
        assertTrue("La hauteur du LayoutParams doit être >= 0",
                vue.getLayoutParams().height >= 0);
    }

    /**
     * Vérifie que la hauteur mesurée est cohérente avec la hauteur du LayoutParams.
     *
     * <p><b>Cas nominal</b></p>
     */
    @Test
    public void testOnMeasure_HauteurMesuree_EgaleALayoutParamsHeight() {
        // Given — une NonScrollListView avec LayoutParams initialisé
        NonScrollListView vue = new NonScrollListView(contexte);
        vue.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        int widthSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.AT_MOST);

        // When — on appelle onMeasure
        vue.measure(widthSpec, heightSpec);

        // Then — la hauteur mesurée correspond bien au LayoutParams.height
        assertTrue("La hauteur mesurée doit correspondre au LayoutParams.height",
                vue.getMeasuredHeight() == vue.getLayoutParams().height);
    }

    /**
     * Vérifie que la vue peut être mesurée plusieurs fois de suite
     * sans lever d'exception.
     *
     * <p><b>Cas nominal</b></p>
     */
    @Test
    public void testOnMeasure_AppelSuccessif_PasException() {
        // Given — une NonScrollListView avec LayoutParams initialisé
        NonScrollListView vue = new NonScrollListView(contexte);
        vue.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        int widthSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.AT_MOST);

        // When / Then — deux appels successifs ne doivent pas lever d'exception
        try {
            vue.measure(widthSpec, heightSpec);
            vue.measure(widthSpec, heightSpec);
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée lors d'appels successifs : "
                    + e.getMessage());
        }
    }

    // =========================================================================
    // TESTS — onMeasure — CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que {@code onMeasure} fonctionne avec une largeur nulle.
     *
     * <p><b>Cas limite</b> : largeur = 0</p>
     */
    @Test
    public void testOnMeasure_LargeurNulle_PasException() {
        // Given — une largeur de 0
        NonScrollListView vue = new NonScrollListView(contexte);
        vue.setLayoutParams(new ViewGroup.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.AT_MOST);

        // When / Then — aucune exception ne doit être levée avec une largeur nulle
        try {
            vue.measure(widthSpec, heightSpec);
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec une largeur nulle : "
                    + e.getMessage());
        }

        // Then — la hauteur reste cohérente
        assertTrue("La hauteur doit être >= 0 avec une largeur nulle",
                vue.getLayoutParams().height >= 0);
    }

    /**
     * Vérifie que {@code onMeasure} fonctionne avec une hauteur contrainte à 0.
     *
     * <p><b>Cas limite</b> : hauteur contrainte = 0</p>
     */
    @Test
    public void testOnMeasure_HauteurContrainteNulle_PasException() {
        // Given — une hauteur contrainte de 0
        NonScrollListView vue = new NonScrollListView(contexte);
        vue.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0));

        int widthSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST);

        // When / Then — aucune exception ne doit être levée avec hauteur = 0
        try {
            vue.measure(widthSpec, heightSpec);
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec une hauteur contrainte à 0 : "
                    + e.getMessage());
        }

        // Then — la hauteur est bien mise à jour
        assertTrue("La hauteur doit être >= 0",
                vue.getLayoutParams().height >= 0);
    }

    /**
     * Vérifie que la hauteur mesurée ne dépasse pas la limite imposée
     * par {@code Integer.MAX_VALUE >> 2}.
     *
     * <p><b>Cas limite</b> : vérification de la borne supérieure</p>
     */
    @Test
    public void testOnMeasure_HauteurMesuree_NedepasePasLaLimiteMaximale() {
        // Given — une NonScrollListView avec LayoutParams
        NonScrollListView vue = new NonScrollListView(contexte);
        vue.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        int widthSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.AT_MOST);

        // When — on mesure la vue
        vue.measure(widthSpec, heightSpec);

        // Then — la hauteur ne dépasse pas Integer.MAX_VALUE >> 2
        assertTrue("La hauteur mesurée ne doit pas dépasser Integer.MAX_VALUE >> 2",
                vue.getMeasuredHeight() <= (Integer.MAX_VALUE >> 2));
    }

    /**
     * Vérifie que {@code onMeasure} fonctionne avec une très grande largeur.
     *
     * <p><b>Cas limite</b> : largeur maximale</p>
     */
    @Test
    public void testOnMeasure_LargeurMaximale_PasException() {
        // Given — une largeur très grande
        NonScrollListView vue = new NonScrollListView(contexte);
        vue.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        int widthSpec = View.MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, View.MeasureSpec.AT_MOST);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.AT_MOST);

        // When / Then — aucune exception ne doit être levée avec une grande largeur
        try {
            vue.measure(widthSpec, heightSpec);
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec une largeur maximale : "
                    + e.getMessage());
        }

        // Then — la hauteur est correctement mise à jour
        assertTrue("La hauteur doit être >= 0 avec une grande largeur",
                vue.getLayoutParams().height >= 0);
    }

    // =========================================================================
    // TESTS — onMeasure — CAS ERREURS
    // =========================================================================

    /**
     * Vérifie que {@code onMeasure} ne lève pas d'exception
     * avec un mode de mesure UNSPECIFIED.
     *
     * <p><b>Cas erreur</b> : mode UNSPECIFIED</p>
     */
    @Test
    public void testOnMeasure_ModeUnspecified_PasException() {
        // Given — un mode de mesure UNSPECIFIED
        NonScrollListView vue = new NonScrollListView(contexte);
        vue.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        // When / Then — aucune exception ne doit être levée en mode UNSPECIFIED
        try {
            vue.measure(widthSpec, heightSpec);
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec le mode UNSPECIFIED : "
                    + e.getMessage());
        }

        // Then — la hauteur est correctement mise à jour
        assertTrue("La hauteur doit être >= 0 en mode UNSPECIFIED",
                vue.getLayoutParams().height >= 0);
    }
}
