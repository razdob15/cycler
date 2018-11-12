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
import razdob.cycler.models.User;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.MyFonts;
import razdob.cycler.adapters.UsersListAdapter;

/**
 * Created by Raz on 09/08/2018, for project: PlacePicker2
 */
public class UsersListActivity extends AppCompatActivity {
    private static final String TAG = "UsersListActivity";
    private final Context mContext = UsersListActivity.this;

    // Intent Extras
    private static final String TITLE_EXTRA = "title";
    private static final String ACTIVITY_NUM_EXTRA = "activity_num";
    private static final String USERS_EXTRAS = "users_list";

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
    private ArrayList<String> usersIds;
    private ArrayList<User> users;
    private UsersListAdapter mAdapter;
    private MyFonts mFonts;
    private int activityNum = 1;
    private String title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        mFireMethods = new FirebaseMethods(mContext);

        titleTV = findViewById(R.id.title);
        userLV = findViewById(R.id.users_lv);

        mFonts = new MyFonts(mContext);
        titleTV.setTypeface(mFonts.getBoldFont());

        // Get Intent !
        getDataFromIntent();
        // Setup Firebase
        setupFirebaseStaff();

        if (title != null) titleTV.setText(title);
        if (usersIds != null) setupUsers(usersIds);


    }


    /**
     * Init the variables: title, usersIds, activityNum;
     */
    private void getDataFromIntent() {
        Intent intent = getIntent();
        title = intent.getStringExtra(TITLE_EXTRA);
        usersIds = intent.getStringArrayListExtra(USERS_EXTRAS);
        int intentActivityNum = intent.getIntExtra(ACTIVITY_NUM_EXTRA, -1);
        if (intentActivityNum >= 0) activityNum = intentActivityNum;
    }

    public static void start(@NonNull Context context, String title, ArrayList<String> usersIds, int activityNum) {
        Intent intent = new Intent(context, UsersListActivity.class);
        intent.putExtra(TITLE_EXTRA, title);
        intent.putExtra(USERS_EXTRAS, usersIds);
        intent.putExtra(ACTIVITY_NUM_EXTRA, activityNum);
        context.startActivity(intent);
    }

    /**
     * Gets the usersIds from the Intent.
     * Gets the matching UserObjects from the DB.
     * Calling to 'setupUsersListView()' method.
     */
    private void setupUsers(final ArrayList<String> usersIds) {
        users = new ArrayList<>();
        if (usersIds != null && usersIds.size() > 0) {
            mRef.child(mContext.getString(R.string.db_persons)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (String uid : usersIds) {
                        users.add(dataSnapshot.child(uid).getValue(User.class));
                    }
                    setupUsersListView();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
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

                InstProfileActivity.start(mContext, mContext.getString(R.string.search_activity), users.get(position), activityNum);
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

