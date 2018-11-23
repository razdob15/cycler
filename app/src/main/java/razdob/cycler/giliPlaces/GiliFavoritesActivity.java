package razdob.cycler.giliPlaces;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import razdob.cycler.R;
import razdob.cycler.models.PlaceDetails;
import razdob.cycler.myUtils.BottomNavigationViewHelper;
import razdob.cycler.myUtils.FireBaseUtils;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.RazUtils;

/**
 * Created by Raz on 09/11/2018, for project: Cycler
 */
public class GiliFavoritesActivity extends AppCompatActivity {
    private static final String TAG = "GiliFavoritesActivity";
    private final Context mContext = GiliFavoritesActivity.this;
    private final FragmentActivity mActivity = GiliFavoritesActivity.this;

    private static final int ACTIVITY_NUM = 3;

    // Extras Name
    private static final String PLACES_IDS_EXTRA = "places_ids";
    private static final String LIST_TYPE_EXTRA = "list_type";

    // List Type
    private static final String FAVORITES_LIST_TYPE = "type_favorites";
    private static final String USE_LIST_TYPE = "type_use";


    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private FirebaseMethods mFireMethods;
    private String uid;
    /* ---------------------- FIREBASE ----------------------- */

    // Vars
    private ArrayList<String> mPlacesIds;               // Places for recyclers - favorites\income-list (depends by the list-type).
    private ArrayList<String> mFavoritesPlacesIds;      // Favorite places - from DB.
    private GeoDataClient mGeoDataClient;               // Must for getting the place's details.
    private static ArrayList<PlaceDetails> placeDetailsList;  // Calculate with mGeoClient, inferred from mFavoritePlacesIds.
    private String mListType;

    // GUI
    private RecyclerView recyclerView;

    public static ArrayList<PlaceDetails> getPlaceDetailsList() {
        return placeDetailsList;
    }

    public static void setPlaceDetailsList(ArrayList<PlaceDetails> placeDetailsList) {
        if (placeDetailsList != null) GiliFavoritesActivity.placeDetailsList = placeDetailsList;
    }

    public static void addPlaceToList(PlaceDetails placeDetails) {
        if (placeDetailsList == null) placeDetailsList = new ArrayList<>();
        if (!isPlaceInList(placeDetails.getId())) {
            placeDetailsList.add(placeDetails);
        }
    }

    public static void removePlaceFromList(String placeId) {
        if (placeDetailsList == null)  {
            placeDetailsList = new ArrayList<>();
        }
        else if (isPlaceInList(placeId)) {
            for (PlaceDetails pd : placeDetailsList) {
                if (pd.getId().equals(placeId)) {
                    placeDetailsList.remove(pd);
                    return;
                }
            }
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gili);

        mFireMethods = new FirebaseMethods(mContext);
        recyclerView = findViewById(R.id.recycler_view);

        getDataFromIntent();
        if (mListType.equals(USE_LIST_TYPE) && mPlacesIds != null) {
            setupAdapter();
        }

        setupFirebaseStaff();

        BottomNavigationViewHelper.setupBottomNavigationView(mContext, GiliFavoritesActivity.this, ACTIVITY_NUM);
    }

    /**
     * init 'mPlacesIds' & 'mListType'
     */
    private void getDataFromIntent() {
        Intent intent = getIntent();
        mPlacesIds = intent.getStringArrayListExtra(PLACES_IDS_EXTRA);
        if (intent.hasExtra(LIST_TYPE_EXTRA))
            mListType = intent.getStringExtra(LIST_TYPE_EXTRA);
        else
            mListType = FAVORITES_LIST_TYPE;
    }

    /**
     * Get Places' details from  the places id in the income-intent
     */
    private void setupPlacesDetailsList() {
        if (mListType.equals(FAVORITES_LIST_TYPE))
            mPlacesIds = mFavoritesPlacesIds;
        if (placeDetailsList == null) placeDetailsList = new ArrayList<>();
        mGeoDataClient = Places.getGeoDataClient(this);
        for (final String pid : mPlacesIds) {
            mGeoDataClient.getPlaceById(pid).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                    if (!isPlaceInList(pid))
                        placeDetailsList.add(RazUtils.getPlaceDetails(task, pid));
                    if (placeDetailsList.size() == mPlacesIds.size())
                        setupAdapter();

                }
            });
        }
    }


    private void setupAdapter() {
        GilPlacesAdapter.createPlacesAdapter(mContext, mActivity, recyclerView, placeDetailsList, mContext.getString(R.string.gili_favorites_activity));
//        PlacesCirclesImageAdapter.createPlacesCirclesAdapter(mContext, recyclerView, placeDetailsList);
    }

    private static boolean isPlaceInList(String pid) {
        if (placeDetailsList == null) return false;
        for (PlaceDetails pd : placeDetailsList) {
            if (pd.getId().equals(pid)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Gets the favoritePlacesIds from DB.
     * Calls to 'setupPlacesDetailsList()' after it.
     */
    private void setupFirebaseStaff() {
        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();

        // Init mAuthStateListener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (FireBaseUtils.defaultFireAuthListener(mContext, firebaseAuth, TAG, mAuthListener)) {
                    uid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
                }
            }
        };

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mFavoritesPlacesIds = mFireMethods.getFavoritePlacesIds(dataSnapshot);
                if (mListType.equals(FAVORITES_LIST_TYPE)) {
                    // Default places list = Favorites !
                    Log.d(TAG, "onDataChange: use favorite !");
                    setupPlacesDetailsList();
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: DB_Error: " + databaseError.getMessage());
            }
        });
    }

    public static void start(Context context, String type, ArrayList<String> placesIds) {
        Intent intent = new Intent(context, GiliFavoritesActivity.class);
        if (placesIds != null) intent.putExtra(PLACES_IDS_EXTRA, placesIds);
        if (type != null) intent.putExtra(LIST_TYPE_EXTRA, type);
        else intent.putExtra(LIST_TYPE_EXTRA, FAVORITES_LIST_TYPE);
        context.startActivity(intent);
    }

    public static void startForFavorites(Context context) {
        start(context, FAVORITES_LIST_TYPE, null);
    }

    /**
     * Start this activity with a specific places list.
     *
     * @param context   - App Context.
     * @param placesIds - Specific PlacesIdsList.
     */
    public static void startWithList(Context context, ArrayList<String> placesIds) {
        if (placesIds != null)
            start(context, USE_LIST_TYPE, placesIds);
        else {
            Log.w(TAG, "startWithList: placesList is null !!");
            Toast.makeText(context, "Sorry, an error occur.", Toast.LENGTH_SHORT).show();
        }
    }

}
