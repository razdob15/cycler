package razdob.cycler.fivePlaces;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
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
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import razdob.cycler.ChoosePlaceTagsActivity;
import razdob.cycler.MainRegisterActivity;
import razdob.cycler.R;
import razdob.cycler.adapters.GridPlaceImagesAdapter;
import razdob.cycler.instShare.MyShareActivity;
import razdob.cycler.instShare.ShareActivity;
import razdob.cycler.models.Photo;
import razdob.cycler.myUtils.BottomNavigationViewHelper;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.MyFonts;
import razdob.cycler.myUtils.Permissions;
import razdob.cycler.myUtils.RazUtils;
import razdob.cycler.myUtils.UniversalImageLoader;

/**
 * Created by Raz on 29/06/2018, for project: PlacePicker2
 */
public class ViewOnePlaceActivity extends AppCompatActivity
        implements View.OnClickListener {
    private static final String TAG = "ViewOnePlaceActivity";
    private final Context mContext = ViewOnePlaceActivity.this;

    // Constants
    private static final int LOCATION_REQUEST_CODE = 9;
    private static final int NUM_GRID_COLUMNS = 3;
    final static private int CALL_PERMISSION_CODE = 6;

    // Stars Drawables
    private static final int STAR_ON_XML = R.drawable.ic_cycler_star_green;
    private static final int STAR_OFF_XML = R.drawable.ic_cycler_star_blank;



    @Override
    public void onClick(View v) {
        if (v == redHeart) {    // Unlike Place
            Log.d(TAG, "onClick: unlike place: " + placeId);
            redHeart.setVisibility(View.GONE);
            whiteHeart.setVisibility(View.VISIBLE);
            mFireMethods.unlikePlaceDB(placeId);
        } else if (v == whiteHeart) {   // Like Plcae
            Log.d(TAG, "onClick: like place: " + placeId);
            whiteHeart.setVisibility(View.GONE);
            redHeart.setVisibility(View.VISIBLE);
            mFireMethods.likePlaceDB(placeId);
        } else if (v == likeTV || v == likeLL) {
            if (redHeart.getVisibility() == View.VISIBLE) { // Unlike Plcae
                Log.d(TAG, "onClick: unlike place: " + placeId);
                redHeart.setVisibility(View.GONE);
                whiteHeart.setVisibility(View.VISIBLE);
                mFireMethods.unlikePlaceDB(placeId);
            } else {    // Like Place
                Log.d(TAG, "onClick: like place: " + placeId);
                whiteHeart.setVisibility(View.GONE);
                redHeart.setVisibility(View.VISIBLE);
                mFireMethods.likePlaceDB(placeId);
            }
        }else if (v == cameraIV) {  // Open Camera
            Intent intent = new Intent(mContext, MyShareActivity.class);
            intent.putExtra(getString(R.string.intent_place_name), nameTV.getText().toString());
            intent.putExtra(getString(R.string.intent_place_id), placeId);
            intent.putExtra(getString(R.string.intent_place_address), addressTv.getText().toString());
            startActivity(intent);

        } else if (isStarView(v)) { // Rating Stars
            boolean passed = false;
            rate = 0;
            for (int i=0; i<stars.length; i++) {
                if (passed) stars[i].setImageResource(STAR_OFF_XML);
                else stars[i].setImageResource(STAR_ON_XML);
                if (stars[i] == v) {
                    passed = true;
                    rate = i+1;
                }
            }
        } else if (v == rateBtn) {
            if (rate < 1 || rate > 5) {
                Log.d(TAG, "onClick: invalid rate: " + rate);
                Toast.makeText(mContext, "you need to choose how many stars to rate this place", Toast.LENGTH_SHORT).show();
            } else  {
                Log.d(TAG, "onClick: rate the place with rate: " + rate);
                Toast.makeText(mContext, nameTV.getText().toString() +" rated with "+rate+" stars !", Toast.LENGTH_SHORT).show();
                mFireMethods.ratePlaceDB(placeId, rate);
            }

        }

    }




    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods mFireMethods;
    /* ---------------------- FIREBASE ----------------------- */

    // Widgets
    private TextView nameTV, addressTv, distanceTV, phoneTv, websiteTV;
    private TextView likeTV;
    private TextInputLayout tagsIL; // GONE
    private TextInputEditText tagsET;
    private CircleImageView logoIv;
    private ProgressBar progressBar;
    private GridView gridView;
    private ArrayList<Bitmap> mBitmaps;
    private ArrayList<Bitmap> mBitmapsDB;
    private ArrayList<Photo> mPhotos;
    private Button addTagBtn;
    private ImageView whiteHeart, redHeart, cameraIV;
    private LinearLayout likeLL;
    private ImageView[] stars;
    private Button rateBtn;

    // Fonts
    private MyFonts mFonts;

    // Vars
    private int activityNum;
    private GeoDataClient mGeoDataClient;
    private String placeId;
    private ArrayList<String> mFavoritePlacesIds;
    private boolean mLikedByUser = false;
    private int rate = -1;
    private boolean loadDBandGoogle = true;  // Checks if one loading (photos from db or photos from google place took place...)


    private int dismiss_progressBar = 3;
    private int progressBarCounter = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_one_place);

        mFonts = new MyFonts(mContext);
        mFireMethods = new FirebaseMethods(mContext);
        mBitmaps = new ArrayList<>();
        mFavoritePlacesIds = new ArrayList<>();

        Intent intent = getIntent();
        placeId = intent.getStringExtra(getString(R.string.intent_place_id));
        activityNum = intent.getIntExtra(mContext.getString(R.string.activity_number), 0);

        matchWidgetsToIDs();
        setupFirebaseStaff();

        mGeoDataClient = Places.getGeoDataClient(this);
        mGeoDataClient.getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse allPlaces = task.getResult();
                    Place place = allPlaces.get(0);

                    String websiteStr = place.getWebsiteUri() != null ? place.getWebsiteUri().toString() : null;  // Needed because it is URI
                    setupPlaceDetailsWidgets(place.getId(), place.getName().toString(),
                            place.getAddress().toString(), websiteStr,
                            place.getPhoneNumber().toString(), place.getLatLng());

                    allPlaces.release();
                    addToProgressBar();

                } else {
                    Log.e(TAG, "Place not found.");
                }
            }
        });

        getPhotos();
        setupBottomNavigationView();
    }


    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(this, this, bottomNavigationViewEx, mFavoritePlacesIds);
        Menu menu = bottomNavigationViewEx.getMenu();

        MenuItem menuItem = menu.getItem(activityNum);
        menuItem.setChecked(true);
    }

    /**
     * Gets place's details and puts them on the correct format in the widgets.
     */
    private void setupPlaceDetailsWidgets(final String placeId, final String placeName, String placeAddress,
                                          String placeWebsite, String placePhone, LatLng placeLatLng) {
        nameTV.setText(placeName);
        addressTv.setText(placeAddress);
        LatLng curLatLng = getCurrentLatLnt();
        if (curLatLng != null) {
            double dis = (int) (RazUtils.getDistance(placeLatLng, curLatLng) * 10) / 10.0;
            distanceTV.setText(RazUtils.getDistanceString(dis));
        } else {
            distanceTV.setVisibility(View.GONE);
        }
        if (placeWebsite != null && placeWebsite.length() > 0) {
            websiteTV.setText(placeWebsite);
            websiteTV.setContentDescription(placeWebsite);
        }
        else
            websiteTV.setVisibility(View.GONE);
        if (placePhone != null && placePhone.length() > 0)
            phoneTv.setText(placePhone);
        else
            phoneTv.setVisibility(View.GONE);

        addTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewOnePlaceActivity.this, ChoosePlaceTagsActivity.class);
                intent.putExtra(getString(R.string.intent_place_id), placeId);
                intent.putExtra(getString(R.string.intent_place_name), placeName);
                startActivity(intent);
            }
        });

//        whiteHeart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FirebaseDatabase.getInstance().getReference()
//                        .child(getString(R.string.db_persons))
//                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                        .child(getString(R.string.db_field_favorite_places_ids))
//                        .addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                ArrayList<String> favorites = new ArrayList<>();
//                                for (DataSnapshot favDS :dataSnapshot.getChildren()) {
//                                    favorites.add(favDS.getValue(String.class));
//                                }
//                                favorites.add(placeId);
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
//            }
//        });

    }

    /**
     * Request Permission and returns the current LatLng,
     * @return device's current LatLng.
     */
    private LatLng getCurrentLatLnt() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(Permissions.ACCESS_FINE_LOCATION, LOCATION_REQUEST_CODE);
            }
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "currentLatLnt: permission?");
        } else if (locationManager != null) {
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                return new LatLng(lat, lng);
            }
        }
        return null;
    }

    private void matchWidgetsToIDs() {
        nameTV = findViewById(R.id.place_name_tv);
        addressTv = findViewById(R.id.address);
        distanceTV = findViewById(R.id.distance);
        phoneTv = findViewById(R.id.phone);
        websiteTV = findViewById(R.id.website);
        logoIv = findViewById(R.id.logo_image);
        progressBar = findViewById(R.id.placeProfileProgressBar);
        gridView = findViewById(R.id.gridView);
        tagsIL = findViewById(R.id.tags_input_layout);
        tagsET = findViewById(R.id.tags_et);
        addTagBtn = findViewById(R.id.add_tag_btn);
        whiteHeart = findViewById(R.id.image_heart_blank);
        redHeart = findViewById(R.id.image_heart_red);
        cameraIV = findViewById(R.id.camera_iv);
        likeTV = findViewById(R.id.like_tv);
        likeLL = findViewById(R.id.like_ll);
        stars = new ImageView[]{findViewById(R.id.star1),
                findViewById(R.id.star2),
                findViewById(R.id.star3),
                findViewById(R.id.star4),
                findViewById(R.id.star5)};
        rateBtn = findViewById(R.id.rate_btn);

        addTagBtn.setTypeface(mFonts.getBoldFont());

        cameraIV.setVisibility(View.VISIBLE);

        // stars click listeners
        for (ImageView star : stars) {
            star.setOnClickListener(this);
        }
        rateBtn.setOnClickListener(this);

        redHeart.setOnClickListener(this);
        whiteHeart.setOnClickListener(this);
        likeLL.setOnClickListener(this);
        likeTV.setOnClickListener(this);
        cameraIV.setOnClickListener(this);
        phoneTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: phone click.");
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneTv.getText().toString()));

                if (ActivityCompat.checkSelfPermission(mContext, Permissions.CALL_PHONE_PERMISSION[0]) == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onClick: permission granted. make a call.");
                    mContext.startActivity(callIntent);
                } else {
                    requestPhonePermission();
                }
            }
        });

        websiteTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RazUtils.openUrl(mContext, websiteTV.getContentDescription().toString());
            }
        });
    }

    private void requestPhonePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Permissions.CALL_PHONE_PERMISSION[0])) {
            new AlertDialog.Builder(mContext)
                    .setTitle("Call Permission needed")
                    .setMessage("This permission is required to call to restaurants")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) mContext, Permissions.CALL_PHONE_PERMISSION, CALL_PERMISSION_CODE);
                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions((Activity) mContext, Permissions.CALL_PHONE_PERMISSION, CALL_PERMISSION_CODE);
        }
    }

    private void getPhotos() {
        mGeoDataClient.getPlacePhotos(placeId).addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {

                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                // Get the PlacePhotoMetadataBuffer (metadata for the first 10 photos).
                final PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();

                int count = Math.min(20, photoMetadataBuffer.getCount());   // How many photos?
                dismiss_progressBar += count;
                for (int i = 0; i < count; i++) {
                    PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(i);  // Photo in index i
                    // Get a full-size bitmap for the photo.
                    Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                    photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                            PlacePhotoResponse photo = task.getResult();
                            Bitmap bitmap = photo.getBitmap();
                            mBitmaps.add(bitmap);
                            if (mBitmaps.size() == 1)
                                logoIv.setImageBitmap(bitmap);
                            Log.d(TAG, "onComplete: photos: " + mBitmaps);
                            if (mBitmaps.size() == Math.min(20, photoMetadataBuffer.getCount())) {
                                Log.d(TAG, "onComplete: Put photos in the grid");
                                setupAdapter();
                            }
                            addToProgressBar();
                        }
                    });
                }
                Log.d(TAG, "onComplete: finish loop");
                addToProgressBar();
            }
        });
    }

    private void setupAdapter() {
        if (mBitmapsDB != null) mBitmaps.addAll(mBitmapsDB);
        mBitmapsDB= new ArrayList<>();
        GridPlaceImagesAdapter adapter = new GridPlaceImagesAdapter(ViewOnePlaceActivity.this, R.layout.layout_grid_imageview, mBitmaps);

        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bitmap bitmap = mBitmaps.get(position);

                // TODO(!) Make a difference between users-photos and Google-photos

                UniversalImageLoader.bigPhoto(mContext, ViewOnePlaceActivity.this, bitmap);

            }
        });
    }

    private void addToProgressBar() {
        progressBarCounter++;
        if (progressBarCounter >= dismiss_progressBar) {
            Log.d(TAG, "addToProgressBar: progressBar Dismissed.");
            progressBar.setVisibility(View.GONE);
        }
    }

    private boolean isStarView(View view) {
        if (stars != null) {
            for (ImageView star : stars) {
                if (view == star) {
                    return true;
                }
            }
        }
        return false;
    }


    /*
     * ------------------------- Firebase ------------------------------------------
     */

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
        Log.d(TAG, "setupFirebaseStaff: called.");

        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();

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
        mRef.child(getString(R.string.db_persons))
                .child(mAuth.getCurrentUser().getUid())
                .child(getString(R.string.db_field_favorite_places_ids))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mFavoritePlacesIds = new ArrayList<>();
                        for (DataSnapshot placeIdDS : dataSnapshot.getChildren()) {
                            String pid = placeIdDS.getValue(String.class);
                            if (pid.equals(placeId)) {
                                mLikedByUser = true;
                            }
                            mFavoritePlacesIds.add(pid);

                            if (mLikedByUser) {
                                whiteHeart.setVisibility(View.GONE);
                                redHeart.setVisibility(View.VISIBLE);

                            } else {
                                whiteHeart.setVisibility(View.VISIBLE);
                                redHeart.setVisibility(View.GONE);
                            }
                        }
                        addToProgressBar();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "onCancelled: firebaseDatabaseError: " + databaseError.getMessage());
                    }
                });

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // initialize the stars colors
                rate = mFireMethods.getPlaceRateDB(dataSnapshot, placeId, mAuth.getCurrentUser().getUid());
                for (int i=0; i<stars.length; i++ ){
                    if (i < rate) stars[i].setImageResource(STAR_ON_XML);
                    else stars[i].setImageResource(STAR_OFF_XML);
                }

                if (mBitmapsDB == null) mBitmapsDB = new ArrayList<>();
                ArrayList<Photo> photos = mFireMethods.getPlacePhotos(dataSnapshot, placeId);
                final int PHOTOS_FROM_DB_COUNTER = photos.size();
                for (Photo photo : photos) {
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.init(ImageLoaderConfiguration.createDefault(mContext));
                    imageLoader.loadImage(photo.getImage_path(), new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            // loaded bitmap is here (loadedImage)
                            Log.d(TAG, "onLoadingComplete: loadedImageeeee: " + loadedImage);
                            mBitmapsDB.add(loadedImage);
                            if (mBitmapsDB.size() == PHOTOS_FROM_DB_COUNTER) {
                                setupAdapter();
                            }

                        }
                    });
//                    mBitmapsDB.add(RazUtils.getBitmapFromURL(photo.getImage_path()));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
