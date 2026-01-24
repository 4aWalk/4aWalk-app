package fr.iutrodez.a4awalk.GestionListes.GestionItemRando;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import fr.iutrodez.a4awalk.entity.Hike;
import fr.iutrodez.a4awalk.R;

public class RandoViewHolder extends RecyclerView.ViewHolder{
    /**
     * TextView qui contient le nom de la randonnée
     */
    private TextView libelleRando;

    /**
     * ImageView qui contient le nombre de participants de la randonnée
     */
    private TextView nbParticipantsRando;

    /**
     * ImageView qui contient le nombre de jours de la randonnée
     */
    private TextView nbJoursRando;

    /**
     * Constructeur avec en argument une vue correspondant
     * à un item de la liste
     * Le constructeur permet d'initialiser les identifiants des
     * widgets déclarés en tant qu'attributs
     * @param itemView vue décrivant l'affichage d'un item de la liste
     */
    public RandoViewHolder(View itemView) {
        super(itemView);
        libelleRando = (TextView) itemView.findViewById(R.id.nom_rando);
        nbParticipantsRando = (TextView) itemView.findViewById(R.id.nb_participants_rando);
        nbJoursRando = (TextView) itemView.findViewById(R.id.nb_jours_rando);
    }
    /**
     * Permet de placer les informations contenues dans l'argument
     * dans les widgets d'un item de la liste
     * @param hike l'instance qui doit être affichée
     */
    public void bind(Hike hike){
        libelleRando.setText(hike.getLibelle());
        nbParticipantsRando.setText(hike.participantSize() + " participants");
        nbJoursRando.setText(String.valueOf(hike.getDureeJours()) + " jours");
    }
}
