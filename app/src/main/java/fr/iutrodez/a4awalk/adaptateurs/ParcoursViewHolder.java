package fr.iutrodez.a4awalk.adaptateurs;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;

import fr.iutrodez.a4awalk.modeles.entites.Course;
import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.services.gestionAPI.randonnee.ServiceRandonnee;

public class ParcoursViewHolder extends RecyclerView.ViewHolder {

    private TextView nomParcours;
    private TextView nomRandonnee;

    public ParcoursViewHolder(View itemView) {
        super(itemView);
        nomParcours = (TextView) itemView.findViewById(R.id.nom_parcours);
        nomRandonnee = (TextView) itemView.findViewById(R.id.nom_hike);
    }

    public void bind(Course course, HashMap<Long, String> dictionnaireRandos){
        nomParcours.setText("Parcours n°" + course.getId());

        long hikeId = course.getHikeId();

        // On cherche le nom de la rando dans notre dictionnaire pré-chargé
        if (dictionnaireRandos.containsKey(hikeId)) {
            nomRandonnee.setText(dictionnaireRandos.get(hikeId));
        } else {
            nomRandonnee.setText("Randonnée inconnue");
        }
    }

}
