package fr.iutrodez.a4awalk.GestionListes;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import fr.iutrodez.a4awalk.AppelAPI;
import fr.iutrodez.a4awalk.entity.Hike;
import fr.iutrodez.a4awalk.GestionListes.GestionItemRando.DetailsRando;
import fr.iutrodez.a4awalk.GestionListes.GestionItemRando.ItemRandoAdapter;
import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.entity.Participant;
import fr.iutrodez.a4awalk.entity.PointOfInterest;
import fr.iutrodez.a4awalk.entity.User;
import fr.iutrodez.a4awalk.model.enums.Level;
import fr.iutrodez.a4awalk.model.enums.Morphology;

public class FragmentListeRandonnees extends Fragment implements View.OnClickListener{

    private static final String URL_RANDOS = "http://98.94.8.220:8080/hikes/my";

    public final static String HIKE_ID_KEY = "HIKE_ID";

    public final static String CHILD_MESSAGE_KEY = "CHILD_MESSAGE";

    /**
     * Liste source des données à afficher :
     * chaque élément contient une instance de ItemDetailsRando (une photo
     * et son libellé)
     */
    private ArrayList<Hike> listeRandos;

    private Set<PointOfInterest> pointsInterets;

    private ItemRandoAdapter adaptateur;

    /**
     * Element permettant d'afficher la liste des randonnées
     */
    private RecyclerView randoRecyclerView;

    private View fab;

    private TextView messageView;

    private User user;

    private Level level;

    private Morphology morpho;

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

        level = Level.DEBUTANT;
        morpho = Morphology.LEGERE;
        user  = new User("LAPEYRE", "Tony", "tony.lapeyre@iut-rodez.fr", "12854", "a", 20, level, morpho);
        listeRandos = new ArrayList<>();

        randoRecyclerView = vueDuFragment.findViewById(R.id.liste_rando);
        messageView = vueDuFragment.findViewById(R.id.empty_message);
        LinearLayoutManager gestionnaireLineaire = new LinearLayoutManager(vueDuFragment.getContext());
        randoRecyclerView.setLayoutManager(gestionnaireLineaire);
        initialiseListeRandos();
        fab = vueDuFragment.findViewById(R.id.fab_add_hike);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logique pour ajouter une randonnée
                Log.d("ACTION", "Clic sur le bouton ajouter !");
                Intent intent = new Intent(getActivity(), DetailsRando.class);
                intent.putExtra("ID_PAGE",2);
                startActivity(intent);
            }
        });
        return vueDuFragment;
    }

    public void initialiseListeRandos() {
        AppelAPI.appelAPI(URL_RANDOS, requireContext(), new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                if (result == null || result.length() == 0) {
                    randoRecyclerView.setVisibility(View.GONE);
                    messageView.setVisibility(View.VISIBLE);
                    messageView.setText(R.string.no_hikes_message);
                    Log.i("pas de randonnee", "aucune randonnée dispo");
                } else {
                    randoRecyclerView.setVisibility(View.VISIBLE);
                    messageView.setVisibility(View.GONE);
                    recupInfosRandos(result);
                }
            }

            @Override
            public void onError(String message) {
                Log.i("erreur", message);
            }
        });
    }

    private void recupInfosRandos(JSONArray reponse) {
        try {
            for (int i = 0; i < reponse.length(); i++) {
                // On extrait les variables du JSON
                JSONObject randoJson = reponse.getJSONObject(i);
                Long id = (long) randoJson.getInt("id");
                String name = randoJson.getString("libelle");

                JSONObject departObj = randoJson.getJSONObject("depart");
                Long idDepart = (long) departObj.getInt("id");
                double latDepart = departObj.getDouble("latitude");
                double lonDepart = departObj.getDouble("longitude");
                String nomDepart = departObj.getString("name");
                PointOfInterest POIDepart = new PointOfInterest(idDepart, nomDepart, latDepart, lonDepart);


                JSONObject arriveeObj = randoJson.getJSONObject("arrivee");
                Long idArrivee = (long) departObj.getInt("id");
                double latArrivee = arriveeObj.getDouble("latitude");
                double lonArrivee = arriveeObj.getDouble("longitude");
                String nomArrivee = arriveeObj.getString("name");
                PointOfInterest POIArrivee = new PointOfInterest(idArrivee, nomArrivee, latArrivee, lonArrivee);

                Set<Participant> ensembleParticipants = new HashSet<>();
                // 1. On récupère le tableau des participants
                JSONArray participantsArray = randoJson.getJSONArray("participants");
                int nbParticipants = participantsArray.length();

                //TODO participant et points d'intérêts
                for (int j = 0; j < participantsArray.length(); j++) {
                    JSONObject partJson = participantsArray.getJSONObject(j);

                    // On extrait les données du participant (selon votre JSON)
                    long idPart = partJson.getLong("id");
                    int age = partJson.getInt("age");
                    // Note : assurez-vous que votre classe Participant possède un constructeur adapté
                    // ou utilisez les setters.
                    Participant p = new Participant();
                    p.setId(idPart);
                    p.setAge(age);
                    String niveauStr = partJson.getString("niveau");
                    p.setNiveau(Level.valueOf(niveauStr));

                    String morphoStr = partJson.getString("morphologie");
                    p.setMorphologie(Morphology.valueOf(morphoStr));

                    // Booléens et doubles
                    p.setCreator(partJson.getBoolean("creator"));
                    p.setBesoinKcal(partJson.getInt("besoinKcal"));
                    p.setBesoinEauLitre(partJson.getInt("besoinEauLitre"));
                    p.setCapaciteEmportMaxKg(partJson.getDouble("capaciteEmportMaxKg"));

                    ensembleParticipants.add(p);
                }

                int dureeJours = randoJson.getInt("dureeJours");
                Hike hike = new Hike(id, name, POIDepart, POIArrivee, dureeJours ,user);
                hike.setParticipants(ensembleParticipants);
                listeRandos.add(hike);

            }
            // 4. On crée notre objet Java
              affichageInfosRando();
        } catch (JSONException e) {
            e.printStackTrace();
            // Erreur si le JSON est mal formé ou si une clé est fausse
        }
    }

    private void affichageInfosRando() {
        // 4. Initialisation de l'Adapter avec l'interface (Callback)
        adaptateur = new ItemRandoAdapter(listeRandos, new ItemRandoAdapter.OnRandoClickListener() {
            @Override
            public void onRandoClick(Hike hike) {

                // A. Création de l'intention pour ouvrir la page de détail
                // Remplacez "DetailActivity.class" par le nom de votre classe de destination
                Intent intent = new Intent(getActivity(), DetailsRando.class);

                // B. Passage des informations à l'autre page (facultatif mais utile)
                // Supposons que ItemDetailsRando a des getters
                intent.putExtra("ID_PAGE",1);
                intent.putExtra("HIKE_OBJECT", hike);

                // C. Lancement de l'activité
                startActivity(intent);
            }
        });
        randoRecyclerView.setAdapter(adaptateur);
    }

    @Override
    public void onClick(View v) {

    }
}