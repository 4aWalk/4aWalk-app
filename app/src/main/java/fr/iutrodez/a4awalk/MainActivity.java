package fr.iutrodez.a4awalk;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test); // ton layout principal

        Button btnOpen = findViewById(R.id.btnOpenPopup);

        btnOpen.setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.popup_participant);
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );

            ImageButton btnClose = dialog.findViewById(R.id.btnClose);
            Button btnVoirSac = dialog.findViewById(R.id.btnVoirSac);
            Button btnValider = dialog.findViewById(R.id.btnValider);

            btnClose.setOnClickListener(view -> dialog.dismiss());

            btnVoirSac.setOnClickListener(view -> {
                Toast.makeText(this, "Ouverture du sac...", Toast.LENGTH_SHORT).show();
            });

            btnValider.setOnClickListener(view -> {
                Toast.makeText(this, "Participant ajoutée", Toast.LENGTH_SHORT).show();
            });

            dialog.show();
        });
    }
}
