package razdob.cycler.instProfile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import razdob.cycler.BigPhotoFragment;
import razdob.cycler.MainRegisterActivity;
import razdob.cycler.R;
import razdob.cycler.UsersListActivity;
import razdob.cycler.models.Photo;
import razdob.cycler.models.User;
import razdob.cycler.models.UserAccountSettings;
import razdob.cycler.models.UserSettings;
import razdob.cycler.myUtils.BottomNavigationViewHelper;
import razdob.cycler.myUtils.FireBaseUtils;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.GridImageAdapter;
import razdob.cycler.myUtils.MyFonts;
import razdob.cycler.myUtils.UniversalImageLoader;


/**
 * Created by Raz on 03/06/2018, for project: PlacePicker2
 */

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ProfileFragment";
    private static final String NEW_PROFILE_EXTRA = "new_profile";

    public interface OnGridImageSelectedListener {
        void onGridSelected(Photo photo, int activityNumber);
    }

    OnGridImageSelectedListener mOnGridImageSelectedListener;

    @Override
    public void onClick(View v) {
        if (v == mFollowers || v == followersLL) {
            Log.d(TAG, "onClick: followers");
            if (mFollowersCount == 0) {
                Toast.makeText(mContext, "no followers", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "onClick: navigate to UsersListActivity: " + followersIds);

                UsersListActivity.start(mContext, mContext.getString(R.string.toolbar_followers), followersIds, ACTIVITY_NUM);
            }
        } else if (v == mFollowing || v == followingsLL) {
            Log.d(TAG, "onClick: Show the followingsIds");
            if (mFollowingsCount == 0) {
                Toast.makeText(mContext, "no followings", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "onClick: navigate to UsersListActivity: " + followingsIds);

                UsersListActivity.start(mContext, mContext.getString(R.string.toolbar_followings), followingsIds, ACTIVITY_NUM);
            }

        }
    }

    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRIDS_COLUMNS = 3;

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mRef;
    private FirebaseMethods mFirebaseMethods;
    /* ---------------------- FIREBASE ----------------------- */

    // GUI
    private TextView mPosts, mFollowers, mFollowing, mDisplayName, mUserName, mWebsite, mDescription;
    private ProgressBar mProgressBar, profileImagePB;
    private CircleImageView mProfileIV;
    private GridView gridView;
    private Toolbar toolbar;
    private ImageView profileMenuIV;
    private BottomNavigationViewEx bottomNavigationViewEx;
    private LinearLayout followersLL, followingsLL;

    // Vars
    private long mFollowersCount = 0;
    private long mFollowingsCount = 0;
    private long mPostsCount = 0;
    private ArrayList<String> favoritesPlacesIds;
    private User mUser;

    // Followers & followingsIds
    private ArrayList<String> followersIds;
    private ArrayList<String> followingsIds;

    private boolean newProfileImage = false; // todo(!): Use this to load the image immidietly after the change.
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mContext = getActivity();
        mFirebaseMethods = new FirebaseMethods(mContext);

        getBitmapFromBundle();

        matchWidgetsToIds(view);
        setupFirebaseStaff();

        // Fonts
        MyFonts myFonts = new MyFonts(mContext);
        mUserName.setTypeface(myFonts.getBoldFont());

        Log.d(TAG, "onCreateView: started");

        setupToolbar();


        setupGridView();


        TextView editProfileTV = view.findViewById(R.id.text_edit_profile);
        editProfileTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to " + mContext.getString(R.string.edit_profile));
                Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                startActivity(intent);
                Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        return view;
    }

    private void getBitmapFromBundle() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            newProfileImage = true;
            Bitmap bitmap = bundle.getParcelable(NEW_PROFILE_EXTRA);
            mProfileIV.setImageBitmap(bitmap);
        }
    }

    /**
     * Creates new ProfileFragment and pass ite arguments.
     *
     * @param fragmentActivity - Opening Fragment Activity.
     * @param profileBitmap    - new profile bitmap argument.
     */
    public static void showProfile(FragmentActivity fragmentActivity, Bitmap profileBitmap) {
        Log.d(TAG, "showProfile: called.");

        ProfileFragment fragment = new ProfileFragment();
        if (profileBitmap != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(NEW_PROFILE_EXTRA, profileBitmap);
            fragment.setArguments(bundle);
        }
        FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack("Profile");
        transaction.commit();
    }

    public static void showProfile(FragmentActivity fragmentActivity) {
        showProfile(fragmentActivity, null);
    }


    private void matchWidgetsToIds(View view) {
        Log.d(TAG, "matchWidgetsToIds: called.");
        mDisplayName = view.findViewById(R.id.display_name);
        mUserName = view.findViewById(R.id.profile_name_tv);
        mWebsite = view.findViewById(R.id.website);
        mDescription = view.findViewById(R.id.description);
        mProfileIV = view.findViewById(R.id.profile_image);
        mPosts = view.findViewById(R.id.tv_posts);
        mFollowers = view.findViewById(R.id.tv_followers);
        mFollowing = view.findViewById(R.id.tv_following);
        mProgressBar = view.findViewById(R.id.profileProgressBar);
        profileImagePB = view.findViewById(R.id.profile_image_pb);
        gridView = view.findViewById(R.id.gridView);
        toolbar = view.findViewById(R.id.profile_toolbar);
        profileMenuIV = view.findViewById(R.id.profileMenu_iv);
        bottomNavigationViewEx = view.findViewById(R.id.bottomNavViewBar);

        followersLL = view.findViewById(R.id.followers_ll);
        followingsLL = view.findViewById(R.id.followings_ll);

        followersLL.setOnClickListener(this);
        followingsLL.setOnClickListener(this);


    }

    @Override
    public void onAttach(Context context) {
        try {
            mOnGridImageSelectedListener = (OnGridImageSelectedListener) getActivity();

        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
        super.onAttach(context);

    }

    private void setupGridView() {
        Log.d(TAG, "setupGridView: Settings up image grid");

        final ArrayList<Photo> photos = new ArrayList<>();
        final ArrayList<String> placeIds = new ArrayList<>();

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Photos Grid...
                photos.addAll(mFirebaseMethods.getUserPhotos(dataSnapshot, Objects.requireNonNull(mAuth.getCurrentUser()).getUid()));
                for (Photo photo : photos) {
                    placeIds.add(photo.getPlace_id());
                }

                // Setup user's image grid
                int gridWidth = mContext.getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth / NUM_GRIDS_COLUMNS;
                gridView.setColumnWidth(imageWidth);

                ArrayList<String> imgUrls = new ArrayList<>();
                for (int i = 0; i < photos.size(); i++) {
                    imgUrls.add(photos.get(i).getImage_path());
                }

                // TODO(2) Load te photos faster

                GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview,
                        "", imgUrls, placeIds);
                gridView.setAdapter(adapter);

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mOnGridImageSelectedListener.onGridSelected(photos.get(position), ACTIVITY_NUM);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                FireBaseUtils.dbErrorMessage(TAG, databaseError);
            }
        });
    }


    // --------------- Setup Followers & Followings & Posts Count --------------- //

    /**
     * Setup the mFollowersCount & followersIds from the DB.
     * Set mFollowers text.
     * Update followers in FirebaseMethods.
     * Add listener to mFollowers TextView (this).
     */
    private void setupFollowers(DataSnapshot mainDS, String uid) {
        Log.d(TAG, "setupFollowers: called.");

        mFollowersCount = 0;
        followersIds = new ArrayList<>();

        for (DataSnapshot followerDS : mainDS.child(mContext.getString(R.string.db_field_followers)).child(uid).getChildren()) {
            followersIds.add(followerDS.getKey());
        }
        mFollowersCount = followersIds.size();
        mFollowers.setText(String.valueOf(mFollowersCount));
        mFirebaseMethods.updateFollowers(mFollowersCount);
        mFollowers.setOnClickListener(ProfileFragment.this);

    }

    /**
     * Setup the mFollowingsCount & followingsIds from the DB.
     * Set mFollowing text.
     * Update followings in FirebaseMethods.
     * Add listener to mFollowing TextView (this).
     */
    private void setupFollowing(DataSnapshot mainDS, String uid) {
        mFollowingsCount = 0;
        followingsIds = new ArrayList<>();

        for (DataSnapshot followingDS : mainDS
                .child(mContext.getString(R.string.db_field_following))
                .child(uid).getChildren()) {
            followingsIds.add(followingDS.getKey());
        }
        mFollowingsCount = followingsIds.size();
        mFollowing.setText(String.valueOf(mFollowingsCount));
        mFirebaseMethods.updateFollowings(mFollowingsCount);
        mFollowing.setOnClickListener(ProfileFragment.this);
    }

    private void setupPostsCount(DataSnapshot mainDS, String uid) {
        Log.d(TAG, "setupPostsCount: called.");
        mPostsCount = mainDS.child(mContext.getString(R.string.db_user_photos))
                .child(uid).getChildrenCount();
        mPosts.setText(String.valueOf(mPostsCount));
    }

    private void setProfileWidgets(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidgets: setting widgets with datas retrieving from firebase: " + userSettings.toString());

        mUser = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();
        if (mUser == null)
            Log.d(TAG, "setProfileWidgets: mUser is null");
        else {
            if (mUser.getFavoritePlacesIDs() != null) {
                Log.d(TAG, "setProfileWidgets: mUser have favorite places");
                favoritesPlacesIds = (ArrayList<String>) mUser.getFavoritePlacesIDs();
            } else {
                Log.d(TAG, "setProfileWidgets: mUser doesn't have favorite places.");
                favoritesPlacesIds = new ArrayList<>();
            }
            if (mUser.getName() != null) {
                mUserName.setText(mUser.getName());
            }

            if (mUser.getProfile_photo() != null) {
//                UniversalImageLoader.setImage(mContext, mUser.getProfile_photo(), mProfileIV, profileImagePB, "");
                UniversalImageLoader.setImagePicasso(mUser.getProfile_photo(), mProfileIV, profileImagePB);
                mProfileIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BigPhotoFragment.createBigPhoto(getActivity(), mUser.getProfile_photo());
                    }
                });
            } else
                Log.d(TAG, "setProfileWidgets: user doesn't have a profile photo yet.");
        }

        BottomNavigationViewHelper.setupBottomNavigationView(mContext, Objects.requireNonNull(getActivity()), ACTIVITY_NUM);


        if (settings == null) {
            Log.d(TAG, "setProfileWidgets: settings is null");
            changeToUnSetup(mDisplayName);
            changeToUnSetup(mDescription);
            changeToUnSetup(mWebsite);
        } else {
            if (mUserName.getText().length() == 0) mUserName.setText(settings.getUserName());
            setupOneDetailText(mDisplayName, settings.getDisplayName());
            setupOneDetailText(mWebsite, settings.getWebsite());
            setupOneDetailText(mDescription, settings.getDescription());
        }


        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * Gets a TextView and a String.
     * If the string is not null (or empty):
     * Puts the string as the TV text.
     * Changes the textColor to Black or Blue (according the TV object) if possible [depends by the SDK Version].
     * Changes the textTypeSurface to Normal.
     * Else:
     * Changes the TV to unSetup [calls to  changeToUnSetup(TV)].
     *
     * @param item   - The TV.
     * @param detail - The String.
     */
    private void setupOneDetailText(TextView item, String detail) {
        Log.d(TAG, "setupOneDetailText: details: " + detail + "; item: " + item);
        if (detail != null && detail.length() > 0) {
            item.setText(detail);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (item == mDisplayName || item == mDescription) {
                    item.setTextColor(mContext.getColor(R.color.colorBlack));
                } else if (item == mWebsite) {
                    item.setTextColor(mContext.getColor(R.color.colorLinkBlue));
                }
            }
            item.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        } else if (item == mDisplayName || item == mWebsite || item == mDescription) {
            changeToUnSetup(item);
        }

    }

    /**
     * Changes the textColor to Grey if possible [depends by the SDK Version].
     * Changes the textTypeSurface to Italic.
     * Puts the matching 'hint'.
     *
     * @param item - The TV.
     */
    private void changeToUnSetup(TextView item) {
        Log.d(TAG, "changeToUnSetup: item: " + item);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            item.setTextColor(mContext.getColor(R.color.colorGrey));
        }
        item.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        if (item == mDisplayName) item.setText("Display Name");
        if (item == mDescription) item.setText("Description");
        if (item == mWebsite) item.setText("Website");
    }

    private void setupToolbar() {

        ((InstProfileActivity) getActivity()).setSupportActionBar(toolbar);

        profileMenuIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to account settings");
                startActivity(new Intent(mContext, AccountSettingsActivity.class));
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


    }



    /*
     * ----------------------------- Firebase ------------------------------------------
     */


    private void setupFirebaseStaff() {
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
                    mContext.startActivity(new Intent(mContext, MainRegisterActivity.class));
                } else {
                    Log.d(TAG, "onAuthStateChanged: User is logged-in :) uid = " + firebaseAuth.getCurrentUser().getUid());
                }
            }
        };
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                setupFollowers(dataSnapshot, uid);
                setupFollowing(dataSnapshot, uid);
                setupPostsCount(dataSnapshot, uid);

                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot, uid));
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
