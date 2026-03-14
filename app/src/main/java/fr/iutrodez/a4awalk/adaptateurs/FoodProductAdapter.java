package fr.iutrodez.a4awalk.adaptateurs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.FoodProduct;

public class FoodProductAdapter extends RecyclerView.Adapter<FoodProductAdapter.FoodProductViewHolder> {

    private List<FoodProduct> listeProduits;
    private OnProductDeleteListener deleteListener;

    // Interface pour gérer le clic sur le bouton de suppression depuis l'Activité
    public interface OnProductDeleteListener {
        void onDeleteClick(FoodProduct produit);
    }

    // Constructeur de l'Adapter
    public FoodProductAdapter(List<FoodProduct> listeProduits, OnProductDeleteListener deleteListener) {
        this.listeProduits = listeProduits;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public FoodProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // On "gonfle" (inflate) le layout XML de la ligne
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_product, parent, false);
        return new FoodProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodProductViewHolder holder, int position) {
        // On récupère le produit correspondant à la position actuelle dans la liste
        FoodProduct produit = listeProduits.get(position);

        // On remplit les champs de texte
        holder.tvNom.setText(produit.getNom());

        // On crée une chaîne de caractères pour les détails (masse, calories, prix)
        String details = String.format("%s g | %s Kcal | %s €",
                produit.getMasseGrammes(),
                produit.getApportNutritionnelKcal(),
                produit.getPrixEuro());
        holder.tvDetails.setText(details);

        // On gère le clic sur le bouton supprimer (la corbeille)
        holder.btnSupprimer.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(produit); // Remonte l'info à l'activité
            }
        });
    }

    @Override
    public int getItemCount() {
        return listeProduits.size();
    }

    // Classe interne ViewHolder qui représente les éléments graphiques d'une ligne
    public static class FoodProductViewHolder extends RecyclerView.ViewHolder {

        TextView tvNom;
        TextView tvDetails;
        ImageButton btnSupprimer;

        public FoodProductViewHolder(@NonNull View itemView) {
            super(itemView);
            // On fait le lien avec les IDs de item_food_product.xml
            tvNom = itemView.findViewById(R.id.tv_item_fp_nom);
            tvDetails = itemView.findViewById(R.id.tv_item_fp_details);
            btnSupprimer = itemView.findViewById(R.id.btn_item_fp_supprimer);
        }
    }
}