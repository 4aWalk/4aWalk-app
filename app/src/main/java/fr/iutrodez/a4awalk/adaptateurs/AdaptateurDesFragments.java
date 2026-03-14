package fr.iutrodez.a4awalk.adaptateurs;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import fr.iutrodez.a4awalk.fragments.FragmentListeParcours;
import fr.iutrodez.a4awalk.fragments.FragmentListeRandonnees;
import fr.iutrodez.a4awalk.modeles.entites.User;

public class AdaptateurDesFragments extends FragmentStateAdapter {
    private static final int NB_FRAGMENT = 2;
    private User user; // Stockage local du user

    public AdaptateurDesFragments(FragmentActivity activite, User user) {
        super(activite);
        this.user = user;
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return FragmentListeRandonnees.newInstance(user);
            case 1:
                return FragmentListeParcours.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return NB_FRAGMENT;
    }
}