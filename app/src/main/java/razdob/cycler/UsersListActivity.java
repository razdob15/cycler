package razdob.cycler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import razdob.cycler.instProfile.InstProfileActivity;
import razdob.cycler.instSearch.SearchUserActivity;
import razdob.cycler.models.User;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.MyFonts;
import razdob.cycler.myUtils.UsersListAdapter;

/**
 * Created by Raz on 09/08/2018, for project: PlacePicker2
 */
public class UsersListActivity extends AppCompatActivity {
    private static final String TAG = "UsersListActivity";
    private final Context mContext = UsersListActivity.this;

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods mFireMethods;
    /* ---------------------- FIREBASE ----------------------- */


    // GUI
    private TextView titleTV;
    private ListView userLV;

    // vars
    private ArrayList<User> users;
    private UsersListAdapter mAdapter;
    private Intent mIntent;
    private MyFonts mFonts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        titleTV = findViewById(R.id.title);
        userLV = findViewById(R.id.users_lv);

        mFonts = new MyFonts(mContext);
        titleTV.setTypeface(mFonts.getBoldFont());

        // Set title Text from intent.
        mIntent = getIntent();
        titleTV.setText(mIntent.getStringExtra(mContext.getString(R.string.intent_title)));

        users = new ArrayList<>();

        mFireMethods = new FirebaseMethods(mContext);
        setupFirebaseStaff();
        setupUsers();

    }

    /**
     * Gets the usersIds from the Intent.
     * Gets the matching UserObjects from the DB.
     * Calling to 'setupUsersListView()' method.
     */
    private void setupUsers() {
        final ArrayList<String> usersIds = mIntent.getStringArrayListExtra(mContext.getString(R.string.intent_users));
        if (usersIds != null && usersIds.size() > 0) {
            mRef.child(mContext.getString(R.string.db_persons)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (String uid : usersIds) {
                        users.add(dataSnapshot.child(uid).getValue(User.class));
                    }
                    setupUsersListView();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: DBError: " + databaseError.getMessage());
                }
            });
        }
    }

    /**
     * Initializes the mAdapter.
     * Puts mAdapter as the usersLV's adapter.
     * Add listener to itemClick -> sending to 'InstProfileActivity' with the extras like SearchActivity sends.
     */
    private void setupUsersListView() {
        mAdapter = new UsersListAdapter(mContext, R.layout.layout_user_list_item, users);
        userLV.setAdapter(mAdapter);

        userLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: Selected user: " + users.get(position).toString());

                // Navigate to selected-user's profile activity
                Intent intent = new Intent(mContext, InstProfileActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.search_activity));
                intent.putExtra(getString(R.string.intent_user), users.get(position));
                startActivity(intent);

            }
        });
    }



    /*
     * ------------------------- Firebase ------------------------------------------
     */

    /**
     * Checks to see if the @param 'user' is logged in
     * @param fireUser
     */
    private void checkCurrentUser(FirebaseUser fireUser) {
        Log.d(TAG, "checkCurrentUser: checking if the user is logged in");
        if (fireUser == null) {
            startActivity(new Intent(mContext, MainRegisterActivity.class));
        }
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

    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        checkCurrentUser(mAuth.getCurrentUser());

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAuthListener != null)
            mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}

