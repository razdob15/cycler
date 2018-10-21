package razdob.cycler.un_used__14_8;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import razdob.cycler.MainRegisterActivity;
import razdob.cycler.R;
import razdob.cycler.dialogs.NotRestaurantDialog;
import razdob.cycler.fivePlaces.FivePlacesActivity;
import razdob.cycler.myUtils.StringManipulation;

/**
 * Created by Raz on 08/03/2018, for project: PlacePicker2
 */

public class TestGoogleMaps extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "TestGoogleMaps";

    // TODO(2): Open in the place latLng (If not null)

    private static final int PLACE_PICKER_REQUEST = 1000;
    private GoogleApiClient mClient;
    private Context mContext = TestGoogleMaps.this;

    // Firebase Staff
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    // Vars
    private String uid;
    private String mapType = "";
    private GeoDataClient mGeoDataClient;
    private ArrayList<String> favoritePlacesIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_map_activity);
        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.intent_map_type))) {
            mapType = intent.getStringExtra(getString(R.string.intent_map_type));
        }


        setupFirebaseStaff();
        mGeoDataClient = Places.getGeoDataClient(TestGoogleMaps.this);
        mClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        uid = getIntent().getStringExtra("user_uid");

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(TestGoogleMaps.this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    // TODO(!): Focus the map on the current business place LatLng !

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                final Place place = PlacePicker.getPlace(data, this);
                Log.d(TAG, "onActivityResult: PlaceName(1)" + place.getName());


                // Business Place Intent
                if (mapType.equals(getString(R.string.intent_business_map_place))) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("business_users").child(uid);
                    reference.child("latLng").setValue(place.getLatLng());
                    Toast.makeText(this, "Your place Choosing was successful", Toast.LENGTH_SHORT).show();
                    finish();


                } else if (mapType.equals(getString(R.string.intent_map_five_places))) {
                    Log.d(TAG, "onActivityResult: 5 favoritePlacesIds to choose !");
                    if (StringManipulation.isMapCoordinates(place.getName().toString())) {  // not a restaurant! reopen map!
                        Log.d(TAG, "onActivityResult: this is a location and not a place!");
                        Toast.makeText(this, "Please, choose a restaurant", Toast.LENGTH_SHORT).show();

                        reopenMap();
                    } else {
                        if (!isRestaurant(place.getPlaceTypes())) {  // Not a restaurant! reopen map!
                            Log.d(TAG, "onActivityResult: place is not a restaurant! Show dialog!");
                            NotRestaurantDialog notRestaurantDialog = new NotRestaurantDialog(mContext);
                            notRestaurantDialog.show();
                        } else {
                            // Place is OK, add to favorites
                            Log.d(TAG, "onActivityResult: Adding place: " + place.getId() + " to favorites.");
                            chooseFavoritePlace(place.getId());



                        }
                    }
                } else {
                    Log.d(TAG, "onActivityResult: NOT OK !!");
                }
            }
        }
    }

    private void chooseFavoritePlace(String placeId) {
        Log.d(TAG, "chooseFavoritePlace: add place: " + placeId + " to favorites.");
        if (favoritePlacesIds.contains(placeId)) {
            Toast.makeText(mContext, "You've already choose this place", Toast.LENGTH_LONG).show();
            reopenMap();
        } else {
            favoritePlacesIds.add(placeId);

            DatabaseReference lovePlacesRef = mRef.child(getString(R.string.db_persons))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(getString(R.string.db_field_favorite_places_ids));
            lovePlacesRef.setValue(favoritePlacesIds);


            if (favoritePlacesIds.size() >= 3) {
                Intent intent = new Intent(TestGoogleMaps.this, FivePlacesActivity.class);
                intent.putStringArrayListExtra(getString(R.string.intent_love_places_ids), favoritePlacesIds);
                startActivity(intent);
            } else {
                Toast.makeText(TestGoogleMaps.this, "" + favoritePlacesIds.size() + " places was chosen", Toast.LENGTH_SHORT).show();
                reopenMap();
            }
        }
    }

    private boolean isRestaurant(List<Integer> placeTypes) {
        Log.d(TAG, "isRestaurant: checking if place has some food type: " + placeTypes);
        for (int type : placeTypes) {
            if (type == Place.TYPE_RESTAURANT || type == Place.TYPE_FOOD || type == Place.TYPE_CAFE ||
                    type == Place.TYPE_BAKERY || type == Place.TYPE_BAR || type == Place.TYPE_CASINO ||
                    type == Place.TYPE_CONVENIENCE_STORE || type == Place.TYPE_DEPARTMENT_STORE || type == Place.TYPE_FUNERAL_HOME ||
                    type == Place.TYPE_LIQUOR_STORE || type == Place.TYPE_MEAL_DELIVERY || type == Place.TYPE_MEAL_TAKEAWAY ||
                    type == Place.TYPE_NIGHT_CLUB || type == Place.TYPE_SHOPPING_MALL || type == Place.TYPE_GROCERY_OR_SUPERMARKET ||
                    type == Place.TYPE_HEALTH) {
                return true;
            }
        }
        return false;
    }

    private void reopenMap() {
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(TestGoogleMaps.this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, "reopenMap: GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException" + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();  // TODO(!) Check the back press without this line !
        Log.d(TAG, "onBackPressed: BackPressed");
        startActivity(new Intent(mContext, PersonProfileActivity.class));
    }


    /*
     * ------------------------- Firebase ------------------------------------------
     */


    @Override
    protected void onStart() {
        super.onStart();
        mClient.connect();

        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mClient.disconnect();

        if (mAuthListener != null) mAuth.removeAuthStateListener(mAuthListener);
    }


    private void setupFirebaseStaff() {
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();

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
        if (mapType.equals(getString(R.string.intent_map_five_places))) {
            mRef.child(getString(R.string.db_persons))
                    .child(mAuth.getCurrentUser().getUid())
                    .child(getString(R.string.db_field_favorite_places_ids))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            favoritePlacesIds = new ArrayList<>();
                            for (DataSnapshot placeIdDS : dataSnapshot.getChildren()) {
                                favoritePlacesIds.add(placeIdDS.getValue(String.class));
                            }
                            Toast.makeText(mContext, "OK", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: started");

    }


    /*
     */

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the mFireUser receives a prompt to install
     * Play services inside the SupportMapFragment. The API invokes this method after the mFireUser has
     * installed Google Play services and returned to the app.
     *//*
     * map.addMarker(new MarkerOptions()
    .position(coord)
    .title("Hello world"));


    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        LatLng sydney = new LatLng(-33.852, 151.211);
        googleMap.addMarker(new MarkerOptions().position(sydney)
                .title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        Log.i("aiaiaiaiia", sydney.toString());
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }
    }*/

}