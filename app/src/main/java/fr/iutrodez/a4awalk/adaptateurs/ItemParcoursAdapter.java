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

    private final List<Course> lesDonnees;
    private final HashMap<Integer, String> dictionnaireRandos;
    private final OnParcoursClickListener listener;

    // Remplacez votre interface actuelle par celle-ci :
    public interface OnParcoursClickListener {
        void onRandoClick(Course route, int position); // Ajout du paramètre position
    }

    public ItemParcoursAdapter(List<Course> donnees, HashMap<Integer, String> dictionnaireRandos, OnParcoursClickListener listener) {
        this.lesDonnees = donnees;
        this.dictionnaireRandos = dictionnaireRandos;
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

    @Override
    public void onBindViewHolder(@NonNull ParcoursViewHolder holder, int position) {
        Course myObject = lesDonnees.get(position);

        holder.bind(myObject, dictionnaireRandos);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // getAdapterPosition() est plus sûr que d'utiliser 'position' directement
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onRandoClick(myObject, currentPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return lesDonnees.size();
    }
}