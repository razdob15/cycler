package razdob.cycler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import razdob.cycler.dialogs.CustomDialog;
import razdob.cycler.dialogs.NotRestaurantDialog;
import razdob.cycler.feed.HomeActivity;
import razdob.cycler.fivePlaces.FivePlacesActivity;
import razdob.cycler.instProfile.AccountSettingsActivity;
import razdob.cycler.models.User;
import razdob.cycler.models.UserAccountSettings;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.RemoteConfigConsts;
import razdob.cycler.myUtils.StringManipulation;

/**
 * Created by Raz on 15/08/2018, for project: PlacePicker2
 */
public class ChooseFavoritePlacesActivity extends AppCompatActivity {
    private static final String TAG = "ChooseFavoritePlaces";
    private static final int PLACE_PICKER_REQUEST = 8;
    private final Context mContext = ChooseFavoritePlacesActivity.this;

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    /* ---------------------- FIREBASE ----------------------- */
    // Vars
    private FirebaseMethods mFireMethods;
    private ArrayList<String> favoritePlacesIds;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initFirebase();
        mFireMethods = new FirebaseMethods(mContext);

        Intent intent = getIntent();
        if (intent.hasExtra(mContext.getString(R.string.intent_favorite_places))) {
            favoritePlacesIds = intent.getStringArrayListExtra(getString(R.string.intent_favorite_places));
        }
        if (favoritePlacesIds == null) favoritePlacesIds = new ArrayList<>();

        if (favoritePlacesIds.size() >= RemoteConfigConsts.MIN_FAVORITES_COUNT) {
            Log.d(TAG, "onCreate: no need to choose favorites");
            goToHome();
            return;
        }

        final CustomDialog dialog = new CustomDialog(mContext, "Please, Choose "
                + (RemoteConfigConsts.MIN_FAVORITES_COUNT - favoritePlacesIds.size()) + " more restaurants you like. \nThis part is important to get the best restaurants for you :-)",
                1, "OK", null);
        dialog.setClick1(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                openMap();
            }
        });
        dialog.show();
        dialog.setCancelable(false);
    }

    private void initFirebase() {
        Log.d(TAG, "initFirebase: called.");

        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();
    }


    /**
     * Sends a request to OnActivityResult
     */
    private void openMap() {

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void reopenMap() {
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, "reopenMap: GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException" + e.getMessage());
        }
    }

    private void chooseFavoritePlace(String placeId) {
        Log.d(TAG, "chooseFavoritePlace: add place: " + placeId + " to favorites.");
        if (favoritePlacesIds.contains(placeId)) {
            Toast.makeText(mContext, "You've already choose this place", Toast.LENGTH_LONG).show();
            reopenMap();
        } else {
            favoritePlacesIds.add(placeId);
            mFireMethods.likePlaceDB(placeId);


            if (favoritePlacesIds.size() < RemoteConfigConsts.MIN_FAVORITES_COUNT) {

                Toast.makeText(mContext, "" + favoritePlacesIds.size() + " places was chosen", Toast.LENGTH_SHORT).show();
                reopenMap();
            } else {
                final CustomDialog favExplanationDialog = new CustomDialog(mContext,
                        "Excellent! Now, these restaurants will be in your favorites :)",
                        1, "OK", null);
                favExplanationDialog.setClick1(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        favExplanationDialog.dismiss();
                        Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                        intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                        intent.putExtra(mContext.getString(R.string.intent_first_time), true);
                        startActivity(intent);
                    }
                });
                favExplanationDialog.show();


//                Intent intent = new Intent(mContext, FivePlacesActivity.class);
//                finish();
//                intent.putStringArrayListExtra(getString(R.string.intent_love_places_ids), favoritePlacesIds);
//                startActivity(intent);
            }

        }
    }


    private void showNotRestaurantDialog(final String placeId) {
        final NotRestaurantDialog dialog = new NotRestaurantDialog(mContext);

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
                } else if (favoritePlacesIds.contains(place.getId())) { // Existing place ! reopen map !
                    Log.d(TAG, "onActivityResult: duplicate plcae! need to choose another place.");
                    Toast.makeText(mContext, "You've already choose this place. Please choose another restaurant", Toast.LENGTH_LONG).show();
                    reopenMap();
                } else {    // Place is OK.
                    // Check if it is a restaurant.
                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!mFireMethods.isRestaurant(dataSnapshot, place.getId(), place.getPlaceTypes())) {
                                Log.d(TAG, "onDataChange: place is not a restaurant! show dialog!");
                                showNotRestaurantDialog(place.getId());
                            } else {
                                // Place is OK, add to favorites
                                Log.d(TAG, "onActivityResult: open This place View: " + place.getId());
                                favoritePlacesIds.add(place.getId());
                                mFireMethods.likePlaceDB(place.getId());
                                if (favoritePlacesIds.size() < RemoteConfigConsts.MIN_FAVORITES_COUNT) {
                                    Toast.makeText(mContext, place.getName().toString() + " added to your favorites.\nYou need to choose "
                                            + (RemoteConfigConsts.MIN_FAVORITES_COUNT - favoritePlacesIds.size()) + " more places", Toast.LENGTH_LONG).show();
                                    reopenMap();
                                } else {
                                    finishChoosing(dataSnapshot);
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.w(TAG, "onCancelled: DBError:  " + databaseError.getMessage());
                        }
                    });

//                    if (!RazUtils.isRestaurant(place.getPlaceTypes())) {  // Not a restaurant! Open Dialog
//                        Log.d(TAG, "onActivityResult: place is not a restaurant! Show dialog!");
//                        showNotRestaurantDialog(place.getId());
//                    } else {
//                        // Place is OK, add to favorites
//                        Log.d(TAG, "onActivityResult: open This place View: " + place.getId());
//                        favoritePlacesIds.add(place.getId());
//                        mFireMethods.likePlaceDB(place.getId());
//                        if (favoritePlacesIds.size() < FAVORITES_COUNT) {
//                            Toast.makeText(mContext, place.getName().toString() + " added to your favorites.\nYou need to choose "
//                                    + (FAVORITES_COUNT - favoritePlacesIds.size()) + " more places", Toast.LENGTH_LONG).show();
//                            reopenMap();
//                        } else {
//                            Intent intent = new Intent(mContext, FivePlacesActivity.class);
//                            intent.putStringArrayListExtra(getString(R.string.intent_love_places_ids), favoritePlacesIds);
//                            startActivity(intent);
//                        }
//
//
////                        chooseFavoritePlace(place.getId());
//                    }
                }
            } else {
                Log.d(TAG, "onActivityResult: NOT OK !!");
            }
        }
    }

    private void finishChoosing(DataSnapshot mainDS) {
        if (mFireMethods.userHasName(mainDS)) {
            goToHome();
        } else {
            goToUserSettings();
        }
    }

    /**
     * Navigates to HomeActivity.
     */
    private void goToHome() {
        Log.d(TAG, "goToHome: Navigating to the HomeActivity.");
        Intent intent = new Intent(mContext, HomeActivity.class);
        intent.putExtra(mContext.getString(R.string.calling_activity), mContext.getString(R.string.choose_favorite_places_activity));
        startActivity(intent);
        finish();
    }

    /**
     * Open UserSettings to update his details
     */
    private void goToUserSettings() {
        Log.d(TAG, "goToUserSettings: navigates to: " + mContext.getString(R.string.edit_profile));
        Intent intent = new Intent(mContext, AccountSettingsActivity.class);
        intent.putExtra(getString(R.string.calling_activity), getString(R.string.choose_favorite_places_activity));
        startActivity(intent);
        finish();
    }


//    private void setupFirebase() {
//        mAuth = FirebaseAuth.getInstance();
//        mRef = FirebaseDatabase.getInstance().getReference();
//
//        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//
//                // Check if user is logged in
//                if (firebaseAuth.getCurrentUser() == null) {
//                    Log.d(TAG, "onAuthStateChanged: User log-out");
//                    mAuth.removeAuthStateListener(mAuthStateListener);
//                    startActivity(new Intent(mContext, MainRegisterActivity.class));
//                } else {
//                    Log.d(TAG, "onAuthStateChanged: User is logged-in :) uid = " + firebaseAuth.getCurrentUser().getUid());
//                }
//            }
//        };
//        if (mFireMethods.getFavoritePlacesIds() == null) {
//
//            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    favoritePlacesIds = new ArrayList<>();
//                    for (DataSnapshot ds : dataSnapshot.child(getString(R.string.db_persons))
//                            .child(mAuth.getCurrentUser().getUid())
//                            .child(getString(R.string.db_field_favorite_places_ids))
//                            .getChildren()) {
//                        favoritePlacesIds.add(ds.getValue(String.class));
//
//                    }
//                    setupBottomNavigationView();
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    Log.w(TAG, "onCancelled: DB_Error: " + databaseError.getMessage());
//                }
//            });
//        } else {
//            favoritePlacesIds = mFireMethods.getFavoritePlacesIds();
//            setupBottomNavigationView();
//        }
//    }


}
