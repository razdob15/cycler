package razdob.cycler.un_used__14_8;

import android.content.Context;
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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import razdob.cycler.R;
import razdob.cycler.adapters.CurrentSubjectsAdapter;
import razdob.cycler.models.User;
import razdob.cycler.myUtils.FirebaseMethods;

/**
 * Created by Raz on 02/07/2018, for project: PlacePicker2
 */
public class MomentSubjectsActivity extends AppCompatActivity {
    private static final String TAG = "MomentSubjectsActivity";

    Context mContext = MomentSubjectsActivity.this;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mRef;

    // Widgets
    private TextView placeNameTV;
    private RecyclerView tagsRecyclerView;
    private TextInputEditText subjectsIT;
    private Button finishBtn, addNewTagBtn;

    // Vars
    private ArrayList<String> mSubjects;
    private ArrayList<String> mSubsKeeper;
    private CurrentSubjectsAdapter mAdapter;
    private FirebaseMethods mFirebaseMethods;
    private User myUser;
    public ArrayList<String> chosenSubs;
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d(TAG, "afterTextChanged: input Changed: " + s);

        }
    };



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_place_tags);
        Log.d(TAG, "onCreate: Created.");

        mSubjects = new ArrayList<>();
        mSubsKeeper = new ArrayList<>();
        chosenSubs = new ArrayList<>();
        mFirebaseMethods = new FirebaseMethods(mContext);
        setupWidgets();
        setupFirebaseStaff();

        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: chosen_subjects: " + chosenSubs);

                // TODO(!!!) Add the algorithm to the nextActivity !
            }
        });

        addNewTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: AddNewTag_click !");
            }
        });

    }

    private void setupWidgets() {
        placeNameTV = findViewById(R.id.place_name_tv);
        tagsRecyclerView = findViewById(R.id.tags_gv);
        subjectsIT = findViewById(R.id.tags_text_input);
        finishBtn = findViewById(R.id.finish_btn);
        addNewTagBtn = findViewById(R.id.add_new_tag_btn);

        subjectsIT.setHint(mContext.getString(R.string.choose_current_subjects_hint));
    }

    /**
     * setting up the RecyclerView's adapter and make it 3-columns grid.
     */
    private void setupAdapter() {
        mAdapter = new CurrentSubjectsAdapter(mContext, mSubjects, null);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 3);
        tagsRecyclerView.setLayoutManager(layoutManager);
        tagsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        tagsRecyclerView.setAdapter(mAdapter);
    }


    /*
     * ----------------------------- Firebase ------------------------------------------
     */

    private void setupFirebaseStaff() {
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();

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
                for (DataSnapshot mainDS : dataSnapshot.getChildren()) {
                    if (mainDS.getKey().equals(getString(R.string.db_field_moment_subjects))) {
                        Log.d(TAG, "onDataChange: found the subjects");
                        for (DataSnapshot ds : mainDS.getChildren()) {
                            String subject = ds.getValue(String.class);

                            Log.d(TAG, "onDataChange: subject: "+subject);
                            mSubjects.add(subject);
                            mSubsKeeper.add(subject);
                        }
                        subjectsIT.addTextChangedListener(mTextWatcher);
                        setupAdapter();
                    } else if (mainDS.getKey().equals(getString(R.string.db_persons))) {
                        myUser = mainDS.child(mAuth.getCurrentUser().getUid()).getValue(User.class);
                    }
                }

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













/*
*
*
*
* java.lang.RuntimeException: Unable to start activity ComponentInfo{razdob.cycler/razdob.cycler.instProfile.AccountSettingsActivity}: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.lang.Integer.intValue()' on a null object reference
	at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2721)
	at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2782)
	at android.app.ActivityThread.-wrap12(ActivityThread.java)
	at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1521)
	at android.os.Handler.dispatchMessage(Handler.java:102)
	at android.os.Looper.loop(Looper.java:163)
	at android.app.ActivityThread.main(ActivityThread.java:6228)
	at java.lang.reflect.Method.invoke(Native Method)
	at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:904)
	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:794)
Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.lang.Integer.intValue()' on a null object reference
	at razdob.cycler.instProfile.AccountSettingsActivity.getIncomingIntent(AccountSettingsActivity.java:107)
	at razdob.cycler.instProfile.AccountSettingsActivity.onCreate(AccountSettingsActivity.java:64)
	at android.app.Activity.performCreate(Activity.java:6871)
	at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1119)
	at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2674)
	... 9 more
*
*
*
*
**/