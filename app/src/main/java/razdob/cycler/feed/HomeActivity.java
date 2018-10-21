package razdob.cycler.feed;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.Objects;

import razdob.cycler.R;
import razdob.cycler.dialogs.CustomDialog;
import razdob.cycler.models.Photo;
import razdob.cycler.myUtils.BottomNavigationViewHelper;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.Permissions;
import razdob.cycler.myUtils.SectionPagerAdapter;


/**
 * Created by Raz on 09/08/2018, for project: PlacePicker2
 */
public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private final Context mContext = HomeActivity.this;

    private static final int ACTIVITY_NUM = 0;

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    /* ---------------------- FIREBASE ----------------------- */

    // GUI
    private ViewPager mViewPager;
    private BottomNavigationViewEx bottomNavigationViewEx;

    // Vars
    private FirebaseMethods mFireMethods;
    private ArrayList<String> followingsIds;
    private ArrayList<String> currentSubjects;
    private ArrayList<Photo> photos;
    private final int LOCATION_PERMISSION_CODE = 11;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: started.");

        checkLocationPermission();

        mFireMethods = new FirebaseMethods(mContext);
        mViewPager = findViewById(R.id.viewpager_container);
        bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);

        followingsIds = new ArrayList<>();
        photos = new ArrayList<>();

        setupFirebaseStaff();
        setupBottomNavigationView();
    }

    private void setupViewPager() {
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());


        Bundle feedBundle = new Bundle();
        Bundle placesBundle = new Bundle();

        Intent intent = getIntent();

        if (intent.hasExtra(mContext.getString(R.string.calling_activity)) &&
                intent.getStringExtra(mContext.getString(R.string.calling_activity)).equals(mContext.getString(R.string.choose_favorite_places_activity))) {
            // First time(?) in this activity, need an explain...
            Log.d(TAG, "setupViewPager: open explain dialog for HomeActivity");

            openExplainDialog();
        }

        currentSubjects = intent.getStringArrayListExtra(mContext.getString(R.string.intent_current_subjects));
        placesBundle.putStringArrayList(mContext.getString(R.string.intent_current_subjects), currentSubjects);
        feedBundle.putParcelableArrayList(mContext.getString(R.string.bundle_photos), photos);

        FeedFragment feedFragment = new FeedFragment();
        NearbyPlacesFragment placesFragment = new NearbyPlacesFragment();

        feedFragment.setArguments(feedBundle);
        placesFragment.setArguments(placesBundle);

        adapter.addFragment(placesFragment); // Index 0
        adapter.addFragment(feedFragment);   // Index 1


        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs_top);
        tabLayout.setupWithViewPager(mViewPager);

        Objects.requireNonNull(tabLayout.getTabAt(0)).setText("PLACES");
        Objects.requireNonNull(tabLayout.getTabAt(1)).setText("FEED");

        if (intent.hasExtra(mContext.getString(R.string.return_to_fragment))) {
            if (intent.getStringExtra(mContext.getString(R.string.return_to_fragment)).equals(mContext.getString(R.string.feed_fragment))) {
                Log.d(TAG, "setupViewPager: Open As FeedFragment");
                mViewPager.setCurrentItem(1);
            }
        }
    }


    private void openExplainDialog() {
        Log.d(TAG, "openExplainDialog: open a dialog to explain to the user how to use this activity");

        final CustomDialog dialog = new CustomDialog(mContext, mContext.getString(R.string.home_explain_dialog_title),
                mContext.getString(R.string.home_explain_dialog_text1), 1, "OK, Next", null);
        dialog.setClick1(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.setDialogText(mContext.getString(R.string.home_explain_dialog_text2));
                dialog.setText1(null);
                dialog.setText2("Nice!");
                dialog.setClick2(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "onClick: finish dialog. Dismiss.");
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }


    /*
     * ----------------------------- PERMISSIONS ---------------------------------------
     */

    /**
     * if Location-Permission is not granted -> Sends LOCATION_PERMISSION_CODE to onRequestPermissionsResult
     */
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Permissions.ACCESS_FINE_LOCATION[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, Permissions.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                final CustomDialog dialog = new CustomDialog(mContext, mContext.getString(R.string.location_permission_dialog_title), mContext.getString(R.string.location_permission_dialog_text),
                        2, "OK", "Cancel");
                dialog.setClick1(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(HomeActivity.this, Permissions.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_CODE);
                        dialog.dismiss();
                    }
                });
                dialog.setClick2(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            } else {
                finish();
                startActivity(new Intent(mContext, HomeActivity.class));
            }
        }
    }


    /*
     * -------------------------- BOTTOM NAVIGATION ------------------------------------
     */

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx, mFireMethods.getFavoritePlacesIds());
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    /*
     * ----------------------------- Firebase ------------------------------------------
     */

    private void setupFirebaseStaff() {
        Log.d(TAG, "setupFirebaseStaff: called.");

        // Retrieve mFireApp app.
        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();

        // Init mAuthStateListener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                // Check if user is logged in

                if (user == null) {
                    Log.d(TAG, "onAuthStateChanged: User log-out");
                } else {
                    Log.d(TAG, "onAuthStateChanged: User is logged-in :) uid = " + firebaseAuth.getCurrentUser().getUid());
                }
            }
        };

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                followingsIds.add(mAuth.getCurrentUser().getUid());
                for (DataSnapshot followingDS : dataSnapshot
                        .child(mContext.getString(R.string.db_field_following))
                        .child(mAuth.getCurrentUser().getUid()).getChildren()) {

                    followingsIds.add(followingDS.getKey());
                }

                for (DataSnapshot userPhotosDS : dataSnapshot
                        .child(mContext.getString(R.string.db_user_photos)).getChildren()) {
                    if (followingsIds.contains(userPhotosDS.getKey())) {
                        for (DataSnapshot photoDS : userPhotosDS.getChildren()) {
                            photos.add(mFireMethods.getPhotoFromDB(photoDS));
                        }
                    }
                }

                setupViewPager();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: DatabaseError: " + databaseError.getMessage());
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAuthStateListener != null)
            mAuth.removeAuthStateListener(mAuthStateListener);
    }


}
