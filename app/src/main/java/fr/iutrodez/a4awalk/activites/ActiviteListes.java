package fr.iutrodez.a4awalk.activites;

import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.adaptateurs.AdaptateurDesFragments;
import fr.iutrodez.a4awalk.modeles.entites.User;

public class ActiviteListes extends HeaderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_listes);

        configurerToolbar();

        // --- Ton code de récupération adapté à l'activité ---
        User user;
        Intent intentionRecu = getIntent();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            user = intentionRecu.getParcelableExtra("USER_DATA", User.class);
        } else {
            user = intentionRecu.getParcelableExtra("USER_DATA");
        }

        ViewPager2 pager = findViewById(R.id.activity_main_viewpager);
        TabLayout gestionnaireOnglet = findViewById(R.id.tab_layout);

        // On passe l'objet 'user' à l'adaptateur
        pager.setAdapter(new AdaptateurDesFragments(this, user));

        String[] titreOnglet = {getString(R.string.onglet_randonnees),
                getString(R.string.onglet_parcours)};

        new TabLayoutMediator(gestionnaireOnglet, pager,
                (tab, position) -> tab.setText(titreOnglet[position])).attach();

        // Si on arrive ici depuis un clic sur le logo, on gère l'onglet
        gererRedirectionOnglet();
    }

    // --- MÉTHODES POUR GÉRER LE CLIC SUR LE LOGO ---

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Met à jour l'Intent
        gererRedirectionOnglet();
    }

    private void gererRedirectionOnglet() {
        int ongletCible = getIntent().getIntExtra("ONGLET_CIBLE", -1);

        if (ongletCible != -1) {
            ViewPager2 viewPager = findViewById(R.id.activity_main_viewpager);
            if (viewPager != null) {
                viewPager.setCurrentItem(ongletCible, true);
            }
        }
    }
}