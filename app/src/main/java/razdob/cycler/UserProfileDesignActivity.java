package razdob.cycler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;
import razdob.cycler.instProfile.AccountSettingsActivity;
import razdob.cycler.models.User;
import razdob.cycler.models.UserAccountSettings;
import razdob.cycler.myUtils.RazUtils;
import razdob.cycler.myUtils.UniversalImageLoader;

/**
 * Created by Raz on 11/07/2018, for project: PlacePicker2
 */
public class UserProfileDesignActivity extends AppCompatActivity{
    private static final String TAG = "UserProfileDesActivity";
    private Context mContext = UserProfileDesignActivity.this;

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private StorageReference mStorageRef;
    /* ---------------------- FIREBASE ----------------------- */

    // User Details
    private User mUser;
    private UserAccountSettings mUserAccountSettings;


    // Widgets
    private TextView topName, postsTV, followersTV, followingsTV, editProfileTV;
    private TextView displayName, description, website;
    private ImageView menuIV;
    private CircleImageView profileIV;
    private GridView gridView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);


        matchWidgetsToIds();
        setupClickListeners();
        setupFirebaseStaff();
    }

    private void matchWidgetsToIds() {
        topName = findViewById(R.id.profile_name_tv);
        profileIV = findViewById(R.id.profile_image);
        postsTV = findViewById(R.id.tv_posts);
        followersTV = findViewById(R.id.tv_followers);
        followingsTV = findViewById(R.id.tv_following);
        editProfileTV = findViewById(R.id.text_edit_profile);
        displayName = findViewById(R.id.display_name);
        description = findViewById(R.id.description);
        website = findViewById(R.id.website);
        gridView = findViewById(R.id.gridView);
        menuIV = findViewById(R.id.profileMenu_iv);
        progressBar = findViewById(R.id.profileProgressBar);
    }

    private void initWidgetsDetails(User user, UserAccountSettings accountSettings) {
        displayName.setText(accountSettings.getDisplayName());
        topName.setText(user.getName());
        postsTV.setText(String.valueOf(accountSettings.getPosts()));
        followingsTV.setText(String.valueOf(accountSettings.getFollowing()));
        followersTV.setText(String.valueOf(accountSettings.getFollowers()));
        description.setText(accountSettings.getDescription());
        website.setText(accountSettings.getWebsite());
        website.setContentDescription(accountSettings.getWebsite());
        UniversalImageLoader.setImage(mContext, user.getProfile_photo(), profileIV, null, "");
        // TODO(BETA) Init grid layout when they will be images... [fast loading]


        progressBar.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        editProfileTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to " + mContext.getString(R.string.edit_profile));
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        menuIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to account settings");
                startActivity(new Intent(mContext, AccountSettingsActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RazUtils.openUrl(mContext, website.getContentDescription().toString());
            }
        });
    }


    /* --------------------------------- Firebase ------------------------------------------*/

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
        Log.d(TAG, "setupFirebaseStaff: called.");

        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();
        FirebaseStorage storage = FirebaseStorage.getInstance(mFireApp, "gs://cyclerproject.appspot.com");
        mStorageRef = storage.getReference();


        // Init mAuthStateListener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                // Check if user is logged in

                if (user == null) {
                    Log.d(TAG, "onAuthStateChanged: User log-out");
                    mAuth.removeAuthStateListener(mAuthListener);
                    startActivity(new Intent(mContext, MainRegisterActivity.class));
                } else {
                    Log.d(TAG, "onAuthStateChanged: User is logged-in :) uid = " + user.getUid());
                }
            }
        };

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String uid = mAuth.getCurrentUser().getUid();
                mUser = dataSnapshot.child(getString(R.string.db_persons)).child(uid)
                        .getValue(User.class);
                mUserAccountSettings = dataSnapshot.child(getString(R.string.db_user_account_settings))
                        .child(uid).getValue(UserAccountSettings.class);
                if (mUserAccountSettings == null)
                    mUserAccountSettings = new UserAccountSettings(mUser.getName(), uid);

                initWidgetsDetails(mUser, mUserAccountSettings);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: DB_Error: "+ databaseError.getMessage());
                progressBar.setVisibility(View.GONE);
                Toast.makeText(mContext, "An error occur", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
