package fr.iutrodez.a4awalk.activites;

import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.adaptateurs.AdaptateurDesFragments;
import fr.iutrodez.a4awalk.modeles.entites.User;

/**
 * Activité principale affichant les listes de randonnées et de parcours.
 * Utilise un ViewPager2 avec un TabLayout pour naviguer entre deux onglets.
 * Reçoit l'objet User depuis l'intent de l'activité appelante.
 */
public class ActiviteListes extends HeaderActivity {

    /**
     * Point d'entrée de l'activité.
     * Récupère l'utilisateur connecté, configure le ViewPager2 avec ses onglets
     * et gère une éventuelle redirection vers un onglet spécifique.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_listes);

        // Initialise la toolbar héritée de HeaderActivity
        configurerToolbar();

        // Récupération de l'objet User transmis par l'activité précédente via l'Intent
        User user;
        Intent intentionRecu = getIntent();

        // Gestion de la compatibilité selon la version Android :
        // getParcelableExtra(String, Class) est disponible à partir de API 33 (TIRAMISU)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            user = intentionRecu.getParcelableExtra("USER_DATA", User.class);
        } else {
            // Méthode dépréciée mais nécessaire pour les versions antérieures à API 33
            user = intentionRecu.getParcelableExtra("USER_DATA");
        }

        // Liaison des vues avec leurs identifiants XML
        ViewPager2 pager = findViewById(R.id.activity_main_viewpager);
        TabLayout gestionnaireOnglet = findViewById(R.id.tab_layout);

        // Associe l'adaptateur de fragments au ViewPager2.
        // L'adaptateur reçoit 'user' pour le transmettre à chaque fragment.
        pager.setAdapter(new AdaptateurDesFragments(this, user));

        // Titres des onglets définis dans les ressources strings.xml
        String[] titreOnglet = {
                getString(R.string.onglet_randonnees),
                getString(R.string.onglet_parcours)
        };

        // Synchronise le TabLayout avec le ViewPager2 :
        // à chaque changement de page, le bon onglet est sélectionné et inversement.
        new TabLayoutMediator(gestionnaireOnglet, pager,
                (tab, position) -> tab.setText(titreOnglet[position])).attach();

        // Gère un éventuel onglet cible transmis dans l'intent (ex. : clic sur le logo)
        gererRedirectionOnglet();
    }

    /**
     * Appelé quand l'activité reçoit un nouvel Intent sans être recrée (singleTop/singleTask).
     * Met à jour l'intent courant et redirige vers le bon onglet si demandé.
     *
     * @param intent Le nouvel intent reçu.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Remplace l'intent courant pour que getIntent() retourne le plus récent
        setIntent(intent);
        gererRedirectionOnglet();
    }

    /**
     * Lit l'extra "ONGLET_CIBLE" dans l'intent courant et navigue vers cet onglet
     * si une valeur valide est présente (différente de -1, valeur par défaut).
     */
    private void gererRedirectionOnglet() {
        // Récupère l'index de l'onglet cible (-1 si absent de l'intent)
        int ongletCible = getIntent().getIntExtra("ONGLET_CIBLE", -1);

        if (ongletCible != -1) {
            ViewPager2 viewPager = findViewById(R.id.activity_main_viewpager);
            if (viewPager != null) {
                // Navigue vers l'onglet cible avec une animation de défilement (true)
                viewPager.setCurrentItem(ongletCible, true);
            }
        }
    }
}