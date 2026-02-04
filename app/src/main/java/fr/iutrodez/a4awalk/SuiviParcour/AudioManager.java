package fr.iutrodez.a4awalk.SuiviParcour;

import android.content.Context;
import android.media.MediaPlayer;

public class AudioManager {
    private MediaPlayer mediaPlayer;

    public AudioManager(Context context, int soundResId) {
        mediaPlayer = MediaPlayer.create(context, soundResId);
    }

    public void play() {
        if (mediaPlayer != null) mediaPlayer.start();
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}


