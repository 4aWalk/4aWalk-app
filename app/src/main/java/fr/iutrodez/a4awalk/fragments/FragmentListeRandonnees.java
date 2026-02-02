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
import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.adaptateurs.ItemRandoAdapter;
import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceRandonnee;

public class FragmentListeRandonnees extends Fragment implements View.OnClickListener {

    public final static String HIKE_ID_KEY = "HIKE_ID";
    public final static String CHILD_MESSAGE_KEY = "CHILD_MESSAGE";

    private ArrayList<Hike> listeRandos;
    private ItemRandoAdapter adaptateur;
    private RecyclerView randoRecyclerView;
    private View fab;
    private TextView messageView;
    private User user;
    private Intent intentionRecu;

    public static FragmentListeRandonnees newInstance() {
        return new FragmentListeRandonnees();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vueDuFragment = inflater.inflate(R.layout.fragment_liste_randonnees, container, false);

        // Initialisation User factice
        intentionRecu = requireActivity().getIntent();
        user = (User) intentionRecu.getParcelableExtra("USER_DATA");
        listeRandos = new ArrayList<>();

        randoRecyclerView = vueDuFragment.findViewById(R.id.liste_rando);
        messageView = vueDuFragment.findViewById(R.id.empty_message);

        randoRecyclerView.setLayoutManager(new LinearLayoutManager(vueDuFragment.getContext()));

        // Initialisation du Token
        TokenManager tokenManager = new TokenManager(getActivity());
        String token = tokenManager.getToken();

        // Appel de la méthode qui utilise le service
        initialiseListeRandos(token);

        fab = vueDuFragment.findViewById(R.id.fab_add_hike);
        fab.setOnClickListener(v -> {
            Log.d("ACTION", "Clic sur le bouton ajouter !");
            Intent intent = new Intent(getActivity(), ActiviteGestionRandonnee.class);
            intent.putExtra("ID_PAGE", 2);
            startActivity(intent);
        });

        return vueDuFragment;
    }

    /**
     * Utilise le ServiceRandonnee pour charger les données
     */
    public void initialiseListeRandos(String token) {

        ServiceRandonnee.recupererRandonneesUtilisateur(requireContext(), token, user, new ServiceRandonnee.RandoCallback() {
            @Override
            public void onSuccess(ArrayList<Hike> randonnees) {
                // Mise à jour de la liste locale
                listeRandos = randonnees;

                // Gestion de l'affichage vide/plein
                if (listeRandos.isEmpty()) {
                    randoRecyclerView.setVisibility(View.GONE);
                    messageView.setVisibility(View.VISIBLE);
                    messageView.setText(R.string.no_hikes_message);
                    Log.i("INFO", "Aucune randonnée disponible");
                } else {
                    randoRecyclerView.setVisibility(View.VISIBLE);
                    messageView.setVisibility(View.GONE);
                    affichageInfosRando();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e("ERREUR API", "Erreur lors de la récupération des randos: " + error.toString());
                // Ici, vous pourriez afficher un Toast ou une Snackbar pour avertir l'utilisateur
            }
        });
    }

    private void affichageInfosRando() {
        adaptateur = new ItemRandoAdapter(listeRandos, hike -> {
            Intent intent = new Intent(getActivity(), ActiviteGestionRandonnee.class);
            intent.putExtra("ID_PAGE", 1);
            intent.putExtra("HIKE_OBJECT", hike);
            startActivity(intent);
        });
        randoRecyclerView.setAdapter(adaptateur);
    }

    @Override
    public void onClick(View v) {
        // ...
    }
}