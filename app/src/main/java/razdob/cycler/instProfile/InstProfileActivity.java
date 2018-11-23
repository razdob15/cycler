package razdob.cycler.instProfile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import razdob.cycler.R;
import razdob.cycler.models.User;
import razdob.cycler.myUtils.BottomNavigationViewHelper;
import razdob.cycler.ViewInstCommentsFragment;
import razdob.cycler.ViewPostFragment;
import razdob.cycler.models.Photo;
import razdob.cycler.ViewProfileFragment;

/**
 * Created by Raz on 27/05/2018, for project: PlacePicker2
 */
public class InstProfileActivity extends AppCompatActivity implements
        ProfileFragment.OnGridImageSelectedListener,
        ViewProfileFragment.OnGridImageSelectedListener,
        ViewPostFragment.OnCommentThreadSelectedListener {
    private static final String TAG = "InstProfileActivity";

    // Intent Extras
    private static final String CALLING_ACTIVITY_EXTRA = "calling_activity";
    private static final String USER_EXTRA = "intent_user";
    private static final String ACTIVITY_NUM_EXTRA = "activity_num";
    private static final String NEW_PROFILE_EXTRA = "new_profile";

    // Finals
    private final Context mContext = InstProfileActivity.this;
    private final FragmentActivity mActivity = InstProfileActivity.this;


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


    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private String uid;
    /* ---------------------- FIREBASE ----------------------- */

    // Intent Vars
    private int mActivityNum = 4;
    private User user;
    private Bitmap profileBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inst_profile);
        Log.d(TAG, "onCreate: starting:");

        mFireApp = FirebaseApp.getInstance("mFireApp");
        uid = Objects.requireNonNull(FirebaseAuth.getInstance(mFireApp).getCurrentUser()).getUid();

        getDataFromIntent();
        BottomNavigationViewHelper.setupBottomNavigationView(mContext, mActivity, mActivityNum);
        showCorrectFragment();
    }

    private void showCorrectFragment() {
        if (user != null) {
            if (user.getUser_id().equals(Objects.requireNonNull(FirebaseAuth.getInstance(mFireApp).getCurrentUser()).getUid())) {
                // This is the Current User !
                Log.d(TAG, "init: inflating user's Profile");
                ProfileFragment.showProfile(mActivity);

            } else {
                Log.d(TAG, "init: inflating ViewProfile");
                ViewProfileFragment.show(mActivity, user);
            }
        } else {
            ProfileFragment.showProfile(mActivity);
        }
    }

    private void getDataFromIntent() {
        Log.d(TAG, "getDataFromIntent: called.");
        Intent intent = getIntent();

        if (intent.hasExtra(CALLING_ACTIVITY_EXTRA)) {
            Log.d(TAG, "init: searching for user object attached as intent extra.");
            if (intent.hasExtra(USER_EXTRA)) {
                user = intent.getParcelableExtra(USER_EXTRA);
            } else {
                Log.d(TAG, "init: Not valid extras. User is NULL");
                Toast.makeText(mContext, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "init: inflating Profile");
        }
        int activityNum = intent.getIntExtra(ACTIVITY_NUM_EXTRA, -1);
        if (activityNum != -1 && !user.getUser_id().equals(uid))
            mActivityNum = activityNum;
        profileBitmap = intent.getParcelableExtra(NEW_PROFILE_EXTRA);
    }


    public static void start(Context context, String callingActivity, User user, int activityNum, Bitmap newProfileBitmap) {
        Log.d(TAG, "start: callingActivity: " + callingActivity + "; User: " + user);

        Intent intent = new Intent(context, InstProfileActivity.class);
        if (callingActivity != null) intent.putExtra(CALLING_ACTIVITY_EXTRA, callingActivity);
        if (user != null) intent.putExtra(USER_EXTRA, user);
        if (activityNum >= 0) intent.putExtra(ACTIVITY_NUM_EXTRA, activityNum);
        if (newProfileBitmap != null) intent.putExtra(NEW_PROFILE_EXTRA, newProfileBitmap);
        context.startActivity(intent);
    }
    public static void start(Context context, String callingActivity, User user, int activityNum) { start(context, callingActivity, user, activityNum, null); }
    public static void start(Context context) { start(context, null, null, -1); }

}


