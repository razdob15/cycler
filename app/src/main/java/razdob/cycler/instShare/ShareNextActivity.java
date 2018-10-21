package razdob.cycler.instShare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import razdob.cycler.ChoosePlaceTagsActivity;
import razdob.cycler.MainRegisterActivity;
import razdob.cycler.R;
import razdob.cycler.dialogs.MustLocationDialog;
import razdob.cycler.dialogs.NotRestaurantDialog;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.MyFonts;
import razdob.cycler.myUtils.StringManipulation;
import razdob.cycler.myUtils.UniversalImageLoader;

/**
 * Created by Raz on 09/06/2018, for project: PlacePicker2
 */
public class ShareNextActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ShareNextActivity";
    private static final int SHARE_TAGS_REQUEST_CODE = 99;
    private final Context mContext = ShareNextActivity.this;

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mRef;
    private FirebaseMethods mFireMethods;
    /* ---------------------- FIREBASE ----------------------- */

    // Map
    private GeoDataClient mGeoDataClient;

    // Widgets
    private EditText captionET;
    private TextView photoTitleTV;
    private TextView chooseLocTV;
    private TextView shareTV;
    private TextView placeNameTV, placeAddressTV, tagsTV, chooseTagsTV;
    private ImageView editLocIV, editTagsIV;
    private ImageView backArrow;
    private LinearLayout tagsLL;


    @Override
    public void onClick(View v) {
        if (v == chooseLocTV || v == editLocIV) {
            openMap();
        } else if (v == chooseTagsTV || v == editTagsIV) {
            chooseTags();
        } else if (v == backArrow) {
            finish();
        } else if (v == shareTV) {
            if (placeName == null) {
                openDialogChoosePlace();
            } else { // Place Was Chose - OK
                if (placeId != null){
                    uploadPhoto();
                } else {
                    Log.w(TAG, "onClick: Error: PlaceId is Null !");
                }
            }
        }

    }

    // Vars
    private String mAppend = "file:/";
    private long imageCount = 0;
    private String imgUrl;
    private Bitmap bitmap;
    private Intent intent;
    private String placeId, placeName, placeAddress;
    private final int PLACE_PICKER_REQUEST = 9;
    private ArrayList<String> favoritePlacesIds;
    private MyFonts mFonts;
    private String placeTags = "";

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(mContext.getString(R.string.sp_main_place_tags),MODE_PRIVATE);
        placeTags = preferences.getString(mContext.getString(R.string.sp_place_tags),"");
        setupRestaurantDetails();
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next_in_share);
        setupFirebaseStaff();
        mFireMethods = new FirebaseMethods(mContext);
        favoritePlacesIds = new ArrayList<>();
        mFonts = new MyFonts(mContext);

        Log.d(TAG, "onCreate: chchchchc: " +getIntent().hasExtra("chcek"));

        setupWidgets();
        setupClicksListeners();

        getPlaceFromIntent();
        if (placeId != null && placeName != null) {
            setupRestaurantDetails();
        } else {
            photoTitleTV.setText(mContext.getString(R.string.title_upload_casual_photo));
        }

        if (placeName != null && placeId != null) {
            Log.d(TAG, "onCreate: a place is already chosen.");
            chooseLocTV.setVisibility(View.GONE);
        }

        setImage();
    }

    private void uploadPhoto() {
        // Upload the image to firebase
        String photoType;
        String caption = captionET.getText().toString();

        if (placeName != null && placeId != null) {
            Log.d(TAG, "onClick: place Photo");
            photoType = getString(R.string.place_photo);
            if (intent.hasExtra(getString(R.string.selected_image))) {  // Came from GALLERY
                imgUrl = intent.getStringExtra(getString(R.string.selected_image));
                mFireMethods.uploadNewPhoto(photoType, caption, imageCount, imgUrl, bitmap, placeId, placeTags);

            } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {  // Came rom CAMERA
                bitmap = intent.getParcelableExtra(getString(R.string.selected_bitmap));
                mFireMethods.uploadNewPhoto(photoType, caption, imageCount, null, bitmap, placeId);
            }
        } else {
            Log.w(TAG, "uploadPhoto: ERROR - Photo is not a place Photo !!");
        }

    }

    private void openDialogChoosePlace() {
        Log.d(TAG, "openDialogChoosePlace: open ChooseRestDialog");
        final MustLocationDialog dialog = new MustLocationDialog(mContext);
        dialog.setCancelable(false);
        dialog.setYesClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
                dialog.dismiss();
            }
        });
        dialog.setNoClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    /**
     * Sets up the widgets
     */
    private void setupWidgets() {
        captionET = findViewById(R.id.caption);
        photoTitleTV = findViewById(R.id.photo_title_tv);
        chooseLocTV = findViewById(R.id.choose_location_tv);
        shareTV = findViewById(R.id.share_tv);
        placeNameTV = findViewById(R.id.place_name_tv);
        placeAddressTV = findViewById(R.id.place_address_tv);
        editLocIV = findViewById(R.id.edit_location_iv);
        tagsTV = findViewById(R.id.tags_tv);
        editTagsIV = findViewById(R.id.edit_tags_iv);
        chooseTagsTV = findViewById(R.id.choose_tags_tv);
        backArrow = findViewById(R.id.back_arrow_iv);
        tagsLL = findViewById(R.id.tags_ll);

        editLocIV.setVisibility(View.GONE);
        editTagsIV.setVisibility(View.GONE);
        tagsLL.setVisibility(View.GONE);

        chooseLocTV.setTypeface(mFonts.getBoldItalicFont());
        placeNameTV.setTypeface(mFonts.getBoldFont());
        placeAddressTV.setTypeface(mFonts.getLightItalicFont());
        tagsTV.setTypeface(mFonts.getBoldItalicFont());
    }

    private void setupClicksListeners() {
        editTagsIV.setOnClickListener(this);
        chooseTagsTV.setOnClickListener(this);
        chooseLocTV.setOnClickListener(this);
        editLocIV.setOnClickListener(this);
        backArrow.setOnClickListener(this);
        shareTV.setOnClickListener(this);
    }

    private void choosePlace(Place place) {
        placeId = place.getId();
        placeName = place.getName().toString();
        placeAddress = place.getAddress().toString();
        Log.d(TAG, "choosePlace: adddfdafsagress:  "+placeAddress);
        placeTags = "";
    }

    private void chooseTags() {
        if (placeId == null)
            return;

        Intent intent = new Intent(mContext, ChoosePlaceTagsActivity.class);
        intent.putExtra(mContext.getString(R.string.intent_place_id), placeId);
        intent.putExtra(mContext.getString(R.string.calling_activity), mContext.getString(R.string.share_next_activity));
        startActivity(intent);

    }

    /*
     *   -------------------------- MAP ------------------------------
     */

    /**
     * Sends a request to OnActivityResult
     */
    private void openMap() {
        mGeoDataClient = Places.getGeoDataClient(mContext);

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(ShareNextActivity.this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the map again
     */
    private void reopenMap() {
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(ShareNextActivity.this), PLACE_PICKER_REQUEST);
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
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!mFireMethods.isRestaurant(dataSnapshot, place.getId(), place.getPlaceTypes())) {
                                Log.d(TAG, "onDataChange: place is not a restaurant! show dialog!");
                                showNotRestaurantDialog(place);
                            } else {
                                // Place is OK, add to favorites
                                Log.d(TAG, "onActivityResult: open This place View: " + place.getId());
                                choosePlace(place);
                                setupRestaurantDetails();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "onCancelled: DBError:  "+ databaseError.getMessage());
                        }
                    });

//
//                    if (!RazUtils.isRestaurant(place.getPlaceTypes())) {  // Not a restaurant! Open Dialog
//                        Log.d(TAG, "onActivityResult: place is not a restaurant! Show dialog!");
//                        showNotRestaurantDialog(place);
//                    } else {
//                        // Place is OK, add to favorites
//                        Log.d(TAG, "onActivityResult: open This place View: " + place.getId());
//                        placeId = place.getId();
//                        placeName = place.getName().toString();
//                        placeAddress = place.getAddress().toString();
//                        setupRestaurantDetails();
//                    }
                }
            } else {
                Log.d(TAG, "onActivityResult: NOT OK !!");
            }
        }
    }

    /**
     * Shows a NotRestaurantDialog for the chosen place.
     */
    private void showNotRestaurantDialog(final Place place) {
        final NotRestaurantDialog dialog = new NotRestaurantDialog(mContext);

        dialog.setYesClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Yes Click !");
                dialog.dismiss();
                choosePlace(place);
                setupRestaurantDetails();
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
     * Updates the activity TextViews to the chosen activity.
     * Requirement Before:
     *  - PlaceName
     *  - PlaceId
     *  - Widgets
     */
    private void setupRestaurantDetails() {
        Log.d(TAG, "setupRestaurantDetails: choose restaurant: " + placeId + " for the photo.");
        if (mRef == null) setupFirebaseStaff();

        if (placeName != null) {
            chooseLocTV.setText("Location:");
            editLocIV.setVisibility(View.VISIBLE);
            placeNameTV.setText(placeName);
            placeAddressTV.setText(placeAddress);
            photoTitleTV.setText(mContext.getString(R.string.title_upload_place_photo) + " " + placeName);

            if (placeId != null && (placeTags == null || placeTags.length() <= 1)) {
                mRef.child(mContext.getString(R.string.db_users_tag_places))
                        .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.hasChild(placeId)) {
                                    placeTags = "";
                                    for (DataSnapshot tagDS : dataSnapshot.child(placeId)
                                            .getChildren()) {
                                        placeTags += "#" + tagDS.getKey() + " ";
                                    }
                                    tagsTV.setText(placeTags);
                                    if (dataSnapshot.child(placeId).getChildrenCount() > 0) {
                                        chooseTagsTV.setText("TAGS:");
                                        editTagsIV.setVisibility(View.VISIBLE);
                                    }
                                    else {
                                        chooseTagsTV.setText("Tag This Place");
                                        editTagsIV.setVisibility(View.GONE);
                                    }
                                }
                                tagsLL.setVisibility(View.VISIBLE);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.w(TAG, "onCancelled: DBError: " + databaseError.getMessage());
                            }
                        });
            } else {
                tagsTV.setText(placeTags);
                tagsLL.setVisibility(View.VISIBLE);
                chooseTagsTV.setText("TAGS:");
                editTagsIV.setVisibility(View.VISIBLE);
            }
        }

        // TODO(!): Continue from here !! Choose the restaurant as the photo's location
    }




    /**
     * initializes the placeId & placeNameTV from the incoming intent.
     */
    private void getPlaceFromIntent() {
        if (intent == null) intent = getIntent();
        if (intent.hasExtra(mContext.getString(R.string.intent_place_id))) {
            placeId = intent.getStringExtra(mContext.getString(R.string.intent_place_id));
        }
        if (intent.hasExtra(mContext.getString(R.string.intent_place_name))) {
            placeName = intent.getStringExtra(mContext.getString(R.string.intent_place_name));
        }
        if (intent.hasExtra(mContext.getString(R.string.intent_place_address))) {
            placeAddress = intent.getStringExtra(mContext.getString(R.string.intent_place_address));
        }
    }

    private void someMethod() {
        /*
            Step(1): Create a data model for photos

            Step(2): Add properties to the photo Objects (caption, date, imageUrl, photo_id, tags, user_id)

            Step(3): Count the number of photos that the user already has.

            Step(4):
                A) Upload the photo to Firebase Storage.
                B) Insert into 'photos' node
                C) Insert into 'user_photos' node
         */
    }

    /**
     * Gets the image url from the incoming intent and displays the chosen image.
     */
    private void setImage() {
        if (intent == null) intent = getIntent();
        final ImageView imageView = findViewById(R.id.image_share);
        if (intent.hasExtra(mContext.getString(R.string.intent_size))) {
            Log.d(TAG, "setImage: BYTES EXTRA !");

//            final int MB = 1000000;
//            int size = intent.getIntExtra(mContext.getString(R.string.intent_size), 0);
//            int n = size/MB;
//            if (size % MB != 0)
//                n++;
//
//            byte[] bytes = new byte[size];
//
//            for (int i=0; i<n; i++) {
//                byte[] temp = intent.getByteArrayExtra(mContext.getString(R.string.intent_byte_arr)+i);
//                if (i == n-1) {
//                    System.arraycopy(temp, 0, bytes, i * 1000000, size - i*MB);
//                } else {
//                    System.arraycopy(temp, 0, bytes, i * 1000000, 1000000);
//                }
//            }
//            Log.d(TAG, "setImage: length: " + bytes.length);
//            Log.d(TAG, "setImage: last : " + bytes[bytes.length-1]);

//            byte[] bytes = intent.getByteArrayExtra(mContext.getString(R.string.intent_byte_arr));
//            bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
//            imageView.setImageBitmap(bitmap);

        }
        else if (intent.hasExtra(mContext.getString(R.string.intent_selected_photo_uri))) {
            imgUrl = intent.getStringExtra(mContext.getString(R.string.intent_selected_photo_uri));
            try {
                bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), Uri.parse(imgUrl));
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (intent.hasExtra(getString(R.string.selected_image))) {
            // came from GALLERY
            imgUrl = intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "setImage: get new image url from gallery: " + imgUrl);
            UniversalImageLoader.setImage(mContext, imgUrl, imageView, null, mAppend);

        } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
            // Came rom CAMERA
            bitmap = intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Log.d(TAG, "setImage: got new bitmap from the camera");
            imageView.setImageBitmap(bitmap);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bitmap != null)
                    UniversalImageLoader.bigPhoto(mContext, ShareNextActivity.this, bitmap);
                else if (imgUrl != null)
                    UniversalImageLoader.bigPhoto(mContext, ShareNextActivity.this, imgUrl);
            }
        });

    }



    /*
     * ----------------------------- Firebase ------------------------------------------
     */


    private void setupFirebaseStaff() {
        Log.d(TAG, "setupFirebaseStaff: called.");

        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();

        Log.d(TAG, "setupFirebaseStaff: image count: " + imageCount);

        // Init mAuthStateListener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                // Check if user is logged in

                if (user == null) {
                    Log.d(TAG, "onAuthStateChanged: User log-out");
                    startActivity(new Intent(ShareNextActivity.this, MainRegisterActivity.class));
                } else {
                    Log.d(TAG, "onAuthStateChanged: User is logged-in :) uid = " + firebaseAuth.getCurrentUser().getUid());
                }
            }
        };
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                favoritePlacesIds = mFireMethods.getFavoritePlacesIds(dataSnapshot);
                if (intent == null) intent = getIntent();
                if (intent.hasExtra(mContext.getString(R.string.intent_place_id))) {
                    imageCount = mFireMethods.getPlaceImageCount(dataSnapshot,
                            intent.getStringExtra(mContext.getString(R.string.intent_place_id)));
                } else
                    imageCount = mFireMethods.getUserPhotosCount(dataSnapshot);
                Log.d(TAG, "onDataChange: image count: " + imageCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAuthStateListener != null)
            mAuth.removeAuthStateListener(mAuthStateListener);
    }


}
