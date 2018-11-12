package razdob.cycler.fivePlaces;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import razdob.cycler.MainRegisterActivity;
import razdob.cycler.R;
import razdob.cycler.adapters.PlaceListAdapter;
import razdob.cycler.dialogs.NotRestaurantDialog;
import razdob.cycler.models.PlaceDetails;
import razdob.cycler.myUtils.BottomNavigationViewHelper;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.RecyclerPlaceTouchHelper;
import razdob.cycler.myUtils.RecyclerPlaceTouchHelperListener;
import razdob.cycler.myUtils.StringManipulation;

/**
 * Created by Raz on 25/06/2018, for project: PlacePicker2
 */
public class FivePlacesActivity extends AppCompatActivity implements RecyclerPlaceTouchHelperListener {
    private static final String TAG = "FivePlacesActivity";
    private static final int ACTIVITY_NUM = 3;
    private static final int PLACE_PICKER_REQUEST = 12;
    private final Context mContext = FivePlacesActivity.this;

    // TODO(!) Don't loading the details more than once if it is unnecessary! (Save them from the last time user was in this activity...)
    //                       This TODO talks also about DataActivity !

    // Loading shorting
    public static boolean load = true;
    public static HashMap<String, PlaceDetails> idDetailsHM;  // KEY=Place id; VALUE=PlaceDetails Object

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private DatabaseReference mFavoritePlacesRef;
    private FirebaseMethods mFireMethods;
    /* ---------------------- FIREBASE ----------------------- */

    // Vars
    private ArrayList<String> mFavoritePlacesIds;
    private PlaceListAdapter mAdapter;
    private GeoDataClient mGeoDataClient;

    // Widgets
    private RecyclerView recyclerView;
    private CoordinatorLayout rootLayout;
    private LinearLayout noFavLL;
    private TextView addPlacesBtn;
    private ProgressBar mProgressBar;


    /* ------------------------------- ACTIVITY -------------------------------*/
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_five_places);
        matchWidgetsToIds();
        setupFirebaseStaff();

        BottomNavigationViewHelper.setupBottomNavigationView(mContext, FivePlacesActivity.this, ACTIVITY_NUM);
        mGeoDataClient = Places.getGeoDataClient(this);


        mFireMethods = new FirebaseMethods(mContext);
        if (idDetailsHM == null) {
            idDetailsHM = new HashMap<>();
        }

        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.toolbar_favorites));


        mFavoritePlacesRef = mRef.child(getString(R.string.db_persons))
                .child(mAuth.getCurrentUser().getUid())
                .child(getString(R.string.db_field_favorite_places_ids));

//        setupRecycler();
    }

    private void setupRecycler() {

        if (mFavoritePlacesIds == null || mFavoritePlacesIds.size() == 0) {
            Log.d(TAG, "onCreate:No Favorites Places");
            noFavLL.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            addPlacesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: OpenMap!");
                    openMap();
                }
            });
        } else if (mFavoritePlacesIds.size() != idDetailsHM.size()) return;

        else {
            noFavLL.setVisibility(View.GONE);
            if (load)
                mAdapter = new PlaceListAdapter(this, mFavoritePlacesIds, mFavoritePlacesIds, getString(R.string.five_places_activity), null);
            else {
                mAdapter = new PlaceListAdapter(mContext, idDetailsHM, mFavoritePlacesIds, getString(R.string.five_places_activity));
            }
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        mProgressBar.setVisibility(View.GONE);

        ItemTouchHelper.SimpleCallback itemTouchCallBack = new RecyclerPlaceTouchHelper(0,
                ItemTouchHelper.START, this);
        new ItemTouchHelper(itemTouchCallBack).attachToRecyclerView(recyclerView);
    }

    private void showNotRestaurantDialog(final String placeId) {
        final NotRestaurantDialog dialog = new NotRestaurantDialog(FivePlacesActivity.this);

        dialog.setYesClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Yes Click !");
                dialog.dismiss();
                chooseFavoritePlace(placeId);
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

    private void matchWidgetsToIds() {
        recyclerView = findViewById(R.id.recycler_view);
        rootLayout = findViewById(R.id.root_layout);
        noFavLL = findViewById(R.id.no_favorites_ll);
        addPlacesBtn = findViewById(R.id.add_places_from_map_btn);
        mProgressBar = findViewById(R.id.progress_bar);


        // Hide widgets that for DataActivity only.
        findViewById(R.id.choose_subjects_iv).setVisibility(View.GONE);
        findViewById(R.id.relLayout0).setVisibility(View.GONE);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, final int position) {
        Log.d(TAG, "onSwiped: item swiped: " + position);

        if (viewHolder instanceof PlaceListAdapter.MyViewHolder) {

            // Vars
            if (mFavoritePlacesIds != null && mFavoritePlacesIds.size() > 0) {
                final String placeId = mFavoritePlacesIds.get(viewHolder.getAdapterPosition());
                final PlaceDetails deletePD = idDetailsHM.get(placeId);
                final int deleteIndex = viewHolder.getAdapterPosition();

                Log.d(TAG, "onSwiped: mFavoritePlacesIds: " + mFavoritePlacesIds);

                // Deletes
                idDetailsHM.remove(placeId);
                mAdapter.removeItem(viewHolder.getAdapterPosition());
                mFavoritePlacesIds.remove(placeId);
                mFireMethods.unlikePlaceDB(placeId);

                // Update
                mFavoritePlacesRef.setValue(mFavoritePlacesIds);

                // Swipe & Restore
                mGeoDataClient.getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                        if (task.isSuccessful()) {
                            PlaceBufferResponse allPlaces = task.getResult();
                            Place place = allPlaces.get(0);
                            String name = place.getName().toString();

                            Log.d(TAG, "onComplete: " + name + "removed. ID: " + placeId + ". position: " + deleteIndex);
                            Snackbar snackbar = Snackbar.make(rootLayout, name + " removed from list!", Snackbar.LENGTH_LONG);
                            snackbar.setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mAdapter.restoreItem(placeId, deleteIndex);
                                    mFavoritePlacesRef.setValue(mFavoritePlacesIds);
                                    idDetailsHM.put(placeId, deletePD);
                                    mFireMethods.likePlaceDB(placeId);
                                }
                            });
                            snackbar.setActionTextColor(Color.YELLOW);
                            snackbar.show();

                            allPlaces.release();

                        } else {
                            Log.e(TAG, "Place not found.");
                        }
                    }
                });
            }
        }

    }

    @Override
    public void onBackPressed() {
        finish();
    }


    /* ------------------------------- MAP --------------------------------------*/

    /**
     * Sends a request to OnActivityResult
     */
    private void openMap() {

        mGeoDataClient = Places.getGeoDataClient(mContext);

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(FivePlacesActivity.this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void reopenMap() {
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(FivePlacesActivity.this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, "reopenMap: GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException" + e.getMessage());
        }
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
                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!mFireMethods.isRestaurant(dataSnapshot, place.getId(), place.getPlaceTypes())) {
                                Log.d(TAG, "onDataChange: place is not a restaurant! Show dialog!");
                                showNotRestaurantDialog(place.getId());
                            } else {
                                // Place is OK, add to favorites
                                Log.d(TAG, "onActivityResult: Adding place: " + place.getId() + " to favorites.");
                                chooseFavoritePlace(place.getId());
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

    private void chooseFavoritePlace(String placeId) {
        Log.d(TAG, "chooseFavoritePlace: add place: " + placeId + " to favorites.");
        if (mFavoritePlacesIds.contains(placeId)) {
            Toast.makeText(mContext, "You've already choose this place", Toast.LENGTH_LONG).show();
            reopenMap();
        } else {

            mFireMethods.likePlaceDB(placeId);
            mFavoritePlacesIds.add(placeId);

            mFavoritePlacesRef.setValue(mFavoritePlacesIds);

            if (mFavoritePlacesIds.size() >= 3) {
                Intent intent = new Intent(FivePlacesActivity.this, FivePlacesActivity.class);
                intent.putStringArrayListExtra(getString(R.string.intent_love_places_ids), mFavoritePlacesIds);
                startActivity(intent);
            } else {
                Toast.makeText(FivePlacesActivity.this, "" + mFavoritePlacesIds.size() + " places was chosen", Toast.LENGTH_SHORT).show();
                reopenMap();
            }
        }
    }


    /* --------------------------------- Firebase ----------------------------*/

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) mAuth.removeAuthStateListener(mAuthListener);
    }

    private void setupFirebaseStaff() {
        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();

        // Init mAuthStateListener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                // Check if user is logged in

                if (user == null) {
                    Log.d(TAG, "onAuthStateChanged: User log-out");
                    mAuth.removeAuthStateListener(mAuthListener);
                    startActivity(new Intent(mContext, MainRegisterActivity.class));
                } else {
                    Log.d(TAG, "onAuthStateChanged: User is logged-in :) uid = " + user.getUid());
                }
            }
        };

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String uid = mAuth.getCurrentUser().getUid();

                mFavoritePlacesIds = new ArrayList<>();
                DataSnapshot favoritesDS = dataSnapshot.child(getString(R.string.db_persons)).child(uid).child(getString(R.string.db_field_favorite_places_ids));
                final long favoritesCount = favoritesDS.getChildrenCount();

                // The last place that choose to favorites will showed in the top.
                for (long i = favoritesCount - 1; i >= 0; i--) {
                    final String placeId = favoritesDS.child(String.valueOf(i)).getValue(String.class);

                    mFavoritePlacesIds.add(placeId);
                    if (!idDetailsHM.containsKey(placeId)) {
                        mGeoDataClient.getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                                if (task.isSuccessful()) {
                                    PlaceBufferResponse places = task.getResult();
                                    @SuppressLint("RestrictedApi")
                                    Place place = places.get(0);

                                    Log.d(TAG, "onComplete: place: " + place.getName());

                                    PlaceDetails placeDetails = new PlaceDetails(placeId);

                                    // Setting up placeDetails object
                                    if (place.getName() != null)
                                        placeDetails.setName(place.getName().toString());
                                    if (place.getAddress() != null)
                                        placeDetails.setAddress(place.getAddress().toString());
                                    if (place.getPhoneNumber() != null)
                                        placeDetails.setPhone(place.getPhoneNumber().toString());
                                    if (place.getWebsiteUri() != null)
                                        placeDetails.setWebsite(place.getWebsiteUri().toString());

                                    idDetailsHM.put(placeId, placeDetails);
                                    setupRecycler();
                                }
                            }
                        });
                    }
                }
                setupRecycler();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: DB_Error: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PlaceListAdapter.CALL_PERMISSION_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    public static void start(Context context) {
        Intent intent = new Intent(context, FivePlacesActivity.class);
        context.startActivity(intent);
    }
}
