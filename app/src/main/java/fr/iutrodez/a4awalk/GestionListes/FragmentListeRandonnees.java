package fr.iutrodez.a4awalk.GestionListes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import fr.iutrodez.a4awalk.R;

public class FragmentListeRandonnees extends Fragment implements View.OnClickListener{


    public static FragmentListeRandonnees newInstance() {
        FragmentListeRandonnees fragment = new FragmentListeRandonnees();
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // On récupère la vue (le layout) associée au fragment un
        View vueDuFragment = inflater.inflate(R.layout.fragment_liste_randonnees, container, false);
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
    public void onClick(View view) {

    }
}
