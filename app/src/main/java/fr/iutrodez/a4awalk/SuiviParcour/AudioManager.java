package fr.iutrodez.a4awalk.SuiviParcour;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Gestionnaire simple de lecture audio pour un fichier ressource.
 * <p>
 * Permet de jouer et de libérer un son associé à un MediaPlayer.
 */
public class AudioManager {

    /** MediaPlayer utilisé pour la lecture du son */
    private MediaPlayer mediaPlayer;

    /**
     * Crée un AudioManager pour un son donné.
     *
     * @param context    Contexte de l'application ou activité
     * @param soundResId Identifiant de la ressource audio (R.raw.xxx)
     */
    public AudioManager(Context context, int soundResId) {
        mediaPlayer = MediaPlayer.create(context, soundResId);
    }

    /**
     * Lance la lecture du son.
     * Ne fait rien si le MediaPlayer est null.
     */
    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    /**
     * Libère les ressources du MediaPlayer.
     * À appeler lorsque le son n'est plus nécessaire pour éviter les fuites mémoire.
     */
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * Vérifie si le son est en cours de lecture.
     *
     * @return true si en lecture, false sinon
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    /**
     * Stoppe la lecture si elle est en cours.
     */
    public void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
