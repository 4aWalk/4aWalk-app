package fr.iutrodez.a4awalk.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.User;

public class FragmentListeParcours extends Fragment implements View.OnClickListener {

    private User user;

    /**
     * Nouvelle méthode de création qui accepte l'objet User
     */
    public static FragmentListeParcours newInstance(User user) {
        FragmentListeParcours fragment = new FragmentListeParcours();
        Bundle args = new Bundle();
        args.putParcelable("USER_DATA", user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // On récupère la vue associée au fragment
        View vueDuFragment = inflater.inflate(R.layout.fragment_liste_parcours, container, false);

        // --- Récupération du User via les Arguments ---
        if (getArguments() != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                user = getArguments().getParcelable("USER_DATA", User.class);
            } else {
                user = getArguments().getParcelable("USER_DATA");
            }
        }
        // ----------------------------------------------

        return vueDuFragment;
    }

    @Override
    public void onClick(View v) {
        // Logique de clic si nécessaire
    }
}