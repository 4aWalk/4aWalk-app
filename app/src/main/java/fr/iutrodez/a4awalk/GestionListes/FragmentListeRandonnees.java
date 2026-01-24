package fr.iutrodez.a4awalk.GestionListes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;

import fr.iutrodez.a4awalk.AppelAPI;
import fr.iutrodez.a4awalk.entity.Hike;
import fr.iutrodez.a4awalk.GestionListes.GestionItemRando.DetailsRando;
import fr.iutrodez.a4awalk.GestionListes.GestionItemRando.ItemRandoAdapter;
import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.entity.PointOfInterest;
import fr.iutrodez.a4awalk.entity.User;
import fr.iutrodez.a4awalk.model.enums.Level;
import fr.iutrodez.a4awalk.model.enums.Morphology;

public class FragmentListeRandonnees extends Fragment implements View.OnClickListener{

    private static final String URL_RANDOS = "https://prescriptiontrails.org/api/trail/?id=2";

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
        initialiseListeRandos();

        LinearLayoutManager gestionnaireLineaire = new LinearLayoutManager(vueDuFragment.getContext());
        randoRecyclerView.setLayoutManager(gestionnaireLineaire);
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
                //intent.putExtra("HIKE_OBJECT", hike);

                // C. Lancement de l'activité
                startActivity(intent);

            }
        });
        randoRecyclerView.setAdapter(adaptateur);

        /*
         * on associe un écouteur à chacun des 2 boutons de la vue : le fragment
         * courant sera son propre écouteur de clic sur les boutons
         */
        //vueDuFragment.findViewById(R.id.btn_alea).setOnClickListener(this);
        //// on récupère un accès au widget qui affichera le nombre aléatoire
        //zoneResultat = vueDuFragment.findViewById(R.id.texte_resultat);
        return vueDuFragment;
    }

    public void initialiseListeRandos() {
        //AppelAPI.appelAPI(URL_RANDOS, requireContext(), new AppelAPI.VolleyCallback() {
        //    @Override
        //    public void onSuccess(JSONObject result) {
        //        recupInfosRandos(result);
        //    }
//
        //    @Override
        //    public void onError(String message) {
        //        Log.i("erreur", message);
        //    }
        //});
        listeRandos.add(new Hike(1L, "Randonnée1", "depart", "arrivee", 3, user));
        listeRandos.add(new Hike(2L, "Randonnée2", "depart", "arrivee", 1, user));
        listeRandos.add(new Hike(3L, "Randonnée3", "depart", "arrivee", 2, user));
    }

    private void recupInfosRandos(JSONObject reponse) {
        try {
            for (int i = 0; i < reponse.length(); i++) {
                // On extrait les variables du JSON
                Long idRando = reponse.getLong("id");
                String name = reponse.getString("name");
                String departRando = reponse.getString("depart");
                String arriveeRando = reponse.getString("arrivee");
                int nbParticipants = reponse.getInt("nbParticipants");
                int dureeJours = reponse.getInt("nbJours");
                JSONObject tableauPoints = reponse.getJSONObject("pointsInterets");
                Hike hike = new Hike(idRando, name, departRando, arriveeRando, dureeJours ,user);
                listeRandos.add(hike);
                for (int y = 0; y < tableauPoints.length(); y++) {
                    listeRandos.get(i).addPointOfInterest(new PointOfInterest(tableauPoints.getString("name"), tableauPoints.getDouble("lat"), tableauPoints.getDouble("lon"), tableauPoints.getString("description"), hike));
                }

            }
            // 4. On crée notre objet Java
            //detailsRando = new ItemDetailsRando(idRando, libelleRando, departRando, arriveeRando, pointsInterets, nbParticipants, nbJours);
            //affichageInfosRando()
        } catch (JSONException e) {
            e.printStackTrace();
            // Erreur si le JSON est mal formé ou si une clé est fausse
        }
    }
    @Override
    public void onClick(View v) {

    }
}