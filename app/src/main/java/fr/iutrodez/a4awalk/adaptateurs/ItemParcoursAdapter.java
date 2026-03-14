package fr.iutrodez.a4awalk.adaptateurs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

import fr.iutrodez.a4awalk.modeles.entites.Course;
import fr.iutrodez.a4awalk.R;

public class ItemParcoursAdapter extends RecyclerView.Adapter<ParcoursViewHolder>{

    /**
     * Source de données à afficher par la liste
     */
    private final List<Course> lesDonnees;

    /**
     * Dictionnaire (Cache) contenant l'ID de la randonnée et son Nom
     */
    private final HashMap<Long, String> dictionnaireRandos;

    // 1. Variable pour stocker l'écouteur
    private final OnParcoursClickListener listener;

    // 2. Interface pour communiquer avec l'Activity
    public interface OnParcoursClickListener {
        void onRandoClick(Course route);
    }

    /**
     * Constructeur modifié : on ajoute le dictionnaire en paramètre
     * @param donnees liste des données
     * @param dictionnaireRandos cache des noms de randonnées
     * @param listener l'activité qui écoutera le clic
     */
    public ItemParcoursAdapter(List<Course> donnees, HashMap<Long, String> dictionnaireRandos, OnParcoursClickListener listener) {
        this.lesDonnees = donnees;
        this.dictionnaireRandos = dictionnaireRandos; // Initialisation du dictionnaire
        this.listener = listener;
    }

    @NonNull
    @Override
    public ParcoursViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.item_parcours,
                parent,false);
        return new ParcoursViewHolder(view);
    }

    // Signature corrigée : on enlève "String nomHike" qui causait une erreur de compilation
    @Override
    public void onBindViewHolder(@NonNull ParcoursViewHolder holder, int position) {
        Course myObject = lesDonnees.get(position);

        // On passe l'objet Course ET le dictionnaire au ViewHolder
        holder.bind(myObject, dictionnaireRandos);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRandoClick(myObject);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lesDonnees.size();
    }
}