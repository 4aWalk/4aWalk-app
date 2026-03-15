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

    private List<FoodProduct> listeProduits;

    public FoodProductAdapter(List<FoodProduct> listeProduits) {
        this.listeProduits = listeProduits;
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
            // Plus de bouton supprimer ici
        }
    }
}