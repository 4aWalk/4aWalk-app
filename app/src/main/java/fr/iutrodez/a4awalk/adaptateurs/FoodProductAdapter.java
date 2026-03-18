package fr.iutrodez.a4awalk.adaptateurs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.FoodProduct;

public class FoodProductAdapter extends RecyclerView.Adapter<FoodProductAdapter.FoodProductViewHolder> {

    // 1. Création de l'interface pour capter le clic sur un élément
    public interface OnItemClickListener {
        void onItemClick(FoodProduct item);
    }

    private List<FoodProduct> listeProduits;
    private OnItemClickListener listener; // 2. Ajout de la variable pour le listener

    // 3. Mise à jour du constructeur pour qu'il prenne le listener en paramètre
    public FoodProductAdapter(List<FoodProduct> listeProduits, OnItemClickListener listener) {
        this.listeProduits = listeProduits;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_product, parent, false);
        return new FoodProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodProductViewHolder holder, int position) {
        FoodProduct produit = listeProduits.get(position);

        holder.tvNom.setText(produit.getNom());

        // Ajout du nbItem juste avant le prix
        String details = String.format("%s g | %s Kcal | %d items | %s €",
                produit.getMasseGrammes(),
                produit.getApportNutritionnelKcal(),
                produit.getNbItem(),
                produit.getPrixEuro());
        holder.tvDetails.setText(details);

        // 4. Déclenchement de l'événement lors du clic sur la ligne entière
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(produit);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listeProduits.size();
    }

    public static class FoodProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvNom;
        TextView tvDetails;

        public FoodProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNom = itemView.findViewById(R.id.tv_item_fp_nom);
            tvDetails = itemView.findViewById(R.id.tv_item_fp_details);
        }
    }
}