package razdob.cycler.un_used__14_8;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import razdob.cycler.LoginActivity;
import razdob.cycler.R;
import razdob.cycler.adapters.SubjectAdapter;
import razdob.cycler.feed.DataActivity;
import razdob.cycler.myUtils.RazUtils;


public class SubjectsActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "SubjectsActivity";

    // Firebase Database Staff
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mUser;
    private DatabaseReference mRef;
    private ValueEventListener mValueListener;

    // Views
    private TextView mDefineTV;
    private ListView listView;
    private Button mFinishBtn;

    // ListView variable
    private ArrayList<String> subjectList;
    private HashMap<String, ArrayList<Integer>> subjectsAndTypesHM;

    private boolean openSubjectsAdapter = true;

    public static HashMap<String, Boolean> prefHashMap;
    // Need to LOAD dataActivity
    // TODO (!): USE the LOAD to know if reload the data in DataActivity.
    // If there is changes in the preferences....
    public static boolean LOAD = false;


    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: Starts...");

        mAuth.addAuthStateListener(mAuthListener);
        mRef.addValueEventListener(mValueListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: Pause...");
        if (mAuthListener != null)
            mAuth.removeAuthStateListener(mAuthListener);
        if (mValueListener != null)
            mRef.removeEventListener(mValueListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjets);

        Log.i(TAG, "onCreate: Creates... ");

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            finish();
            Toast.makeText(this, "Register or login, please", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
        }else  {mUser = mAuth.getCurrentUser();}

        subjectList = new ArrayList<>();
        mRef = FirebaseDatabase.getInstance().getReference();
        mValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: Read subjects from firebase");
                @SuppressLint("UseSparseArrays") HashMap<Integer, String> subjectsHashMap = new HashMap<>();
                int counter = 0;
                for (DataSnapshot subDS: dataSnapshot.child("subjects").getChildren()) {
                    Log.i(TAG, "onDataChange: Subject #"+counter+subDS.getKey());
                    subjectList.add(subDS.getKey());
                    subjectsHashMap.put(counter, subDS.getKey());
                    counter++;
                }
                prefHashMap = new HashMap<>();
                if (dataSnapshot.child(getString(R.string.db_persons)).child(mAuth.getCurrentUser().getUid()).hasChild("preferences")) {
                    for (DataSnapshot prefDS : dataSnapshot.child("persons").child(mAuth.getCurrentUser().getUid()).child("preferences").getChildren()) {
                        if (prefDS.getValue(Boolean.class)) {
                            prefHashMap.put(prefDS.getKey(), prefDS.getValue(Boolean.class));
                        }
                    }
                }
                if (openSubjectsAdapter) {
                    Log.i(TAG, "onDataChange: Creates SubjectAdapter. length = " + subjectList.size());
                    SubjectAdapter subjectAdapter = new SubjectAdapter(getApplicationContext(), 0, subjectsHashMap, mUser, prefHashMap, true);
                    listView.setAdapter(subjectAdapter);
                    openSubjectsAdapter = false;
                }
                subjectsAndTypesHM = RazUtils.getSubjectsAndTypes(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("databaseError"+getLocalClassName(), ""+databaseError.getMessage());
            }
        };

        // Match views to IDs
        mDefineTV = findViewById(R.id.define_tv);
        listView = findViewById(R.id.subjects_list_v);
        mFinishBtn = findViewById(R.id.finish_btn);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    mAuth.signOut();
                }
            }
        };

        // Finish button OnClick...
        mFinishBtn.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, PersonProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        // If finishBtn -> Go to DataActivity (from google Places)
        if (view == mFinishBtn) {
            Log.d(TAG, "onClick: finishBtn Click");
            Intent intent = new Intent(this, DataActivity.class);
            startActivity(intent);
        }
    }

    // Gets an ArrayList contains strings.
    // Returns an HashMap<Integer, String> contains the strings according to the arrayList
    //  and serial numbers
    @SuppressLint("UseSparseArrays")
    public static HashMap<Integer, String> fromStringArrayListToHashMap(ArrayList<String> stringArrayList) {
        HashMap<Integer, String> integerStringHashMap = new HashMap<>();
        int i = 0;
        for (String string: stringArrayList) {
            integerStringHashMap.put(i, string);
            i++;
        }
        return integerStringHashMap;
    }

    // Menu options.. Sign out
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

