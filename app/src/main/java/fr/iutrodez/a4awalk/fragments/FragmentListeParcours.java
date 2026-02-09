package fr.iutrodez.a4awalk.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;

import java.util.ArrayList;

import fr.iutrodez.a4awalk.activites.ActiviteGestionRandonnee;
import fr.iutrodez.a4awalk.modeles.entites.Course;
import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.adaptateurs.ItemParcoursAdapter;
import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceParcours;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceRandonnee;

public class FragmentListeParcours extends Fragment implements View.OnClickListener {

    private ArrayList<Course> listeParcours;
    private ItemParcoursAdapter adaptateur;
    private RecyclerView parcoursRecyclerView;
    private TextView messageView;
    private User user;
    private Intent intentionRecu;

    public static FragmentListeRandonnees newInstance() {
        return new FragmentListeRandonnees();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vueDuFragment = inflater.inflate(R.layout.fragment_liste_parcours, container, false);

        // Initialisation User factice
        intentionRecu = requireActivity().getIntent();
        user = (User) intentionRecu.getParcelableExtra("USER_DATA");
        listeParcours = new ArrayList<>();

        parcoursRecyclerView = vueDuFragment.findViewById(R.id.liste_parcours);
        messageView = vueDuFragment.findViewById(R.id.empty_message);

        parcoursRecyclerView.setLayoutManager(new LinearLayoutManager(vueDuFragment.getContext()));

        // Initialisation du Token
        TokenManager tokenManager = new TokenManager(getActivity());
        String token = tokenManager.getToken();

        // Appel de la méthode qui utilise le service
        initialiseListeParcours(token);

        return vueDuFragment;
    }

    /**
     * Utilise le ServiceRandonnee pour charger les données
     */
    public void initialiseListeParcours(String token) {

        ServiceParcours.recupererParcoursUtilisateur(requireContext(), token, user, new ServiceParcours.ParcoursCallback() {
            @Override
            public void onSuccess(ArrayList<Course> parcours) {
                // Mise à jour de la liste locale
                listeParcours = parcours;

                // Gestion de l'affichage vide/plein
                if (listeParcours.isEmpty()) {
                    parcoursRecyclerView.setVisibility(View.GONE);
                    messageView.setVisibility(View.VISIBLE);
                    messageView.setText(R.string.no_course_message);
                    Log.i("INFO", "Aucune parcours disponible");
                } else {
                    parcoursRecyclerView.setVisibility(View.VISIBLE);
                    messageView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e("ERREUR API", "Erreur lors de la récupération des parcours: " + error.toString());
                // TODO prévention de l'utilisateur
            }
        });
    }


    @Override
    public void onClick(View v) {
        // ...
    }
}