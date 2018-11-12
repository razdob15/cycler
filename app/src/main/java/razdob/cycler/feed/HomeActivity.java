package razdob.cycler.feed;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import razdob.cycler.R;
import razdob.cycler.dialogs.CustomDialog;
import razdob.cycler.models.Photo;
import razdob.cycler.myUtils.BottomNavigationViewHelper;
import razdob.cycler.myUtils.FireBaseUtils;
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

    // Intent Extras
    private static final String CALLING_ACTIVITY_EXTRA = "calling_activity";
    private static final String RETURN_FRAGMENT_EXTRA = "return_fragment";
    private static final String CURRENT_SUBJECT_EXTRA = "chosen_current_subjects";

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    /* ---------------------- FIREBASE ----------------------- */

    // GUI
    private ViewPager mViewPager;

    // Vars
    private FirebaseMethods mFireMethods;
    private ArrayList<String> currentSubjects;
    private ArrayList<Photo> photos;
    private final int LOCATION_PERMISSION_CODE = 11;
    private String returnToFragment = "";
    private String callingActivity = "";


    public static void start(Context context, ArrayList<String> chosenSubjects, String callingActivity, String returnToFragment) {
        Intent intent = new Intent(context, HomeActivity.class);
        if (chosenSubjects != null)
            intent.putStringArrayListExtra(CURRENT_SUBJECT_EXTRA, chosenSubjects);
        if (callingActivity != null) intent.putExtra(CALLING_ACTIVITY_EXTRA, callingActivity);
        if (returnToFragment != null) intent.putExtra(RETURN_FRAGMENT_EXTRA, returnToFragment);
        context.startActivity(intent);
    }

    public static void start(Context context, ArrayList<String> chosenSubjects, String callingActivity) {
        start(context, chosenSubjects, callingActivity, null);
    }

    public static void start(Context context) {
        start(context, null, null);


    }

    public static void start(Context context, ArrayList<String> chosenSubjects) {
        start(context, chosenSubjects, null);
    }

    private void getDataFromIntent() {
        Log.d(TAG, "getDataFromIntent: called.");

        Intent intent = getIntent();
        currentSubjects = intent.getStringArrayListExtra(CURRENT_SUBJECT_EXTRA);
        callingActivity = intent.getStringExtra(CALLING_ACTIVITY_EXTRA);

        if (intent.hasExtra(mContext.getString(R.string.return_to_fragment))) {
            returnToFragment = intent.getStringExtra(mContext.getString(R.string.return_to_fragment));
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: started.");

        checkLocationPermission();

        mFireMethods = new FirebaseMethods(mContext);
        mViewPager = findViewById(R.id.viewpager_container);

        photos = new ArrayList<>();

        getDataFromIntent();
        if (callingActivity != null && callingActivity.equals(mContext.getString(R.string.choose_favorite_places_activity))) {
            // First time(?) in this activity, need an explain...
            Log.d(TAG, "setupViewPager: open explain dialog for HomeActivity");
            openExplainDialog();
        }
        setupFirebaseStaff();
        BottomNavigationViewHelper.setupBottomNavigationView(mContext, HomeActivity.this, ACTIVITY_NUM);
    }

    private void setupViewPager() {
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());

        FeedFragment feedFragment = FeedFragment.create(photos);
        NearbyPlacesFragment placesFragment = NearbyPlacesFragment.show(currentSubjects);

        adapter.addFragment(placesFragment); // Index 0
        adapter.addFragment(feedFragment);   // Index 1

        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs_top);
        tabLayout.setupWithViewPager(mViewPager);

        Objects.requireNonNull(tabLayout.getTabAt(0)).setText("PLACES");
        Objects.requireNonNull(tabLayout.getTabAt(1)).setText("FEED");

        if (returnToFragment.equals(mContext.getString(R.string.feed_activity))) {
            Log.d(TAG, "setupViewPager: Open As FeedFragment");
            mViewPager.setCurrentItem(1);
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
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                ArrayList<String> followingsIds = mFireMethods.getUserFollowings(uid, dataSnapshot);
                followingsIds.add(uid);

                for (DataSnapshot userPhotosDS : dataSnapshot.child(mContext.getString(R.string.db_user_photos)).getChildren()) {
                    if (followingsIds.contains(userPhotosDS.getKey())) {
                        for (DataSnapshot photoDS : userPhotosDS.getChildren()) {
                            photos.add(mFireMethods.getPhotoFromDB(photoDS));
                        }
                    }
                }

                setupViewPager();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                FireBaseUtils.dbErrorMessage(TAG, databaseError);
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
