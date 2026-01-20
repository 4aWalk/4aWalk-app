package fr.iutrodez.a4awalk;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class PopupUtil {

    public interface PopupCallback {
        void onValidate();
    }

    public static void showDeletePopup(Activity activity, String message, String name, PopupCallback callback) {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.popup_supp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView messagePrevention = dialog.findViewById(R.id.messagePrevention);
        TextView tvRandonnee = dialog.findViewById(R.id.tv_randonnee);
        Button btnAnnuler = dialog.findViewById(R.id.btnAnnuler);
        Button btnSupprimer = dialog.findViewById(R.id.btnSupprimer);

        messagePrevention.setText(message);
        tvRandonnee.setText(name);

        btnAnnuler.setOnClickListener(v -> dialog.dismiss());

        btnSupprimer.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) {
                callback.onValidate();
            }
        });

        dialog.show();
    }
}


