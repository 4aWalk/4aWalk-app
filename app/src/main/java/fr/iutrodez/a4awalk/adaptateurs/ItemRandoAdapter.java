package fr.iutrodez.a4awalk.adaptateurs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.R;

public class ItemRandoAdapter extends RecyclerView.Adapter<RandoViewHolder>{

    private List<Hike> lesDonnees;
    private OnRandoClickListener listener;

    // Mise à jour de l'interface avec 3 actions distinctes
    public interface OnRandoClickListener {
        void onRandoClick(Hike promenade);
        void onEditClick(Hike promenade);
        void onDeleteClick(Hike promenade);
    }

    public ItemRandoAdapter(List<Hike> donnees, OnRandoClickListener listener) {
        this.lesDonnees = donnees;
        this.listener = listener;
    }

    @Override
    public RandoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rando, parent, false);
        return new RandoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RandoViewHolder holder, int position) {
        Hike myObject = lesDonnees.get(position);
        holder.bind(myObject);

        // Action quand on clique sur la ligne entière (Consultation)
        holder.itemView.setOnClickListener(v -> listener.onRandoClick(myObject));

        // Actions sur les nouvelles icônes
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(myObject));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(myObject));
    }

    @Override
    public int getItemCount() { return lesDonnees.size(); }
}