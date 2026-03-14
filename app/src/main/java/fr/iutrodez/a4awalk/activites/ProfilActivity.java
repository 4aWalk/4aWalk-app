package fr.iutrodez.a4awalk.activites;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import fr.iutrodez.a4awalk.R;

public class ProfilActivity extends HeaderActivity {

    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_compte);

        configurerToolbar();

        // Initialiser le toolbar
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
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
        String nom = "Bécogné";
        String prenom = "Néo";
        String age = "21";
        String adresse = "18 avenue durand de gros";
        String email = "neo.becogne@iut-rodez.fr";
        String niveau = "ENTRAINE";
        String morphologie = "MOYENNE";

        userName.setText(nom + " " + prenom);
        userAddress.setText(adresse);
        userAge.setText(age + " ans");
        userEmail.setText(email);
        userLevel.setText(niveau);
        userMorphology.setText(morphologie);

        profileImage.setImageResource(R.drawable.user_icon);

        // Gérer le clic sur le bouton éditer
        ImageButton editButton = findViewById(R.id.edit_button);
        if (editButton != null) {
            editButton.setOnClickListener(v -> {
                // Créer l'intent et passer les données
                Intent intent = new Intent(ProfilActivity.this, UpdateProfilActivity.class);
                intent.putExtra("nom", nom);
                intent.putExtra("prenom", prenom);
                intent.putExtra("age", age);
                intent.putExtra("adresse", adresse);
                intent.putExtra("email", email);
                intent.putExtra("niveau", niveau);
                intent.putExtra("morphologie", morphologie);
                startActivity(intent);
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_header, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_account) {
            Toast.makeText(this, "Vous êtes déjà sur votre profil", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
