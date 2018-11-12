package razdob.cycler.instProfile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import razdob.cycler.MainRegisterActivity;
import razdob.cycler.R;

/**
 * Created by Raz on 31/05/2018, for project: PlacePicker2
 */
public class SignOutFragment extends Fragment {
    private static final String TAG = "SignOutFragment";
    private Context mContext;

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    /* ---------------------- FIREBASE ----------------------- */

    // GUI
    private ProgressBar mProgressBar;
    private TextView confirmSignoutTV, signOutMessage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: SignOut Fragment created");
        View view = inflater.inflate(R.layout.fragment_signout, container, false);
        mContext = getContext();
        confirmSignoutTV = view.findViewById(R.id.confirm_sign_out_tv);
        mProgressBar = view.findViewById(R.id.sign_out_progressbar);
        signOutMessage = view.findViewById(R.id.sign_out_message);
        Button signOutBtn = view.findViewById(R.id.sign_out_btn);

        setupFirebaseAuth();

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to sign out.");

                Log.d(TAG, "onCreate: sign out !");
                AuthUI.getInstance(mFireApp)
                        .signOut(mContext)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d(TAG, "onComplete: sign out !");
                                startActivity(new Intent(mContext, MainRegisterActivity.class));
                            }
                        });





//                mProgressBar.setVisibility(View.VISIBLE);
//                signOutMessage.setVisibility(View.VISIBLE);
//                Intent intent = new Intent(getActivity(), LoginActivity.class);
//                mAuth.signOut();
//                Objects.requireNonNull(getActivity()).finish(); // TODO(!) Change this line
//                startActivity(intent);
            }
        });

        return view;
    }

    /*
     * ------------------------- Firebase ------------------------------------------
     */

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: signing up firebaseAuth");

        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);

        // Init mAuthStateListener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                // Check if user is logged in

                if (user == null) {
                    Log.d(TAG, "onAuthStateChanged: User log-out");

                    Log.d(TAG, "onAuthStateChanged: navigating back to login screen.");
                    Intent intent = new Intent(getActivity(), MainRegisterActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "onAuthStateChanged: User is logged-in :) uid = " + firebaseAuth.getCurrentUser().getUid());
                }
            }
        };
        // Check if user is authentication
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
