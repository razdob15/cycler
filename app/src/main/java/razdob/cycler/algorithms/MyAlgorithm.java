package razdob.cycler.algorithms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import razdob.cycler.R;

/**
 * Created by Raz on 21/07/2018, for project: PlacePicker2
 */
public class MyAlgorithm {
    private static final String TAG = "MyAlgorithm";

    private Context mContext;
    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private DatabaseReference mRef;
    private DataSnapshot mMainDS;
    /* ---------------------- FIREBASE ----------------------- */
    private String userId;
    private HashMap<String, Long> connectedUsers;  // Keys - Users IDs;      Values - Count places in common;
    private String placeId;

    private ArrayList<String> followings;
    private ArrayList<String> currentSubjects;

    // Remote Config - STATIC
    private FirebaseRemoteConfig FIREBASE_REMOTE_CONFIG;// = FirebaseRemoteConfig.getInstance();
    // -999 --> Not initialize yet.
    private static long FOLLOW_MUL = -999;    // if the user follows after a user who likes this place,
    // their connection will multiply by this variable and will be added to the placeScore.
    private static long USER_RATE_MUL = -999; // if the user rated this place, his rate will multiply
    // by this variable and will be added to the placeScore.
    private static long FAVORITE_ADD = -999;  // if this place is in the user's favorites,
    // its score will get this variable value.


    // Place Score !
    private int placeScore = 0;

    // --------------------------- Constructors ------------------------------------- //
    public MyAlgorithm(final Context mContext, DataSnapshot mainDS, final String userId, final HashMap<String, Long> connectedUsers, final String placeId) {
        this.mContext = mContext;
        this.mFireApp = FirebaseApp.getInstance("mFireApp");
        this.mMainDS = mainDS;
        this.userId = userId;
        this.connectedUsers = connectedUsers;
        this.placeId = placeId;
        this.mRef = FirebaseDatabase.getInstance(mFireApp).getReference();
        setupFollowings();
        calculatePlaceScore();
    }

    public MyAlgorithm(final Context context, final DataSnapshot mainDS, String userId) {
        this.mContext = context;
        this.mFireApp = FirebaseApp.getInstance("mFireApp");
        this.userId = userId;
        this.mMainDS = mainDS;
        this.mRef = FirebaseDatabase.getInstance(mFireApp).getReference();
        if (FOLLOW_MUL == -999 || USER_RATE_MUL == -999 || FAVORITE_ADD == -999)
            setupRemoteConfig(mContext, FIREBASE_REMOTE_CONFIG);
        setupFollowings();
        setupConnections(mainDS.child(mContext.getString(R.string.db_field_users_connections)));
    }

    public MyAlgorithm(MyAlgorithm algorithm, Context context) {
        this.mContext = context;
        this.mFireApp = FirebaseApp.getInstance("mFireApp");
        this.mMainDS = algorithm.mMainDS;
        this.userId = algorithm.userId;
        this.connectedUsers = algorithm.connectedUsers;
        this.placeId = algorithm.placeId;
        this.mRef = FirebaseDatabase.getInstance(mFireApp).getReference();
        if (algorithm.followings != null) this.followings = algorithm.followings;
        else setupFollowings();
        if (algorithm.connectedUsers != null) this.connectedUsers = algorithm.connectedUsers;
        else
            setupConnections(mMainDS.child(mContext.getString(R.string.db_field_users_connections)));
    }

    /* ------------------------------------------------------------------------------ //
    // ------------------------- Setups --------------------------------------------- //
    // ------------------------------------------------------------------------------ */

    private void setupFollowings() {
        Log.d(TAG, "setupFollowings: called");
        followings = new ArrayList<>();
        if (mMainDS == null) return;

        for (DataSnapshot followingDS : mMainDS.child(mContext.getString(R.string.db_field_following)).child(userId).getChildren()) {
            followings.add(followingDS.getKey());
        }
    }

    private void setupConnections(DataSnapshot connectionsDS) {
        connectedUsers = new HashMap<>();
        for (DataSnapshot connectedUserDS : connectionsDS.child(userId).getChildren()) {
            connectedUsers.put(connectedUserDS.getKey(), connectedUserDS.getValue(long.class));
        }
    }

    /**
     * initializes: FOLLOW_MUL, USER_RATE_MUL, FAVORITE_ADD according firebase RemoteConfig
     */
    private static void setupRemoteConfig(final Context context, final FirebaseRemoteConfig remoteConfig) {
        FOLLOW_MUL  = 3;
        USER_RATE_MUL = 5;
        FAVORITE_ADD = 10;

        if (FOLLOW_MUL == -999 || USER_RATE_MUL == -999 || FAVORITE_ADD == -999) {
            remoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(true).build());
            HashMap<String, Object> defaults = new HashMap<>();
            defaults.put("overlay_max", 5);
            remoteConfig.setDefaults(defaults);

            final Task<Void> fetch = remoteConfig.fetch(0);
            fetch.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "onSuccess: REMOTE CONFIG !");
                    remoteConfig.activateFetched();
                    FOLLOW_MUL = remoteConfig.getLong(context.getString(R.string.rc_follow_multiply));
                    FAVORITE_ADD = remoteConfig.getLong(context.getString(R.string.rc_favorite_adding));
                    USER_RATE_MUL = remoteConfig.getLong(context.getString(R.string.rc_current_user_rate));

                    Log.d(TAG, "onSuccess: followMul: " + FOLLOW_MUL);
                    Log.d(TAG, "onSuccess: favAdd: " + FAVORITE_ADD);
                    Log.d(TAG, "onSuccess: USER_RATE_MUL: " + USER_RATE_MUL);
                }
            });

        }
    }


    /* ------------------------------------------------------------------------------ //
    // ------------------------- Score Calculates ----------------------------------- //
    // ------------------------------------------------------------------------------ */

    /**
     * Main method to calculate the place's score.
     * uses thw two sub-methods (By graph & By currentSubjects [Tags])
     */
    private void calculatePlaceScore() {
        Log.d(TAG, "calculatePlaceScore: calculating place's score for place: " + placeId);
        calculatePlaceScoreByLikesGraph();
        calculatePlaceScoreByRates();
        calculatePlaceScoreBySubjects();

    }

    /**
     * if the user like this place:
     * Add to score 10.
     * else:
     * Calculates the place's score according the similariot users and followings.
     * *** Includes the calculate if the current user likes the place ***
     */
    private void calculatePlaceScoreByLikesGraph() {
        Log.d(TAG, "calculatePlaceScoreByLikesGraph: Place: " + placeId);
        Log.d(TAG, "calculatePlaceScoreByLikesGraph: Current Score: " + placeScore);

        DataSnapshot placeLikesDS = mMainDS.child(mContext.getString(R.string.db_field_places_likes)).child(placeId);

        for (DataSnapshot userThatLikeDS : placeLikesDS.getChildren()) {
            String userThatLikeID = userThatLikeDS.getKey();
            if (userThatLikeID.equals(userId)) { // Current user == userThatLike
                Log.d(TAG, "calculatePlaceScoreByLikesGraph: User likes the place. Add: " + FAVORITE_ADD);
                placeScore += FAVORITE_ADD;
            } else if (connectedUsers.containsKey(userThatLikeID)) {
                // There is a connection
                long followingMul = followings.contains(userThatLikeID) ? FOLLOW_MUL : 1;
                Log.d(TAG, "calculatePlaceScoreByLikesGraph: User is connected to user that like. followingMul: " + followingMul);
                placeScore += (connectedUsers.get(userThatLikeID)) * followingMul;
            }
        }
    }

    /**
     * Calculates the place's score according the rates that the users gave
     * to this place and their connection to the user - if he follows them or not?
     * <p>
     * for every user who rated this place, check:
     * if user == current user:     [important rate]
     * add to score: [(user's rate multiply - 3) * 10]
     * else if user is connected to the currentUSer:
     * {mul - the multiply coefficient}
     * if currentUser follows after user:
     * mul = 2
     * else:
     * mul = 1
     * add to score: [ (user's rate - 3) * mul ]
     */
    private void calculatePlaceScoreByRates() {
        Log.d(TAG, "calculatePlaceScoreByRates: Place: " + placeId);
        Log.d(TAG, "calculatePlaceScoreByRates: Current Score: " + placeScore);

        DataSnapshot placeRateDS = mMainDS.child(mContext.getString(R.string.db_places_rates)).child(placeId);

        for (DataSnapshot userRateDS : placeRateDS.getChildren()) {
            String ratingUserID = userRateDS.getKey();
            if (ratingUserID.equals(userId)) { // Current user == ratingUserID
                Integer rate = (userRateDS.getValue(Integer.class));
                if (rate != null) {
                    Log.d(TAG, "calculatePlaceScoreByRates: user rated this place with: " + rate + " stars");
                    Log.d(TAG, "calculatePlaceScoreByRates: USER_RATE_MUL: " + USER_RATE_MUL);
                    placeScore += (rate - 3) * USER_RATE_MUL;
                }
            } else // currentUser != ratingUserID
                if (connectedUsers.containsKey(ratingUserID)) { // User id connected to currentUser
                    Integer rate = (userRateDS.getValue(Integer.class));
                    long followingMul = followings.contains(ratingUserID) ? FOLLOW_MUL : 1;
                    if (rate != null) {
                        Log.d(TAG, "calculatePlaceScoreByRates: User is connected to user who rated. rate: " + rate);
                        Log.d(TAG, "calculatePlaceScoreByRates: followingMul: " + followingMul);
                        placeScore += (rate - 3) * followingMul * connectedUsers.get(ratingUserID); // can decrease.. hence the '-3'
                    }
                }
        }
    }

    /**
     * Calculates the score according the current subjects:
     * if the place has a tag with a current subject its (place's) score will be increased by the place's tag-score.
     */
    private void calculatePlaceScoreBySubjects() {
        Log.d(TAG, "calculatePlaceScoreBySubjects: Place: " + placeId);
        Log.d(TAG, "calculatePlaceScoreBySubjects: Current Score: " + placeScore);
        if (currentSubjects == null || placeId == null) return; // Input Check
        Log.d(TAG, "calculatePlaceScoreBySubjects: calculate the place score according its tags and user preferences (current subjects).");
        DataSnapshot placeTagsDS = mMainDS.child(mContext.getString(R.string.db_field_places_tag_counters)).child(placeId);
        if (placeTagsDS != null) {
            for (DataSnapshot tagDS : placeTagsDS.getChildren()) {
                if (currentSubjects.contains(tagDS.getKey())) {
                    Long addToScore = tagDS.getValue(Long.class);
                    if (addToScore != null) {
                        Log.d(TAG, "calculatePlaceScoreBySubjects: add to score: " + addToScore);
                        placeScore += (addToScore * -1);
                    }
                }
            }
        }
//
//        // Calculate by favorites !
//        DataSnapshot favoritesDS = mMainDS
//                .child(mContext.getString(R.string.db_persons))
//                .child(userId)
//                .child(mContext.getString(R.string.db_field_favorite_places_ids));
//        for (DataSnapshot favDS : favoritesDS.getChildren()) {
//            if (Objects.equals(favDS.getValue(String.class), placeId)) {
//                placeScore = Math.abs(placeScore) * 2 + 1;
//                break;
//            }
//        }


    }


    // ------------------------------------------------------------------------------ //
    // ----------------------- Sort & Filter Methods -------------------------------- //
    // ------------------------------------------------------------------------------ //

    /**
     * @param placesIds - List of places Ids.
     * @return List of places Ids sorting by their scores.
     */
    public List<String> sortPlaceIdsByGraphScore(ArrayList<String> placesIds, Context context) {
        if (placesIds == null || placesIds.size() == 0)
            return placesIds;
        Log.d(TAG, "sortPlaceIdsByGraphScore: placesCount: " + placesIds.size());
        @SuppressLint("UseSparseArrays")
        HashMap<Integer, ArrayList<String>> scoresHM = new HashMap<>();
        // Create HashMap
        for (String placeId : placesIds) {
            MyAlgorithm tempAlgorithm = new MyAlgorithm(this, context);
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

    private int[] sortSet(Set<Integer> set) {
        int[] arr = new int[set.size()];
        int i = 0;
        for (Integer integer : set) {
            arr[i] = integer;
            i++;
        }
        Arrays.sort(arr);
        return arr;
    }

    public ArrayList<String> filterPlaces(ArrayList<String> placesIds, ArrayList<String> userSubjects) {
        // Places Tags DataSnapshot
        DataSnapshot ptDS = mMainDS.child(mContext.getString(R.string.db_field_places_tag_counters));
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
                for (String sub: userSubjects) {
                    if (ptDS.hasChild(sub)) {
                        result.add(sub);
                        break;
                    }
                }

            }
        }
        return result;
    }

    // ------------------------------------------------------------------------------ //
    // --------------------------- Like methods ------------------------------------- //
    // ------------------------------------------------------------------------------ //
    // Like
    public void likePlaceGraph(String placeId) {
        Log.d(TAG, "likePlaceGraph: like place: " + placeId);

        this.placeId = placeId;
        for (DataSnapshot userThatLikeDS : mMainDS.child(mContext.getString(R.string.db_field_places_likes)).child(placeId).getChildren()) {
            String userThatLikeID = userThatLikeDS.getKey();
            if (userThatLikeID.equals(userId))
                continue;
            long counter = connectedUsers.containsKey(userThatLikeID) ? connectedUsers.get(userThatLikeID) : 0;
            mRef
                    .child(mContext.getString(R.string.db_field_users_connections))
                    .child(userId)
                    .child(userThatLikeID)
                    .setValue(counter + 1);
            mRef
                    .child(mContext.getString(R.string.db_field_users_connections))
                    .child(userThatLikeID)
                    .child(userId)
                    .setValue(counter + 1);

            connectedUsers.put(userThatLikeID, counter + 1);
        }
    }

    // Unlike
    public void unlikePlaceGraph(String placeId) {
        Log.d(TAG, "unlikePlaceGraph: unlike place: " + placeId);

        this.placeId = placeId;
        for (DataSnapshot userThatLikeDS : mMainDS.child(mContext.getString(R.string.db_field_places_likes)).child(placeId).getChildren()) {
            String userThatLikeID = userThatLikeDS.getKey();
            if (!userThatLikeID.equals(userId) && connectedUsers.containsKey(userThatLikeID) && connectedUsers.get(userThatLikeID) > 0) {
                long counter = connectedUsers.get(userThatLikeID);

                // TODO(!): Check the variable: newValue !!
                Long newValue = counter > 1 ? counter - 1 : null;
                mRef
                        .child(mContext.getString(R.string.db_field_users_connections))
                        .child(userId)
                        .child(userThatLikeID)
                        .setValue(newValue);
                mRef
                        .child(mContext.getString(R.string.db_field_users_connections))
                        .child(userThatLikeID)
                        .child(userId)
                        .setValue(newValue);
                if (connectedUsers.containsKey(userThatLikeID))
                    connectedUsers.remove(userThatLikeID);


//                if (counter > 1) {
//                    // Change Counter
//                    mRef
//                            .child(mContext.getString(R.string.db_field_users_connections))
//                            .child(userId)
//                            .child(userThatLikeID)
//                            .setValue(counter - 1);
//                    mRef
//                            .child(mContext.getString(R.string.db_field_users_connections))
//                            .child(userThatLikeID)
//                            .child(userId)
//                            .setValue(counter - 1);
//
//                    connectedUsers.put(userThatLikeID, counter - 1);
//                } else {
//                    // Delete Connection
//                    mRef
//                            .child(mContext.getString(R.string.db_field_users_connections))
//                            .child(userId)
//                            .child(userThatLikeID)
//                            .setValue(null);
//                    mRef
//                            .child(mContext.getString(R.string.db_field_users_connections))
//                            .child(userThatLikeID)
//                            .child(userId)
//                            .setValue(null);
//
//                    if (connectedUsers.containsKey(userThatLikeID))
//                        connectedUsers.remove(userThatLikeID);
//                }
            }
        }
    }






    /* -----------------------------------------------------------------------------------
     ---------------------------------- Getters & Setters --------------------------------
     ----------------------------------------------------------------------------------- */

    /**
     * Recalculates the place's score (for the new place)
     *
     * @param placeId - new PlaceID for MyAlgorithm.
     */
    public void setPlaceId(String placeId) {
        this.placeId = placeId;
        placeScore = 0;
        if (FOLLOW_MUL == -999 || USER_RATE_MUL == -999 || FAVORITE_ADD == -999) {
            setupRemoteConfig(mContext, FIREBASE_REMOTE_CONFIG);
        } else {
            calculatePlaceScore();
        }
    }

    /**
     * Recalculates the place's score according the new connections (GRAPH).
     *
     * @param connectedUsers - new Graph.
     */
    public void setConnectedUsers(HashMap<String, Long> connectedUsers) {
        this.connectedUsers = connectedUsers;
        calculatePlaceScoreByLikesGraph();
    }

    /**
     * Recalculates the place's score according the new current subjects (tags).
     *
     * @param currentSubjects - The new currentSubjects (TAGS).
     */
    public void setCurrentSubjects(ArrayList<String> currentSubjects) {
        this.currentSubjects = currentSubjects;
        calculatePlaceScoreBySubjects();
    }

    public void setmMainDS(DataSnapshot mMainDS) {
        this.mMainDS = mMainDS;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public DataSnapshot getmMainDS() {
        return mMainDS;
    }

    public String getUserId() {
        return userId;
    }

    public HashMap<String, Long> getConnectedUsers() {
        return connectedUsers;
    }

    public String getPlaceId() {
        return placeId;
    }

    public ArrayList<String> getCurrentSubjects() {
        return currentSubjects;
    }

    public Context getContext() {
        return mContext;
    }

    public int getPlaceScore() {
        return placeScore;
    }

    public void setPlaceScore(int placeScore) {
        this.placeScore = placeScore;
    }

}
