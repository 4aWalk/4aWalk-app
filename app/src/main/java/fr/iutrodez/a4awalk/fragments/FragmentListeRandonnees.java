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
import android.app.AlertDialog;
import org.json.JSONObject;
import fr.iutrodez.a4awalk.services.AppelAPI;

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
        adaptateur = new ItemRandoAdapter(listeRandos, new ItemRandoAdapter.OnRandoClickListener() {

            @Override
            public void onRandoClick(Hike hikeResume) {
                // Clic sur la carte : Consultation (ID_PAGE = 1)
                ouvrirRandonnee(hikeResume, 1);
            }

            @Override
            public void onEditClick(Hike hikeResume) {
                // Clic sur l'icône Modifier : Modification (ID_PAGE = 3)
                ouvrirRandonnee(hikeResume, 3);
            }

            @Override
            public void onDeleteClick(Hike hikeResume) {
                // Clic sur l'icône Supprimer : Affichage de la confirmation
                afficherConfirmationSuppression(hikeResume);
            }
        });

        randoRecyclerView.setAdapter(adaptateur);
    }

    private void ouvrirRandonnee(Hike hikeResume, int idPageAction) {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Chargement des détails...", Toast.LENGTH_SHORT).show();
        }

        ServiceRandonnee.recupererDetailsRandonnee(requireContext(), tokenManager.getToken(), hikeResume.getId(), user, new ServiceRandonnee.RandoDetailCallback() {
            @Override
            public void onSuccess(Hike hikeDetailComplet) {
                Intent intent = new Intent(getActivity(), ActiviteGestionRandonnee.class);
                intent.putExtra("ID_PAGE", idPageAction); // 1 pour Consultation, 3 pour Modification
                intent.putExtra("HIKE_OBJECT", hikeDetailComplet);
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
    }

    private void afficherConfirmationSuppression(Hike rando) {
        String nomRando = rando != null && rando.getLibelle() != null ? rando.getLibelle() : "cette randonnée";
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmation de suppression")
                .setMessage("Etes-vous sûr de vouloir supprimer la randonnée " + nomRando + " ?")
                .setPositiveButton("Oui", (dialog, which) -> supprimerRandonnee(rando))
                .setNegativeButton("Non", null)
                .show();
    }

    private void supprimerRandonnee(Hike rando) {
        ServiceRandonnee.supprimerRandonnee(requireContext(), tokenManager.getToken(), rando.getId(), new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Randonnée supprimée avec succès", Toast.LENGTH_SHORT).show();
                }
                // On rafraîchit la liste pour faire disparaître l'élément supprimé
                initialiseListeRandos(tokenManager.getToken());
            }

            @Override
            public void onError(VolleyError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Erreur lors de la suppression de la randonnée", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        // ...
    }
}