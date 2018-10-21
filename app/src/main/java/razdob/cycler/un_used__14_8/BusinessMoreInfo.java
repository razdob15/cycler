package razdob.cycler.un_used__14_8;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import razdob.cycler.MainRegisterActivity;
import razdob.cycler.R;
import razdob.cycler.adapters.SubjectAdapter;

public class BusinessMoreInfo extends AppCompatActivity {

    // Firebase Staff
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mFireUser;
    private DatabaseReference mRef;
    private ValueEventListener mValueListener;

    private ListView tagsLV;
    private HashMap<Integer, String> subjectsHM;
    private HashMap<String, Boolean> tagsHM;
    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
        mRef.addListenerForSingleValueEvent(mValueListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthListener != null) mAuth.removeAuthStateListener(mAuthListener);
        if (mValueListener != null) mRef.removeEventListener(mValueListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_more_info);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        subjectsHM = new HashMap<>();
        tagsHM = new HashMap<>();
        tagsLV = findViewById(R.id.tags_lv);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    mAuth.removeAuthStateListener(mAuthListener);
                    startActivity(new Intent(BusinessMoreInfo.this, MainRegisterActivity.class));
                }
            }
        };
        // Check if user is authentication
        if (mAuth.getCurrentUser() != null) {
            mFireUser = mAuth.getCurrentUser();
        } else {Log.w(getLocalClassName(), "The user is null !!");}

        mRef = FirebaseDatabase.getInstance().getReference();
        mValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot subDS: dataSnapshot.child("subjects").getChildren()) {
                    subjectsHM.put(i, subDS.getKey());
                    i++;
                }
                if (dataSnapshot.child("business_users").hasChild(mFireUser.getUid())) {
                    DataSnapshot businessDS = dataSnapshot.child("business_users").child(mFireUser.getUid());
                    if (businessDS.hasChild("tags")) {
                        for (DataSnapshot tagDS: businessDS.child("tags").getChildren()) {
                            tagsHM.put(tagDS.getKey(), tagDS.getValue(boolean.class));
                        }
                    }

                }
                progressDialog.dismiss();
                SubjectAdapter tagAdapter = new SubjectAdapter(getApplicationContext(), 0, subjectsHM ,mFireUser, tagsHM, false);
                tagsLV.setAdapter(tagAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("DatabaseError", BusinessMoreInfo.this.toString() + "error = " + databaseError.getMessage());
            }
        };



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, BusinessProfileActivity.class);
        startActivity(intent);

    }

}
