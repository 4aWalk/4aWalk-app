package fr.iutrodez.a4awalk.adaptateursTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.adaptateurs.RandoViewHolder;
import fr.iutrodez.a4awalk.modeles.entites.Hike;

/**
 * Classe de tests d'instrumentation pour {@link RandoViewHolder}.
 *
 * <p>Teste la méthode {@link RandoViewHolder#bind(Hike)} qui :</p>
 * <ul>
 *   <li>Affiche le libellé de la randonnée dans le {@code TextView} {@code nom_rando}</li>
 *   <li>Affiche ou masque l'icône {@code icone_non_optimise} selon la valeur
 *       de {@link Hike#getOptimize()}</li>
 * </ul>
 *
 * <p>Catégories couvertes :</p>
 * <ul>
 *   <li><b>Cas nominaux</b> : libellé valide, randonnée optimisée, randonnée non optimisée.</li>
 *   <li><b>Cas limites</b> : libellé vide, libellé très long, deux appels successifs à
 *       {@code bind()} sur le même {@link RandoViewHolder}.</li>
 *   <li><b>Cas d'erreur</b> : libellé {@code null}.</li>
 * </ul>
 *
 * <p>La vue est inflatée depuis le layout {@code R.layout.item_rando} via
 * {@link ApplicationProvider#getApplicationContext()} pour disposer d'un
 * contexte Android réel sans démarrer d'activité.</p>
 *
 * <p><b>Emplacement du fichier :</b>
 * {@code app/src/androidTest/java/fr/iutrodez/a4awalk/adaptateursTest/RandoViewHolderTest.java}</p>
 *
 * <p>Dépendances requises dans {@code build.gradle (app)} :</p>
 * <pre>
 * androidTestImplementation 'androidx.test.ext:junit:1.x.x'
 * androidTestImplementation 'androidx.test:core:1.x.x'
 * </pre>
 *
 * @author Équipe A4AWalk
 * @version 1.0
 * @see RandoViewHolder
 */
@RunWith(AndroidJUnit4.class)
public class RandoViewHolderTest {

    // -------------------------------------------------------------------------
    // Champs
    // -------------------------------------------------------------------------

    /** ViewHolder testé, recréé avant chaque test. */
    private RandoViewHolder viewHolder;

    /** Vue racine de l'item, inflatée depuis {@code R.layout.item_rando}. */
    private View itemView;

    /** TextView affichant le libellé de la randonnée ({@code R.id.nom_rando}). */
    private TextView libelleView;

    /**
     * ImageView de l'icône "non optimisé" ({@code R.id.icone_non_optimise}).
     * Visible quand {@code hike.getOptimize() == false}, masquée sinon.
     */
    private ImageView iconeNonOptimise;

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    /**
     * Inflate le layout {@code item_rando} et crée le {@link RandoViewHolder}
     * avant chaque test.
     */
    @Before
    public void setUp() {
        // Inflation du layout réel de l'item depuis le contexte applicatif
        itemView = LayoutInflater
                .from(ApplicationProvider.getApplicationContext())
                .inflate(R.layout.item_rando, null, false);

        viewHolder = new RandoViewHolder(itemView);

        // Récupération des vues pour les assertions
        libelleView      = itemView.findViewById(R.id.nom_rando);
        iconeNonOptimise = itemView.findViewById(R.id.icone_non_optimise);
    }

    // -------------------------------------------------------------------------
    // Méthode utilitaire
    // -------------------------------------------------------------------------

    /**
     * Crée un {@link Hike} minimal pour les tests avec le libellé et la valeur
     * d'optimisation fournis.
     *
     * @param libelle  le libellé de la randonnée.
     * @param optimize {@code true} si la randonnée est optimisée, {@code false} sinon.
     * @return une instance de {@link Hike} prête pour les tests.
     */
    private Hike creerHike(String libelle, boolean optimize) {
        Hike hike = new Hike();
        hike.setLibelle(libelle);
        hike.setOptimize(optimize);
        return hike;
    }

    // =========================================================================
    // Tests — Initialisation du ViewHolder
    // =========================================================================

    /**
     * Vérifie que le {@link RandoViewHolder} est correctement créé et non nul
     * après l'inflation du layout.
     *
     * <p><b>Given</b> : le layout {@code item_rando} est inflatable dans le contexte de test.</p>
     * <p><b>When</b> : le {@link RandoViewHolder} est instancié dans {@code setUp()}.</p>
     * <p><b>Then</b> : l'instance est non nulle.</p>
     */
    @Test
    public void viewHolder_apresInflation_estNonNull() {
        // Given / When — fait dans setUp()

        // Then
        assertNotNull("Le ViewHolder ne doit pas être nul après inflation", viewHolder);
    }

    /**
     * Vérifie que la vue {@code nom_rando} est accessible et non nulle après inflation.
     *
     * <p><b>Given</b> : le layout {@code item_rando} contient un TextView avec l'id
     * {@code nom_rando}.</p>
     * <p><b>When</b> : la vue est récupérée via {@code findViewById}.</p>
     * <p><b>Then</b> : le TextView est non nul.</p>
     */
    @Test
    public void textViewNomRando_apresInflation_estNonNull() {
        // Given / When — fait dans setUp()

        // Then
        assertNotNull("Le TextView nom_rando doit être présent dans le layout", libelleView);
    }

    /**
     * Vérifie que la vue {@code icone_non_optimise} est accessible et non nulle
     * après inflation.
     *
     * <p><b>Given</b> : le layout {@code item_rando} contient un ImageView avec l'id
     * {@code icone_non_optimise}.</p>
     * <p><b>When</b> : la vue est récupérée via {@code findViewById}.</p>
     * <p><b>Then</b> : l'ImageView est non nulle.</p>
     */
    @Test
    public void imageViewIconeNonOptimise_apresInflation_estNonNull() {
        // Given / When — fait dans setUp()

        // Then
        assertNotNull("L'ImageView icone_non_optimise doit être présente dans le layout",
                iconeNonOptimise);
    }

    // =========================================================================
    // Tests — bind() — Libellé — Cas nominaux
    // =========================================================================

    /**
     * Vérifie que {@code bind()} affiche correctement le libellé dans le TextView.
     *
     * <p><b>Given</b> : une randonnée avec le libellé "Tour du Mont-Blanc".</p>
     * <p><b>When</b> : {@code bind(hike)} est appelé.</p>
     * <p><b>Then</b> : le TextView {@code nom_rando} affiche "Tour du Mont-Blanc".</p>
     */
    @Test
    public void bind_libelleValide_afficheLibelleDansTextView() {
        // Given
        Hike hike = creerHike("Tour du Mont-Blanc", true);

        // When
        viewHolder.bind(hike);

        // Then
        assertEquals("Le libellé doit être affiché dans le TextView nom_rando",
                "Tour du Mont-Blanc", libelleView.getText().toString());
    }

    /**
     * Vérifie que deux libellés distincts sont bien affichés en fonction du {@link Hike}
     * passé à {@code bind()}.
     *
     * <p><b>Given</b> : deux randonnées avec des libellés différents.</p>
     * <p><b>When</b> : {@code bind()} est appelé sur chacune séparément.</p>
     * <p><b>Then</b> : le TextView affiche à chaque fois le bon libellé.</p>
     */
    @Test
    public void bind_deuxLibellesDistincts_chacunAfficheCorrectement() {
        // Given
        Hike hike1 = creerHike("GR20 Corse",        true);
        Hike hike2 = creerHike("Chemin de Compostelle", false);

        // When / Then — premier bind
        viewHolder.bind(hike1);
        assertEquals("Le premier libellé doit être affiché",
                "GR20 Corse", libelleView.getText().toString());

        // When / Then — second bind
        viewHolder.bind(hike2);
        assertEquals("Le second libellé doit remplacer le premier",
                "Chemin de Compostelle", libelleView.getText().toString());
    }

    // =========================================================================
    // Tests — bind() — Icône non optimisé — Cas nominaux
    // =========================================================================

    /**
     * Vérifie que l'icône {@code icone_non_optimise} est visible quand la randonnée
     * n'est PAS optimisée ({@code optimize = false}).
     *
     * <p><b>Given</b> : une randonnée avec {@code optimize = false}.</p>
     * <p><b>When</b> : {@code bind(hike)} est appelé.</p>
     * <p><b>Then</b> : {@code icone_non_optimise.getVisibility()} vaut {@link View#VISIBLE}.</p>
     */
    @Test
    public void bind_randonneNonOptimisee_iconeEstVisible() {
        // Given
        Hike hike = creerHike("Randonnée sans optimisation", false);

        // When
        viewHolder.bind(hike);

        // Then
        assertEquals("L'icône doit être VISIBLE quand optimize == false",
                View.VISIBLE, iconeNonOptimise.getVisibility());
    }

    /**
     * Vérifie que l'icône {@code icone_non_optimise} est masquée quand la randonnée
     * EST optimisée ({@code optimize = true}).
     *
     * <p><b>Given</b> : une randonnée avec {@code optimize = true}.</p>
     * <p><b>When</b> : {@code bind(hike)} est appelé.</p>
     * <p><b>Then</b> : {@code icone_non_optimise.getVisibility()} vaut {@link View#GONE}.</p>
     */
    @Test
    public void bind_randonneOptimisee_iconeEstMasquee() {
        // Given
        Hike hike = creerHike("Randonnée optimisée", true);

        // When
        viewHolder.bind(hike);

        // Then
        assertEquals("L'icône doit être GONE quand optimize == true",
                View.GONE, iconeNonOptimise.getVisibility());
    }

    /**
     * Vérifie que l'icône est bien masquée après avoir d'abord été affichée.
     * Ce test valide la transition {@code VISIBLE → GONE} lors d'un second {@code bind()}.
     *
     * <p><b>Given</b> : un premier bind avec {@code optimize = false} (icône visible),
     * suivi d'un second bind avec {@code optimize = true}.</p>
     * <p><b>When</b> : les deux {@code bind()} sont appelés successivement.</p>
     * <p><b>Then</b> : l'icône passe de {@code VISIBLE} à {@code GONE}.</p>
     */
    @Test
    public void bind_transitionNonOptimiseVersOptimise_iconePasse_visible_vers_gone() {
        // Given — premier état : non optimisée
        Hike hikeNonOpt = creerHike("Rando A", false);
        viewHolder.bind(hikeNonOpt);
        assertEquals("Pré-condition : l'icône doit être VISIBLE",
                View.VISIBLE, iconeNonOptimise.getVisibility());

        // When — second état : optimisée
        Hike hikeOpt = creerHike("Rando B", true);
        viewHolder.bind(hikeOpt);

        // Then
        assertEquals("L'icône doit passer à GONE après un bind avec optimize=true",
                View.GONE, iconeNonOptimise.getVisibility());
    }

    /**
     * Vérifie que l'icône est bien affichée après avoir d'abord été masquée.
     * Ce test valide la transition {@code GONE → VISIBLE} lors d'un second {@code bind()}.
     *
     * <p><b>Given</b> : un premier bind avec {@code optimize = true} (icône masquée),
     * suivi d'un second bind avec {@code optimize = false}.</p>
     * <p><b>When</b> : les deux {@code bind()} sont appelés successivement.</p>
     * <p><b>Then</b> : l'icône passe de {@code GONE} à {@code VISIBLE}.</p>
     */
    @Test
    public void bind_transitionOptimiseVersNonOptimise_iconePasse_gone_vers_visible() {
        // Given — premier état : optimisée
        Hike hikeOpt = creerHike("Rando A", true);
        viewHolder.bind(hikeOpt);
        assertEquals("Pré-condition : l'icône doit être GONE",
                View.GONE, iconeNonOptimise.getVisibility());

        // When — second état : non optimisée
        Hike hikeNonOpt = creerHike("Rando B", false);
        viewHolder.bind(hikeNonOpt);

        // Then
        assertEquals("L'icône doit passer à VISIBLE après un bind avec optimize=false",
                View.VISIBLE, iconeNonOptimise.getVisibility());
    }

    // =========================================================================
    // Tests — bind() — Cas limites
    // =========================================================================

    /**
     * Vérifie qu'un libellé vide {@code ""} est affiché tel quel sans crash.
     *
     * <p><b>Given</b> : une randonnée avec un libellé vide.</p>
     * <p><b>When</b> : {@code bind(hike)} est appelé.</p>
     * <p><b>Then</b> : le TextView affiche une chaîne vide.</p>
     */
    @Test
    public void bind_libelleVide_afficheChainVide() {
        // Given
        Hike hike = creerHike("", true);

        // When
        viewHolder.bind(hike);

        // Then
        assertEquals("Un libellé vide doit être affiché tel quel",
                "", libelleView.getText().toString());
    }

    /**
     * Vérifie qu'un libellé très long (200 caractères) est affiché sans crash.
     *
     * <p><b>Given</b> : un libellé de 200 caractères identiques.</p>
     * <p><b>When</b> : {@code bind(hike)} est appelé.</p>
     * <p><b>Then</b> : le TextView contient les 200 caractères.</p>
     */
    @Test
    public void bind_libelleTresLong_afficheCorrectement() {
        // Given
        String libelleLong = "A".repeat(200);
        Hike hike = creerHike(libelleLong, true);

        // When
        viewHolder.bind(hike);

        // Then
        assertEquals("Un libellé de 200 caractères doit être affiché intégralement",
                libelleLong, libelleView.getText().toString());
    }

    /**
     * Vérifie que deux appels successifs à {@code bind()} écrasent correctement
     * toutes les données précédentes (libellé et visibilité de l'icône).
     *
     * <p><b>Given</b> : deux randonnées aux données complètement opposées.</p>
     * <p><b>When</b> : {@code bind()} est appelé deux fois de suite sur le même
     * {@link RandoViewHolder}.</p>
     * <p><b>Then</b> : après le second appel, le libellé et la visibilité de l'icône
     * correspondent exclusivement à la seconde randonnée.</p>
     */
    @Test
    public void bind_deuxAppelsSuccessifs_afficheDernieresDonnees() {
        // Given
        Hike premierHike  = creerHike("Première Randonnée", true);   // icône GONE
        Hike deuxiemeHike = creerHike("Deuxième Randonnée", false);  // icône VISIBLE

        // When
        viewHolder.bind(premierHike);
        viewHolder.bind(deuxiemeHike);

        // Then
        assertEquals("Le libellé doit être celui du second bind()",
                "Deuxième Randonnée", libelleView.getText().toString());
        assertEquals("L'icône doit être VISIBLE après le second bind()",
                View.VISIBLE, iconeNonOptimise.getVisibility());
    }

    // =========================================================================
    // Tests — bind() — Cas d'erreur
    // =========================================================================

    /**
     * Vérifie que {@code bind()} avec un libellé {@code null} ne provoque pas de crash.
     * {@code TextView.setText(null)} est toléré par Android et affiche une chaîne vide.
     *
     * <p><b>Given</b> : une randonnée avec {@code libelle = null}.</p>
     * <p><b>When</b> : {@code bind(hike)} est appelé.</p>
     * <p><b>Then</b> : aucune {@link NullPointerException} n'est levée et le TextView
     * affiche une chaîne vide.</p>
     */
    @Test
    public void bind_libelleNull_pasDeCrashEtAfficheChainVide() {
        // Given
        Hike hike = creerHike(null, true);

        // When — TextView.setText(null) est accepté par Android
        viewHolder.bind(hike);

        // Then
        assertEquals("Un libellé null doit être toléré et afficher une chaîne vide",
                "", libelleView.getText().toString());
    }
}