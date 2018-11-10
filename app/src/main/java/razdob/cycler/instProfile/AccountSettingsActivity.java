package razdob.cycler.instProfile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import razdob.cycler.R;
import razdob.cycler.SupportFragment;
import razdob.cycler.myUtils.BottomNavigationViewHelper;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.SectionsStatePagerAdapter;

/**
 * Created by Raz on 31/05/2018, for project: PlacePicker2
 */
public class AccountSettingsActivity extends AppCompatActivity {
    private static final String TAG = "AccountSettingsActivity";
    private static final int ACTIVITY_NUM = 4;

    public static final int EDIT_PROFILE_FRAGMENT_NUM = 0;
    public static final int SUPPORT_FRAGMENT_NUM = 1;
    public static final int SIGN_OUT_FRAGMENT_NUM = 2;

    private Context mContext;

    public SectionsStatePagerAdapter pagerAdapter;
    private ViewPager mViewPager;
    private RelativeLayout mRelativeLayout;

    // Firebase
    private FirebaseMethods mFireMethods;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        mContext = AccountSettingsActivity.this;
        Log.d(TAG, "onCreate: started");

        mViewPager = findViewById(R.id.viewpager_container);
        mRelativeLayout = findViewById(R.id.rel_layout1);
        mFireMethods = new FirebaseMethods(mContext);

        setupSettingsList();

        BottomNavigationViewHelper.setupBottomNavigationView(mContext, AccountSettingsActivity.this, ACTIVITY_NUM);

        setupFragments();
        getIncomingIntent();

        //setup the backArrow for navigating back to "InstProfileActivity"
        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to 'InstProfileActivity'");
                finish();
            }
        });

    }

    private void getIncomingIntent() {
        Intent intent = getIntent();

        if (intent.hasExtra(getString(R.string.selected_image))
                || intent.hasExtra(getString(R.string.selected_bitmap))) {

            // if there is an imageUrl attached asan extra , than it was chosen from the gallery/photo fragment
            Log.d(TAG, "getIncomingIntent: New incoming imageUrl");
            if (intent.getStringExtra(getString(R.string.return_to_fragment)).equals(getString(R.string.edit_profile_fragment))) {
                if (intent.hasExtra(getString(R.string.selected_image))) {
                    // set the new profile image
                    FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null, 0,
                            intent.getStringExtra(getString(R.string.selected_image)), null, null);

                } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                    // set the new profile image
                    FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null, 0,
                            null, (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap)), null);
                }
            }
        }

        if (intent.hasExtra(mContext.getString(R.string.calling_activity))) {
            Log.d(TAG, "getIncomingIntent: received incoming intent from: " + intent.getStringExtra(mContext.getString(R.string.calling_activity)));

            Fragment fragment = pagerAdapter.getItem(EDIT_PROFILE_FRAGMENT_NUM);
            Bundle bundle = new Bundle();
            bundle.putBoolean(mContext.getString(R.string.intent_first_time), intent.getBooleanExtra(mContext.getString(R.string.intent_first_time), false));
            fragment.setArguments(bundle);

            setViewPager(EDIT_PROFILE_FRAGMENT_NUM);
        }
    }

    private void setupFragments() {
        // TODO(!) Check the change (Antonio crash)
        pagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new EditProfileFragment(), getString(R.string.edit_profile));  // Fragment 0
        pagerAdapter.addFragment(new SupportFragment(), mContext.getString(R.string.support_and_feedback));   // Fragment 1
        pagerAdapter.addFragment(new SignOutFragment(), getString(R.string.sign_out));          // Fragment 2
    }

    public void setViewPager(int fragmentNumber) {
        mRelativeLayout.setVisibility(View.GONE);
        Log.d(TAG, "setViewPager: navigating to fragment #" + fragmentNumber);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(fragmentNumber);
    }

    private void setupSettingsList() {
        Log.d(TAG, "setupSettingsList: initializing 'Account Settings' list");
        ListView listView = findViewById(R.id.lvAccountsSettings);

        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.edit_profile));          // Fragment 0
        options.add(getString(R.string.support_and_feedback));  // Fragment 1
        options.add(getString(R.string.sign_out));              // Fragment 2

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: navigating to fragment #" + position);
                setViewPager(position);
            }
        });
    }
}

