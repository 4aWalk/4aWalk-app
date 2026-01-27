package fr.iutrodez.a4awalk.GestionListes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import fr.iutrodez.a4awalk.R;


public class FragmentListeParcours extends Fragment implements View.OnClickListener{

    public static FragmentListeParcours newInstance() {
        FragmentListeParcours fragment = new FragmentListeParcours();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // On récupère la vue (le layout) associée au fragment un
        View vueDuFragment = inflater.inflate(R.layout.fragment_liste_parcours, container, false);

        return vueDuFragment;
    }

    @Override
    public void onClick(View v){

    }
}
