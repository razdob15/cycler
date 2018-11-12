package razdob.cycler.feed;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

import razdob.cycler.ChooseCurrentSubjectsActivity;
import razdob.cycler.MainRegisterActivity;
import razdob.cycler.R;
import razdob.cycler.adapters.PlaceListAdapter;
import razdob.cycler.algorithms.MyAlgorithm;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.Permissions;
import razdob.cycler.myUtils.RazUtils;

/**
 * Created by Raz on 09/08/2018, for project: PlacePicker2
 */
    public class NearbyPlacesFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = "NearbyPlacesFragment";
    private static String CURRENT_SUBJECT_BUNDLE = "chosen_current_subjects";
    private Context mContext;

    // Constants
    private static final String WEB_API_KEY = "AIzaSyD1C5oAtbT4zVzwQlhdV9yq3SXPxlgxoEU";
    private static final String[] RADIUS_ARR = {"500", "1000", "2000"};
    private final int LOCATION_PERMISSION_CODE = 8;

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                Log.d(TAG, "doInBackground: url: " + url);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder buffer = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
                }

                return buffer.toString();

                // Checkers to disconnect...
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, "onPostExecute: result: " + result);
            if (needWaitToSubjects) return;
            try {
                JSONObject mainJson = new JSONObject(result);
                JSONArray placesArrayJson = (JSONArray) mainJson.get("results");

                opens = new ArrayList<>();

                int placesCount = placesArrayJson.length();
                if (placesCount < 1 && mRadius < RADIUS_ARR.length - 1) {
                    Log.d(TAG, "onPostExecute: if(1) placesCount: " + placesCount);
                    Log.d(TAG, "onPostExecute: if(2) RADIUS_ARR[mRadius]:  " + RADIUS_ARR[mRadius] + "km");
                    mRadius++;
                    new NearbyPlacesFragment.JsonTask().execute(googlePlacesUrl(RADIUS_ARR[mRadius]));
                    return;
                }
                // [ELSE]
                ArrayList<String> allNearbyPlaces = new ArrayList<>();
                ArrayList<Boolean> all_opens = new ArrayList<>();
                Log.d(TAG, "onPostExecute: current_subjects: " + currentSubjects);
                Log.d(TAG, "onPostExecute: mPlacesInSubjects: " + mPlacesInSubjects);
                Log.d(TAG, "onPostExecute:  mPlacesIds: " + mPlacesIds);
                for (int i = 0; i < placesCount; i++) {

                    JSONObject placeJson = (JSONObject) placesArrayJson.get(i);
                    String pid = placeJson.getString("place_id");
                    all_opens.add(isPlaceOpenNow(placeJson));
                    allNearbyPlaces.add(pid);

                    if (!mPlacesIds.contains(pid) && mPlacesInSubjects.contains(pid)) {
                        Log.d(TAG, "onPostExecute: add place: " + pid);
                        mPlacesIds.add(pid);
                        opens.add(isPlaceOpenNow(placeJson));
                    }

                    // Check sort by distance:
                    JSONObject placeGeo = (JSONObject) placeJson.get("geometry");
                    JSONObject placeLocation = (JSONObject) placeGeo.get("location");
                    LatLng placeLatLng = new LatLng(placeLocation.getDouble("lat"),
                            placeLocation.getDouble("lng"));
                    LatLng myLatLng = new LatLng(mLoc.getLatitude(), mLoc.getLongitude());
                }

                if (mPlacesIds.size() == 0) {
                    Log.d(TAG, "onPostExecute: no matching places - show all...");
                    mPlacesIds = allNearbyPlaces;
                    opens = all_opens;
                    if (currentSubjects != null && currentSubjects.size() > 0)
                        Toast.makeText(mContext, "no matching places", Toast.LENGTH_SHORT).show();
                }

                setupPlacesAdapter();

            } catch (Throwable t) {
                Log.e("jsonError", "Could not parse malformed JSON: \"" + result + "\"");
                Log.e(TAG, "onPostExecute: throwMessage: " + t.getMessage());
            }
        }
    }


    /*
     **************************** GOOGLE API CLIENT INTERFACE ********************************
     * */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(
                mContext, Permissions.ACCESS_FINE_LOCATION[0]) ==
                PackageManager.PERMISSION_GRANTED) {
            mLoc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLoc != null) {
                // Find places nearby current latLng
                new NearbyPlacesFragment.JsonTask().execute(googlePlacesUrl(RADIUS_ARR[0]));
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    }

    /*
     **************************** GOOGLE API CLIENT INTERFACE ********************************
     * */


    // Google Places staff
    private GoogleApiClient mGoogleApiClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private GeoDataClient mGeoDataClient;

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    /* ---------------------- FIREBASE ----------------------- */

    // Vars
    private ArrayList<String> mPlacesIds;
    private ArrayList<String> currentSubjects;
    private ArrayList<String> mFavoritePlacesIds;
    private ArrayList<String> mPlacesInSubjects;
    private ArrayList<Boolean> opens;
    private Location mLoc;
    private MyAlgorithm mAlgorithm;
    private FirebaseMethods mFireMethods;
    private PlaceListAdapter mAdapter;
    private int mRadius = 0;
    private boolean needWaitToSubjects = true;

    // Widgets
    private RecyclerView recyclerView;
    private CoordinatorLayout rootLayout;
    private ProgressBar mProgressBar;
    private ImageView chooseCurrentSubjectsIV;
    private TextView noPlacesTV;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby_places, container, false);
        mContext = getActivity();
        mFireMethods = new FirebaseMethods(mContext);

        setupWidgets(view);

        mPlacesIds = new ArrayList<>();
        mPlacesInSubjects = new ArrayList<>();
        mFavoritePlacesIds = new ArrayList<>();

        getCurrentSubjectsFromBundle();

        // Setups
        setupFirebaseStaff();
        setupGeoClient();

        return view;
    }

    private void getCurrentSubjectsFromBundle() {
        Bundle args = getArguments();
        if (args != null) {
            currentSubjects = getArguments().getStringArrayList(mContext.getString(R.string.intent_current_subjects));
            Log.d(TAG, "getCurrentSubjectsFromBundle: current_subjects:  " + currentSubjects);
            needWaitToSubjects = (currentSubjects != null && currentSubjects.size() > 0);
        }
    }

    public static NearbyPlacesFragment show(ArrayList<String> currentSubjects) {
        NearbyPlacesFragment fragment = new NearbyPlacesFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(CURRENT_SUBJECT_BUNDLE, currentSubjects);
        fragment.setArguments(bundle);
        return fragment;

    }


    /**
     * Matches the widgets to their IPs
     * sets click listener to chooseCurrentSubjectsIV
     *
     * @param view - The current main view
     */
    private void setupWidgets(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        rootLayout = view.findViewById(R.id.root_layout);
        mProgressBar = view.findViewById(R.id.progress_bar);
        chooseCurrentSubjectsIV = view.findViewById(R.id.choose_subjects_iv);
        noPlacesTV = view.findViewById(R.id.no_places_tv);

        chooseCurrentSubjectsIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChooseCurrentSubjectsActivity.class);
                if (currentSubjects != null)
                    intent.putStringArrayListExtra(mContext.getString(R.string.intent_current_subjects), currentSubjects);
                startActivity(intent);
            }
        });

    }

    /**
     * Sets up the the Google Maps & Google Places API clients.
     */
    private void setupGeoClient() {
//        mPlaceDetectionClient = Places.getPlaceDetectionClient(Objects.requireNonNull(getActivity()), null);
        mGeoDataClient = Places.getGeoDataClient(getActivity(), null);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();


        ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION);
    }


    /**
     * Create the url link to Google Places data.
     *
     * @param radius - Places radius from the user.
     * @return - URL request from Google Places.
     */
    private String googlePlacesUrl(String radius) {
        if (mLoc == null) return null;  // Input check.

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + mLoc.getLatitude();
        url += "," + mLoc.getLongitude();
        url += "&radius=" + radius;
        url += "&type=restaurant";
        url += "&key=" + WEB_API_KEY;

        return url;
    }

    private void setupPlacesAdapter() {
        Log.d(TAG, "setupPlacesAdapter: called.");
        if (mAlgorithm == null) {
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (mAlgorithm == null)
                        mAlgorithm = new MyAlgorithm(mContext, dataSnapshot, FirebaseAuth.getInstance(mFireApp).getCurrentUser().getUid());
                    if (currentSubjects != null) {
                        Log.d(TAG, "getCurrentSubjectsFromBundle: filtered places: " + RazUtils.filterPlaces(mAlgorithm, mPlacesIds, currentSubjects).toString());
                    }

                    mPlacesIds = (ArrayList<String>) mAlgorithm.sortPlaceIdsByGraphScore(mPlacesIds, mContext);

                    mAdapter = new PlaceListAdapter(mContext, mPlacesIds, mFavoritePlacesIds, mContext.getString(R.string.data_activity), opens);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(mAdapter);
                    mProgressBar.setVisibility(View.GONE);

                    Log.d(TAG, "setupPlacesAdapter: end...");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "onCancelled: DBError: " + databaseError.getMessage());
                }
            });


        } else {
            mPlacesIds = (ArrayList<String>) mAlgorithm.sortPlaceIdsByGraphScore(mPlacesIds, mContext);

            mAdapter = new PlaceListAdapter(mContext, mPlacesIds, mFavoritePlacesIds, mContext.getString(R.string.data_activity), opens);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);
            mProgressBar.setVisibility(View.GONE);

            Log.d(TAG, "setupPlacesAdapter: end...");
        }

    }


    /**
     * check if place is open according Google Places' data.
     *
     * @param placeJson - PlaceJson (in google places).
     * @return true / false (we have data) OR null (not know weather the place is open or not).
     */
    private Boolean isPlaceOpenNow(JSONObject placeJson) {
        if (placeJson.has("opening_hours")) {
            try {
                JSONObject opening_hoursObj = (JSONObject) placeJson.get("opening_hours");
                if (opening_hoursObj.has("open_now")) {
                    Log.d(TAG, "isPlaceOpenNow: open place? " + opening_hoursObj.get("open_now"));
                    return (boolean) opening_hoursObj.get("open_now");
                }
            } catch (JSONException e) {
                Log.d(TAG, "isPlaceOpenNow: jsonError");
                e.printStackTrace();
            }
        }
        return null;
    }


    /*
     * ------------------------- Firebase ------------------------------------------
     */
    @Override
    public void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mAuthListener != null) mAuth.removeAuthStateListener(mAuthListener);
    }


    private void setupFirebaseStaff() {
        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();

        // Init mAuthListener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                // Check if user is logged in
                if (user == null) {
                    Log.d(TAG, "onAuthStateChanged: User log-out");
                    startActivity(new Intent(mContext, MainRegisterActivity.class));
                } else {
                    Log.d(TAG, "onAuthStateChanged: User is logged-in :) uid = " + firebaseAuth.getCurrentUser().getUid());
                }
            }
        };
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Favorites places
                mFavoritePlacesIds = mFireMethods.getFavoritePlacesIds(dataSnapshot);
                if (mFavoritePlacesIds == null) mFavoritePlacesIds = new ArrayList<>();

                // My Algorithm
                if (mAlgorithm == null)
                    mAlgorithm = new MyAlgorithm(mContext, dataSnapshot, FirebaseAuth.getInstance(mFireApp).getCurrentUser().getUid());

                if (currentSubjects != null) {
                    mAlgorithm.setCurrentSubjects(currentSubjects);
                    setupPlacesInSubjects(dataSnapshot);
                }
                new NearbyPlacesFragment.JsonTask().execute(googlePlacesUrl(RADIUS_ARR[0]));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: firebaseDatabaseError: " + databaseError.getMessage());
            }
        });


    }

    private void setupPlacesInSubjects(DataSnapshot dataSnapshot) {
        mPlacesInSubjects = new ArrayList<>();

        for (String subject : currentSubjects) {
            Log.d(TAG, "onDataChange: current subject: " + subject);
            DataSnapshot subjectPlacesDS = dataSnapshot.child(mContext.getString(R.string.db_tag_places)).child(subject);
            for (DataSnapshot ds : subjectPlacesDS.getChildren()) { // Add Matching places
                Log.d(TAG, "onDataChange: found a matching place from DB: " + ds.getKey());
                mPlacesInSubjects.add(ds.getKey());
            }
        }
        needWaitToSubjects = false;
    }


}
