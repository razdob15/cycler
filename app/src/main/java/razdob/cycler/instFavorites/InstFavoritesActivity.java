package razdob.cycler.instFavorites;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import razdob.cycler.R;
import razdob.cycler.fivePlaces.FivePlacesActivity;
import razdob.cycler.myUtils.BottomNavigationViewHelper;
import razdob.cycler.myUtils.FirebaseMethods;

/**
 * Created by Raz on 27/05/2018, for project: PlacePicker2
 */
public class InstFavoritesActivity extends AppCompatActivity {

    private static final String TAG = "InstFavoritesActivity";
    public static final int ACTIVITY_NUM = 3;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instagram_data);
        Log.d(TAG, "onCreate: starting:");

        BottomNavigationViewHelper.setupBottomNavigationView(InstFavoritesActivity.this, InstFavoritesActivity.this, ACTIVITY_NUM);
    }

}
