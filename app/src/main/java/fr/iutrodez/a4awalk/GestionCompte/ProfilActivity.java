package fr.iutrodez.a4awalk.GestionCompte;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import fr.iutrodez.a4awalk.R;

public class ProfilActivity extends AppCompatActivity {

    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_compte);

        // Initialiser le toolbar
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        // Gérer le clic sur le bouton éditer
        ImageButton editButton = findViewById(R.id.edit_button);
        if (editButton != null) {
            editButton.setOnClickListener(v -> {
                Toast.makeText(this, "Édition du profil", Toast.LENGTH_SHORT).show();
            });
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
            Toast.makeText(this, "Vous êtes déjà sur votre profil", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.action_logout) {
            Toast.makeText(this, "Déconnexion", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}