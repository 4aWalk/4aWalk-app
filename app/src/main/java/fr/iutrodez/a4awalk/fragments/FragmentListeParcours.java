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
import java.util.HashMap; // N'oublie pas cet import

import fr.iutrodez.a4awalk.SuiviParcour.SuiviParcours;
import fr.iutrodez.a4awalk.activites.ParcoursDetailsActivity; // L'import de la nouvelle activité
import fr.iutrodez.a4awalk.adaptateurs.ItemParcoursAdapter;
import fr.iutrodez.a4awalk.modeles.entites.Course;
import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceParcours;
import fr.iutrodez.a4awalk.services.gestionAPI.randonnee.ServiceRandonnee;

public class FragmentListeParcours extends Fragment implements View.OnClickListener {

    private ArrayList<Course> listeParcours;
    private ItemParcoursAdapter adaptateur;
    private RecyclerView parcoursRecyclerView;
    private TextView messageView;
    private User user;
    private Intent intentionRecu;
    private TokenManager tokenManager;

    public static FragmentListeParcours newInstance() {
        return new FragmentListeParcours();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vueDuFragment = inflater.inflate(R.layout.fragment_liste_parcours, container, false);

        // Initialisation User
        intentionRecu = requireActivity().getIntent();
        user = intentionRecu.getParcelableExtra("USER_DATA");
        listeParcours = new ArrayList<>();

        parcoursRecyclerView = vueDuFragment.findViewById(R.id.liste_parcours);
        messageView = vueDuFragment.findViewById(R.id.empty_message);

        parcoursRecyclerView.setLayoutManager(new LinearLayoutManager(vueDuFragment.getContext()));

        // Initialisation du Token
        tokenManager = new TokenManager(getActivity());

        return vueDuFragment;
    }

    public void initialiseListeParcours(String token) {

        ServiceParcours.recupererParcoursUtilisateur(requireContext(), token, new ServiceParcours.ParcoursCallback() {
            @Override
            public void onSuccess(ArrayList<Course> parcours) {
                listeParcours = parcours;

                if (listeParcours == null || listeParcours.isEmpty()) {
                    parcoursRecyclerView.setVisibility(View.GONE);
                    messageView.setVisibility(View.VISIBLE);
                    messageView.setText(R.string.no_course_message);
                    Log.i("INFO", "Aucun parcours disponible");
                } else {
                    ServiceRandonnee.recupererRandonneesUtilisateur(requireContext(), token, user, false, new ServiceRandonnee.RandoCallback() {
                        @Override
                        public void onSuccess(ArrayList<Hike> randonnees) {
                            HashMap<Integer, String> dictionnaireRandos = new HashMap<>();
                            if (randonnees != null) {
                                for (Hike hike : randonnees) {
                                    dictionnaireRandos.put(hike.getId(), hike.getLibelle());
                                }
                            }

                            parcoursRecyclerView.setVisibility(View.VISIBLE);
                            messageView.setVisibility(View.GONE);

                            adaptateur = new ItemParcoursAdapter(listeParcours, dictionnaireRandos, (route, position) -> {
                                Intent intent = new Intent(requireActivity(), ParcoursDetailsActivity.class);
                                intent.putExtra("COURSE_ID", route.getId());
                                intent.putExtra("NOM_PARCOURS", "Parcours " + (position + 1));

                                startActivity(intent);
                            });

                            parcoursRecyclerView.setAdapter(adaptateur);
                        }

                        @Override
                        public void onError(VolleyError error) {
                            Log.e("ERREUR API", "Erreur lors de la récupération des randos pour le dico: " + error.toString());
                        }
                    });
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e("ERREUR API", "Erreur lors de la récupération des parcours: " + error.toString());
            }
        });
    }

    @Override
    public void onClick(View v) {
        // ...
    }

    @Override
    public void onResume() {
        super.onResume();
        // À chaque fois que le fragment redevient visible, on actualise la liste
        if (tokenManager.getToken() != null) {
            initialiseListeParcours(tokenManager.getToken());
        }
    }
}