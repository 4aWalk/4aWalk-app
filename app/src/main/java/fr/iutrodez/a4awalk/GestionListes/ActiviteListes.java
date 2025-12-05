package fr.iutrodez.a4awalk.GestionListes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import fr.iutrodez.a4awalk.R;

public class ActiviteListes extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_listes);

        ViewPager2 pager = findViewById(R.id.activity_main_viewpager);
        /*
         * on associe au ViewPager un adaptateur (c'est lui qui organise le
         * défilement entre les fragments à afficher)
         * La classe AdaptateurPage a été codée par le développeur (elle hérite de
         * FragmentStateAdapter)
         */
        pager.setAdapter(new AdaptateurDesFragments(this));
    }
}