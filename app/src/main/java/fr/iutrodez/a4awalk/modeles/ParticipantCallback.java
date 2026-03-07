package fr.iutrodez.a4awalk.modeles;

import fr.iutrodez.a4awalk.modeles.entites.Participant;

public interface ParticipantCallback {
    void onActionSuccess(Participant participant);
    void onDeleteAction(Participant participant);
}