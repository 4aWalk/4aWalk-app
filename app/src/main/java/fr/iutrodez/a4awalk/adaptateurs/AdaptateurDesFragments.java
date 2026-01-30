package fr.iutrodez.a4awalk.adaptateurs;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import fr.iutrodez.a4awalk.fragments.FragmentListeParcours;
import fr.iutrodez.a4awalk.fragments.FragmentListeRandonnees;

public class AdaptateurDesFragments extends FragmentStateAdapter {

    /**
     * Nombre de fragments gérés par cet adaptateur
     */
    private static final int NB_FRAGMENT = 2;

    /**
     * Constructeur de base
     *
     * @param activite activité qui contient le ViewPager qui gèrera les fragments
     */
    public AdaptateurDesFragments(FragmentActivity activite) {
        super(activite);
    }

    @Override
    public Fragment createFragment(int position) {
        /*
         * Le ViewPager auquel on associera cet adaptateur devra afficher
         * successivement un fragment de type : FragmentListeRandonnees et FragmentListeParcours.
         * C'est dans cette méthode que l'on décide dans quel
         * ordre sont affichés les fragments, et quel fragment (nom de la classe)
         * doit précisément être affiché
         */
        switch (position) {
            case 0:
                return FragmentListeRandonnees.newInstance();
            case 1:
                return FragmentListeParcours.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {

        // renvoyer le nombre de fragments gérés par l'adaptateur
        return NB_FRAGMENT;

    }
}
