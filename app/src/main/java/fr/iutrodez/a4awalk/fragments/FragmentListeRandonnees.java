package fr.iutrodez.a4awalk.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;

import java.util.ArrayList;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.activites.ActiviteGestionRandonnee;
import fr.iutrodez.a4awalk.adaptateurs.ItemRandoAdapter;
import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.services.gestionAPI.randonnee.ServiceRandonnee;

public class FragmentListeRandonnees extends Fragment implements View.OnClickListener {

    public final static String HIKE_ID_KEY = "HIKE_ID";
    public final static String CHILD_MESSAGE_KEY = "CHILD_MESSAGE";

    private ArrayList<Hike> listeRandos;
    private ItemRandoAdapter adaptateur;
    private RecyclerView randoRecyclerView;
    private View fab;
    private TextView messageView;
    private User user;
    private TokenManager tokenManager;

    // Lanceur d'activité qui écoute le retour (Création OU Modification)
    private ActivityResultLauncher<Intent> randoResultLauncher;

    public static FragmentListeRandonnees newInstance(User user) {
        FragmentListeRandonnees fragment = new FragmentListeRandonnees();
        Bundle args = new Bundle();
        args.putParcelable("USER_DATA", user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialisation du lanceur
        randoResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d("REFRESH", "Retour d'activité avec succès. Actualisation...");
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Liste actualisée", Toast.LENGTH_SHORT).show();
                        }
                        if (tokenManager != null) {
                            initialiseListeRandos(tokenManager.getToken());
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vueDuFragment = inflater.inflate(R.layout.fragment_liste_randonnees, container, false);

        if (getArguments() != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                user = getArguments().getParcelable("USER_DATA", User.class);
            } else {
                user = getArguments().getParcelable("USER_DATA");
            }
        }

        tokenManager = new TokenManager(getActivity());
        listeRandos = new ArrayList<>();

        randoRecyclerView = vueDuFragment.findViewById(R.id.liste_rando);
        messageView = vueDuFragment.findViewById(R.id.empty_message);

        randoRecyclerView.setLayoutManager(new LinearLayoutManager(vueDuFragment.getContext()));

        // Premier chargement
        initialiseListeRandos(tokenManager.getToken());

        // Bouton Flottant
        fab = vueDuFragment.findViewById(R.id.fab_add_hike);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ActiviteGestionRandonnee.class);
            intent.putExtra("ID_PAGE", 2); // Mode Création
            intent.putExtra("USER_DATA", user);
            randoResultLauncher.launch(intent);
        });

        return vueDuFragment;
    }

    public void initialiseListeRandos(String token) {
        if (getContext() == null) return;

        ServiceRandonnee.recupererRandonneesUtilisateur(requireContext(), token, user, new ServiceRandonnee.RandoCallback() {
            @Override
            public void onSuccess(ArrayList<Hike> randonnees) {
                listeRandos = randonnees;

                if (listeRandos.isEmpty()) {
                    randoRecyclerView.setVisibility(View.GONE);
                    messageView.setVisibility(View.VISIBLE);
                    messageView.setText(R.string.no_hikes_message);
                } else {
                    randoRecyclerView.setVisibility(View.VISIBLE);
                    messageView.setVisibility(View.GONE);
                    affichageInfosRando();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e("ERREUR API", "Erreur lors de la récupération des randos: " + error.toString());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Erreur de chargement", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void affichageInfosRando() {
        adaptateur = new ItemRandoAdapter(listeRandos, hikeResume -> {

            // 1. Indiquer à l'utilisateur qu'on charge les détails
            if (getContext() != null) {
                Toast.makeText(getContext(), "Chargement des détails...", Toast.LENGTH_SHORT).show();
            }

            // 2. Appel ciblé pour récupérer l'objet complet de CETTE randonnée
            ServiceRandonnee.recupererDetailsRandonnee(requireContext(), tokenManager.getToken(), hikeResume.getId(), user, new ServiceRandonnee.RandoDetailCallback() {
                @Override
                public void onSuccess(Hike hikeDetailComplet) {
                    // Lancement de l'activité avec l'objet complet
                    Intent intent = new Intent(getActivity(), ActiviteGestionRandonnee.class);
                    intent.putExtra("ID_PAGE", 1); // Mode Consultation/Modification
                    intent.putExtra("HIKE_OBJECT", hikeDetailComplet); // Objet complet
                    intent.putExtra("USER_DATA", user);

                    randoResultLauncher.launch(intent);
                }

                @Override
                public void onError(VolleyError error) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Impossible de charger les détails", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        randoRecyclerView.setAdapter(adaptateur);
    }

    @Override
    public void onClick(View v) {
        // ...
    }
}