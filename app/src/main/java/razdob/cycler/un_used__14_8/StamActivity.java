
package razdob.cycler.un_used__14_8;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import razdob.cycler.R;

import static com.google.android.gms.location.places.Place.TYPE_RESTAURANT;


// TODO: Delete this activity

//Show data activity
public class StamActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener{


    private GoogleApiClient mGoogleApiClient;

    // The SharedPreferences
    SharedPreferences mFavSubjects;

    //Main Layout of the screen
    LinearLayout mData_ll;


    //Progress Bar
    private ProgressBar mProgressBar;
    private int mProgressStatus = 0;
    private Handler mHandler = new Handler();

    PlaceDetectionClient mPlaceDetectionClient;

    // Permission finals, no_meaning numbers
    private final static int MY_PERMISSION_FINE_LOCATION = 101;
    private final static int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // User preferences:
        mFavSubjects = getSharedPreferences("favorite_subjects", 0);
        //Defined below - Request the permission...
        requestPermission();

        //GUI - Assignment the details
        setContentView(R.layout.activity_main_stam);



        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();


        mData_ll = findViewById(R.id.linear_layout_data);
        mData_ll.setVisibility(View.INVISIBLE);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);



        mProgressBar = findViewById(R.id.progressBar);

        // to do (1): Do the progressBar more effective ! (Not like that XD) look at the registerActivity
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mProgressStatus < 100){
                    mProgressStatus++;
                    android.os.SystemClock.sleep(10);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setProgress(mProgressStatus);
                            Log.i("mHandler1", "RRRRRRUN1");
                        }
                    });
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                        mData_ll.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();



        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                    PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {

                        //Run on all this place's types
                        for (int type : placeLikelihood.getPlace().getPlaceTypes()) {
                            // If there is a common type between this place and mFireUser types
                            if (getUserTypes().contains(type)) {

                                //Add this place to the main layout
                                LinearLayout.LayoutParams lpPlace = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                mData_ll.addView(putPlaceView(placeLikelihood.getPlace()), lpPlace);
                                break;
                            }
                        }
                    }

                    // Prevents a memory leak
                    likelyPlaces.release();
                }
            });
        }
    }



    // Request the ACCESS_FINE_LOCATION permission.
    // This Function is useful in Marshmallow SDK
    private void requestPermission() {

        // If the permission ACCESS_FINE_LOCATION hasn't benn granted yet
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            //if Marshmallow or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // requestPermissions defined below
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_FINE_LOCATION);
            }
        }
    }


    // This function runs after the mFireUser decides if he grants the permission or not (דחה או התר)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSION_FINE_LOCATION:
                // If the Permission hasn't granted
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "This app requires location permissions to be granted", Toast.LENGTH_LONG).show();
                    //Close this activity
                    this.finish();
                }
                else {
                    startActivity(this.getIntent());
                }
                break;
        }
    }


    // Returns an arrayList contains all the types (according google)
    // that the mFireUser want to see in the last page.
    private ArrayList<Integer> getUserTypes() {
        ArrayList<Integer> userTypes = new ArrayList<>();

        if (mFavSubjects.getBoolean("fashion", false)) {
            userTypes.add(Place.TYPE_BEAUTY_SALON);
            userTypes.add(Place.TYPE_CLOTHING_STORE);
            userTypes.add(Place.TYPE_DEPARTMENT_STORE);
            userTypes.add(Place.TYPE_HAIR_CARE);
            userTypes.add(Place.TYPE_LAUNDRY);
            userTypes.add(Place.TYPE_SHOE_STORE);
            userTypes.add(Place.TYPE_SHOPPING_MALL);
        }
        if (mFavSubjects.getBoolean("animals", false)) {
            userTypes.add(Place.TYPE_PET_STORE);
            userTypes.add(Place.TYPE_PARK);
            userTypes.add(Place.TYPE_VETERINARY_CARE);
            userTypes.add(Place.TYPE_ZOO);
        }
        if (mFavSubjects.getBoolean("books", false)) {
            userTypes.add(Place.TYPE_LIBRARY);
            userTypes.add(Place.TYPE_BOOK_STORE);
        }
        if (mFavSubjects.getBoolean("bars", false)) {
            userTypes.add(Place.TYPE_BAR);
            userTypes.add(Place.TYPE_LIQUOR_STORE);
            userTypes.add(Place.TYPE_NIGHT_CLUB);
        }
        if (mFavSubjects.getBoolean("food", false)) {
            userTypes.add(Place.TYPE_RESTAURANT);
            userTypes.add(Place.TYPE_FOOD);
            userTypes.add(Place.TYPE_BAKERY);
            userTypes.add(Place.TYPE_CAFE);
            userTypes.add(Place.TYPE_GROCERY_OR_SUPERMARKET);
            userTypes.add(Place.TYPE_MEAL_DELIVERY);
            userTypes.add(Place.TYPE_MEAL_TAKEAWAY);
        }
        if (mFavSubjects.getBoolean("cars", false)) {
            userTypes.add(Place.TYPE_CAR_DEALER);
            userTypes.add(Place.TYPE_CAR_RENTAL);
            userTypes.add(Place.TYPE_CAR_REPAIR);
            userTypes.add(Place.TYPE_CAR_WASH);
            userTypes.add(Place.TYPE_CONVENIENCE_STORE);
            userTypes.add(Place.TYPE_PARKING);
            userTypes.add(Place.TYPE_TAXI_STAND);
        }
        if (mFavSubjects.getBoolean("music", false)) {
            userTypes.add(Place.TYPE_DEPARTMENT_STORE);
        }
        if (mFavSubjects.getBoolean("museums", false)) {
            userTypes.add(Place.TYPE_MUSEUM);
        }
        if (mFavSubjects.getBoolean("parties", false)) {
            userTypes.add(Place.TYPE_LIQUOR_STORE);
            userTypes.add(Place.TYPE_NIGHT_CLUB);
        }
        if (mFavSubjects.getBoolean("vinyl_records", false)) {
            userTypes.add(Place.TYPE_HOME_GOODS_STORE);
            userTypes.add(Place.TYPE_STORE);
        }
        if (mFavSubjects.getBoolean("galleries", false)) {
            userTypes.add(Place.TYPE_ART_GALLERY);
            userTypes.add(Place.TYPE_MUSEUM);
        }
        if (mFavSubjects.getBoolean("sport", false)) {
            userTypes.add(Place.TYPE_BICYCLE_STORE);
            userTypes.add(Place.TYPE_BOWLING_ALLEY);
            userTypes.add(Place.TYPE_GYM);
            userTypes.add(Place.TYPE_HEALTH);
            userTypes.add(Place.TYPE_STADIUM);
        }
        return userTypes;
    }


    // Gets a place
    // Returns a LinearLayout contains the place's details
    public LinearLayout putPlaceView(Place place) {

        LinearLayout linearLayout = new LinearLayout(this);

        // TO DO (4): add a description and an image about the place
        // TO dO (2): Set clickListener to the places ll...
        // String phone = place.getPhone().toString();
        String name = place.getName().toString();
        String address = place.getAddress().toString();


        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        tvName.setTypeface(tvName.getTypeface(), Typeface.BOLD | Typeface.ITALIC);

        TextView tvAddress = new TextView(this);
        tvAddress.setText(address);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        LinearLayout.LayoutParams lpName = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lpName.setMargins(10, 15, 10, 15);

        LinearLayout.LayoutParams lpAddress = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lpAddress.setMargins(10, 5, 10, 30);

        linearLayout.addView(tvName, lpName);
        linearLayout.addView(tvAddress, lpAddress);

        linearLayout.setOrientation(LinearLayout.VERTICAL);

        return linearLayout;
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w("connection_failed", connectionResult.getErrorMessage());
    }
}