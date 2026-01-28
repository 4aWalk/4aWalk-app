package fr.iutrodez.a4awalk.GestionCompte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
                Intent intent = new Intent(ProfilActivity.this, UpdateProfilActivity.class);
                startActivity(intent);
            });
        }

        // Récupérer les vues TextView
        TextView userName = findViewById(R.id.user_name);
        TextView userAddress = findViewById(R.id.user_address);
        TextView userAge = findViewById(R.id.user_age);
        TextView userEmail = findViewById(R.id.user_email);
        TextView userLevel = findViewById(R.id.user_level);
        TextView userMorphology = findViewById(R.id.user_morphology);
        ImageView profileImage = findViewById(R.id.profile_image);

        // Mettre les données en dur (pour l'instant)
        userName.setText("Bécogné Néo");
        userAddress.setText("18 avenue durand de gros");
        userAge.setText("21 ans");
        userEmail.setText("neo.becogne@iut-rodez.fr");
        userLevel.setText("Entraîné");
        userMorphology.setText("Moyen");

        // Exemple : changer l'image si tu veux (optionnel)
        profileImage.setImageResource(R.drawable.user_icon);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_header, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
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
