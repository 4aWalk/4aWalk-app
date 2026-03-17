package fr.iutrodez.a4awalk.adaptateurs;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.R;

public class RandoViewHolder extends RecyclerView.ViewHolder {

    private TextView libelleRando;
    public ImageButton btnEdit; // Ajout
    public ImageButton btnDelete; // Ajout

    public RandoViewHolder(View itemView) {
        super(itemView);
        libelleRando = itemView.findViewById(R.id.nom_rando);
        btnEdit = itemView.findViewById(R.id.btn_edit_rando); // Ajout
        btnDelete = itemView.findViewById(R.id.btn_delete_rando); // Ajout
    }

    public void bind(Hike hike){
        libelleRando.setText(hike.getLibelle());
    }
}