package razdob.cycler;

import android.content.Context;
import android.content.Intent;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import razdob.cycler.adapters.CurrentSubjectsAdapter;
import razdob.cycler.feed.HomeActivity;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.MyFonts;

/**
 * Created by Raz on 02/07/2018, for project: PlacePicker2
 */
public class ChooseCurrentSubjectsActivity extends AppCompatActivity {
    private static final String TAG = "ChoosePlaceTagsActivity";

    final private Context mContext = ChooseCurrentSubjectsActivity.this;

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
    private Button finishBtn, clearBtn;
    private ProgressBar mProgressBar;
    private ImageView xIV;

    // Fonts
    private MyFonts mFonts;

    // Vars
    private ArrayList<String> mTags;
    private ArrayList<String> mTagsKeeper;
    private ArrayList<String> chosenSubjects;
    private CurrentSubjectsAdapter mAdapter;
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
                setupAdapter();
                if (mTags.size() == 0) {
                    Log.d(TAG, "afterTextChanged: show AddNewTagBtn");
                }
            } else {
                mTags = mTagsKeeper;
                setupAdapter();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_place_tags);
        mFonts = new MyFonts(mContext);

        chosenSubjects = getIntent().getStringArrayListExtra(getString(R.string.intent_current_subjects));
        if (chosenSubjects == null)
            chosenSubjects = new ArrayList<>();

        mTags = new ArrayList<>();
        mTagsKeeper = new ArrayList<>();
        mFirebaseMethods = new FirebaseMethods(mContext);
        setupWidgets();
        setupFirebaseStaff();

        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Current Subjects From Adapter" + mAdapter.getCurrentSubjects());
                Log.d(TAG, "onClick: Chosen Subjects From Activity" + chosenSubjects);

                if (chosenSubjects == null || chosenSubjects.size() == 0) {
                    Log.d(TAG, "onClick: not chosenSubjects.");
                    Toast.makeText(mContext, "You can to choose what are you looking for, for giving good places.", Toast.LENGTH_SHORT).show();
                }
                HomeActivity.start(mContext, chosenSubjects, getString(R.string.choose_subjects_activity));
            }
        });
    }

    private void setupWidgets() {
        placeNameTV = findViewById(R.id.place_name_tv);
        tagsRecyclerView = findViewById(R.id.tags_gv);
        tagsIT = findViewById(R.id.tags_text_input);
        finishBtn = findViewById(R.id.finish_btn);
        mProgressBar = findViewById(R.id.progress_bar);
        xIV = findViewById(R.id.x_iv);
        clearBtn = findViewById(R.id.clear_btn);

        xIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagsIT.setText("");
            }
        });
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenSubjects = new ArrayList<>();
                setupAdapter();
            }
        });

        placeNameTV.setText("CYCLER");
        placeNameTV.setTypeface(mFonts.getBoldItalicFont());

        tagsIT.setHint(mContext.getString(R.string.choose_current_subjects_hint));
    }

    /**
     * setting up the RecyclerView's adapter and make it 3-columns grid.
     */
    private void setupAdapter() {
        mAdapter = new CurrentSubjectsAdapter(mContext, mTags, chosenSubjects);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 3);
        tagsRecyclerView.setLayoutManager(layoutManager);
        tagsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        tagsRecyclerView.setAdapter(mAdapter);
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
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mTags = mFirebaseMethods.getAllTags(dataSnapshot);
                mTagsKeeper = mFirebaseMethods.getAllTags(dataSnapshot);

                tagsIT.addTextChangedListener(mTextWatcher);
                setupAdapter();
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
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
