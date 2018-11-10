package razdob.cycler.giliPlaces;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

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
public class GiliActivity extends AppCompatActivity {
    private static final String TAG = "GiliActivity";
    private final Context mContext = GiliActivity.this;

    private static final int ACTIVITY_NUM = 3;
    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private DatabaseReference mFavoritePlacesRef;
    private FirebaseMethods mFireMethods;
    private String uid;
    /* ---------------------- FIREBASE ----------------------- */

    // Vars
    private ArrayList<String> mFavoritePlacesIds;
    private GilPlacesAdapter mAdapter;
    private ArrayList<PlaceDetails> placeDetails;
    private GeoDataClient mGeoDataClient;


    private RecyclerView recyclerView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gili);

        setupFirebaseStaff();
        mFireMethods = new FirebaseMethods(mContext);


        recyclerView = findViewById(R.id.recycler_view);

        BottomNavigationViewHelper.setupBottomNavigationView(mContext, GiliActivity.this, ACTIVITY_NUM);
    }

    private void setupPlacesDetils() {
        placeDetails = new ArrayList<>();
        mGeoDataClient = Places.getGeoDataClient(this);
        for (final String pid : mFavoritePlacesIds) {
            mGeoDataClient.getPlaceById(pid).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                    placeDetails.add(RazUtils.getPlaceDetails(task, pid));
                    if (placeDetails.size() == mFavoritePlacesIds.size())
                        setupRecycler();

                }
            });
        }
    }



    private void setupRecycler() {

        mAdapter = new GilPlacesAdapter(mContext, placeDetails);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

    }


    private void setupFirebaseStaff() {
        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();

        // Init mAuthStateListener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (FireBaseUtils.myFireAuthListener(mContext, firebaseAuth, TAG, mAuthListener)) {
                    uid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
                }

            }
        };

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mFavoritePlacesIds = mFireMethods.getFavoritePlacesIds(dataSnapshot);
                setupPlacesDetils();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: DB_Error: " + databaseError.getMessage());
            }
        });
    }

}
