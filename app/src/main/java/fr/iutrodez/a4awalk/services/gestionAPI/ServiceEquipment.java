package fr.iutrodez.a4awalk.services.gestionAPI;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.iutrodez.a4awalk.modeles.entites.EquipmentItem;
import fr.iutrodez.a4awalk.modeles.enums.TypeEquipment;
import fr.iutrodez.a4awalk.services.AppelAPI;

/**
 * Service gérant les opérations CRUD sur les équipements.
 * <p>
 * Fournit des méthodes pour lier ou retirer un équipement d'une randonnée,
 * récupérer le catalogue complet, créer un nouvel équipement et synchroniser
 * deux listes d'équipements via des appels API séquentiels.
 */
public class ServiceEquipment {

    /** URL de base de l'endpoint équipements. */
    private static final String URL_EQUIPMENTS = "http://98.94.8.220:8080/equipments";

    /**
     * Lie un équipement existant à une randonnée via un appel POST.
     * <p>
     * Si {@code ownerId} est renseigné, il est ajouté en query parameter
     * pour indiquer le propriétaire de l'équipement.
     *
     * @param context   Contexte Android.
     * @param token     Token JWT d'authentification.
     * @param hikeId    Identifiant de la randonnée cible.
     * @param equipId   Identifiant de l'équipement à lier.
     * @param ownerId   Identifiant du propriétaire (peut être {@code null}).
     * @param callback  Callback Volley pour traiter la réponse ou l'erreur.
     */
    public static void lierEquipmentARandonnee(Context context, String token, int hikeId,
                                               int equipId, Integer ownerId,
                                               AppelAPI.VolleyObjectCallback callback) {
        String url = "http://98.94.8.220:8080/hikes/" + hikeId + "/equipment/" + equipId;

        // Ajout du query parameter owner si fourni
        if (ownerId != null) {
            url += "?owner=" + ownerId;
        }

        AppelAPI.post(url, token, null, context, callback);
    }

    /**
     * Retire un équipement d'une randonnée via un appel DELETE.
     *
     * @param context  Contexte Android.
     * @param token    Token JWT d'authentification.
     * @param hikeId   Identifiant de la randonnée cible.
     * @param equipId  Identifiant de l'équipement à retirer.
     * @param callback Callback Volley pour traiter la réponse ou l'erreur.
     */
    public static void retirerEquipmentDeRandonnee(Context context, String token, int hikeId,
                                                   int equipId,
                                                   AppelAPI.VolleyObjectCallback callback) {
        String url = "http://98.94.8.220:8080/hikes/" + hikeId + "/equipment/" + equipId;
        AppelAPI.delete(url, token, context, callback);
    }

    /**
     * Parse les équipements regroupés par catégorie depuis un objet JSON.
     * <p>
     * Attend une structure {@code equipmentGroups -> catégorie -> items[]}.
     *
     * @param response L'objet JSON contenant la clé {@code equipmentGroups}.
     * @return Liste des {@link EquipmentItem} extraits, vide si aucun trouvé.
     */
    public static List<EquipmentItem> extractEquipmentGroups(JSONObject response) {
        List<EquipmentItem> equipList = new ArrayList<>();
        JSONObject groupsJson = response.optJSONObject("equipmentGroups");

        if (groupsJson != null) {
            try {
                // Itération sur chaque catégorie d'équipements
                Iterator<String> keys = groupsJson.keys();
                while (keys.hasNext()) {
                    String categoryKey = keys.next();
                    JSONObject categoryObj = groupsJson.optJSONObject(categoryKey);
                    if (categoryObj != null) {
                        JSONArray itemsArray = categoryObj.optJSONArray("items");
                        if (itemsArray != null) {
                            for (int i = 0; i < itemsArray.length(); i++) {
                                JSONObject e = itemsArray.getJSONObject(i);
                                equipList.add(constructEqFromJson(e));
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e("ServiceEquipment", "Erreur parsing des équipements : " + e.getMessage());
            }
        }
        return equipList;
    }

    /**
     * Alias de {@link #extractEquipmentGroups(JSONObject)} maintenu
     * pour la compatibilité avec {@code ServiceRandonnee}.
     *
     * @param response L'objet JSON source.
     * @return Liste des {@link EquipmentItem} extraits.
     */
    public static List<EquipmentItem> extractEquipmentCatalogue(JSONObject response) {
        return extractEquipmentGroups(response);
    }

    /**
     * Construit un objet {@link EquipmentItem} depuis un objet JSON.
     * <p>
     * Le champ {@code type} est converti en {@link TypeEquipment} ; si la valeur
     * est inconnue, {@link TypeEquipment#AUTRE} est utilisé par sécurité.
     *
     * @param obj L'objet JSON représentant un équipement.
     * @return Un {@link EquipmentItem} renseigné.
     * @throws JSONException En cas d'erreur d'accès au JSON.
     */
    public static EquipmentItem constructEqFromJson(JSONObject obj) throws JSONException {
        EquipmentItem item = new EquipmentItem();
        item.setId(obj.optInt("id", 0));
        item.setNom(obj.optString("nom", ""));
        item.setDescription(obj.optString("description", ""));
        item.setMasseGrammes(obj.optDouble("masseGrammes", 0.0));
        item.setNbItem(obj.optInt("nbItem", 1));
        item.setMasseAVide(obj.optDouble("masseAVide", 0.0));

        // Le champ ownerId est optionnel ; il peut être null côté API
        if (obj.has("ownerId") && !obj.isNull("ownerId")) {
            item.setOwnerId(obj.getInt("ownerId"));
        } else {
            item.setOwnerId(null);
        }

        // Conversion sécurisée de la chaîne vers l'enum TypeEquipment
        try {
            item.setType(TypeEquipment.valueOf(obj.optString("type", "AUTRE")));
        } catch (IllegalArgumentException ex) {
            item.setType(TypeEquipment.AUTRE);
        }
        return item;
    }

    /**
     * Récupère l'intégralité du catalogue d'équipements depuis l'API.
     *
     * @param context  Contexte Android.
     * @param token    Token JWT d'authentification.
     * @param callback Callback Volley pour traiter la réponse brute.
     */
    public static void getAllEquipments(Context context, String token,
                                        AppelAPI.VolleyCallback callback) {
        AppelAPI.get(URL_EQUIPMENTS, token, context, callback);
    }

    /**
     * Crée un nouvel équipement via un appel POST à l'API.
     *
     * @param context     Contexte Android.
     * @param token       Token JWT d'authentification.
     * @param nom         Nom de l'équipement.
     * @param masse       Masse en grammes.
     * @param description Description textuelle.
     * @param type        Catégorie de l'équipement ({@link TypeEquipment}).
     * @param masseAVide  Masse à vide (contenant sans contenu) en grammes.
     * @param nbItem      Nombre d'unités.
     * @param callback    Callback Volley pour traiter la réponse ou l'erreur.
     */
    public static void creerNouveauEquipement(Context context, String token, String nom,
                                              double masse, String description, TypeEquipment type,
                                              double masseAVide, int nbItem,
                                              AppelAPI.VolleyObjectCallback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("nom", nom);
            body.put("masseGrammes", masse);
            body.put("description", description);
            body.put("type", type.name());
            body.put("masseAVide", masseAVide);
            body.put("nbItem", nbItem);
        } catch (JSONException e) {
            Log.e("ServiceEquipment", "Erreur JSON création équipement", e);
        }
        AppelAPI.post(URL_EQUIPMENTS, token, body, context, callback);
    }

    /**
     * Synchronise la liste des équipements d'une randonnée avec l'API.
     * <p>
     * Compare {@code listeInitiale} et {@code listeModifiee} pour déterminer
     * les équipements à ajouter (présents dans modifiée mais pas dans initiale)
     * et ceux à supprimer (présents dans initiale mais pas dans modifiée).
     * Les ajouts sont effectués en premier, séquentiellement, puis les suppressions.
     *
     * @param context        Contexte Android.
     * @param token          Token JWT d'authentification.
     * @param hikeId         Identifiant de la randonnée concernée.
     * @param listeInitiale  État d'origine des équipements liés à la randonnée.
     * @param listeModifiee  Nouvel état souhaité des équipements.
     */
    public static void synchroniserEquipments(Context context, String token, int hikeId,
                                              List<EquipmentItem> listeInitiale,
                                              List<EquipmentItem> listeModifiee) {

        // 1. Détermination des équipements à AJOUTER (nouveaux dans listeModifiee)
        List<EquipmentItem> aAjouter = new ArrayList<>();
        for (EquipmentItem temp : listeModifiee) {
            boolean existeDeja = false;
            for (EquipmentItem orig : listeInitiale) {
                if (temp.getId() == orig.getId()) {
                    existeDeja = true;
                    break;
                }
            }
            if (!existeDeja) aAjouter.add(temp);
        }

        // 2. Détermination des équipements à SUPPRIMER (absents de listeModifiee)
        List<EquipmentItem> aSupprimer = new ArrayList<>();
        for (EquipmentItem orig : listeInitiale) {
            boolean estConserve = false;
            for (EquipmentItem temp : listeModifiee) {
                if (orig.getId() == temp.getId()) {
                    estConserve = true;
                    break;
                }
            }
            if (!estConserve) aSupprimer.add(orig);
        }

        // 3. Exécution séquentielle : ajouts d'abord, suppressions ensuite
        ajouterSequentiellement(context, token, hikeId, aAjouter, 0, () ->
                supprimerSequentiellement(context, token, hikeId, aSupprimer, 0)
        );
    }

    /**
     * Lie les équipements de {@code liste} à la randonnée un par un, de manière
     * séquentielle (chaque appel API attend la réponse du précédent).
     * <p>
     * En cas d'erreur sur un équipement, la liaison continue avec le suivant
     * pour ne pas bloquer toute la file.
     *
     * @param context    Contexte Android.
     * @param token      Token JWT d'authentification.
     * @param hikeId     Identifiant de la randonnée cible.
     * @param liste      Liste des équipements à lier.
     * @param index      Indice courant dans la liste (utiliser 0 pour démarrer).
     * @param onTermine  Runnable exécuté une fois tous les ajouts traités.
     */
    private static void ajouterSequentiellement(Context context, String token, int hikeId,
                                                List<EquipmentItem> liste, int index,
                                                Runnable onTermine) {
        // Condition d'arrêt : tous les équipements ont été traités
        if (index >= liste.size()) {
            if (onTermine != null) onTermine.run();
            return;
        }

        EquipmentItem eq = liste.get(index);
        lierEquipmentARandonnee(context, token, hikeId, eq.getId(), eq.getOwnerId(),
                new AppelAPI.VolleyObjectCallback() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        Log.i("ServiceEquipment", "Équipement " + eq.getId() + " lié avec succès.");
                        // Passage à l'équipement suivant après succès
                        ajouterSequentiellement(context, token, hikeId, liste, index + 1, onTermine);
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Log.e("ServiceEquipment", "Erreur liaison équipement " + eq.getId());
                        // On continue malgré l'erreur pour ne pas bloquer les suivants
                        ajouterSequentiellement(context, token, hikeId, liste, index + 1, onTermine);
                    }
                }
        );
    }

    /**
     * Retire les équipements de {@code liste} de la randonnée un par un, de manière
     * séquentielle (chaque appel API attend la réponse du précédent).
     * <p>
     * Un code HTTP 204 est traité comme un succès (suppression effectuée sans contenu).
     * En cas d'autre erreur, la suppression continue avec l'équipement suivant.
     *
     * @param context  Contexte Android.
     * @param token    Token JWT d'authentification.
     * @param hikeId   Identifiant de la randonnée cible.
     * @param liste    Liste des équipements à retirer.
     * @param index    Indice courant dans la liste (utiliser 0 pour démarrer).
     */
    private static void supprimerSequentiellement(Context context, String token, int hikeId,
                                                  List<EquipmentItem> liste, int index) {
        // Condition d'arrêt : tous les équipements ont été traités
        if (index >= liste.size()) return;

        EquipmentItem eq = liste.get(index);
        retirerEquipmentDeRandonnee(context, token, hikeId, eq.getId(),
                new AppelAPI.VolleyObjectCallback() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        Log.i("ServiceEquipment", "Équipement " + eq.getId() + " retiré avec succès.");
                        supprimerSequentiellement(context, token, hikeId, liste, index + 1);
                    }

                    @Override
                    public void onError(VolleyError error) {
                        // Le code 204 signifie que la suppression s'est bien effectuée
                        if (error.networkResponse != null && error.networkResponse.statusCode == 204) {
                            Log.i("ServiceEquipment", "Équipement " + eq.getId() + " retiré (204).");
                        } else {
                            Log.e("ServiceEquipment", "Erreur retrait équipement " + eq.getId());
                        }
                        // On continue avec l'équipement suivant dans tous les cas
                        supprimerSequentiellement(context, token, hikeId, liste, index + 1);
                    }
                }
        );
    }

    /**
     * Extrait les équipements du sac à dos d'un participant depuis un tableau JSON.
     * <p>
     * Retourne un {@link java.util.Set} pour éviter les doublons.
     *
     * @param equipementsJson Tableau JSON contenant les équipements du participant.
     * @return Ensemble des {@link EquipmentItem} extraits, vide si le tableau est null.
     */
    public static java.util.Set<EquipmentItem> extractEquipmentsForBackpack(JSONArray equipementsJson) {
        java.util.Set<EquipmentItem> equipements = new java.util.HashSet<>();
        if (equipementsJson != null) {
            for (int j = 0; j < equipementsJson.length(); j++) {
                JSONObject eqJson = equipementsJson.optJSONObject(j);
                if (eqJson != null) {
                    EquipmentItem item = new EquipmentItem();
                    item.setId(eqJson.optInt("id"));
                    item.setNom(eqJson.optString("nom"));
                    item.setMasseGrammes(eqJson.optDouble("masseGrammes", 0.0));
                    item.setNbItem(eqJson.optInt("nbItem", 1));
                    equipements.add(item);
                }
            }
        }
        return equipements;
    }
}