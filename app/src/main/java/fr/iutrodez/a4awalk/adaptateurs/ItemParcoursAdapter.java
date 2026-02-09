package fr.iutrodez.a4awalk.adaptateurs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.iutrodez.a4awalk.modeles.entites.Course;
import fr.iutrodez.a4awalk.R;

public class ItemParcoursAdapter extends RecyclerView.Adapter<ParcoursViewHolder>{

    /**
     * Source de données à afficher par la liste
     */
    private final List<Course> lesDonnees;

    // 1. Variable pour stocker l'écouteur
    private final OnParcoursClickListener listener;

    // 2. Interface pour communiquer avec l'Activity
    public interface OnParcoursClickListener {
        void onRandoClick(Course route);
    }

    /**
     * Constructeur modifié : on ajoute le listener en paramètre
     * @param donnees liste des données
     * @param listener l'activité qui écoutera le clic
     */
    public ItemParcoursAdapter(List<Course> donnees, OnParcoursClickListener listener) {
        this.lesDonnees = donnees;
        this.listener = listener; // On sauvegarde l'écouteur
    }

    @NonNull
    @Override
    public ParcoursViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.item_parcours,
                parent,false);
        return new ParcoursViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParcoursViewHolder holder, int position) {
        Course myObject = lesDonnees.get(position);

        holder.bind(myObject);

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