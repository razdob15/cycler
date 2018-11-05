package razdob.cycler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
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
import java.util.Objects;

import razdob.cycler.adapters.TagsAdapter;
import razdob.cycler.fivePlaces.ViewOnePlaceActivity;
import razdob.cycler.models.User;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.MyFonts;
import razdob.cycler.myUtils.StringManipulation;

/**
 * Created by Raz on 02/07/2018, for project: PlacePicker2
 */
public class ChoosePlaceTagsActivity extends AppCompatActivity {
    private static final String TAG = "ChoosePlaceTagsActivity";

    private final Context mContext = ChoosePlaceTagsActivity.this;

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mRef;
    /* ---------------------- FIREBASE ----------------------- */

    // Widgets
    private TextView placeNameTV;
    private RecyclerView tagsRecyclerView;
    private TextInputEditText tagsIT;
    private Button finishBtn, addNewTagBtn;
    private ProgressBar mProgressBar;
    private ImageView xIV;

    // Fonts
    private MyFonts mFonts;

    // Vars
    private ArrayList<String> mTags;
    private ArrayList<String> mTagsKeeper;
    private ArrayList<String> chosenTags;
    private TagsAdapter mAdapter;
    private String placeName;
    private String placeId;
    private FirebaseMethods mFirebaseMethods;
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO(!): Show ADD_BTN always(?)
            Log.d(TAG, "afterTextChanged: input Changed: " + s);
            String input = s.toString().toLowerCase();
            Log.d(TAG, "afterTextChanged: input: " + input);
            if (input.length() > 0) {
                ArrayList<String> tempTags = new ArrayList<>();
                for (String tag : mTagsKeeper) {
                    if (tag.length() >= input.length() && tag.toLowerCase().substring(0, input.length()).contentEquals(input)) {
                        tempTags.add(tag);
                    }
                }
                mTags = tempTags;
                setupTagsAdapter();
                if (mTags.size() == 0) {
                    Log.d(TAG, "afterTextChanged: show AddNewTagBtn");
                    addNewTagBtn.setVisibility(View.VISIBLE);
                }
            } else {
                addNewTagBtn.setVisibility(View.GONE);
                mTags = mTagsKeeper;
                setupTagsAdapter();
            }
        }
    };
    private User myUser;
    private GeoDataClient mGeoDataClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_place_tags);

        mFonts = new MyFonts(mContext);

        mTags = new ArrayList<>();
        mTagsKeeper = new ArrayList<>();
        if (chosenTags == null) chosenTags = new ArrayList<>();
        mFirebaseMethods = new FirebaseMethods(mContext);
        setupWidgets();
        setupFirebaseStaff();

        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateExemptions();
                Intent incomeIntent = getIntent();
                if (incomeIntent.hasExtra(mContext.getString(R.string.calling_activity)) &&
                        incomeIntent.getStringExtra(mContext.getString(R.string.calling_activity)).equals(mContext.getString(R.string.share_next_activity))) {
                    SharedPreferences preferences = getSharedPreferences(mContext.getString(R.string.sp_main_place_tags),MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    StringBuilder tagsTxt = new StringBuilder();
                    for (String tag: chosenTags) {
                        tagsTxt.append("#").append(tag).append(" ");
                    }
                    editor.putString(mContext.getString(R.string.sp_place_tags), tagsTxt.toString());
                    editor.apply();
                    finish();
                } else {
                    ViewOnePlaceActivity.start(mContext, placeId, 0);
                    finish();
                }

            }
        });

        addNewTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = tagsIT.getText().toString();
                mFirebaseMethods.saveNewPlaceTagFromUser(tag, placeId);
                mTags.add(StringManipulation.placeTagFormat(tag));
                mTagsKeeper.add(tag);
                chosenTags.add(tag);
                setupTagsAdapter();
                tagsIT.setText("");
            }
        });

    }

    private void updateExemptions() {
        mRef.child(mContext.getString(R.string.db_tags_exemptions))
                .child(placeId).child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).setValue(true);

//
//        ArrayList<String> temp;
//        if (myUser.getExemption_places_ids() != null) {
//            temp = (ArrayList<String>) myUser.getExemption_places_ids();
//        } else
//            temp = new ArrayList<>();
//        if (!temp.contains(placeId))
//            temp.add(placeId);
//        myUser.setExemption_places_ids(temp);
//        mRef.child(getString(R.string.db_persons))
//                .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
//                .setValue(myUser);
    }

    private void setupWidgets() {
        placeNameTV = findViewById(R.id.place_name_tv);
        tagsRecyclerView = findViewById(R.id.tags_gv);
        tagsIT = findViewById(R.id.tags_text_input);
        finishBtn = findViewById(R.id.finish_btn);
        addNewTagBtn = findViewById(R.id.add_new_tag_btn);
        mProgressBar = findViewById(R.id.progress_bar);
        xIV = findViewById(R.id.x_iv);

        xIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagsIT.setText("");
            }
        });

        placeNameTV.setTypeface(mFonts.getBoldItalicFont());

        tagsIT.setHint(mContext.getString(R.string.choose_place_tags_hint));
        tagsIT.setImeOptions(EditorInfo.IME_ACTION_DONE);
        tagsIT.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(tagsIT.getWindowToken(), 0);
                    } else Log.d(TAG, "onEditorAction: aaaaaaaaaaadddddddddddddddd");

                    return true;
                }
                return false;
            }
        });


        Intent intent = getIntent();
        placeId = intent.getStringExtra(getString(R.string.intent_place_id));
        mGeoDataClient = Places.getGeoDataClient(mContext);
        mGeoDataClient.getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    Place place = places.get(0);

                    Log.d(TAG, "onComplete: intersting: places.getCount(): " + places.getCount());

                    placeName = place.getName().toString();
                    placeNameTV.setText(placeName);

                    places.release();

                } else {
                    Log.e(TAG, "Place not found.");
                }
            }
        });

//        findViewById(R.id.like_ll).setVisibility(View.GONE);
//        findViewById(R.id.image_heart_blank).setVisibility(View.GONE);
//        findViewById(R.id.image_heart_red).setVisibility(View.GONE);
    }

    /**
     * setting up the RecyclerView's adapter and make it 3-columns grid.
     */
    private void setupTagsAdapter() {
        mAdapter = new TagsAdapter(mContext, mTags, placeId, chosenTags);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 3);
        tagsRecyclerView.setLayoutManager(layoutManager);
        tagsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        tagsRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode: " + requestCode);
    }


    /*
     * ----------------------------- Firebase ------------------------------------------
     */

    private void setupFirebaseStaff() {
        Log.d(TAG, "setupFirebaseStaff: called.");

        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();

        // Init mAuthStateListener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                // Check if user is logged in

                if (user == null) {
                    Log.d(TAG, "onAuthStateChanged: User log-out");
                } else {
                    Log.d(TAG, "onAuthStateChanged: User is logged-in :) uid = " + firebaseAuth.getCurrentUser().getUid());
                }
            }
        };

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot rootDS : dataSnapshot.getChildren()) {
                    if (rootDS.getKey().equals(getString(R.string.db_field_tags))) {
                        // Setup all the tagsList
                        for (DataSnapshot ds : dataSnapshot.child(getString(R.string.db_field_tags)).getChildren()) {
                            String tag = ds.child("en").getValue(String.class);
                            mTags.add(tag);
                            mTagsKeeper.add(tag);
                        }
                        tagsIT.addTextChangedListener(mTextWatcher);

                        setupTagsAdapter();
                    } else if (rootDS.getKey().equals(getString(R.string.db_persons))) {
                        myUser = rootDS.child(mAuth.getCurrentUser().getUid()).getValue(User.class);
                    } else if (rootDS.getKey().equals(getString(R.string.db_users_tag_places))) {
                        if (chosenTags == null) chosenTags = new ArrayList<>();
                        for (DataSnapshot userTagPlaceDS : rootDS.child(mAuth.getCurrentUser().getUid())
                                .child(placeId).getChildren()) {
                            chosenTags.add(userTagPlaceDS.getKey());
                        }
                        setupTagsAdapter();
                    }
                }


                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: DatabaseError: " + databaseError.getMessage());
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
