package fr.iutrodez.a4awalk.modeles.entites;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_TOKEN = "auth_token";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public TokenManager(Context context) {
        // Mode Private : seule cette application peut lire ces données
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Sauvegarder le token
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply(); // 'apply' est asynchrone et plus rapide que 'commit'
    }

    // Récupérer le token
    public String getToken() {
        //return sharedPreferences.getString(KEY_TOKEN, null); // null est la valeur par défaut
        return "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0MkA0YXdhbGsuZnIiLCJ1c2VySWQiOjIsImlhdCI6MTc3MDAzNDgyNCwiZXhwIjoxNzcwMTIxMjI0fQ.GI92JgWV0cdRYD6Cnz7ubf9EqqRizNQJ0U1KQnbwSPI";
    }

    // Supprimer le token (pour la déconnexion)
    public void clearToken() {
        editor.remove(KEY_TOKEN);
        editor.apply();
    }
}
