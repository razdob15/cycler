package razdob.cycler.instSearch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.LogWriter;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;

import razdob.cycler.MainRegisterActivity;
import razdob.cycler.R;
import razdob.cycler.dialogs.NotRestaurantDialog;
import razdob.cycler.fivePlaces.FivePlacesActivity;
import razdob.cycler.fivePlaces.ViewOnePlaceActivity;
import razdob.cycler.instProfile.InstProfileActivity;
import razdob.cycler.models.User;
import razdob.cycler.myUtils.BottomNavigationViewHelper;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.RazUtils;
import razdob.cycler.myUtils.StringManipulation;
import razdob.cycler.myUtils.UsersListAdapter;

/**
 * Created by Raz on 18/06/2018, for project: PlacePicker2
 */
public class SearchUserActivity extends AppCompatActivity {
    private static final String TAG = "SearchUserActivity";
    private static final int PLACE_PICKER_REQUEST = 5;
    private static final int ACTIVITY_NUM = 1;
    private final Context mContext = SearchUserActivity.this;


    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mRef;
    /* ---------------------- FIREBASE ----------------------- */

    // Google Maps
    private GeoDataClient mGeoDataClient;

    // Firebase
    private FirebaseMethods mFireMethods;

    // Widgets
    private EditText mSearchParam;
    private ListView mListView;
    private FloatingActionButton addNewPlaceBtn;

    // Vars
    private List<User> mUserList;
    private UsersListAdapter mAdapter;
    private ArrayList<String> favoritePlacesIds;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inst_search);
        mFireMethods = new FirebaseMethods(mContext);

        matchWidgetsToIds();
        Log.d(TAG, "onCreate: starting:");

        addNewPlaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        setupFirebase();
//        hideSoftKeyboard();  // TODO(0): why?
        initTextListener();
    }

    private void matchWidgetsToIds() {
        addNewPlaceBtn = findViewById(R.id.add_new_fav_btn2);
        mSearchParam = findViewById(R.id.search_tv);
        mListView = findViewById(R.id.list_view);

        mSearchParam.setImeActionLabel("CHASD",EditorInfo.IME_ACTION_SEARCH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                final Place place = PlacePicker.getPlace(data, this);
                Log.d(TAG, "onActivityResult: PlaceName(1)" + place.getName());

                Log.d(TAG, "onActivityResult: 5 favoritePlacesIds to choose !");
                if (StringManipulation.isMapCoordinates(place.getName().toString())) {  // not a restaurant! reopen map!
                    Log.d(TAG, "onActivityResult: this is a location and not a place!");
                    Toast.makeText(this, "Please, choose a restaurant", Toast.LENGTH_SHORT).show();
                    reopenMap();

                } else {
                    FirebaseDatabase.getInstance(mFireApp).getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!mFireMethods.isRestaurant(dataSnapshot, place.getId(), place.getPlaceTypes())) {
                                Log.d(TAG, "onDataChange: place is not a restaurant! Show dialog!");
                                showNotRestaurantDialog(place.getId());
                            } else {
                                // Place is OK, add to favorites
                                Log.d(TAG, "onActivityResult: open This place View: " + place.getId());
                                ViewOnePlaceActivity.start(mContext, place.getId(), ACTIVITY_NUM);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.w(TAG, "onCancelled: DBError: " + databaseError.getMessage());

                        }
                    });
                }
            } else {
                Log.d(TAG, "onActivityResult: NOT OK !!");
            }
        }
    }

    private void showNotRestaurantDialog(final String placeId) {
        final NotRestaurantDialog dialog = new NotRestaurantDialog(SearchUserActivity.this);

        dialog.setYesClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Yes Click !");
                dialog.dismiss();
                openPlaceView(placeId);
                mFireMethods.markPlaceAsRestaurantDB(placeId);
            }
        });
        dialog.setNoClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Not Click !");
                dialog.dismiss();
                reopenMap();
            }
        });
        dialog.show();
    }

    /**
     * open the place view.
     *
     * @param placeId - place's id to show
     */
    private void openPlaceView(String placeId) {
        Log.d(TAG, "openPlaceView: navigating to ViewOnePlaceActivity with place: " + placeId);
        ViewOnePlaceActivity.start(mContext, placeId, ACTIVITY_NUM);
    }

    private void initTextListener() {
        Log.d(TAG, "initTextListener: initializing");
        mUserList = new ArrayList<>();

        mSearchParam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = mSearchParam.getText().toString();//.toLowerCase(Locale.getDefault());
                searchForMatch(text);
            }
        });

    }

    private void searchForMatch(String keyword) {
        Log.d(TAG, "searchForMatch: searching for a match: " + keyword);
        mUserList.clear();
        // update the users list view
        if (keyword.length() > 0) {
            DatabaseReference reference = FirebaseDatabase.getInstance(mFireApp).getReference();
            Query query = reference.child(getString(R.string.db_persons))
                    .orderByChild(getString(R.string.db_field_name)).equalTo(keyword);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnap : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: found a user: " + singleSnap.getValue(User.class).toString());

                        mUserList.add(singleSnap.getValue(User.class));
                        // update the users list view
                        updateUsersList();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    private void updateUsersList() {
        Log.d(TAG, "updateUsersList: updating users list");
        mAdapter = new UsersListAdapter(SearchUserActivity.this, R.layout.layout_user_list_item,
                mUserList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: Selected user: " + mUserList.get(position).toString());

                // Navigate to selected-user's profile activity
                Intent intent = new Intent(SearchUserActivity.this, InstProfileActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.search_activity));
                intent.putExtra(getString(R.string.intent_user), mUserList.get(position));
                startActivity(intent);

            }
        });

    }

    private void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(this, this, bottomNavigationViewEx, favoritePlacesIds);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    /**
     * Sends a request to OnActivityResult
     */
    private void openMap() {

        mGeoDataClient = Places.getGeoDataClient(mContext);


        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(SearchUserActivity.this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void reopenMap() {
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(SearchUserActivity.this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, "reopenMap: GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException" + e.getMessage());
        }
    }

    private void chooseFavoritePlace(String placeId) {
        Log.d(TAG, "chooseFavoritePlace: add place: " + placeId + " to favorites.");
        if (favoritePlacesIds.contains(placeId)) {
            Toast.makeText(SearchUserActivity.this, "You've already choose this place", Toast.LENGTH_LONG).show();
            reopenMap();
        } else {
            favoritePlacesIds.add(placeId);

            mFireMethods.likePlaceDB(placeId);


            if (favoritePlacesIds.size() >= 3) {
                Intent intent = new Intent(SearchUserActivity.this, FivePlacesActivity.class);
                intent.putStringArrayListExtra(getString(R.string.intent_love_places_ids), favoritePlacesIds);
                startActivity(intent);
            } else {
                Toast.makeText(SearchUserActivity.this, "" + favoritePlacesIds.size() + " places was chosen", Toast.LENGTH_SHORT).show();
                reopenMap();
            }
        }
    }

    private void setupFirebase() {
        Log.d(TAG, "setupFirebase: called.");

        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                // Check if user is logged in
                if (firebaseAuth.getCurrentUser() == null) {
                    Log.d(TAG, "onAuthStateChanged: User log-out");
                    mAuth.removeAuthStateListener(mAuthStateListener);
                    startActivity(new Intent(mContext, MainRegisterActivity.class));
                } else {
                    Log.d(TAG, "onAuthStateChanged: User is logged-in :) uid = " + firebaseAuth.getCurrentUser().getUid());
                }
            }
        };
        if (mFireMethods.getFavoritePlacesIds() == null) {

            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    favoritePlacesIds = mFireMethods.getFavoritePlacesIds(dataSnapshot);

                    setupBottomNavigationView();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "onCancelled: DB_Error: " + databaseError.getMessage());
                }
            });
        } else {
            favoritePlacesIds = mFireMethods.getFavoritePlacesIds();
            setupBottomNavigationView();
        }
    }
}
