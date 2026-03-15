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

        // --- GESTION DU PROFIL ---
        if (id == R.id.action_account) {
            // Vérifie si on est déjà sur la page ProfilActivity (ou sa modification)
            if (this instanceof ProfilActivity || this instanceof UpdateProfilActivity) {
                Toast.makeText(this, "Vous êtes déjà sur votre profil", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, ProfilActivity.class));
            }
            return true;
        }

        // --- GESTION DE LA DÉCONNEXION ---
        if (id == R.id.action_logout) {
            Toast.makeText(this, "Déconnexion...", Toast.LENGTH_SHORT).show();

            // Retour au Login
            Intent intent = new Intent(this, ActivitePrincipale.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finish();
            return true;
        }

        // --- GESTION DES NOURRITURES ---
        if (id == R.id.action_items) {
            // Vérifie si on est déjà sur la page de gestion des nourritures
            if (this instanceof ActiviteGestionFoodProducts) {
                Toast.makeText(this, "Vous êtes déjà sur la gestion des nourritures", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, ActiviteGestionFoodProducts.class));
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}