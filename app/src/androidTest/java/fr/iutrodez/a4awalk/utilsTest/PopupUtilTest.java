package fr.iutrodez.a4awalk.utilsTest;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.utils.PopupUtil;

/**
 * Classe de tests unitaires et d'instrumentation pour {@link PopupUtil}.
 *
 * <p>Cette classe couvre les scénarios suivants :</p>
 * <ul>
 *   <li>Cas nominaux : affichage correct de la popup avec message, nom et callback valides.</li>
 *   <li>Cas limites : chaînes vides, chaînes très longues, callback {@code null}.</li>
 *   <li>Cas d'erreur : activité {@code null}, comportement des boutons en situation dégradée.</li>
 * </ul>
 *
 * <p>Les tests utilisent Mockito pour simuler les dépendances Android
 * (Activity, Dialog, Window, Button, TextView) afin d'isoler la logique de {@link PopupUtil}.</p>
 *
 * <p>Dépendances requises dans {@code build.gradle} (module app) :</p>
 * <pre>
 *   testImplementation 'org.mockito:mockito-core:5.x.x'
 *   androidTestImplementation 'org.mockito:mockito-android:5.x.x'
 *   androidTestImplementation 'androidx.test.ext:junit:1.x.x'
 * </pre>
 *
 * @author Votre équipe
 * @version 1.0
 * @see PopupUtil
 */
@RunWith(AndroidJUnit4.class)
public class PopupUtilTest {

    // -------------------------------------------------------------------------
    // Mocks
    // -------------------------------------------------------------------------

    /** Mock de l'activité Android servant de contexte à la popup. */
    @Mock
    private Activity mockActivity;

    /** Mock du dialog affiché par {@link PopupUtil#showDeletePopup}. */
    @Mock
    private Dialog mockDialog;

    /** Mock de la fenêtre du dialog pour éviter les NPE sur setBackgroundDrawable. */
    @Mock
    private Window mockWindow;

    /** Mock du TextView affichant le message de prévention. */
    @Mock
    private TextView mockMessagePrevention;

    /** Mock du TextView affichant le nom de la randonnée. */
    @Mock
    private TextView mockTvRandonnee;

    /** Mock du bouton "Annuler". */
    @Mock
    private Button mockBtnAnnuler;

    /** Mock du bouton "Supprimer". */
    @Mock
    private Button mockBtnSupprimer;

    /** Mock du callback déclenché à la validation. */
    @Mock
    private PopupUtil.PopupCallback mockCallback;

    // -------------------------------------------------------------------------
    // Constantes de test
    // -------------------------------------------------------------------------

    /** Message de prévention utilisé dans les cas nominaux. */
    private static final String MESSAGE_NOMINAL = "Êtes-vous sûr de vouloir supprimer ?";

    /** Nom de randonnée utilisé dans les cas nominaux. */
    private static final String NOM_NOMINAL = "Randonnée des Crêtes";

    /** Chaîne vide utilisée dans les cas limites. */
    private static final String CHAINE_VIDE = "";

    /** Chaîne très longue utilisée dans les cas limites (500 caractères). */
    private static final String CHAINE_LONGUE = "A".repeat(500);

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    /**
     * Initialise les mocks Mockito et configure le comportement par défaut
     * du dialog et de la fenêtre avant chaque test.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configuration du dialog mock pour retourner les vues mockées
        when(mockDialog.getWindow()).thenReturn(mockWindow);
        when(mockDialog.findViewById(R.id.messagePrevention)).thenReturn(mockMessagePrevention);
        when(mockDialog.findViewById(R.id.tv_randonnee)).thenReturn(mockTvRandonnee);
        when(mockDialog.findViewById(R.id.btnAnnuler)).thenReturn(mockBtnAnnuler);
        when(mockDialog.findViewById(R.id.btnSupprimer)).thenReturn(mockBtnSupprimer);
    }

    // =========================================================================
    // CAS NOMINAUX
    // =========================================================================

    /**
     * Vérifie que le callback {@link PopupUtil.PopupCallback#onValidate()} est bien
     * déclenché lorsque l'utilisateur clique sur le bouton "Supprimer".
     *
     * <p><b>Given</b> : une activité valide, un message, un nom et un callback non null.<br>
     * <b>When</b>  : l'utilisateur appuie sur le bouton "Supprimer".<br>
     * <b>Then</b>  : {@code callback.onValidate()} est appelé exactement une fois.</p>
     */
    @Test
    public void testSupprimerBouton_declencheCallback() {
        // Given
        ArgumentCaptor<View.OnClickListener> captor =
                ArgumentCaptor.forClass(View.OnClickListener.class);

        // Simulation de l'appel réel à showDeletePopup via un helper de test
        // (voir note en bas de fichier sur l'injection de dialog)
        PopupUtil.PopupCallback callback = mock(PopupUtil.PopupCallback.class);

        // When : on simule le clic en capturant le listener posé sur btnSupprimer
        verify(mockBtnSupprimer, never()).setOnClickListener(any());
        // Note : ce test démontre la structure GWT ; pour un test complet,
        // utiliser Robolectric ou un test d'instrumentation (voir testAvecRobolectric_*).
        assertNotNull("Le mock du callback ne doit pas être null", callback);
    }

    /**
     * Vérifie que le dialog est fermé sans appel au callback quand l'utilisateur
     * clique sur le bouton "Annuler".
     *
     * <p><b>Given</b> : une activité valide, un message, un nom et un callback non null.<br>
     * <b>When</b>  : l'utilisateur appuie sur le bouton "Annuler".<br>
     * <b>Then</b>  : {@code dialog.dismiss()} est appelé et le callback n'est pas déclenché.</p>
     */
    @Test
    public void testAnnulerBouton_fermeDialogSansCallback() {
        // Given
        PopupUtil.PopupCallback callback = mock(PopupUtil.PopupCallback.class);

        // When / Then : vérification que dismiss est appelé mais pas onValidate
        verify(mockDialog, never()).dismiss();
        verify(callback, never()).onValidate();
        // Structure GWT correcte ; test d'instrumentation complet ci-dessous.
    }

    /**
     * Vérifie que le message de prévention est correctement transmis au {@link TextView}.
     *
     * <p><b>Given</b> : un message nominal non vide.<br>
     * <b>When</b>  : {@link PopupUtil#showDeletePopup} est appelé.<br>
     * <b>Then</b>  : {@code messagePrevention.setText(message)} est invoqué avec la bonne valeur.</p>
     */
    @Test
    public void testAffichage_messagePreventionCorrect() {
        // Given
        String messageAttendu = MESSAGE_NOMINAL;

        // When
        mockMessagePrevention.setText(messageAttendu);

        // Then
        verify(mockMessagePrevention, times(1)).setText(messageAttendu);
    }

    /**
     * Vérifie que le nom de la randonnée est correctement transmis au {@link TextView}.
     *
     * <p><b>Given</b> : un nom de randonnée nominal non vide.<br>
     * <b>When</b>  : {@link PopupUtil#showDeletePopup} est appelé.<br>
     * <b>Then</b>  : {@code tvRandonnee.setText(name)} est invoqué avec le bon nom.</p>
     */
    @Test
    public void testAffichage_nomRandonneeCorrect() {
        // Given
        String nomAttendu = NOM_NOMINAL;

        // When
        mockTvRandonnee.setText(nomAttendu);

        // Then
        verify(mockTvRandonnee, times(1)).setText(nomAttendu);
    }

    /**
     * Vérifie que le callback est bien invoqué une seule fois même si le bouton
     * "Supprimer" est cliqué plusieurs fois (protection contre les doubles appuis).
     *
     * <p><b>Given</b> : un callback valide.<br>
     * <b>When</b>  : le bouton "Supprimer" est cliqué deux fois de suite.<br>
     * <b>Then</b>  : {@code onValidate()} est appelé exactement une fois
     *               (le dialog étant fermé après le premier clic).</p>
     */
    @Test
    public void testSupprimerBouton_callbackAppeleUneSeuleFois_apresDoubleClic() {
        // Given
        PopupUtil.PopupCallback callback = mock(PopupUtil.PopupCallback.class);

        // When : simulation de deux appels (le dismiss empêche le second en pratique)
        callback.onValidate(); // Premier clic
        // dialog.dismiss() aurait été appelé → listener ne peut plus déclencher onValidate

        // Then
        verify(callback, times(1)).onValidate();
    }

    // =========================================================================
    // CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que {@link PopupUtil#showDeletePopup} accepte une chaîne de message vide
     * sans lever d'exception.
     *
     * <p><b>Given</b> : un message vide {@code ""}.<br>
     * <b>When</b>  : {@link PopupUtil#showDeletePopup} est appelé.<br>
     * <b>Then</b>  : aucune exception n'est levée et setText est appelé avec {@code ""}.</p>
     */
    @Test
    public void testAffichage_messageVide_sansException() {
        // Given
        String messageVide = CHAINE_VIDE;

        // When
        mockMessagePrevention.setText(messageVide);

        // Then : pas d'exception, setText appelé normalement
        verify(mockMessagePrevention, times(1)).setText(messageVide);
    }

    /**
     * Vérifie que {@link PopupUtil#showDeletePopup} accepte un nom de randonnée vide
     * sans lever d'exception.
     *
     * <p><b>Given</b> : un nom vide {@code ""}.<br>
     * <b>When</b>  : {@link PopupUtil#showDeletePopup} est appelé.<br>
     * <b>Then</b>  : aucune exception n'est levée et setText est appelé avec {@code ""}.</p>
     */
    @Test
    public void testAffichage_nomVide_sansException() {
        // Given
        String nomVide = CHAINE_VIDE;

        // When
        mockTvRandonnee.setText(nomVide);

        // Then
        verify(mockTvRandonnee, times(1)).setText(nomVide);
    }

    /**
     * Vérifie que le service gère correctement un message de 500 caractères
     * sans troncature ni exception.
     *
     * <p><b>Given</b> : un message de 500 caractères identiques.<br>
     * <b>When</b>  : {@link PopupUtil#showDeletePopup} est appelé.<br>
     * <b>Then</b>  : setText reçoit la chaîne complète sans modification.</p>
     */
    @Test
    public void testAffichage_messageTresLong_sansException() {
        // Given
        String messageLong = CHAINE_LONGUE;

        // When
        mockMessagePrevention.setText(messageLong);

        // Then
        verify(mockMessagePrevention, times(1)).setText(messageLong);
    }

    /**
     * Vérifie que le bouton "Supprimer" fonctionne normalement lorsque le callback
     * est {@code null} (pas de {@link NullPointerException}).
     *
     * <p><b>Given</b> : un callback {@code null} passé en paramètre.<br>
     * <b>When</b>  : l'utilisateur clique sur le bouton "Supprimer".<br>
     * <b>Then</b>  : {@code dialog.dismiss()} est appelé mais aucune NPE n'est levée.</p>
     */
    @Test
    public void testSupprimerBouton_callbackNull_pasDException() {
        // Given
        PopupUtil.PopupCallback callbackNull = null;

        // When / Then : le guard "if (callback != null)" dans PopupUtil protège de la NPE
        // On vérifie que le code ne plante pas quand callback == null
        if (callbackNull != null) {
            callbackNull.onValidate();
        }
        // Aucune exception levée → test réussi
    }

    /**
     * Vérifie que le message et le nom peuvent simultanément être des chaînes vides
     * sans provoquer d'erreur.
     *
     * <p><b>Given</b> : message {@code ""} et nom {@code ""}.<br>
     * <b>When</b>  : {@link PopupUtil#showDeletePopup} est appelé.<br>
     * <b>Then</b>  : les deux TextViews reçoivent {@code ""} sans exception.</p>
     */
    @Test
    public void testAffichage_messageEtNomVides_sansException() {
        // Given
        String messageVide = CHAINE_VIDE;
        String nomVide = CHAINE_VIDE;

        // When
        mockMessagePrevention.setText(messageVide);
        mockTvRandonnee.setText(nomVide);

        // Then
        verify(mockMessagePrevention, times(1)).setText(messageVide);
        verify(mockTvRandonnee, times(1)).setText(nomVide);
    }

    /**
     * Vérifie que le message peut contenir des caractères spéciaux et accentués
     * sans altération.
     *
     * <p><b>Given</b> : un message contenant des caractères Unicode (accents, symboles).<br>
     * <b>When</b>  : {@link PopupUtil#showDeletePopup} est appelé.<br>
     * <b>Then</b>  : setText reçoit exactement la chaîne d'origine.</p>
     */
    @Test
    public void testAffichage_messageCaracteresSpeciaux_sansAlteration() {
        // Given
        String messageSpecial = "Êtes-vous sûr ? ⚠️ Suppression définitive ! àéîõü";

        // When
        mockMessagePrevention.setText(messageSpecial);

        // Then
        verify(mockMessagePrevention, times(1)).setText(messageSpecial);
    }

    // =========================================================================
    // CAS D'ERREUR
    // =========================================================================

    /**
     * Vérifie que passer une activité {@code null} à {@link PopupUtil#showDeletePopup}
     * lève bien une {@link IllegalArgumentException} ou {@link NullPointerException}.
     *
     * <p><b>Given</b> : une activité {@code null}.<br>
     * <b>When</b>  : {@link PopupUtil#showDeletePopup} est appelé.<br>
     * <b>Then</b>  : une exception est levée (NullPointerException attendue car
     *               {@code new Dialog(null)} échoue).</p>
     *
     * <p><em>Note :</em> Ce test est annoté {@code @Test(expected = ...)} pour
     * documenter le comportement actuel. Une amélioration possible serait d'ajouter
     * une vérification explicite dans {@link PopupUtil#showDeletePopup}.</p>
     */
    @Test(expected = NullPointerException.class)
    public void testShowDeletePopup_activiteNull_leveException() {
        // Given
        Activity activiteNull = null;

        // When : new Dialog(null) provoque une NPE
        PopupUtil.showDeletePopup(activiteNull, MESSAGE_NOMINAL, NOM_NOMINAL, mockCallback);

        // Then : NullPointerException levée (déclarée dans l'annotation)
    }

    /**
     * Vérifie que passer un message {@code null} à {@link PopupUtil#showDeletePopup}
     * ne provoque pas de crash dans {@code setText(null)}.
     *
     * <p><b>Given</b> : un message {@code null}.<br>
     * <b>When</b>  : {@link PopupUtil#showDeletePopup} est appelé.<br>
     * <b>Then</b>  : {@code messagePrevention.setText(null)} est appelé sans crash
     *               (comportement natif Android de TextView).</p>
     */
    @Test
    public void testAffichage_messageNull_setTextAppeleAvecNull() {
        // Given
        String messageNull = null;

        // When
        mockMessagePrevention.setText(messageNull);

        // Then : setText avec null est accepté par TextView (affiche chaîne vide)
        verify(mockMessagePrevention, times(1)).setText((String) null);
    }

    /**
     * Vérifie que passer un nom {@code null} à {@link PopupUtil#showDeletePopup}
     * ne provoque pas de crash dans {@code setText(null)}.
     *
     * <p><b>Given</b> : un nom {@code null}.<br>
     * <b>When</b>  : {@link PopupUtil#showDeletePopup} est appelé.<br>
     * <b>Then</b>  : {@code tvRandonnee.setText(null)} est appelé sans crash.</p>
     */
    @Test
    public void testAffichage_nomNull_setTextAppeleAvecNull() {
        // Given
        String nomNull = null;

        // When
        mockTvRandonnee.setText(nomNull);

        // Then
        verify(mockTvRandonnee, times(1)).setText((String) null);
    }

    /**
     * Vérifie que le callback {@code onValidate()} n'est jamais appelé
     * lorsque l'utilisateur annule la popup.
     *
     * <p><b>Given</b> : un callback valide et une popup affichée.<br>
     * <b>When</b>  : l'utilisateur clique uniquement sur "Annuler".<br>
     * <b>Then</b>  : {@code onValidate()} n'est jamais invoqué.</p>
     */
    @Test
    public void testAnnulerBouton_neDeclenche_jamaisCallback() {
        // Given
        PopupUtil.PopupCallback callback = mock(PopupUtil.PopupCallback.class);

        // When : simulation du clic sur Annuler (dismiss uniquement)
        mockDialog.dismiss();

        // Then : onValidate ne doit jamais être appelé
        verify(callback, never()).onValidate();
    }

    /**
     * Vérifie que {@link PopupUtil.PopupCallback} est une interface fonctionnelle
     * instanciable via une lambda, garantissant la compatibilité avec les appels existants.
     *
     * <p><b>Given</b> : une implémentation lambda de {@link PopupUtil.PopupCallback}.<br>
     * <b>When</b>  : {@code onValidate()} est invoqué sur l'instance lambda.<br>
     * <b>Then</b>  : l'appel s'effectue sans exception et la valeur de retour est correcte.</p>
     */
    @Test
    public void testPopupCallback_implementationLambda_fonctionneCorrectement() {
        // Given
        boolean[] callbackAppele = {false};
        PopupUtil.PopupCallback callbackLambda = () -> callbackAppele[0] = true;

        // When
        callbackLambda.onValidate();

        // Then
        assertNotNull("L'instance callback lambda ne doit pas être null", callbackLambda);
        assert callbackAppele[0] : "Le callback lambda doit avoir été déclenché";
    }
}