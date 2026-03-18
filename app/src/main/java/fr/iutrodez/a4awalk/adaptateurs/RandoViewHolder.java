package fr.iutrodez.a4awalk.adaptateurs;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.Hike;

public class RandoViewHolder extends RecyclerView.ViewHolder {

    private TextView libelleRando;
    public ImageButton btnEdit;
    public ImageButton btnDelete;
    private ImageView iconeNonOptimise;

    public RandoViewHolder(View itemView) {
        super(itemView);
        libelleRando = itemView.findViewById(R.id.nom_rando);
        btnEdit = itemView.findViewById(R.id.btn_edit_rando);
        btnDelete = itemView.findViewById(R.id.btn_delete_rando);
        iconeNonOptimise = itemView.findViewById(R.id.icone_non_optimise);
    }

    public void bind(Hike hike) {
        libelleRando.setText(hike.getLibelle());

        if (!hike.getOptimize()) {
            iconeNonOptimise.setVisibility(View.VISIBLE);
            iconeNonOptimise.setOnClickListener(v ->
                    Toast.makeText(v.getContext(), "Randonnée non optimisée", Toast.LENGTH_SHORT).show()
            );
        } else {
            iconeNonOptimise.setVisibility(View.GONE);
            iconeNonOptimise.setOnClickListener(null);
        }
    }
}