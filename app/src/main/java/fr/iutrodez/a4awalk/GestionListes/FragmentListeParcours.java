package fr.iutrodez.a4awalk.GestionListes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;


public class FragmentListeParcours extends Fragment implements View.OnClickListener{

    public static FragmentListeParcours newInstance() {
        FragmentListeParcours fragment = new FragmentListeParcours();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // On récupère la vue (le layout) associée au fragment un
        View vueDuFragment = inflater.inflate(R.layout.fragment_un, container, false);
        /*
         * on associe un écouteur à chacun des 2 boutons de la vue : le fragment
         * courant sera son propre écouteur de clic sur les boutons
         */
        vueDuFragment.findViewById(R.id.btn_alea).setOnClickListener(this);
        vueDuFragment.findViewById(R.id.btn_vider).setOnClickListener(this);
        // on récupère un accès au widget qui affichera le nombre aléatoire
        zoneResultat = vueDuFragment.findViewById(R.id.texte_resultat);
        return vueDuFragment;
    }

    @Override
    public void onClick(View v){
        if (v.getId() == R.id.btn_vider) { // clic sur le bouton vider
            zoneResultat.setText(getString(R.string.vide_alea));
        } else { // clic sur le bouton générer
            double nbAlea = Math.random();
            int entierAlea = (int)(Math.random() * 1000);
            zoneResultat.setText(getString(R.string.resultat_alea) + entierAlea);
        }
    }
}
