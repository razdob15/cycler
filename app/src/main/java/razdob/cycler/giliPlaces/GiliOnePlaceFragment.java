package razdob.cycler.giliPlaces;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import razdob.cycler.R;
import razdob.cycler.instShare.MyShareActivity;
import razdob.cycler.models.PlaceDetails;
import razdob.cycler.myUtils.FireBaseUtils;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.StringManipulation;
import razdob.cycler.myUtils.UniversalImageLoader;

/**
 * Created by Raz on 16/11/2018, for project: Cycler
 */
public class GiliOnePlaceFragment extends Fragment {
    private static final String TAG = "GiliOnePlaceFragment";

    // Extras
    private static final String PLACE_NAME_EXTRA = "place_name";
    private static final String PLACE_ADDRESS_EXTRA = "place_address";
    private static final String PLACE_BITMAP_EXTRA = "place_bitmap";
    private static final String PLACE_ID_EXTRA = "place_id";


    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private FirebaseMethods mFireMethods;
    /* ---------------------- FIREBASE ----------------------- */


    // Vars
    private String placeId, placeName, placeAddress;
    private Bitmap placeBtm;
    private ArrayList<String> likedUsersIds, likeUsersPhotos;
    private boolean isFavorite = false;
    private PlaceDetails placeDetails;

    // GUI
    private TextView placeNameTV, placeAddressTV, placeTagsTV;
    private ImageView mainIV, cameraIV, mHeartIV;
    private LinearLayout likedUsersProfilesLL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_gili_one_place, container, false);
        mFireMethods = new FirebaseMethods(getContext());

        matchWidgetsToIds(view);
        addClickListeners();

        getDataFromBundle();
        setupViewData();

        setupFirebaseStaff();

        return view;
    }

    private void matchWidgetsToIds(View view) {
        placeNameTV = view.findViewById(R.id.place_name_tv);
        placeAddressTV = view.findViewById(R.id.place_address);
        placeTagsTV = view.findViewById(R.id.place_tags);
        mainIV = view.findViewById(R.id.main_iv);
        likedUsersProfilesLL = view.findViewById(R.id.liked_users_ll);
        cameraIV = view.findViewById(R.id.camera_iv);
        mHeartIV = view.findViewById(R.id.heart_iv);
    }

    private void addClickListeners() {
        cameraIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Camera Click");
                // TODO(!): Check parcel size...
                MyShareActivity.start(getContext(), placeId, placeName, placeAddress);
            }
        });

        mHeartIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Heart Click");
                if (isFavorite)
                    unlikePlace();
                else
                    likePlace();
            }
        });
    }

    /**
     * Set the heart to be full & Green
     */
    private void guiLikePlace() {
        mHeartIV.setImageResource(R.drawable.ic_cycler_heart_green);
    }

    /**
     * Set the heart to be empty.
     */
    private void guiUnlikePlace() {
        mHeartIV.setImageResource(R.drawable.ic_cycler_heart_blank);
    }

    /**
     * Like place in DB & Visually.
     * Updates isFavorite to TRUE.
     */
    private void likePlace() {
        Log.d(TAG, "likePlace: called.");
        mFireMethods.likePlaceDB(placeId);
        GiliFavoritesActivity.addPlaceToList(placeDetails);
        guiLikePlace();
        isFavorite = true;
    }

    /**
     * Unlikes place in DB & Visually.
     * Updates isFavorite to FALSE.
     */
    private void unlikePlace() {
        Log.d(TAG, "unlikePlace: called.");
        mFireMethods.unlikePlaceDB(placeId);
        GiliFavoritesActivity.removePlaceFromList(placeDetails.getId());
        guiUnlikePlace();
        isFavorite = false;
    }

    /**
     * Set texts...
     */
    private void setupViewData() {
        if (placeName != null) placeNameTV.setText(placeName);
        if (placeAddress != null) placeAddressTV.setText(placeAddress);
        if (placeBtm != null) mainIV.setImageBitmap(placeBtm);
    }

    private void addLikedProfileToLayout(String photoUrl) {

        Log.d(TAG, "addLikedProfileToLayout: photoUrl: " + photoUrl);
        ImageView imageView = new CircleImageView(getContext());

        int imageLength = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imageLength, imageLength);
        int margin = getResources().getDimensionPixelSize(R.dimen.small_margin);
        params.setMarginStart(margin);
        imageView.setLayoutParams(params);
        UniversalImageLoader.setImagePicasso(photoUrl, imageView, R.drawable.cycler_logo, R.drawable.cycler_logo, null);

//        UniversalImageLoader.setImage(getContext(), photoUrl, imageView, null, "");
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        likedUsersProfilesLL.addView(imageView);

    }

    private void getDataFromBundle() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            placeId = bundle.getString(PLACE_ID_EXTRA);
            placeName = bundle.getString(PLACE_NAME_EXTRA);
            placeAddress = bundle.getString(PLACE_ADDRESS_EXTRA);
            placeBtm = bundle.getParcelable(PLACE_BITMAP_EXTRA);

            placeDetails = new PlaceDetails(placeId);
            placeDetails.setName(placeName);
            placeDetails.setAddress(placeAddress);
            placeDetails.setImg(placeBtm);

            bundle.clear();
        }

    }

    public static void showOnePlace(FragmentActivity activity, PlaceDetails placeDetails) {
        if (placeDetails == null || placeDetails.getId() == null) {
            Log.w(TAG, "showOnePlace: place is null! can't show it!");
            return;
        }

        Log.d(TAG, "showOnePlace: place: " + placeDetails.toString());
        GiliOnePlaceFragment fragment = new GiliOnePlaceFragment();
        Bundle bundle = new Bundle();
        Log.d(TAG, "showOnePlace: place: " + placeDetails);
        bundle.putString(PLACE_NAME_EXTRA, placeDetails.getName());
        bundle.putString(PLACE_ADDRESS_EXTRA, placeDetails.getAddress());
        bundle.putParcelable(PLACE_BITMAP_EXTRA, placeDetails.getImg());
        bundle.putString(PLACE_ID_EXTRA, placeDetails.getId());

        fragment.setArguments(bundle);

        FragmentManager manager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.container, fragment, "giliOnePlace");
        transaction.addToBackStack(null);
        transaction.commit();
    }


    // --------------------------- Firebase ------------------------------------------ //

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
                if (FireBaseUtils.defaultFireAuthListener(getContext(), firebaseAuth, TAG, mAuthListener)) {
                    Log.d(TAG, "onAuthStateChanged: Auth is OK");
                } else {
                    Log.d(TAG, "onAuthStateChanged: Auth is Fail!");
                }
            }
        };

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> tags = mFireMethods.getPlaceTagsList(dataSnapshot, placeId);
                String tagsText = StringManipulation.placeTagsFormat(tags);
                if (tags == null) placeTagsTV.setVisibility(View.GONE);
                else placeTagsTV.setText(tagsText);
                likedUsersIds = mFireMethods.placeUsersLikes(dataSnapshot, placeId);
                likeUsersPhotos = new ArrayList<>();
                for (String uid : likedUsersIds) {
                    String url = mFireMethods.getUserPorofilePhoto(dataSnapshot, uid);
                    if (url != null) likeUsersPhotos.add(url);
                }
                int photosCount = Math.min(likeUsersPhotos.size(), 10);
                for (int i = 0; i < photosCount; i++) {
                    addLikedProfileToLayout(likeUsersPhotos.get(i));
                }
                isFavorite = mFireMethods.isFavorite(dataSnapshot, placeId);
                if (isFavorite) {
                    guiLikePlace();
                } else {
                    guiUnlikePlace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                FireBaseUtils.dbErrorMessage(TAG, databaseError);
            }
        });


    }

}
