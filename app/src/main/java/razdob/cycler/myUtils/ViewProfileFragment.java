package razdob.cycler.myUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import razdob.cycler.MainRegisterActivity;
import razdob.cycler.R;
import razdob.cycler.UsersListActivity;
import razdob.cycler.fivePlaces.FivePlacesActivity;
import razdob.cycler.instProfile.AccountSettingsActivity;
import razdob.cycler.models.Photo;
import razdob.cycler.models.User;
import razdob.cycler.models.UserAccountSettings;
import razdob.cycler.models.UserSettings;

import static android.view.View.GONE;


/**
 * Created by Raz on 03/06/2018, for project: PlacePicker2
 */
public class ViewProfileFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "ViewProfileFragment";

    public interface OnGridImageSelectedListener {
        void onGridSelected(Photo photo, int activityNumber);
    }

    @Override
    public void onClick(View v) {
        if (v == mFollowers || v  == followersLL) {
            Log.d(TAG, "onClick: followers");
            if (mFollowersCount == 0) {
                Toast.makeText(mContext, "no followers", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "onClick: navigate to UsersListActivity: " + followersIds);
                Intent intent = new Intent(mContext, UsersListActivity.class);
                intent.putExtra(mContext.getString(R.string.intent_title), mContext.getString(R.string.toolbar_followers));
                intent.putStringArrayListExtra(mContext.getString(R.string.intent_users), followersIds);
                mContext.startActivity(intent);
            }
        } else if (v == mFollowing || v == followingsLL) {
            Log.d(TAG, "onClick: Show the followingsIDs");
            if (mFollowingsCount == 0) {
                Toast.makeText(mContext, "no followings", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "onClick: navigate to UsersListActivity: " + followingsIDs);
                Intent intent = new Intent(mContext, UsersListActivity.class);
                intent.putExtra(mContext.getString(R.string.intent_title), mContext.getString(R.string.toolbar_followings));
                intent.putStringArrayListExtra(mContext.getString(R.string.intent_users), followingsIDs);
                mContext.startActivity(intent);
            }

        }
    }

    OnGridImageSelectedListener mOnGridImageSelectedListener;

    private static final int ACTIVITY_NUM = 1;
    private static final int NUM_GRIDS_COLUMNS = 3;

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mRef;
    private FirebaseMethods mFireMethods;
    private String uid;
    /* ---------------------- FIREBASE ----------------------- */

    // GUI
    private TextView mPosts, mFollowers, mFollowing, mDisplayName, mUserName, mWebsite, mDescription, mFollow, mUnfollow;
    private ProgressBar mProgressBar, profileImagePB;
    private CircleImageView mProfileIV;
    private GridView gridView;
    private Toolbar toolbar;
    private ImageView mBackArrow;
    private BottomNavigationViewEx bottomNavigationViewEx;
    private TextView editProfile;
    private LinearLayout followersLL, followingsLL;

    // Vars
    private User mViewingUser;
    private int mFollowersCount = 0;
    private int mFollowingsCount = 0;
    private int mPostsCount = 0;

    // Followers & followingsIDs
    private ArrayList<String> followersIds;
    private ArrayList<String> followingsIDs;


    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_profile, container, false);
        mDisplayName = view.findViewById(R.id.display_name);
        mUserName = view.findViewById(R.id.user_name);
        mWebsite = view.findViewById(R.id.website);
        mDescription = view.findViewById(R.id.description);
        mProfileIV = view.findViewById(R.id.profile_image);
        mPosts = view.findViewById(R.id.tv_posts);
        mFollowers = view.findViewById(R.id.tv_followers);
        mFollowing = view.findViewById(R.id.tv_following);
        mProgressBar = view.findViewById(R.id.profileProgressBar);
        gridView = view.findViewById(R.id.gridView);
        toolbar = view.findViewById(R.id.profile_toolbar);
        bottomNavigationViewEx = view.findViewById(R.id.bottomNavViewBar);
        mFollow = view.findViewById(R.id.follow);
        mUnfollow = view.findViewById(R.id.unfollow);
        editProfile = view.findViewById(R.id.text_edit_profile);
        mBackArrow = view.findViewById(R.id.backArrow);
        profileImagePB = view.findViewById(R.id.profile_image_pb);
        followersLL = view.findViewById(R.id.followers_ll);
        followingsLL = view.findViewById(R.id.followings_ll);

        followersLL.setOnClickListener(this);
        followingsLL.setOnClickListener(this);

        mContext = getActivity();
        mFireMethods = new FirebaseMethods(mContext);
        Log.d(TAG, "onCreateView: started");

        BottomNavigationViewHelper.setupBottomNavigationView(mContext, Objects.requireNonNull(getActivity()), ACTIVITY_NUM);
        setupFirebaseStaff();

        try {
            mViewingUser = getUserFromBundle();
            init();
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
            Toast.makeText(mContext, "something went wring", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
        }



        isFollowing();
        setupFollowers();
        setupFollowing();
        setupPostsCount();

        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: now following: " + mViewingUser.getName());

                follow();
                setFollowing();
            }
        });
        mUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: now unFollowing: " + mViewingUser.getName());

                unFollow();
                setUnfollowing();
            }
        });


//        setupGridView();

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to " + mContext.getString(R.string.edit_profile));
                Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        return view;
    }

    /**
     * remove the follow in the DB.
     * Decreases the mFollowersCount by 1 (if bigger than 0, else - send  a WARN).
     * Updates the mFollowers [TextView] text to the mFollowersCount's value.
     * Remove (safely) the uid from followersIds
     */
    private void unFollow() {
        Log.d(TAG, "unFollow: called.");
        // unFollow in the DB
        mRef
                .child(getString(R.string.db_field_following))
                .child(uid)
                .child(mViewingUser.getUser_id())
                .removeValue();

        mRef
                .child(getString(R.string.db_field_followers))
                .child(mViewingUser.getUser_id())
                .child(uid)
                .removeValue();

        // Update Counter
        if (mFollowersCount > 0) mFollowersCount--;
        else Log.w(TAG, "unFollow: Followers need to be smaller than 0 !");
        // Update mFollowers[TV]
        mFollowers.setText(String.valueOf(mFollowersCount));
        if (followersIds.contains(uid))
            followersIds.remove(uid);
    }

    /**
     * Adds the follow in the DB.
     * Increases the mFollowersCount by 1.
     * Updates the mFollowers [TextView] text to the mFollowersCount's value.
     * Adds (safely) uid to followersIds.
     */
    private void follow() {
        Log.d(TAG, "follow: called.");
        // Follow in the DB
        mRef
                .child(getString(R.string.db_field_following))
                .child(uid)
                .child(mViewingUser.getUser_id())
                .child(getString(R.string.db_field_user_id))
                .setValue(mViewingUser.getUser_id());

        mRef
                .child(getString(R.string.db_field_followers))
                .child(mViewingUser.getUser_id())
                .child(uid)
                .child(getString(R.string.db_field_user_id))
                .setValue(uid);

        // Update Counter
        mFollowersCount++;
        // Update mFollowers[TV]
        mFollowers.setText(String.valueOf(mFollowersCount));
        if (!followersIds.contains(uid))
            followersIds.add(uid);
    }

    private void init() {
        // Ser the profile widgets
        Query query = mRef.child(getString(R.string.db_user_account_settings))
                .orderByChild(getString(R.string.db_field_user_id)).equalTo(mViewingUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnap : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found a user: " + singleSnap.getValue(UserAccountSettings.class).toString());

                    UserSettings settings = new UserSettings();
                    settings.setUser(mViewingUser);
                    settings.setSettings(singleSnap.getValue(UserAccountSettings.class));
                    setProfileWidgets(settings);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // Get the users profile photos
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<Photo> photos = new ArrayList<>();
                final ArrayList<String> placeIds = new ArrayList<>();
                photos.addAll(mFireMethods.getUserPhotos(dataSnapshot, mViewingUser.getUser_id()));
                for (Photo photo: photos) { placeIds.add(photo.getPlace_id());}


                // Setup user's image grid
                int gridWidth = mContext.getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth / NUM_GRIDS_COLUMNS;
                gridView.setColumnWidth(imageWidth);

                ArrayList<String> imgUrls = new ArrayList<>();
                for (int i = 0; i < photos.size(); i++) {
                    imgUrls.add(photos.get(i).getImage_path());
                }

                // TODO(2) Load the photos faster
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
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: DatabaseError: " + databaseError.getMessage());
            }
        });

    }

    private void setupFollowers() {
        mFollowersCount = 0;
        followersIds = new ArrayList<>();

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot followerDS: dataSnapshot
                        .child(mContext.getString(R.string.db_field_followers))
                        .child(mViewingUser.getUser_id()).getChildren()) {
                    followersIds.add(followerDS.getKey());
                }
                mFollowersCount = followersIds.size();
                mFollowers.setText(String.valueOf(mFollowersCount));
                mFireMethods.updateFollowers(mFollowersCount);
                mFollowers.setOnClickListener(ViewProfileFragment.this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: FollowersDBError: " + databaseError.getMessage());
            }
        });

//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//        Query query = reference.child(getString(R.string.db_field_followers))
//                .child(mViewingUser.getUser_id());
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    mFollowersCount = Math.toIntExact(dataSnapshot.getChildrenCount());
//                } else {
//                    for (DataSnapshot singleSnap : dataSnapshot.getChildren()) {
//                        Log.d(TAG, "onDataChange: found a follower: " + singleSnap.getValue());
//                        mFollowersCount++;
//                    }
//                }
//                mFollowers.setText(String.valueOf(mFollowersCount));
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

    private void setupFollowing() {
        mFollowingsCount = 0;
        followingsIDs = new ArrayList<>();

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot followingDS: dataSnapshot
                        .child(mContext.getString(R.string.db_field_following))
                        .child(mViewingUser.getUser_id()).getChildren()) {
                    followingsIDs.add(followingDS.getKey());
                }
                mFollowingsCount = followingsIDs.size();
                mFollowing.setText(String.valueOf(mFollowingsCount));
                mFireMethods.updateFollowings(mFollowingsCount);
                mFollowing.setOnClickListener(ViewProfileFragment.this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: FollowingDBError: " + databaseError.getMessage());
            }
        });


//        mFollowingsCount = 0;
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//        Query query = reference.child(getString(R.string.db_field_following))
//                .child(mViewingUser.getUser_id());
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    mFollowingsCount = Math.toIntExact(dataSnapshot.getChildrenCount());
//                } else {
//                    for (DataSnapshot singleSnap : dataSnapshot.getChildren()) {
//                        Log.d(TAG, "onDataChange: found a following: " + singleSnap.getValue());
//                        mFollowingsCount++;
//                    }
//                }
//                mFollowing.setText(String.valueOf(mFollowingsCount));
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

    private void setupPostsCount() {
        mPostsCount = 0;
        Query query = mRef.child(getString(R.string.db_user_photos))
                .child(mViewingUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mPostsCount = Math.toIntExact(dataSnapshot.getChildrenCount());
                } else {
                    for (DataSnapshot singleSnap : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: found a post: " + singleSnap.getValue());
                        mPostsCount++;
                    }
                }
                mPosts.setText(String.valueOf(mPostsCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setFollowing() {
        Log.d(TAG, "setFollowing: updating UI for following this user.");
        mFollow.setVisibility(GONE);
        mUnfollow.setVisibility(View.VISIBLE);
        editProfile.setVisibility(GONE);
    }

    private void setUnfollowing() {
        Log.d(TAG, "setFollowing: updating UI for unfollowing this user.");
        mFollow.setVisibility(View.VISIBLE);
        mUnfollow.setVisibility(GONE);
        editProfile.setVisibility(GONE);
    }

    private void setCurrentUserProfile() {
        Log.d(TAG, "setFollowing: updating UI for showing this user own profile.");
        mFollow.setVisibility(GONE);
        mUnfollow.setVisibility(GONE);
        editProfile.setVisibility(View.VISIBLE);
    }

    private void isFollowing() {
        Log.d(TAG, "isFollowing: checking if follojwing this user.");
        setUnfollowing();

        Query query = mRef.child(getString(R.string.db_field_following))
                .child(uid)
                .orderByChild(getString(R.string.db_field_user_id)).equalTo(mViewingUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnap : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found a user: " + singleSnap.getValue());
                    setFollowing();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setupImagesGrid(final ArrayList<Photo> photos, ArrayList<String> placesIds) {
        // setup our image grid
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth / NUM_GRIDS_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        ArrayList<String> imgUrls = new ArrayList<>();
        for (int i = 0; i < photos.size(); i++) {
            imgUrls.add(photos.get(i).getImage_path());
        }
        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview,
                "", imgUrls, placesIds);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mOnGridImageSelectedListener.onGridSelected(photos.get(position), ACTIVITY_NUM);
            }
        });
    }

    private User getUserFromBundle() {
        Log.d(TAG, "getUserFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.intent_user));
        } else {
            return null;
        }

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

    private void setProfileWidgets(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidgets: setting widgets with datas retrieving from firebase: " + userSettings.toString());

        final User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(mContext, user.getProfile_photo(), mProfileIV, profileImagePB, "");
        mProfileIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UniversalImageLoader.bigPhoto(mContext, getActivity(), user.getProfile_photo());
            }
        });
        mUserName.setText(user.getName());

        if (settings.getDisplayName() != null && settings.getDisplayName().length() > 0) {
            mDisplayName.setText(settings.getDisplayName());
            mDisplayName.setVisibility(View.VISIBLE);
        } else mDisplayName.setVisibility(GONE);
        if (settings.getWebsite() != null && settings.getWebsite().length() > 0) {
            mWebsite.setText(settings.getWebsite());
            mWebsite.setVisibility(View.VISIBLE);
        }
        else mWebsite.setVisibility(GONE);
        if (settings.getDescription() != null && settings.getDescription().length() > 0) {
            mDescription.setText(settings.getDescription());
            mDescription.setVisibility(View.VISIBLE);
        }
        else mDescription.setVisibility(GONE);

        mDescription.setText(settings.getDescription());
        mPosts.setText(String.valueOf(settings.getPosts()));
        mFollowers.setText(String.valueOf(settings.getFollowers()));
        mFollowing.setText(String.valueOf(settings.getFollowing()));

        mProgressBar.setVisibility(GONE);

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStack();
                getActivity().finish();
            }
        });
    }




    /*
     * ----------------------------- Firebase ------------------------------------------
     */


    private void setupFirebaseStaff() {
        Log.d(TAG, "setupFirebaseStaff: called.");

        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();
        uid = mAuth.getCurrentUser().getUid();

        // Init mAuthStateListener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
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
