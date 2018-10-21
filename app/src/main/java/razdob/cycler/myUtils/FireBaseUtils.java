package razdob.cycler.myUtils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

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
import java.util.HashMap;

import razdob.cycler.MainActivity;
import razdob.cycler.R;
import razdob.cycler.models.User;
import razdob.cycler.models.UserAccountSettings;
import razdob.cycler.models.UserSettings;


public class FireBaseUtils {

    // Firebase Database Staff
    // private static FirebaseDatabase mFirebaseDatabase;
    private static FirebaseAuth mFirebaseAuth;
    // private static FirebaseAuth.AuthStateListener mAuthStateListener;
    private static DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance(FirebaseApp.getInstance("mFireApp")).getReference();

    //Variables
    private static String name;
    private static String address;
    private static ArrayList<String> subjects;
    private static HashMap<String, Boolean> userPreferences;

    // Static and important variables
    public static String mUserName;
    public static String mUserAddress;
    public static FirebaseUser mUser;
    public static char mUserType = 'o'; // 'b' for Business.  'p' for private.


    // Returns all the subjects in the Firebase...
    public static ArrayList<String> getSubjectsList() {
        subjects = new ArrayList<>();
        mDatabaseReference.child("subjects").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { //something changed!
                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    String subject = locationSnapshot.getKey();
                    subjects.add(subject);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("databaseError", databaseError.getMessage());
            }
        });
        return subjects;
    }


    // Returns all the subjects and every subject's (int) types
    public static HashMap<String, ArrayList<Integer>> getSubjectsAndTypes() {
        final HashMap<String, ArrayList<Integer>> subjectsTypes_HM = new HashMap<>();
        mDatabaseReference.child("subjects").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot subject : dataSnapshot.getChildren()) {
                    ArrayList<Integer> types = new ArrayList<>();
                    for (DataSnapshot type : subject.child("types").getChildren()) {
                        types.add(type.getValue(int.class));
                    }
                    subjectsTypes_HM.put(subject.getKey(), types);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("databaseError", databaseError.getMessage());
            }
        });

        return subjectsTypes_HM;


    }

    // Returns The user's name
    public static String getUserName(String userUid) {
        String name = "Anonynous";
        DatabaseReference userDB = mDatabaseReference.child("users").child(userUid);
        userDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return name;

    }


    @Nullable
    public static String getUserName() {

        mFirebaseAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance("mFireApp"));
        if (mFirebaseAuth.getCurrentUser() == null)
            return null;

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        DatabaseReference userRef = mDatabaseReference.child("users").child(user.getUid());

        userRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            // This method is called once with the initial value and again
            // whenever data at this location is updated.
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("name").getValue(String.class);
                if (name != null)
                    Log.i("user_name", name);
            }

            // Failed to read value
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("database_error", databaseError.getMessage());
            }
        });


        return name;
    }


    // Returns the user's address
    @Nullable
    public static String getUserAddress() {
        mFirebaseAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance("mFireApp"));
        if (mFirebaseAuth.getCurrentUser() == null)
            return null;

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        DatabaseReference userRef = mDatabaseReference.child("users").child(user.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                address = dataSnapshot.child("address").getValue(String.class);
            }

            // Failed to read value
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("database_error", databaseError.getMessage());
            }
        });
        return address;
    }

    // Returns user's email address
    @Nullable
    public static String getUserEmail() {
        mFirebaseAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance("mFireApp"));
        if (mFirebaseAuth.getCurrentUser() == null)
            return null;

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        return user.getEmail();

    }

    @Nullable
    public static HashMap<String, Boolean> getUserPreferences(String uid) {

        userPreferences = new HashMap<>();
        DatabaseReference userRef = mDatabaseReference.child("users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            // This method is called once with the initial value and again
            // whenever data at this location is updated.
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot subjectSnapshot : dataSnapshot.child("preferences").getChildren()) {
                    userPreferences.put(subjectSnapshot.getKey(), subjectSnapshot.getValue(boolean.class));
                    Log.i("subject_name", subjectSnapshot.getKey());
                }

            }

            // Failed to read value
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("database_error", databaseError.getMessage());
            }
        });
        return userPreferences;
    }

    public static String getNameByUid(final String uid) {
        name = "Anonymous";
        mDatabaseReference.child("users").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name = dataSnapshot.child(uid).child("name").getValue(String.class);
                Log.i("aaaaaaaa", "AAAAAAAAAAAAAAAAA");
                if (name == null)
                    name = "Anonymous";
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("database_error", databaseError.getMessage());
            }
        });
        return name;

    }

    // Saves The information in the DB in package 'users' in user.uid() package.
    // Shows the correct Toast
    public static void saveUserInformation(Context context, String uid, User user) {
        // Set user information strings
        if (user == null) {
            Toast.makeText(context, "null pointer...", Toast.LENGTH_LONG).show();
            return;
        }
        if (user.getUserType().equals("business")) {
            mDatabaseReference.child("business_users").child(uid).push().setValue(user);
        } else if (user.getUserType().equals("person")) {
            mDatabaseReference.child("users").child(uid).push().setValue(user);
        }

        Toast.makeText(context, "Information saved !", Toast.LENGTH_LONG).show();

    }

    public static String zipName(String str) {
        // '.', '#', '$', '[', or ']'
        String string = "";
        for (char c : str.toCharArray()) {
            switch (c) {
                case '.':
                    string += "R@z!*";
                    break;
                case '#':
                    string += "R@z!+";
                    break;
                case '$':
                    string += "R@z!&";
                    break;
                case '[':
                    string += "R@z!<";
                    break;
                case ']':
                    string += "R@z!>";
                    break;
                case '/':
                    string += "R@z!1";
                    break;
                default:
                    string += c;

            }
        }
        return string;
    }

    public static String unzipName(String str) {
        // '.', '#', '$', '[', or ']'
        String string = "";
        for (int i = 0; i < str.length() - 4; i++) {
            if (str.substring(i, i + 4).equals("R@z!")) {
                switch (str.charAt(i + 4)) {
                    case '*':
                        string += '.';
                        break;
                    case '+':
                        string += '#';
                        break;
                    case '&':
                        string += '$';
                        break;
                    case '<':
                        string += '[';
                        break;
                    case '>':
                        string += ']';
                        break;
                    case '1':
                        string += '/';
                        break;
                }
                i += 4;
            } else {
                string += str.charAt(i);
            }
        }
        return string + str.substring(str.length() - 4);
    }

    public static void sendVerificationEmail(final Context context) {
        FirebaseUser user = FirebaseAuth.getInstance(FirebaseApp.getInstance("mFireApp")).getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(context.toString(), "sendEmailVerification:onComplete: successful");
                                context.startActivity(new Intent(context, MainActivity.class));
                            } else  {
                                Toast.makeText(context, "Couldn't send verification email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
     * Retrieves the accout settongs for the user currently  logged-in
     * Database: user_account_settings Node.
     * @param dataSnapshot
     * @param TAG
     * @return
     */
    private UserSettings getUserSettings(Context context, DataSnapshot dataSnapshot, String userUid, String TAG) {
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from firebase");

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User("anonymous", userUid);

        for (DataSnapshot ds: dataSnapshot.getChildren()) {

            // user_account_settings Node
            if (ds.getKey().equals(context.getString(R.string.db_user_account_settings))) {
                Log.d(TAG, "getUserAccountSettings: dataSnapshot: " + ds);
                settings = ds.child(userUid).getValue(UserAccountSettings.class);
                Log.d(TAG, "getUserAccountSettings: Retrieve user_account_settings information" + settings.toString());
            }

            else if (ds.getKey().equals(context.getString(R.string.db_persons))) {
                Log.d(TAG, "getUserAccountSettings: dataSnapshot: "+ds);
                user = ds.child(userUid).getValue(User.class);
                Log.d(TAG, "getUserAccountSettings: Retrieve user information" + user.toString());
            }

        }

        return new UserSettings(user, settings);

    }

}



