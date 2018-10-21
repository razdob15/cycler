package razdob.cycler;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import razdob.cycler.feed.HomeActivity;
import razdob.cycler.instProfile.AccountSettingsActivity;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.Permissions;
import razdob.cycler.myUtils.RemoteConfigConsts;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int LOCATION_REQUEST_CODE = 5;
    private final Context mContext = MainActivity.this;


    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mFireUser;
    private FirebaseMethods mFireMethods;
    private FirebaseRemoteConfig mRemoteConfig;
    /* ---------------------- FIREBASE ----------------------- */

    //GUI
    private TextView personTV, businessTV;
    private ImageView personIV, businessIV;
    private ProgressBar progressBar;

    // UserTypes
    public static HashMap<String, Boolean> userPref;
    public static HashMap<String, ArrayList<Integer>> subjectsAndTypes;

    // Fonts
    public static Typeface FONT1;
    public static Typeface FONT2;

    // vars
    private ArrayList<String> mFavPlacesIds;


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Starts");
        mFireMethods = new FirebaseMethods(mContext);
        setupFirebaseStaff();
        loadDataFromDB();


        personTV = findViewById(R.id.person_tv);
        personIV = findViewById(R.id.person_iv);
        businessTV = findViewById(R.id.business_tv);
        businessIV = findViewById(R.id.business_iv);
        progressBar = findViewById(R.id.main_progress_bar);

        userPref = new HashMap<>();

        FONT1 = Typeface.createFromAsset(getAssets(), "fonts/IndustrialRevolution-Regular.ttf");
        FONT2 = Typeface.createFromAsset(getAssets(), "fonts/IndustrialRevolution-Italic.ttf");

    }




    private void setupFirebaseStaff() {
        Log.d(TAG, "setupFirebaseStaff: called.");

        // Retrieve mFireApp app.
        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    firebaseAuth.signOut();
                    finish();
                }
            }
        };

        if (mAuth.getCurrentUser() != null) {
            mFireUser = mAuth.getCurrentUser();
            Log.i("user_details: ", "User UID: = " + mFireUser.getUid());
            Log.i("user_details: ", "User displayName: = " + mFireUser.getDisplayName());
            Log.i("user_details: ", "User email: = " + mFireUser.getEmail());
        }
    }

    private void loadDataFromDB() {
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: check if the user exists");
                DataSnapshot personsDS = dataSnapshot.child(mContext.getString(R.string.db_persons));
                DataSnapshot usersAccountSettingsDS = dataSnapshot.child(mContext.getString(R.string.db_user_account_settings));
                String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                if (!mFireMethods.userHasName(dataSnapshot)) {
                    Log.d(TAG, "onDataChange: user doesn't have a name !");
                    goToUserSettings();
                    return;
                }
//                if (!personsDS.hasChild(uid) || !usersAccountSettingsDS.hasChild(uid)) {
//                    // User not in DB
//                    Log.d(TAG, "onDataChange: user not in DB");
//                    goToUserSettings();
//                    return;
//                }
//                if ((personsDS.hasChild(uid) && usersAccountSettingsDS.hasChild(uid))) {
//                    if (!usersAccountSettingsDS.child(uid).hasChild(mContext.getString(R.string.db_field_display_name)) || !personsDS.child(uid).hasChild(mContext.getString(R.string.db_field_name))) {
//                        Log.d(TAG, "onDataChange: user has no NAME");
//                        goToUserSettings();
//                        return;
//                    }
//                }

                Log.d(TAG, "onDataChange: Check if user has favorite places.");
                if (personsDS.child(uid).hasChild(mContext.getString(R.string.db_field_favorite_places_ids))) {
                    Log.d(TAG, "onDataChange: user has favorites");
                    if (personsDS.child(uid).child(mContext.getString(R.string.db_field_favorite_places_ids)).getChildrenCount() < RemoteConfigConsts.MIN_FAVORITES_COUNT) {
                        Log.d(TAG, "onDataChange: user doesn't have enough favorites. has: " +
                                personsDS.child(uid).child(mContext.getString(R.string.db_field_favorite_places_ids)).getChildrenCount());

                        // Create FavoritesList
                        mFavPlacesIds = new ArrayList<>();
                        for (DataSnapshot placeDS : personsDS.child(uid)
                                .child(mContext.getString(R.string.db_field_favorite_places_ids))
                                .getChildren()) {
                            mFavPlacesIds.add(placeDS.getValue(String.class));
                        }
                        // Need to choose more favorites places
                        chooseFavoritePlaces();
                        return;
                    }
                } else {
                    Log.d(TAG, "onDataChange: user has no favorites");

                    mFavPlacesIds = null;
                    chooseFavoritePlaces();
                    return;
                }

                // User has Name & Favorites
                goToHomeActivity();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("DatabaseError", databaseError.getMessage());
            }
        });

    }


    /**
     * Navigates to AccountSettingsActivity and put firstTime as an extra.
     * Finishes this activity.
     */
    private void goToUserSettings() {
        Log.d(TAG, "onClick: navigating to: " + mContext.getString(R.string.edit_profile));

        Intent intent = new Intent(mContext, AccountSettingsActivity.class);
        intent.putExtra(mContext.getString(R.string.calling_activity), getString(R.string.main_activity));
        intent.putExtra(mContext.getString(R.string.intent_first_time), true);
        startActivity(intent);
        finish();
    }


    private void chooseFavoritePlaces() {
        Log.d(TAG, "chooseFavoritePlaces: Navigate to favorites: " + mFavPlacesIds);

        checkPermission();
    }

    /**
     * Navigates to ChooseFavoritePlacesActivity with the correct extras.
     */
    private void goToChooseFavoritePlaces() {
        if (mFavPlacesIds == null) mFavPlacesIds = new ArrayList<>();
        Intent intent = new Intent(mContext, ChooseFavoritePlacesActivity.class);
        intent.putExtra(mContext.getString(R.string.intent_favorite_places), mFavPlacesIds);
        intent.putExtra(mContext.getString(R.string.intent_favorite_count), RemoteConfigConsts.MIN_FAVORITES_COUNT);
        startActivity(intent);
        finish();
    }

    /**
     * Navigates to HomeActivity.
     */
    private void goToHomeActivity() {
        Log.d(TAG, "goToHomeActivity: Navigating to the profile.");
        Intent intent = new Intent(mContext, HomeActivity.class);
        startActivity(intent);
        finish();
    }


    // Request the ACCESS_FINE_LOCATION permission.
    // This Function is useful in Marshmallow SDK
    private void checkPermission() {
        Log.d(TAG, "checkPermission: called.");
        // If the permission ACCESS_FINE_LOCATION hasn't benn granted yet
        if (ContextCompat.checkSelfPermission(this, Permissions.ACCESS_FINE_LOCATION[0]) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            //if Marshmallow or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // requestPermissions defined below
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        } else {
            Log.d(TAG, "checkPermission: now permission request needed.");
            goToChooseFavoritePlaces();
        }
    }

    /**
     * Permission request with a dialog.
     */
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Permissions.ACCESS_FINE_LOCATION[0])) {
            new AlertDialog.Builder(this)
                    .setTitle("Location Permission is required")
                    .setMessage("The location-permission is required to show you the best restaurants in your area")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "onClick: OK. Permission Granted.");
                            ActivityCompat.requestPermissions(MainActivity.this, Permissions.ACCESS_FINE_LOCATION, LOCATION_REQUEST_CODE);
                            checkPermission();
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "onClick: cancel the location-permission dialog");
                            dialog.dismiss();
                            checkPermission();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this, Permissions.ACCESS_FINE_LOCATION, LOCATION_REQUEST_CODE);
        }
    }
}
