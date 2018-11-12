package razdob.cycler.instHome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nostra13.universalimageloader.core.ImageLoader;

import razdob.cycler.MainRegisterActivity;
import razdob.cycler.R;
import razdob.cycler.models.Photo;
import razdob.cycler.myUtils.BottomNavigationViewHelper;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.adapters.MainFeedListAdapter;
import razdob.cycler.myUtils.SectionPagerAdapter;
import razdob.cycler.myUtils.UniversalImageLoader;
import razdob.cycler.ViewInstCommentsFragment;

/**
 * Created by Raz on 27/05/2018, for project: PlacePicker2
 */
public class InstHomeActivity extends AppCompatActivity
        implements MainFeedListAdapter.OnLoadMoreItemsListener{

    @Override
    public void onLoadMoreItems() {
        Log.d(TAG, "onLoadMoreItems: displaying more photos");
        HomeFragment fragment = (HomeFragment) getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" + mViewPage.getCurrentItem());
        if (fragment != null) {
            fragment.displayMorePhotos();
        }
    }

    private static final String TAG = "InstHomeActivity";
    private static final int ACTIVITY_NUM = 0;
    private static final int HOME_FRAGMENT = 1;

    // Firebase Staff
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods mFireMethods;


    private Context mContext = InstHomeActivity.this;

    // Widgets
    private ViewPager mViewPage;
    private FrameLayout mFrameLayout;
    private RelativeLayout mRelLayout;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instagram_data);
        Log.d(TAG, "onCreate: starting:");

        mFireMethods = new FirebaseMethods(mContext);

        mViewPage = findViewById(R.id.viewpager_container);
        mFrameLayout = findViewById(R.id.container);
        mRelLayout = findViewById(R.id.relLayoutParent);

        setupFirebaseStaff();
        initImageLoader();
        mFireMethods = new FirebaseMethods(InstHomeActivity.this);
        BottomNavigationViewHelper.setupBottomNavigationView(mContext, InstHomeActivity.this, ACTIVITY_NUM);

        setupViewPager();
    }

    public void onCommentThreadSelected(Photo photo, String callingActivity) {
        Log.d(TAG, "onCommentThreadSelected: selected a comment thread");
        ViewInstCommentsFragment fragment = new ViewInstCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.bundle_photo), photo);
        args.putString(getString(R.string.home_activity), getString(R.string.home_activity));
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_inst_comment_fragment));
        transaction.commit();
    }

    public void hideLayout() {
        Log.d(TAG, "hideLayout: hiding layout");
        mRelLayout.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mFrameLayout.getVisibility() == View.VISIBLE) {
            showLayout();
        }
    }

    public void showLayout() {
        Log.d(TAG, "showLayout: showing layout");
        mRelLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
    }

    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(this);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    /**
    * Responsible for adding the 3 tabs: Camera, Home, Messages
    * */
    private void setupViewPager() {
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new CameraFragment());      // Index 0
        adapter.addFragment(new HomeFragment());        // Index 1
        adapter.addFragment(new MessagesFragment());    // Index 2

        mViewPage.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs_top);
        tabLayout.setupWithViewPager(mViewPage);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_camera);
        tabLayout.getTabAt(1).setIcon(R.drawable.cycler_logo);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_arrow);
    }

    /*
     * ------------------------- Firebase ------------------------------------------
     */

    /**
     * Checks to see if the @param 'user' is logged in
     * @param fireUser
     */
    private void checkCurrentUser(FirebaseUser fireUser) {
        Log.d(TAG, "checkCurrentUser: checking if the user is logged in");
        if (fireUser == null) {
            startActivity(new Intent(mContext, MainRegisterActivity.class));
        }
    }

    private void setupFirebaseStaff() {
        FirebaseApp mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);

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
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        mViewPage.setCurrentItem(HOME_FRAGMENT);
        checkCurrentUser(mAuth.getCurrentUser());

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAuthListener != null)
            mAuth.removeAuthStateListener(mAuthListener);
    }



}

