package fr.iutrodez.a4awalk.adaptateurs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.EquipmentItem;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder> {

    // 1. L'interface pour le clic
    public interface OnItemClickListener {
        void onItemClick(EquipmentItem item);
    }

    private List<EquipmentItem> listeEquipments;
    private OnItemClickListener listener;

    // 2. Le constructeur prend maintenant le listener en paramètre
    public EquipmentAdapter(List<EquipmentItem> listeEquipments, OnItemClickListener listener) {
        this.listeEquipments = listeEquipments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EquipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_equipment, parent, false);
        return new EquipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipmentViewHolder holder, int position) {
        EquipmentItem produit = listeEquipments.get(position);

        holder.tvNom.setText(produit.getNom());

        String details = String.format("%s g | %s item(s) | %s",
                produit.getMasseGrammes(),
                produit.getNbItem(),
                produit.getType() != null ? produit.getType().name() : "Inconnu");
        holder.tvDetails.setText(details);

        // 3. On déclenche le clic sur toute la ligne
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(produit);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listeEquipments.size();
    }

    public static class EquipmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvNom;
        TextView tvDetails;

        public EquipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNom = itemView.findViewById(R.id.tv_item_eq_nom);
            tvDetails = itemView.findViewById(R.id.tv_item_eq_details);
        }
    }
}