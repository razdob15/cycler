package razdob.cycler.un_used__14_8;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.multidex.MultiDex;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.datepicker.DatePickerBuilder;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import razdob.cycler.MainRegisterActivity;
import razdob.cycler.R;
import razdob.cycler.dialogs.NotRestaurantDialog;
import razdob.cycler.fivePlaces.FivePlacesActivity;
import razdob.cycler.models.User;
import razdob.cycler.models.UserAccountSettings;
import razdob.cycler.myUtils.FirebaseInserts;
import razdob.cycler.models.Business;
import razdob.cycler.myUtils.StringManipulation;
import razdob.cycler.tests.MaterialSubjectsActivity;

import static razdob.cycler.myUtils.FirebaseInserts.*;

public class PersonProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Constants
    private static final String TAG = "PersonProfileActivity";
    public static final String ANONYMOUS = "anonymous";

    private static final int PICK_IMAGE_REQUEST = 222;  // Image Request
    private static final int RC_SIGN_IN = 120;
    private static final int PARAMS = 2;  // How many *must* parameters we want before the user can continue?
    private static final int PLACE_PICKER_REQUEST = 1000;

    // Firebase Staff:
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mFireUser;
    private ArrayList<String> favoritePlacesIds;
    private StorageReference mStorageReference;

    // Google places API
    private GeoDataClient mGeoDataClient;
    private GoogleApiClient mClient;

    // GUI
    private Button chooseProfileImageBtn;
    private TextInputEditText nameET, addressET;
    private ProgressBar progressBar;
    private ImageView profileImageView;
    // Header
    private TextView headName, headMail;
    private ImageView headProfileImage;
    // Drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    // Vars
    private boolean[] continueBtnClickable = new boolean[PARAMS];
    private List<String> lovePlacesIds;
    private Uri profileImageURI;
    private TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            switch (actionId) {
                case EditorInfo.IME_ACTION_NEXT:
                    Toast.makeText(PersonProfileActivity.this, "Next", Toast.LENGTH_SHORT).show();
                    break;
                case EditorInfo.IME_ACTION_SEND:
                    Toast.makeText(PersonProfileActivity.this, "Send", Toast.LENGTH_SHORT).show();
                    if (myUser == null) myUser = new User();
                    myUser.setName(nameET.getText().toString());
                    myUser.setAddress(addressET.getText().toString());
                    savePersonInfo(mFireUser.getUid(), myUser, mUserAccountSettings);
                    break;
            }
            return false;
        }
    };
    // User Data
    public static User myUser;
    public static String mUserName;
    public static String mUserAddress = null;
    private UserAccountSettings mUserAccountSettings;

    // User Profile Image URI
    private String userPImageUri;

    // User Preferences
    public static HashMap<String, Boolean> userPreferences;

    private int showProgressBar = 0;
    final int WHEN_TO_DISMISS_PB = 3;

    /* --------------------------------------- ACTIVITY ------------------------------------------*/
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        MultiDex.install(this);
        setContentView(R.layout.activity_person_profile);

        userPreferences = new HashMap<>();
        lovePlacesIds = new ArrayList<>();

        // Connect items to their Ids.
        matchWidgetsToId();

        // Side Navigation & Header
        NavigationView navigationView = findViewById(R.id.side_navigate);
        View headView = navigationView.getHeaderView(0);
        headName = headView.findViewById(R.id.head_name);
        headMail = headView.findViewById(R.id.head_email);
        headProfileImage = headView.findViewById(R.id.head_profile_image);
        navigationView.setNavigationItemSelectedListener(this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                savePersonInfo(mFireUser.getUid(), myUser, mUserAccountSettings);
                switch (item.getItemId()) {
                    case R.id.action_main:      // Index 0
                        break;
                    case R.id.action_map:       // Index 1
                        Toast.makeText(PersonProfileActivity.this, "MAP !", Toast.LENGTH_SHORT).show();
                        openMap();
                        break;
                    case R.id.action_camera:    // Index 2
                        Toast.makeText(PersonProfileActivity.this, "Take A photo !", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_like:      // Index 3
                        Intent intent2 = new Intent(PersonProfileActivity.this, FivePlacesActivity.class);
                        if (myUser.getFavoritePlacesIDs() != null)
                            intent2.putStringArrayListExtra(getString(R.string.intent_love_places_ids), (ArrayList<String>) myUser.getFavoritePlacesIDs());
                        else
                            intent2.putStringArrayListExtra(getString(R.string.intent_love_places_ids), new ArrayList<String>());
                        startActivity(intent2);
                        break;
                    case R.id.action_next:      // Index 4
                        if (mFireUser == null) {
                            Toast.makeText(PersonProfileActivity.this, "Sorry, You need to log-in first", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            startActivity(new Intent(PersonProfileActivity.this, MainRegisterActivity.class));
                            break;
                        }
                        if (nameET.getText().toString().length() == 0) {
                            Toast.makeText(PersonProfileActivity.this, "Fill your name, please", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        mUserName = nameET.getText().toString();
                        mUserAddress = addressET.getText().toString();
                        if (myUser == null) myUser = new User();
                        myUser.setName(mUserName);
                        myUser.setAddress(mUserAddress);
                        myUser.setProfile_photo(userPImageUri);

                        Intent intent = new Intent(PersonProfileActivity.this, SubjectsActivity.class);
                        startActivity(intent);
                        break;

                }
                return true;
            }
        });

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.cycler_green)));
        } else {
            setSupportActionBar(toolbar);
        }

        setupFirebaseStaff();

        headMail.setText(mFireUser.getEmail());

        // Date Picker
        findViewById(R.id.date_picker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PersonProfileActivity.this, "Open Date Picker !", Toast.LENGTH_SHORT).show();

                DatePickerBuilder dpb = new DatePickerBuilder()
                        .setFragmentManager(getSupportFragmentManager())
                        .setStyleResId(R.style.BetterPickersDialogFragment)
                        .setYearOptional(true);
                dpb.show();
            }
        });


        nameET.setText(mUserName);
        headName.setText(mUserName);
        addressET.setText(mUserAddress);

        // Editor action Listener
        nameET.setOnEditorActionListener(editorActionListener);
        addressET.setOnEditorActionListener(editorActionListener);

        // Add navigationMenu
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        chooseProfileImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
        // Check again...
        continueBtnClickable[0] = nameET.getText().toString().length() > 0;
        continueBtnClickable[1] = addressET.getText().toString().length() > 0;

        // text watchers - Refer to continueBtn's clickable.
        nameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                continueBtnClickable[0] = count > 0;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                continueBtnClickable[0] = count > 0;
            }

            @Override
            public void afterTextChanged(Editable s) {
                continueBtnClickable[0] = s.length() > 0;
                mUserName = s.toString();
                if (myUser != null) myUser.setName(s.toString());
                if (mUserAccountSettings != null) mUserAccountSettings.setUserName(s.toString());
            }
        });
        addressET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                continueBtnClickable[1] = count > 0;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                continueBtnClickable[1] = count > 0;
            }

            @Override
            public void afterTextChanged(Editable s) {
                continueBtnClickable[1] = s.length() > 0;
                mUserAddress = s.toString();
                myUser.setAddress(s.toString());
            }
        });

        // Storage.. Profile Image
        mStorageReference.child("images/persons/" + mFireUser.getUid() + "/profile_photo").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                userPImageUri = uri.toString();
                loadImageFromUrl(userPImageUri);
                addToShowProgressBar();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override

            public void onFailure(@NonNull Exception e) {
            }
        });

        progressBar.setVisibility(View.GONE);
        FirebaseCrash.log("PersonActivity Created");

    }

    private void matchWidgetsToId() {
        addressET = findViewById(R.id.address_ti);
        nameET = findViewById(R.id.name_it);
        progressBar = findViewById(R.id.progressBar);
        chooseProfileImageBtn = findViewById(R.id.choose_image_btn);
        profileImageView = findViewById(R.id.profile_iv);
        mDrawerLayout = findViewById(R.id.drawer);
    }

    // ProgressBar Dismiss(?)
    private void addToShowProgressBar() {
        showProgressBar++;
        if (showProgressBar >= WHEN_TO_DISMISS_PB)
            progressBar.setVisibility(View.GONE);
    }

    // Open Sign out Dialog
    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        // set title
        alertDialogBuilder.setTitle("Sign out");

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you want to sign out?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseAuth.getInstance().signOut();
                        onSignedOutCleanup();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    /**
     * Responsible for:
     * SING IN
     * PICK IMAGE
     * PLACE PICKER
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {  // Sign in - Auth
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in !", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Signed-in CANCELED", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            addToShowProgressBar();

            // Pick an image and put it in the imageView
            profileImageURI = data.getData();
            userPImageUri = profileImageURI.toString();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), profileImageURI);
                profileImageView.setImageBitmap(bitmap);
                headProfileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (profileImageURI != null && mFireUser != null) {

                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Uploading image...");
                progressDialog.show();

                String imageLoc = "images/persons/" + mFireUser.getUid();
                FirebaseInserts.uploadFile(this, imageLoc, profileImageURI, "profile.jpg", progressDialog);
            }

        } else if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                final Place place = PlacePicker.getPlace(data, this);
                Log.d(TAG, "onActivityResult: PlaceName(1)" + place.getName());


                Log.d(TAG, "onActivityResult: 5 favoritePlacesIds to choose !");
                if (StringManipulation.isMapCoordinates(place.getName().toString())) {  // not a restaurant! reopen map!
                    Log.d(TAG, "onActivityResult: this is a location and not a place!");
                    Toast.makeText(this, "Please, choose a restaurant", Toast.LENGTH_SHORT).show();

                    reopenMap();
                } else {
                    if (!isRestaurant(place.getPlaceTypes())) {  // Not a restaurant! Open Dialog
                        Log.d(TAG, "onActivityResult: place is not a restaurant! Show dialog!");
                        final NotRestaurantDialog notRestaurantDialog = new NotRestaurantDialog(PersonProfileActivity.this);

                        notRestaurantDialog.setYesClick(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "onClick: Yes Click !");
                                notRestaurantDialog.dismiss();
                                chooseFavoritePlace(place.getId());
                            }
                        });
                        notRestaurantDialog.setNoClick(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "onClick: Not Click !");
                                notRestaurantDialog.dismiss();
                                reopenMap();
                            }
                        });
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


    /* -------------------------------- TOP MENU (Only support btn)  ------------------------------*/
    // TODO(1): Change this from a menu to 1 ImageView (Support)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        /*if (id == R.id.tb_action_settings){
            Toast.makeText(this, "tb_action SETTINGS", Toast.LENGTH_SHORT).show();
            return true;
        }*/
        if (id == R.id.tb_action_supprt) {
            Toast.makeText(this, "SUPPORT", Toast.LENGTH_SHORT).show();
        }

        return mToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }


    /* -------------------------------------- Profile Image  --------------------------------------*/
    // Open the image's chooser
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select your profile image"), PICK_IMAGE_REQUEST);
    }

    // Picasso loads url
    private void loadImageFromUrl(String url) {
        Log.i("person_uri", url);
        Picasso.get().load(url).placeholder(R.mipmap.cycler_launcher)
                .error(R.mipmap.cycler_launcher)
                .into(profileImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.i("profileImage", "The user's profile image was loaded successfully ! ");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.i("profileImage", "The user's profile image was not loaded successfully !:( ");
                    }
                });

//        Picasso.with(this).load(url).placeholder(R.mipmap.cycler_logo)
//                .error(R.mipmap.cycler_logo)
//                .into(headProfileImage, new com.squareup.picasso.Callback() {
//
//                    @Override
//                    public void onSuccess() {
//                        Log.i("user_profile_image", "The user's profile image was loaded successfully ! ");
//                    }
//
//                    @Override
//                    public void onError() {
//                        Log.i("user_profile_image", "The user's profile image loading got an Error ! ");
//                    }
//                });


    }

    private boolean isArrTrue(boolean[] booleans) {
        for (boolean bool : booleans)
            if (!bool) return false;
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.main:
                Intent intent = new Intent(PersonProfileActivity.this, MomentSubjectsActivity.class);
                startActivity(intent);
                break;
            case R.id.preferences:
                Intent intent1 = new Intent(this, MaterialSubjectsActivity.class);
                startActivity(intent1);
                break;
            case R.id.settings:
                Toast.makeText(this, "Settings Click", Toast.LENGTH_SHORT).show();
                break;
            case R.id.event:
                Toast.makeText(this, "Events Click", Toast.LENGTH_SHORT).show();
                break;
            case R.id.search:
                break;
            case R.id.favorites:
                Intent intent2 = new Intent(this, FivePlacesActivity.class);
                if (myUser.getFavoritePlacesIDs() != null) {
                    intent2.putStringArrayListExtra(getString(R.string.intent_love_places_ids), (ArrayList<String>) myUser.getFavoritePlacesIDs());
                } else
                    intent2.putStringArrayListExtra(getString(R.string.intent_love_places_ids), new ArrayList<String>());
                startActivity(intent2);
                break;
            case R.id.logout:
//                onSignedOutCleanup();  // TODO(?) Maybe destroy
                FirebaseAuth.getInstance().signOut();
                break;

        }
        return false;
    }




    /* -------------------------------------- GOOGLE MAP -----------------------------------------*/

    /**
     * Sends a request to OnActivityResult
     */
    private void openMap() {

        mGeoDataClient = Places.getGeoDataClient(PersonProfileActivity.this);
//        mClient = new GoogleApiClient
//                .Builder(this)
//                .addApi(Places.GEO_DATA_API)
//                .addApi(Places.PLACE_DETECTION_API)
//                .build();

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(PersonProfileActivity.this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the placeTypes contains a type which is connected to food.
     *
     * @param placeTypes some Place's types
     * @return True if at least one of the types is connected to food. Otherwise-False.
     */
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
            startActivityForResult(builder.build(PersonProfileActivity.this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, "reopenMap: GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException" + e.getMessage());
        }
    }

    private void chooseFavoritePlace(String placeId) {
        Log.d(TAG, "chooseFavoritePlace: add place: " + placeId + " to favorites.");
        if (favoritePlacesIds.contains(placeId)) {
            Toast.makeText(PersonProfileActivity.this, "You've already choose this place", Toast.LENGTH_LONG).show();
            reopenMap();
        } else {
            favoritePlacesIds.add(placeId);

            DatabaseReference lovePlacesRef = mRef.child(getString(R.string.db_persons))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(getString(R.string.db_field_favorite_places_ids));
            lovePlacesRef.setValue(favoritePlacesIds);


            if (favoritePlacesIds.size() >= 3) {
                Intent intent = new Intent(PersonProfileActivity.this, FivePlacesActivity.class);
                intent.putStringArrayListExtra(getString(R.string.intent_love_places_ids), favoritePlacesIds);
                startActivity(intent);
            } else {
                Toast.makeText(PersonProfileActivity.this, "" + favoritePlacesIds.size() + " places was chosen", Toast.LENGTH_SHORT).show();
                reopenMap();
            }
        }
    }





    /*
     * ------------------------- Firebase ------------------------------------------
     */

    /**
     * Checks to see if the @param 'user' is logged in
     *
     * @param fireUser
     */
    private void checkCurrentUser(FirebaseUser fireUser) {
        Log.d(TAG, "checkCurrentUser: checking if the user is logged in");
        if (fireUser == null) {
            startActivity(new Intent(PersonProfileActivity.this, MainRegisterActivity.class));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mClient.connect();

        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mClient.disconnect();

        if (mAuthStateListener != null) mAuth.removeAuthStateListener(mAuthStateListener);
    }

    private void setupFirebaseStaff() {
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        // Init mAuthStateListener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                // Check if user is logged in
                checkCurrentUser(user);

                if (firebaseAuth.getCurrentUser() == null) {
                    Log.d(TAG, "onAuthStateChanged: User log-out");
                    mAuth.removeAuthStateListener(mAuthStateListener);
                    startActivity(new Intent(PersonProfileActivity.this, MainRegisterActivity.class));
                } else {
                    Log.d(TAG, "onAuthStateChanged: User is logged-in :) uid = " + firebaseAuth.getCurrentUser().getUid());
                }
            }
        };
        // Check if user is authentication
        if (mAuth.getCurrentUser() != null) {
            mFireUser = mAuth.getCurrentUser();
        } else {
            Log.w(TAG, "onCreateView: " + "The user is null !!");
        }

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uid = mAuth.getCurrentUser().getUid();
                // User Object
                try {
                    Log.d(TAG, "onDataChange: user FOUND");
                    myUser = dataSnapshot.child(getString(R.string.db_persons))
                            .child(uid).getValue(User.class);
                } catch (NullPointerException e) {
                    Log.e(TAG, "onDataChange: NullPointerException" + e.getMessage());
                    myUser = new User(PersonProfileActivity.this, dataSnapshot.child(getString(R.string.db_persons)), mFireUser.getUid());
                }
                if (myUser != null) {
                    mUserName = myUser.getName();
                    mUserAddress = myUser.getAddress();
                    userPreferences = myUser.getPreferences();
                    favoritePlacesIds = (ArrayList<String>) myUser.getFavoritePlacesIDs();
                    userPImageUri = myUser.getProfile_photo();
                    addressET.setText(mUserAddress);
                    nameET.setText(mUserName);
                }

                // UserAccountSettings Object
                    Log.d(TAG, "onDataChange: USerAccountSettings FOUND");
                    DataSnapshot tempSnap = dataSnapshot.child(getString(R.string.db_user_account_settings))
                            .child(uid);
                    mUserAccountSettings = tempSnap.getValue(UserAccountSettings.class);
                    if (mUserAccountSettings == null)
                        mUserAccountSettings = new UserAccountSettings(mUserName, mAuth.getCurrentUser().getUid());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: DatabaseError: " + databaseError.getMessage());
            }
        });
    }

    private void onSignedOutCleanup() {
        mUserName = ANONYMOUS;
        myUser = null;
    }


    /*       -------------------------- Unused functions ----------------------------------       */
    private String getAutoCompleteUrl(String place) {

        // Obtain browser key from https://code.google.com/apis/console
        String key = "AIzaSyCpyoTT3_E9ZdZloM5jKb3-n02FyO5gMhU";
        //

        // place to be be searched
        String input = "input=" + place;

        // place type to be searched
        String types = "types=geocode";

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = input + "&" + types + "&" + sensor + "&" + key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" + parameters;

        return url;
    }

    private HashMap<Place, Boolean> fromArrayListToTrueHashMap(ArrayList<Business> places) {
        HashMap<Place, Boolean> hashMap = new HashMap<>();
        for (Business b : places) {
            hashMap.put(b, true);
        }
        return hashMap;
    }

}



