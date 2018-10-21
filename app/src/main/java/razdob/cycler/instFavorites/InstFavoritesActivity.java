package razdob.cycler.instFavorites;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import razdob.cycler.R;
import razdob.cycler.myUtils.BottomNavigationViewHelper;
import razdob.cycler.myUtils.FirebaseMethods;

/**
 * Created by Raz on 27/05/2018, for project: PlacePicker2
 */
public class InstFavoritesActivity extends AppCompatActivity {

    private static final String TAG = "InstFavoritesActivity";
    public static final int ACTIVITY_NUM = 3;

    private FirebaseMethods mFireMethods;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instagram_data);
        Log.d(TAG, "onCreate: starting:");
        mFireMethods = new FirebaseMethods(InstFavoritesActivity.this);

        setupBottomNavigationView();
    }

    /*
     * BottomNavigationView setup
     * */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(this, this, bottomNavigationViewEx, mFireMethods.getFavoritePlacesIds());
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
