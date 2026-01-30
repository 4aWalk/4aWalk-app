package fr.iutrodez.a4awalk.adaptateurs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.R;

public class ItemRandoAdapter extends RecyclerView.Adapter<RandoViewHolder>{

    /**
     * Source de données à afficher par la liste
     */
    private List<Hike> lesDonnees;

    // 1. Variable pour stocker l'écouteur
    private OnRandoClickListener listener;

    // 2. Interface pour communiquer avec l'Activity
    public interface OnRandoClickListener {
        void onRandoClick(Hike promenade);
    }

    /**
     * Constructeur modifié : on ajoute le listener en paramètre
     * @param donnees liste des données
     * @param listener l'activité qui écoutera le clic
     */
    public ItemRandoAdapter(List<Hike> donnees, OnRandoClickListener listener) {
        this.lesDonnees = donnees;
        this.listener = listener; // On sauvegarde l'écouteur
    }

    @Override
    public RandoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.item_rando,
                parent,false);
        return new RandoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RandoViewHolder holder, int position) {
        Hike myObject = lesDonnees.get(position);

        // Affichage des données (texte, image...)
        holder.bind(myObject);

        // 3. GESTION DU CLIC
        // On pose un écouteur sur l'élément entier (itemView)
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On déclenche la méthode de l'interface en lui passant l'objet cliqué
                listener.onRandoClick(myObject);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lesDonnees.size();
    }
}