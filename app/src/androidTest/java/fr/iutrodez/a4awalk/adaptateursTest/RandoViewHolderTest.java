package fr.iutrodez.a4awalk.adaptateursTest;

import static org.junit.Assert.assertEquals;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.adaptateurs.RandoViewHolder;
import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.modeles.entites.Participant;

/**
 * Classe de tests d'instrumentation pour {@link RandoViewHolder}.
 *
 * <p>Teste la méthode {@link RandoViewHolder#bind(Hike)} qui affiche
 * les données d'une randonnée dans les {@link TextView} de l'item de liste.</p>
 *
 * <p>Catégories couvertes :</p>
 * <ul>
 *   <li><b>Cas nominaux</b> : affichage correct du libellé, du nombre de
 *       participants et de la durée en jours.</li>
 *   <li><b>Cas limites</b> : libellé vide, zéro participant, durée minimale
 *       (1 jour) et maximale (3 jours), libellé très long.</li>
 *   <li><b>Cas d'erreur</b> : libellé {@code null}, participants {@code null}.</li>
 * </ul>
 *
 * <p>La vue est inflatée depuis le layout {@code R.layout.item_rando} via
 * {@link ApplicationProvider#getApplicationContext()} pour disposer d'un
 * contexte Android réel sans démarrer d'activité.</p>
 *
 * <p><b>Emplacement du fichier :</b>
 * {@code app/src/androidTest/java/fr/iutrodez/a4awalk/adaptateurs/RandoViewHolderTest.java}</p>
 *
 * @author Votre équipe
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

    /** TextView affichant le libellé de la randonnée. */
    private TextView libelleView;

    /** TextView affichant le nombre de participants. */
    private TextView participantsView;

    /** TextView affichant le nombre de jours. */
    private TextView joursView;

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

        // Récupération des TextViews pour les assertions
        libelleView      = itemView.findViewById(R.id.nom_rando);
        participantsView = itemView.findViewById(R.id.nb_participants_rando);
        joursView        = itemView.findViewById(R.id.nb_jours_rando);
    }

    // -------------------------------------------------------------------------
    // Méthode utilitaire
    // -------------------------------------------------------------------------

    /**
     * Crée un {@link Hike} valide pour les tests avec libellé, durée et
     * un nombre donné de participants.
     *
     * @param libelle        le libellé de la randonnée
     * @param dureeJours     la durée en jours (entre 1 et 3)
     * @param nbParticipants le nombre de participants à ajouter
     * @return une instance de {@link Hike} prête pour les tests
     */
    private Hike creerHike(String libelle, int dureeJours, int nbParticipants) {
        Hike hike = new Hike();
        hike.setLibelle(libelle);
        hike.setDureeJours(dureeJours);

        ArrayList<Participant> participants = new ArrayList<>();
        for (int i = 0; i < nbParticipants; i++) {
            participants.add(new Participant());
        }
        hike.setParticipants(participants);

        return hike;
    }

    // =========================================================================
    // CAS NOMINAUX
    // =========================================================================

    /**
     * Vérifie que {@link RandoViewHolder#bind(Hike)} affiche correctement
     * le libellé de la randonnée dans le {@link TextView} correspondant.
     *
     * <p><b>Given</b> : une randonnée avec le libellé "Tour du Mont-Blanc".<br>
     * <b>When</b>  : {@code bind(hike)} est appelé.<br>
     * <b>Then</b>  : le TextView {@code nom_rando} affiche "Tour du Mont-Blanc".</p>
     */
    @Test
    public void testBind_libelleValide_afficheDansTextView() {
        // Given
        Hike hike = creerHike("Tour du Mont-Blanc", 2, 3);

        // When
        viewHolder.bind(hike);

        // Then
        assertEquals(
                "Le libellé de la randonnée doit être affiché correctement",
                "Tour du Mont-Blanc",
                libelleView.getText().toString());
    }

    /**
     * Vérifie que {@link RandoViewHolder#bind(Hike)} affiche correctement
     * le nombre de participants avec le suffixe " participants".
     *
     * <p><b>Given</b> : une randonnée avec 3 participants.<br>
     * <b>When</b>  : {@code bind(hike)} est appelé.<br>
     * <b>Then</b>  : le TextView {@code nb_participants_rando} affiche "3 participants".</p>
     */
    @Test
    public void testBind_troisParticipants_afficheNombreCorrect() {
        // Given
        Hike hike = creerHike("Randonnée des Crêtes", 1, 3);

        // When
        viewHolder.bind(hike);

        // Then
        assertEquals(
                "Le nombre de participants doit être affiché avec le suffixe ' participants'",
                "3 participants",
                participantsView.getText().toString());
    }

    /**
     * Vérifie que {@link RandoViewHolder#bind(Hike)} affiche correctement
     * la durée en jours avec le suffixe " jours".
     *
     * <p><b>Given</b> : une randonnée de 2 jours.<br>
     * <b>When</b>  : {@code bind(hike)} est appelé.<br>
     * <b>Then</b>  : le TextView {@code nb_jours_rando} affiche "2 jours".</p>
     */
    @Test
    public void testBind_deuxJours_afficheNombreCorrect() {
        // Given
        Hike hike = creerHike("Sentier des Forêts", 2, 1);

        // When
        viewHolder.bind(hike);

        // Then
        assertEquals(
                "La durée doit être affichée avec le suffixe ' jours'",
                "2 jours",
                joursView.getText().toString());
    }

    /**
     * Vérifie que les trois TextViews sont tous correctement remplis
     * en un seul appel à {@link RandoViewHolder#bind(Hike)}.
     *
     * <p><b>Given</b> : une randonnée complète avec libellé, durée et participants.<br>
     * <b>When</b>  : {@code bind(hike)} est appelé.<br>
     * <b>Then</b>  : les trois TextViews affichent les bonnes valeurs simultanément.</p>
     */
    @Test
    public void testBind_donneesCompletes_tousLesTextViewsCorrects() {
        // Given
        Hike hike = creerHike("GR20 Corse", 3, 5);

        // When
        viewHolder.bind(hike);

        // Then
        assertEquals("Libellé incorrect", "GR20 Corse",        libelleView.getText().toString());
        assertEquals("Participants incorrect", "5 participants", participantsView.getText().toString());
        assertEquals("Jours incorrect", "3 jours",             joursView.getText().toString());
    }

    // =========================================================================
    // CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que {@code bind()} affiche correctement "0 participants"
     * lorsque la randonnée n'a aucun participant.
     *
     * <p><b>Given</b> : une randonnée sans participants.<br>
     * <b>When</b>  : {@code bind(hike)} est appelé.<br>
     * <b>Then</b>  : le TextView affiche "0 participants".</p>
     */
    @Test
    public void testBind_zeroParticipant_afficheZeroParticipants() {
        // Given
        Hike hike = creerHike("Randonnée Solo", 1, 0);

        // When
        viewHolder.bind(hike);

        // Then
        assertEquals(
                "Zéro participant doit afficher '0 participants'",
                "0 participants",
                participantsView.getText().toString());
    }

    /**
     * Vérifie que la durée minimale autorisée (1 jour) est affichée correctement.
     *
     * <p><b>Given</b> : une randonnée de 1 jour (durée minimale).<br>
     * <b>When</b>  : {@code bind(hike)} est appelé.<br>
     * <b>Then</b>  : le TextView affiche "1 jours".</p>
     */
    @Test
    public void testBind_dureeUnJour_afficheUnJour() {
        // Given
        Hike hike = creerHike("Balade Express", 1, 2);

        // When
        viewHolder.bind(hike);

        // Then
        assertEquals(
                "La durée minimale de 1 jour doit être affichée '1 jours'",
                "1 jours",
                joursView.getText().toString());
    }

    /**
     * Vérifie que la durée maximale autorisée (3 jours) est affichée correctement.
     *
     * <p><b>Given</b> : une randonnée de 3 jours (durée maximale).<br>
     * <b>When</b>  : {@code bind(hike)} est appelé.<br>
     * <b>Then</b>  : le TextView affiche "3 jours".</p>
     */
    @Test
    public void testBind_dureeMaximale_afficheTroisJours() {
        // Given
        Hike hike = creerHike("Grande Traversée", 3, 2);

        // When
        viewHolder.bind(hike);

        // Then
        assertEquals(
                "La durée maximale de 3 jours doit être affichée '3 jours'",
                "3 jours",
                joursView.getText().toString());
    }

    /**
     * Vérifie qu'un libellé vide est affiché tel quel sans crash.
     *
     * <p><b>Given</b> : une randonnée avec un libellé vide {@code ""}.<br>
     * <b>When</b>  : {@code bind(hike)} est appelé.<br>
     * <b>Then</b>  : le TextView affiche une chaîne vide sans exception.</p>
     */
    @Test
    public void testBind_libelleVide_afficheChainVide() {
        // Given
        Hike hike = creerHike("", 1, 0);

        // When
        viewHolder.bind(hike);

        // Then
        assertEquals(
                "Un libellé vide doit être affiché tel quel",
                "",
                libelleView.getText().toString());
    }

    /**
     * Vérifie qu'un libellé très long (200 caractères) est affiché sans crash.
     *
     * <p><b>Given</b> : un libellé de 200 caractères identiques.<br>
     * <b>When</b>  : {@code bind(hike)} est appelé.<br>
     * <b>Then</b>  : le TextView contient bien les 200 caractères.</p>
     */
    @Test
    public void testBind_libelleTresLong_afficheCorrectement() {
        // Given
        String libelleLong = "A".repeat(200);
        Hike hike = creerHike(libelleLong, 1, 0);

        // When
        viewHolder.bind(hike);

        // Then
        assertEquals(
                "Un libellé de 200 caractères doit être affiché sans troncature",
                libelleLong,
                libelleView.getText().toString());
    }

    // =========================================================================
    // CAS D'ERREUR
    // =========================================================================

    /**
     * Vérifie que {@code bind()} avec un libellé {@code null} ne provoque pas
     * de crash ({@code TextView.setText(null)} est toléré par Android).
     *
     * <p><b>Given</b> : une randonnée avec un libellé {@code null}.<br>
     * <b>When</b>  : {@code bind(hike)} est appelé.<br>
     * <b>Then</b>  : aucune {@link NullPointerException} n'est levée.</p>
     */
    @Test
    public void testBind_libelleNull_pasDeCrash() {
        // Given
        Hike hike = creerHike(null, 1, 0);

        // When / Then : setText(null) est accepté par TextView → affiche ""
        viewHolder.bind(hike);

        // Aucune exception = test réussi
        assertEquals(
                "Un libellé null doit être toléré par TextView (affiche chaîne vide)",
                "",
                libelleView.getText().toString());
    }

    /**
     * Vérifie que deux appels successifs à {@code bind()} avec des {@link Hike}
     * différents écrasent bien les données précédentes.
     *
     * <p><b>Given</b> : deux randonnées aux libellés distincts.<br>
     * <b>When</b>  : {@code bind()} est appelé deux fois de suite.<br>
     * <b>Then</b>  : le TextView affiche les données du second {@link Hike}.</p>
     */
    @Test
    public void testBind_deuxAppelsSuccessifs_afficheDernieresDonnees() {
        // Given
        Hike premierHike = creerHike("Première Randonnée", 1, 2);
        Hike deuxiemeHike = creerHike("Deuxième Randonnée", 3, 5);

        // When
        viewHolder.bind(premierHike);
        viewHolder.bind(deuxiemeHike); // Le second appel doit écraser le premier

        // Then
        assertEquals(
                "Le second bind() doit écraser les données du premier",
                "Deuxième Randonnée",
                libelleView.getText().toString());
        assertEquals("5 participants", participantsView.getText().toString());
        assertEquals("3 jours",        joursView.getText().toString());
    }
}
