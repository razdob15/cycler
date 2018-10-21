package razdob.cycler.instProfile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import razdob.cycler.R;
import razdob.cycler.models.User;
import razdob.cycler.myUtils.ViewInstCommentsFragment;
import razdob.cycler.myUtils.ViewPostFragment;
import razdob.cycler.models.Photo;
import razdob.cycler.myUtils.ViewProfileFragment;

/**
 * Created by Raz on 27/05/2018, for project: PlacePicker2
 */
public class InstProfileActivity extends AppCompatActivity implements
        ProfileFragment.OnGridImageSelectedListener,
        ViewProfileFragment.OnGridImageSelectedListener,
        ViewPostFragment.OnCommentThreadSelectedListener {

    private static final String TAG = "InstProfileActivity";

    @Override
    public void onCommentThreadSelectedListener(Photo photo) {
        Log.d(TAG, "onCommentThreadSelectedListener: selected a comment thread");

        ViewInstCommentsFragment fragment = new ViewInstCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.bundle_photo), photo);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_inst_comment_fragment));
        transaction.commit();

    }

    @Override
    public void onGridSelected(Photo photo, int activityNumber) {
        Log.d(TAG, "onGridSelected: selected an image gridView: " + photo.toString());

        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putInt(getString(R.string.activity_number), activityNumber);


        // todo(1) check this args... open photo quickly
        args.putString(getString(R.string.intent_selected_photo_uri), photo.getImage_path());
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();

    }

    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;
    private Context mContext = InstProfileActivity.this;

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mFireUser;
    private StorageReference mStorageReference;// = FirebaseStorage.getInstance().getReference();
    private DatabaseReference mRef;
    private ValueEventListener mValueListener;
    /* ---------------------- FIREBASE ----------------------- */

    // GUI
    private ProgressBar mProgressBar;
    private ImageView mProfileImage;
    private String mImgUrl;
    private TextView mDisplayNameTV;
    private TextView mDescriptionTV;
    private TextView mWebsiteTV;
    private TextView mProfileNameTV;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inst_profile);
        Log.d(TAG, "onCreate: starting:");

        init();


/*
        matchWidgetsToIds();

        // Firebase...
        setupFirebaseAuth();
        setupFirebaseDB();
        searchImageInStorage();

        //
        setupBottomNavigationView();
        setupToolbar();
        setupActivityWidgets();


        // Temporary !!
        tempGridSetup();*/
    }



    private void init() {
        Log.d(TAG, "init: iInflating " + getString(R.string.profile));

        mFireApp = FirebaseApp.getInstance("mFireApp");

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.calling_activity))) {
            Log.d(TAG, "init: searching for user object attached as intent extra.");
            if (intent.hasExtra(getString(R.string.intent_user))) {
                User user = intent.getParcelableExtra(getString(R.string.intent_user));

                if (user.getUser_id() == null || !user.getUser_id().equals(FirebaseAuth.getInstance(mFireApp).getCurrentUser().getUid())) {


                    Log.d(TAG, "init: inflating ViewProfile");
                    ViewProfileFragment fragment = new ViewProfileFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(getString(R.string.intent_user),
                            intent.getParcelableExtra(getString(R.string.intent_user)));
                    fragment.setArguments(args);

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    transaction.addToBackStack(getString(R.string.view_inst_profile_fragment));
                    transaction.commit();
                } else {
                    Log.d(TAG, "init: inflating Profile");
                    ProfileFragment fragment = new ProfileFragment();
                    FragmentTransaction transaction = InstProfileActivity.this.getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    transaction.addToBackStack(getString(R.string.profile));
                    transaction.commit();
                }
            } else {
                Log.d(TAG, "init: Not valid extras");
                Toast.makeText(mContext, "something went wrong", Toast.LENGTH_SHORT).show(); 
            }
        } else {
            Log.d(TAG, "init: inflating Profile");
            ProfileFragment fragment = new ProfileFragment();
            FragmentTransaction transaction = InstProfileActivity.this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment);
            transaction.addToBackStack(getString(R.string.profile));
            transaction.commit();
        }
    }

    @Override
    public void onBackPressed() { }
}


