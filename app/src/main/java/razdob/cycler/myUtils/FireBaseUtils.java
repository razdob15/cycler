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
import razdob.cycler.MainRegisterActivity;
import razdob.cycler.R;
import razdob.cycler.models.User;
import razdob.cycler.models.UserAccountSettings;
import razdob.cycler.models.UserSettings;


public class FireBaseUtils {
    public static String zipName(String str) {
        // '.', '#', '$', '[', or ']'
        StringBuilder string = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '.':
                    string.append("R@z!*");
                    break;
                case '#':
                    string.append("R@z!+");
                    break;
                case '$':
                    string.append("R@z!&");
                    break;
                case '[':
                    string.append("R@z!<");
                    break;
                case ']':
                    string.append("R@z!>");
                    break;
                case '/':
                    string.append("R@z!1");
                    break;
                default:
                    string.append(c);

            }
        }
        return string.toString();
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



    /**
     * this function need to be called in firebaseAuthListener.
     * @param context - app context.
     * @param auth - Firebase Auth - the variable in the onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) function.
     * @param TAG - Activity's TAG (For logs).
     * @param authStateListener - to remove it from auth if needed.
     * @return whether the user is signing in or not.
     */
    public static boolean defaultFireAuthListener(Context context, FirebaseAuth auth, String TAG, FirebaseAuth.AuthStateListener authStateListener) {
        Log.d(TAG, "defaultFireAuthListener: called.");

        FirebaseUser user = auth.getCurrentUser();
        // Check if user is logged in

        if (user == null) {
            Log.d(TAG, "onAuthStateChanged: User log-out");
            auth.removeAuthStateListener(authStateListener);
            context.startActivity(new Intent(context, MainRegisterActivity.class));
            return false;
        } else {
            Log.d(TAG, "onAuthStateChanged: User is logged-in :) uid = " + user.getUid());
            return true;
        }

    }

    /**
     * Warning Log if we have DatabaseError in Firebase.
     * @param TAG - for log.
     * @param databaseError - The Error.
     */
    public static void dbErrorMessage(String TAG, DatabaseError databaseError) {
        Log.w(TAG, "dbErrorMessage: DBError: " + databaseError.getMessage());
    }


}



