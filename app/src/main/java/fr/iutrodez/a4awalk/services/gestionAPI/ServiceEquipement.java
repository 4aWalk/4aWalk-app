package fr.iutrodez.a4awalk.services.gestionAPI;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import fr.iutrodez.a4awalk.modeles.entites.EquipmentItem;
import fr.iutrodez.a4awalk.modeles.enums.TypeEquipment;

public class ServiceEquipement {

    public static List<EquipmentItem> extractEquipmentGroups(JSONObject response) {
        List<EquipmentItem> equipList = new ArrayList<>();
        JSONObject groupsJson = response.optJSONObject("equipmentGroups");

        if (groupsJson != null) {
            try {
                Iterator<String> keys = groupsJson.keys();
                while (keys.hasNext()) {
                    String categoryKey = keys.next();
                    JSONObject categoryObj = groupsJson.optJSONObject(categoryKey);
                    if (categoryObj != null) {
                        JSONArray itemsArray = categoryObj.optJSONArray("items");
                        if (itemsArray != null) {
                            for (int i = 0; i < itemsArray.length(); i++) {
                                JSONObject e = itemsArray.getJSONObject(i);
                                EquipmentItem item = new EquipmentItem();
                                item.setId(e.getInt("id"));
                                item.setNom(e.getString("nom"));
                                item.setType(TypeEquipment.valueOf(e.optString("type", "AUTRE")));
                                item.setNbItem(e.optInt("nbItem", 0));
                                equipList.add(item);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e("ServiceEquipement", "Erreur parsing des équipements");
            }
        }
        return equipList;
    }
}