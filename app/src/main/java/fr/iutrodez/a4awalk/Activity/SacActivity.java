package fr.iutrodez.a4awalk.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;

import fr.iutrodez.a4awalk.R;

public class SacActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sac_a_dos);

        ImageButton btnRetour = findViewById(R.id.btnRetour);

        btnRetour.setOnClickListener(v -> {
            finish(); // revient à l'Activity précédente
        });
    }
}
