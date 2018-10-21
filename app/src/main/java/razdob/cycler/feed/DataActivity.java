package razdob.cycler.feed;

import android.annotation.SuppressLint;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import razdob.cycler.ChooseCurrentSubjectsActivity;
import razdob.cycler.MainRegisterActivity;
import razdob.cycler.R;
import razdob.cycler.adapters.PlaceListAdapter;
import razdob.cycler.algorithms.MyAlgorithm;
import razdob.cycler.myUtils.BottomNavigationViewHelper;
import razdob.cycler.myUtils.Permissions;
import razdob.cycler.myUtils.RazUtils;


public class DataActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = "DataActivity";
    private final Context mContext = DataActivity.this;

    // Constants
    private static final int ACTIVITY_NUM = 0;
    private static final String WEB_API_KEY = "AIzaSyBgEJEE8dbugDMhhCq_weuQukk9SaOVTt8";
    private static final String[] RADIUS_ARR = {"500", "1000", "2000"};


    // Permissions
    private final int LOCATION_PERMISSION_CODE = 4;

    // Google Places staff
    private GoogleApiClient mGoogleApiClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private GeoDataClient mGeoDataClient;

    // Firebase
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    // Vars
    private ArrayList<String> mPlacesIds;
    private ArrayList<String> mFavoritePlacesIds;
    private ArrayList<String> mCurrentPlacesIds;
    private ArrayList<String> currentSubjects;
    private ArrayList<String> mPlacesInSubjects;  //Places that appears under the current subjects (According DB)
    private PlaceListAdapter mAdapter;
    private boolean loadPlaces;
    private MyAlgorithm mAlgorithm;
    private Intent mIntent;
    private Location mLoc;
    private int mRadius = 0;


    // Widgets
    private RecyclerView recyclerView;
    private CoordinatorLayout rootLayout;
    private ProgressBar mProgressBar;
    private BottomNavigationViewEx bottomNavigationViewEx;
    private ImageView chooseCurrentSubjectsIV;
    private TextView noPlacesTV;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_five_places);
        checkPermission();
        Log.d(TAG, "onCreate: created.");

        if (mIntent == null) mIntent = getIntent();
        loadPlaces = mIntent.getBooleanExtra(getString(R.string.intent_load_places), true);
        mCurrentPlacesIds = mIntent.getStringArrayListExtra(getString(R.string.intent_current_places));


        mPlacesIds = new ArrayList<>();
        mPlacesInSubjects = new ArrayList<>();
        mFavoritePlacesIds = new ArrayList<>();

        // Setups
        setupGeoClient();
        setupWidgets();
        setupFirebaseStaff();
    }


    private void setupWidgets() {
        recyclerView = findViewById(R.id.recycler_view);
        rootLayout = findViewById(R.id.root_layout);
        mProgressBar = findViewById(R.id.progress_bar);
        bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        chooseCurrentSubjectsIV = findViewById(R.id.choose_subjects_iv);
        noPlacesTV = findViewById(R.id.no_places_tv);

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

    private void setupGeoClient() {
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        mGeoDataClient = Places.getGeoDataClient(this, null);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();


        // TODO(2): Delete this.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mPlaceDetectionClient.getCurrentPlace(null).addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: WWWWWWWWWWWWWWWWWWWWW" + task.getResult());
                }
            }
        });
    }


    // Request the ACCESS_FINE_LOCATION permission.
    // This Function is useful in Marshmallow SDK
    private void checkPermission() {

        // If the permission ACCESS_FINE_LOCATION hasn't benn granted yet
        if (ContextCompat.checkSelfPermission(this,
                Permissions.ACCESS_FINE_LOCATION[0]) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            //if Marshmallow or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // requestPermissions defined below
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_CODE);
            }
        } else {
            Log.d(TAG, "checkPermission: now permission request needed.");
        }
    }

    /**
     * Permission request with a dialog.
     */
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Permissions.ACCESS_FINE_LOCATION[0])) {
            new AlertDialog.Builder(this)
                    .setTitle("Location Permission needed")
                    .setMessage("This permission is required to show you the best restaurants in your area")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "onClick: OK Click");
                            ActivityCompat.requestPermissions(DataActivity.this, Permissions.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_CODE);
                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this, Permissions.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_CODE);
        }
    }

    // Unnecessary
    private ArrayList<Integer> getUserTypes() {
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            arrayList.add(i);
        }
        return arrayList;
    }
    private void getNearbyPlaces() {
        if ((mCurrentPlacesIds == null || loadPlaces) &&
                ActivityCompat.checkSelfPermission(
                        this, Permissions.ACCESS_FINE_LOCATION[0]) ==
                        PackageManager.PERMISSION_GRANTED) {

            // TODO(1): Increase the radius between the user and the "nearby" places!!
            final Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient
                    .getCurrentPlace(null);
            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {

                    // Likelihood places buffer
                    final PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                    // TODO(!): Use currentSubjects to know what to show to the user.

                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {  // All likelihood places
                        final Place place = placeLikelihood.getPlace().freeze();

                        Log.d(TAG, "onComplete: score !!! : " + mAlgorithm.getPlaceScore());

                        String pid = place.getId();
                        if (mPlacesInSubjects != null && mPlacesInSubjects.contains(pid)) {
                            mPlacesIds.add(pid);
                        } else if (mPlacesInSubjects == null || mPlacesInSubjects.size() == 0) {
//                            mPlacesIds.add(pid);
                            noPlacesTV.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                    setupPlacesAdapter();

                    // Prevents a memory leak
                    likelyPlaces.release();
                }
            });
        } else if (mCurrentPlacesIds != null) {
            mPlacesIds = (ArrayList<String>) RazUtils.sortPlaceIdsByGraphScore(mCurrentPlacesIds, mAlgorithm, mContext);
            setupPlacesAdapter();
        }
    }

    private void setupPlacesAdapter() {

        mPlacesIds = (ArrayList<String>) RazUtils.sortPlaceIdsByGraphScore(mPlacesIds, mAlgorithm, mContext);

        mAdapter = new PlaceListAdapter(this, mPlacesIds, mFavoritePlacesIds, getString(R.string.data_activity), null);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, DataActivity.this, bottomNavigationViewEx, mFavoritePlacesIds);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(
                this, Permissions.ACCESS_FINE_LOCATION[0]) ==
                PackageManager.PERMISSION_GRANTED) {
            mLoc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLoc != null) {
                // Find places nearby current latLng
                new JsonTask().execute(googlePlacesUrl(RADIUS_ARR[0]));
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();


            // Progress Dialog (?)
  /*          pd = new ProgressDialog(mContext);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();*/
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
                if (connection != null) { connection.disconnect(); }
                try { if (reader != null) { reader.close(); }
                } catch (IOException e) { e.printStackTrace(); }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            /*if (pd.isShowing()){
                pd.dismiss();
            }*/
            Log.d(TAG, "onPostExecute: result(!!): " + result);

            try {

                JSONObject mainJson = new JSONObject(result);
                Log.d("json(1)", mainJson.toString());

                JSONArray placesArrayJson = (JSONArray) mainJson.get("results");
                Log.d("json(2)", "JSON array: " + placesArrayJson);



                int placesCount = placesArrayJson.length();
                if (placesCount < 3) {
                    for (int i=0; i< placesCount; i++ ) {
                        JSONObject placeJson = (JSONObject) placesArrayJson.get(i);
                        String pid = placeJson.getString("place_id");

                        if (!mPlacesIds.contains(pid) && mPlacesInSubjects.contains(pid))
                            mPlacesIds.add(pid);
                    }
                    if (mRadius < RADIUS_ARR.length - 1) {
                        mRadius++;
                        new JsonTask().execute(googlePlacesUrl(RADIUS_ARR[mRadius]));
                        return;
                    }
                }
                // [ELSE]
                ArrayList<String> allNearbyPlaces = new ArrayList<>();
                for (int i=0; i< placesCount; i++ ){

                    JSONObject placeJson = (JSONObject) placesArrayJson.get(i);
                    String pid = placeJson.getString("place_id");
                    allNearbyPlaces.add(pid);


                    if (!mPlacesIds.contains(pid) && mPlacesInSubjects.contains(pid))
                        mPlacesIds.add(pid);

                    // Check sort by distance:
                    JSONObject placeGeo = (JSONObject) placeJson.get("geometry");
                    JSONObject placeLocation = (JSONObject) placeGeo.get("location");
                    LatLng placeLatLng = new LatLng(placeLocation.getDouble("lat"),
                            placeLocation.getDouble("lng"));
                    LatLng myLatLng = new LatLng(mLoc.getLatitude(), mLoc.getLongitude());



                    Log.d(TAG, "onPostExecute: distance("+i+"): "+RazUtils.getDistance(placeLatLng, myLatLng));
                }

                if (mPlacesIds.size() == 0) {
                    Toast.makeText(mContext, "couldn't find exactly what you want in your area, so I'll show you all the restaurants in your area", Toast.LENGTH_SHORT).show();
                    mPlacesIds = allNearbyPlaces;
                }

                setupPlacesAdapter();

            } catch (Throwable t) {
                Log.e("jsonError", "Could not parse malformed JSON: \"" + result + "\"");
                Log.e(TAG, "onPostExecute: throwMessage: " + t.getMessage());
            }
        }
    }


    private String googlePlacesUrl(String radius) {
        if (mLoc == null) return null;  // Input check.

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + mLoc.getLatitude();
        url += ","+ mLoc.getLongitude();
        url += "&radius="+radius;
        url += "&type=restaurant";
        url += "&key="+WEB_API_KEY;

        return url;
    }

    /*
     * ------------------------- Firebase ------------------------------------------
     */
    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mAuthListener != null) mAuth.removeAuthStateListener(mAuthListener);
    }

    private void setupFirebaseStaff() {
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();

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
        mRef
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mFavoritePlacesIds = new ArrayList<>();
                        for (DataSnapshot placeIdDS : dataSnapshot.child(getString(R.string.db_persons))
                                .child(mAuth.getCurrentUser().getUid())
                                .child(getString(R.string.db_field_favorite_places_ids)).getChildren()) {
                            mFavoritePlacesIds.add(placeIdDS.getValue(String.class));
                        }
                        setupBottomNavigationView();


                        mAlgorithm = new MyAlgorithm(mContext, dataSnapshot, FirebaseAuth.getInstance().getCurrentUser().getUid());

                        mIntent = getIntent();
                        currentSubjects = mIntent.getStringArrayListExtra(getString(R.string.intent_current_subjects));

                        if (currentSubjects != null) {
                            mAlgorithm.setCurrentSubjects(currentSubjects);
                            mPlacesInSubjects = new ArrayList<>();

                            for (String subject : currentSubjects) {
                                Log.d(TAG, "onDataChange: current subject: " + subject);
                                DataSnapshot subjectPlacesDS = dataSnapshot.child(mContext.getString(R.string.db_tag_places)).child(subject);
                                for (DataSnapshot ds : subjectPlacesDS.getChildren()) { // Add Matching places
                                    Log.d(TAG, "onDataChange: found a matching place from DB: " + ds.getKey());
                                    mPlacesInSubjects.add(ds.getKey());
                                }
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "onCancelled: firebaseDatabaseError: " + databaseError.getMessage());
                    }
                });


    }
}
