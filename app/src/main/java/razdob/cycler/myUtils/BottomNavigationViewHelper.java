package razdob.cycler.myUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

import razdob.cycler.feed.DataActivity;
import razdob.cycler.R;
import razdob.cycler.feed.HomeActivity;
import razdob.cycler.fivePlaces.FivePlacesActivity;
import razdob.cycler.instProfile.InstProfileActivity;
import razdob.cycler.instSearch.SearchUserActivity;
import razdob.cycler.instShare.MyShareActivity;
import razdob.cycler.instShare.ShareActivity;

/**
 * Created by Raz on 27/05/2018, for project: PlacePicker2
 */
public class BottomNavigationViewHelper {

    private static final String TAG = "BottomNavigationViewHel";

    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx) {
        Log.d(TAG, "setupBottomNavigationView: Setting up BottomNavigationView");
        if (bottomNavigationViewEx == null)
            return;
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
    }

    public static void enableNavigation(final Context context, final Activity callingActivity, BottomNavigationViewEx view, @NonNull final ArrayList<String> favoritePlacesIds) {
        if (view == null)
            return;
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ic_house:         // ITEM_ID = 0
//                        Intent intent1 = new Intent(context, DataActivity.class);
                        Intent intent1 = new Intent(context, HomeActivity.class);
                        context.startActivity(intent1);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                    case R.id.ic_search:        // ITEM_ID = 1
                        Intent intent2 = new Intent(context, SearchUserActivity.class);
                        context.startActivity(intent2);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                    case R.id.ic_share:        // ITEM_ID = 2
                        Log.d(TAG, "onNavigationItemSelected: click on item2");
                        context.startActivity(new Intent(context, MyShareActivity.class));
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                    case R.id.ic_likes:         // ITEM_ID = 3
                        Intent intent = new Intent(context, FivePlacesActivity.class);
//                        intent.putStringArrayListExtra(context.getString(R.string.intent_love_places_ids), favoritePlacesIds);
                        context.startActivity(intent);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                    case R.id.ic_profile:     // ITEM_ID = 4
                        context.startActivity(new Intent(context, InstProfileActivity.class));
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                }


                return false;
            }
        });

    }

}