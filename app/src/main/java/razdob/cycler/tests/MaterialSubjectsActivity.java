package razdob.cycler.tests;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import razdob.cycler.MainRegisterActivity;
import razdob.cycler.R;

/**
 * Created by Raz on 18/03/2018, for project: PlacePicker2
 */

public class MaterialSubjectsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MaterialSubjectsActivit";

    //Firebase Staff
    private DatabaseReference mPrefRef;
    private ValueEventListener mValueListener;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mFireUser;

    HashMap<CardView, String> stringCardViewHashMap;

    //Subjects - prevents doubles...
    ArrayList<String> subjects;

    //User Preferences
    HashMap<String, Boolean> userPrefs;
    HashMap<String, ArrayList<String>> subsHasMap;

    //GUI
    GridLayout gridLayout;

    @Override
    protected void onStart() {
        super.onStart();
        mPrefRef.addListenerForSingleValueEvent(mValueListener);
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthListener != null) mAuth.removeAuthStateListener(mAuthListener);
        if (mValueListener != null) mPrefRef.removeEventListener(mValueListener);
    }

    @Override
    public void onClick(View v) {
        // Card Click
        if (v.getClass().equals(CardView.class)) {
            CardView cv = (CardView) v;
            if (stringCardViewHashMap.containsKey(cv)) {
                String key = stringCardViewHashMap.get(cv);
                int index = subjects.indexOf(key);
                if (userPrefs.containsKey(key) && userPrefs.get(key)) {
                    userPrefs.put(key, false);
                    mPrefRef.child("persons").child(mFireUser.getUid()).child("preferences").child(key).setValue(false);
                    cv.setCardBackgroundColor(getResources().getColor(R.color.colorWhite));
                } else {
                    userPrefs.put(key, true);
                    mPrefRef.child("persons").child(mFireUser.getUid()).child("preferences").child(key).setValue(true);
                    cv.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));

                    if (subsHasMap.containsKey(key)) {
                        for (String subsub : subsHasMap.get(key)) {
                            if (!subjects.contains(subsub)) {

                                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                View row = inflater.inflate(R.layout.activity_subject_custom, null, true);

                                CardView cardView = row.findViewById(R.id.card_view);
                                TextView subNameTV = cardView.findViewById(R.id.subject_name);
                                subNameTV.setText(subsub);

                                if (cardView.getParent() != null)
                                    ((ViewGroup) cardView.getParent()).removeView(cardView);
                                subjects.add(index, subsub);
                                index++;
                                if (userPrefs.containsKey(subsub) && userPrefs.get(subsub))
                                    cardView.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                stringCardViewHashMap.put(cardView, subsub);
                                gridLayout.addView(cardView, index);

                                cardView.setOnClickListener(MaterialSubjectsActivity.this);
                            }
                        }
                    }
                }

            }
        }
    }

    // TODO (!!): Add sub-Subjects. They will be opened when the user clicks on the MainSubject...

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subjects_new_design);

        Log.d(TAG, "onCreate: Created.");

        gridLayout = findViewById(R.id.subjects_list_gl);
        subjects = new ArrayList<>();
        userPrefs = new HashMap<>();
        stringCardViewHashMap = new HashMap<>();
        subsHasMap = new HashMap<>();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    mAuth.removeAuthStateListener(mAuthListener);
                    startActivity(new Intent(MaterialSubjectsActivity.this, MainRegisterActivity.class));
                }
            }
        };
        if (mAuth.getCurrentUser() != null) {
            mFireUser = mAuth.getCurrentUser();
        }

        mPrefRef = FirebaseDatabase.getInstance().getReference();

        mValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("persons").child(mFireUser.getUid()).hasChild("preferences")) {
                    for (DataSnapshot prefDS : dataSnapshot.child("persons").child(mFireUser.getUid()).child("preferences").getChildren()) {
                        userPrefs.put(prefDS.getKey(), prefDS.getValue(Boolean.class));
                    }
                }
                for (DataSnapshot subDS : dataSnapshot.child("subjects").getChildren()) {
                    if (!subjects.contains(subDS.getKey())) {
                        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View row = inflater.inflate(R.layout.activity_subject_custom, null, true);

                        CardView cardView = row.findViewById(R.id.card_view);
                        TextView subNameTV = cardView.findViewById(R.id.subject_name);
                        subNameTV.setText(subDS.getKey());

                        if (cardView.getParent() != null)
                            ((ViewGroup) cardView.getParent()).removeView(cardView);
                        subjects.add(subDS.getKey());

                        if (userPrefs.containsKey(subDS.getKey()) && userPrefs.get(subDS.getKey()))
                            cardView.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        stringCardViewHashMap.put(cardView, subDS.getKey());
                        gridLayout.addView(cardView);
                        cardView.setOnClickListener(MaterialSubjectsActivity.this);
                    }
                    if (subDS.hasChild("subs")) {
                        ArrayList<String> subsArrayList = new ArrayList<>();
                        for (DataSnapshot subsubDS : subDS.child("subs").getChildren()) {
                            subsArrayList.add(subsubDS.getKey());
                        }
                        subsHasMap.put(subDS.getKey(), subsArrayList);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("databaseError" + getLocalClassName(), "" + databaseError.getMessage());
            }
        };

    }
}
