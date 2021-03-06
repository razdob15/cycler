package razdob.cycler.myUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import razdob.cycler.R;
import razdob.cycler.algorithms.MyAlgorithm;
import razdob.cycler.models.PlaceDetails;

/**
 * Created by Raz on 07/05/2018, for project: PlacePicker2
 */
public class RazUtils {
    private static final String TAG = "RazUtils";

    /* -----------------------------------------------------------------------
    --------------------------- Distance Functions ---------------------------
    ------------------------------------------------------------------------- */

    /**
     * Returns the distance in km as double
     *
     * @param latLng1 First location
     * @param latLng2 Second location
     * @return Distance between the two locations in km.
     */
    public static double getDistance(LatLng latLng1, LatLng latLng2){
        double lat1 = latLng1.latitude, lat2 = latLng2.latitude;
        double lng1 = latLng1.longitude, lng2 = latLng2.longitude;
        double earthRadius = 3958.75;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    /**
     * Gets a distance in km and returns a representing String with the correct units of measurement
     *
     * @param distance In km
     * @return distance + "m"    OR     distance + "km"
     */
    public static String getDistanceString(double distance) {
        if (distance > 1)
            return String.valueOf(distance) + "km";
        distance *= 1000;
        if (distance > 1)
            return String.valueOf((int) distance) + "m";
        else
            return "very close";
    }

    /* -----------------------------------------------------------------------
    --------------------------- Place Functions ------------------------------
    ------------------------------------------------------------------------- */

    /**
     * Checks if the placeTypes contains a type which is connected to food.
     *
     * @param placeTypes some Place's types
     * @return True if at least one of the types is connected to food. Otherwise-False.
     */
    public static boolean isRestaurant(List<Integer> placeTypes) {
        for (int type : placeTypes) {
            if (type == Place.TYPE_RESTAURANT || type == Place.TYPE_FOOD || type == Place.TYPE_CAFE ||
                    type == Place.TYPE_BAKERY || type == Place.TYPE_BAR || type == Place.TYPE_CASINO ||
                    type == Place.TYPE_CONVENIENCE_STORE || type == Place.TYPE_DEPARTMENT_STORE || type == Place.TYPE_FUNERAL_HOME ||
                    type == Place.TYPE_LIQUOR_STORE || type == Place.TYPE_MEAL_DELIVERY || type == Place.TYPE_MEAL_TAKEAWAY ||
                    type == Place.TYPE_NIGHT_CLUB || type == Place.TYPE_SHOPPING_MALL || type == Place.TYPE_GROCERY_OR_SUPERMARKET ||
                    type == Place.TYPE_HEALTH) {
                return true;
            }
        }


        return false;
    }


    public static ArrayList<String> filterPlaces(MyAlgorithm algorithm, ArrayList<String> placesIds, ArrayList<String> userSubjects) {
        // TODO(!): Check this function !
        // Places Tags DataSnapshot
        DataSnapshot ptDS = algorithm.getmMainDS().child(algorithm.getContext().getString(R.string.db_field_places_tag_counters));
        ArrayList<String> result = new ArrayList<>();
        for (String pid : placesIds) {  // Nearby Places
            if (ptDS.child(pid).getChildrenCount() < userSubjects.size()) { // Go for the DS
                for (DataSnapshot pTagDS : ptDS.child(pid).getChildren()) {
                    String tag = pTagDS.getKey();
                    if (userSubjects.contains(tag)) {
                        result.add(tag);
                        break;
                    }
                }
            } else { // Go for the subjects
                for (String sub : userSubjects) {
                    if (ptDS.hasChild(sub)) {
                        result.add(sub);
                        break;
                    }
                }

            }
        }
        return result;
    }

    /**
     * TODO(1): Move this method to MyAlgorithm Class !
     *
     * @param placesIds - List of places Ids.
     * @param algorithm - My Algorithm object, to calculate the places' scores
     * @return List of places Ids sorting by their scores.
     */
    public static List<String> sortPlaceIdsByGraphScore(ArrayList<String> placesIds, MyAlgorithm algorithm, Context context) {
        if (placesIds == null || placesIds.size() == 0)
            return placesIds;
        Log.d(TAG, "sortPlaceIdsByGraphScore: placesCount: " + placesIds.size());
        @SuppressLint("UseSparseArrays")
        HashMap<Integer, ArrayList<String>> scoresHM = new HashMap<>();
        // Create HashMap
        for (String placeId : placesIds) {
            MyAlgorithm tempAlgorithm = new MyAlgorithm(algorithm, context);
            tempAlgorithm.setPlaceId(placeId);
            int score = tempAlgorithm.getPlaceScore();
            Log.d(TAG, "sortPlaceIdsByGraphScore: place: " + placeId + " has score: " + score);
            if (!scoresHM.containsKey(score))
                scoresHM.put(score, new ArrayList<String>());
            scoresHM.get(score).add(placeId);
        }

        Log.d(TAG, "sortPlaceIdsByGraphScore: scoresHM: " + scoresHM.keySet().toString());
        int[] sortScores = sortSet(scoresHM.keySet());
        Log.d(TAG, "sortPlaceIdsByGraphScore: sortScores: " + Arrays.toString(sortScores));
        List<String> res = new ArrayList<>();
        for (Integer sc : sortScores) {
            res.addAll(scoresHM.get(sc));
        }

        Collections.reverse(res);
        return res;
    }

    private static int[] sortSet(Set<Integer> set) {
        int[] arr = new int[set.size()];
        int i = 0;
        for (Integer integer : set) {
            arr[i] = integer;
            i++;
        }
        Arrays.sort(arr);
        return arr;
    }


    /**
     * Open a web url in a browser.
     *
     * @param context - the Context that the link will open from.
     * @param urlLink - The link.
     */
    public static void openUrl(Context context, String urlLink) {

        if ((urlLink.length() < 8) || (!urlLink.substring(0, 7).equals("http://") && !urlLink.substring(0, 8).equals("https://"))) {
            Log.d(TAG, "openUrl: web address is not valid: " + urlLink);
            Toast.makeText(context, "web address is not valid", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "openUrl: opening url: " + urlLink);
            Uri uri = Uri.parse(urlLink);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        }
    }

    public static PlaceDetails getPlaceDetails(Task<PlaceBufferResponse> task, String placeId) {
        PlaceDetails placeDetails = new PlaceDetails(placeId);
        if (task.isSuccessful()) {
            PlaceBufferResponse places = task.getResult();
            if (places != null) {
                Place place = places.get(0);

                Log.d(TAG, "onComplete: place: " + place.getName());

                // Setting up placeDetails object
                if (place.getName() != null)
                    placeDetails.setName(place.getName().toString());
                if (place.getAddress() != null)
                    placeDetails.setAddress(place.getAddress().toString());
                if (place.getPhoneNumber() != null)
                    placeDetails.setPhone(place.getPhoneNumber().toString());
                if (place.getWebsiteUri() != null)
                    placeDetails.setWebsite(place.getWebsiteUri().toString());
            }
        }
        return placeDetails;
    }

    public static Task<PlacePhotoResponse> getPlaceOnePhotoTask(GeoDataClient geoDataClient, Task<PlacePhotoMetadataResponse> task,
                                                                boolean isRandom) {
        // Get the list of photos.
        PlacePhotoMetadataResponse photos = task.getResult();
        // Get the PlacePhotoMetadataBuffer (metadata for the first 10 photos).
        PlacePhotoMetadata photoMetadata;
        if (photos != null) {
            PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
            if (photoMetadataBuffer == null || photoMetadataBuffer.getCount() == 0)
                return null;
            if (isRandom) {
                Random random = new Random();
                int i = random.nextInt(photoMetadataBuffer.getCount());
                photoMetadata = photoMetadataBuffer.get(i);  // Photo in index i
            } else {
                photoMetadata = photoMetadataBuffer.get(0);  // Photo in index 0
            }
            return geoDataClient.getPhoto(photoMetadata);

        }
        return null;
    }


}
