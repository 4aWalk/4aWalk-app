package fr.iutrodez.a4awalk.adaptateurs;

import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import fr.iutrodez.a4awalk.modeles.entites.Course;
import fr.iutrodez.a4awalk.R;

public class ParcoursViewHolder extends RecyclerView.ViewHolder {

    private TextView dateParcours;
    private TextView nomRandonnee;

    public ParcoursViewHolder(View itemView) {
        super(itemView);
        dateParcours = itemView.findViewById(R.id.date_parcours);
        nomRandonnee = itemView.findViewById(R.id.nom_hike);
    }

    public void bind(Course course, HashMap<Integer, String> dictionnaireRandos){

        DateTimeFormatter formateurStandard = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        dateParcours.setText(course.getDateRealisation().format(formateurStandard));

        // 2. Récupère le nom de la rando
        int hikeId = course.getHikeId();

        if (dictionnaireRandos.containsKey(hikeId)) {
            nomRandonnee.setText(dictionnaireRandos.get(hikeId));
        } else {
            nomRandonnee.setText("Randonnée inconnue");
        }
    }
}