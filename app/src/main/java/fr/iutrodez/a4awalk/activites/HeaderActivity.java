package fr.iutrodez.a4awalk.activites;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import fr.iutrodez.a4awalk.R;

public abstract class HeaderActivity extends AppCompatActivity {

    protected void configurerToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            // GESTION DU CLIC SUR LE LOGO -> Retour à l'accueil (ActiviteListes)
            ImageView logo = findViewById(R.id.logo);
            if (logo != null) {
                logo.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ActiviteListes.class);
                    // Empêche de rouvrir l'activité si on y est déjà
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    // On dit explicitement d'aller sur l'onglet 0 (Randonnées)
                    intent.putExtra("ONGLET_CIBLE", 0);

                    startActivity(intent);
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_header, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_account) {
            startActivity(new Intent(this, ProfilActivity.class)); // Modifie si besoin
            return true;
        }

        if (id == R.id.action_logout) {
            Toast.makeText(this, "Déconnexion...", Toast.LENGTH_SHORT).show();

            // Retour au Login
            Intent intent = new Intent(this, ActivitePrincipale.class);
            // Destruction de l'historique d'activités avec les flags
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finish(); // On ferme l'activité actuelle
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}