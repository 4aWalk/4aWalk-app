package fr.iutrodez.a4awalk.GestionCompte;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import fr.iutrodez.a4awalk.R;

public class HeaderActivity extends AppCompatActivity {

    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Initialiser le toolbar
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            // Supprime le titre par défaut
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
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
            // Gestion du compte
            Toast.makeText(this, "Gestion du compte", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(this, AccountActivity.class));
            return true;
        }

        if (id == R.id.action_logout) {
            // Déconnexion
            Toast.makeText(this, "Déconnexion", Toast.LENGTH_SHORT).show();
            // logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}