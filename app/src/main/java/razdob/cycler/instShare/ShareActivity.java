package razdob.cycler.instShare;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.Objects;

import razdob.cycler.R;
import razdob.cycler.myUtils.BottomNavigationViewHelper;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.Permissions;
import razdob.cycler.myUtils.SectionPagerAdapter;

/**
 * Created by Raz on 07/06/2018, for project: PlacePicker2
 */
public class ShareActivity extends AppCompatActivity {
    private static final String TAG = "ShareActivity";
    private final Context mContext = ShareActivity.this;

    // TODO(!): Need redesign? open camera immediately?


    //constants
    private static final int ACTIVITY_NUM = 2;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    private ViewPager mViewPager;

    // Vars
    private FirebaseMethods firebaseMethods;
    private Intent mIntent;
    private String placeId, placeName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Log.d(TAG, "onCreate: started.");

        firebaseMethods = new FirebaseMethods(mContext);
        setupBottomNavigationView();

        if (mIntent == null) mIntent = getIntent();
        if (mIntent.hasExtra(getString(R.string.intent_place_id))) {
            placeId = mIntent.getStringExtra(getString(R.string.intent_place_id));
        } if (mIntent.hasExtra(mContext.getString(R.string.intent_place_name))) {
            placeName = mIntent.getStringExtra(mContext.getString(R.string.intent_place_name));
        }


        if (checkPermissionsArray(Permissions.PERMISSIONS)) {
            setupViewPager();
        } else {
            verifyPermissions(Permissions.PERMISSIONS);
        }

    }

    /**
     * Returns the current tab number
     * 0 = GalleryFragment
     * 1 = PhotoFragment
     *
     * @return int
     */
    public int getCurrentTabNumber() {
        return mViewPager.getCurrentItem();
    }

    public int getTask() {
        if (mIntent == null) mIntent = getIntent();
        Log.d(TAG, "getTask: TASK: " + mIntent.getFlags());
        return mIntent.getFlags();
    }

    /**
     * Setup viewpager for manager the tabs
     */
    private void setupViewPager() {
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        Bundle bundle = new Bundle();
        bundle.putString(mContext.getString(R.string.intent_place_id), placeId);
        bundle.putString(mContext.getString(R.string.intent_place_name), placeName);

        MyGalleryFragment galleryFragment = new MyGalleryFragment();
        PhotoFragment photoFragment = new PhotoFragment();

        galleryFragment.setArguments(bundle);
        photoFragment.setArguments(bundle);

        adapter.addFragment(photoFragment);   // Index 0
        adapter.addFragment(galleryFragment); // Index 1

        mViewPager = findViewById(R.id.viewpager_container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs_bottom);
        tabLayout.setupWithViewPager(mViewPager);

        Objects.requireNonNull(tabLayout.getTabAt(0)).setText(getString(R.string.gallery));
        tabLayout.getTabAt(1).setText(getString(R.string.photo));
    }

    /**
     * verifiy all the permissions passed to the array
     *
     * @param permissions
     */
    public void verifyPermissions(String[] permissions) {
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        ActivityCompat.requestPermissions(ShareActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }

    /**
     * Check an array of permissions
     *
     * @param permissions
     * @return
     */
    public boolean checkPermissionsArray(String[] permissions) {
        Log.d(TAG, "checkPermissionsArray: checking permissions array.");

        for (String check : permissions) {
            if (!checkPermissions(check)) {
                return false;
            }
        }
        return true;


    }

    /**
     * Check a single permission is it has been verified
     *
     * @param permission
     * @return
     */
    public boolean checkPermissions(String permission) {
        Log.d(TAG, "checkPermissions: checking permission: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermissions: \n Permission was not granted for: " + permission);
            return false;
        } else {
            Log.d(TAG, "checkPermissions: \n Permission was granted for: " + permission);
            return true;
        }
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(this, this, bottomNavigationViewEx, firebaseMethods.getFavoritePlacesIds());
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}