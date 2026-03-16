package fr.iutrodez.a4awalk.adaptateurs;

import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashMap;
import fr.iutrodez.a4awalk.modeles.entites.Course;
import fr.iutrodez.a4awalk.R;

public class ParcoursViewHolder extends RecyclerView.ViewHolder {

    private TextView nomParcours;
    private TextView nomRandonnee;

    public ParcoursViewHolder(View itemView) {
        super(itemView);
        nomParcours = itemView.findViewById(R.id.nom_parcours);
        nomRandonnee = itemView.findViewById(R.id.nom_hike);
    }

    public void bind(Course course, HashMap<Integer, String> dictionnaireRandos, int position){

        nomParcours.setText("Parcours " + (position + 1));

        // 2. Récupère le nom de la rando
        int hikeId = course.getHikeId();

        if (dictionnaireRandos.containsKey(hikeId)) {
            nomRandonnee.setText(dictionnaireRandos.get(hikeId));
        } else {
            nomRandonnee.setText("Randonnée inconnue");
        }
    }
}