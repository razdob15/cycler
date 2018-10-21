package razdob.cycler;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.fabric.sdk.android.Fabric;

public class MainRegisterActivity extends AppCompatActivity {
    private static final String TAG = "MainRegisterActivity";

    private static final int RC_SIGN_IN = 59;
    private SignInButton googleBtn;
    private LinearLayout emailReg;

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseAuth mAuth;
    private FirebaseApp mFireApp;
    private FirebaseOptions mFireOptions;
    /* ---------------------- FIREBASE ----------------------- */

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth.AuthStateListener mAuthListener;

    // Progress GUI
    private ProgressBar progressBar;
    private TextView progressTV;


    // TODO(!): Debug here and check the problem when the user signs-out!!

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuthListener != null)
            mAuth.addAuthStateListener(mAuthListener);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_test_register2);

        initFirebaseStaff();

//        Log.d(TAG, "onCreate: sign out !");
//        AuthUI.getInstance(mFireApp)
//                .signOut(this)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    public void onComplete(@NonNull Task<Void> task) {
//                        Log.d(TAG, "onComplete: sign out !");
//                    }
//                });

        Log.i(TAG, "onCreate: created.");

        matchWidgetsToIDs();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Log.d(TAG, "onAuthStateChanged: user is in !");
                    startActivity(new Intent(MainRegisterActivity.this, MainActivity.class));

                    // TODO(1): Use the FireAuth's display-name !!!
                    // TODO(2): Design login theme (in the styles.xml file).
                    // TODO(3): Make sure FireAuth works well -> Upload :)  https://play.google.com/apps/publish/?account=6828651447828228443#AppDashboardPlace:p=razdob.cycler&appid=4975306597084454439

                    initFirebaseStaff();
                } else {
                    Log.d(TAG, "onAuthStateChanged: user is out. need to log-in");
                }
            }
        };


        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainRegisterActivity.this, "Google register is not working yet. please sign up with Email & Password", Toast.LENGTH_LONG).show();
//                signIn();
            }
        });

        emailReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(MainRegisterActivity.this, RegisterActivity.class));
                my_sign_in_method();
            }
        });




        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
//                    @Override
//                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//                        Toast.makeText(MainRegisterActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .build();

//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
//                    @Override
//                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//                        Toast.makeText(MainRegisterActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .build();

    }

    private void matchWidgetsToIDs() {
        googleBtn = findViewById(R.id.google_btn);
        googleBtn.setVisibility(View.GONE); // Temporary. TODO(BETA):
        emailReg = findViewById(R.id.email_register);
        progressBar = findViewById(R.id.main_register_pb);
        progressTV = findViewById(R.id.main_register_tv);
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential: Success" );
                        } else {
                            Log.w(TAG, "signInWithCredential: Failure");
                            Toast.makeText(MainRegisterActivity.this, "Authentication failed !", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void initFirebaseStaff() {
        if (mFireApp != null) return;
        mFireOptions = new FirebaseOptions.Builder()
                .setApplicationId(getString(R.string.firebase_application_id)) // Required for Analytics.
                .setApiKey(getString(R.string.firebase_api_key)) // Required for Auth.
                .setDatabaseUrl("https://cyclerproject.firebaseio.com") // Required for RTDB.
                .setStorageBucket("https://console.firebase.google.com/u/0/project/cyclerproject/storage/cyclerproject.appspot.com/files") // Requires for Storage.
                .setGcmSenderId("911064108385")
                .build();

        try {
            // Initialize with mFireApp app.
            FirebaseApp.initializeApp(this /* Context */, mFireOptions, "mFireApp");
        } catch (java.lang.IllegalStateException e) {
            Log.w(TAG, "initFirebaseStaff: " + e.getMessage());
        }

        // Retrieve mFireApp app.
        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
    }





    /* ----------------------------------------------------------------------------------------------------------------------------------*/
    /* ------------------------------------------------------- Firebase Email&Password Sign-in ------------------------------------------*/
    /* ----------------------------------------------------------------------------------------------------------------------------------*/

    private void my_sign_in_method() {

        //I rather using a list, this way handling providers is separeted from the login methods
//        List<AuthUI.IdpConfig> providers = Arrays.asList(
//                new AuthUI.IdpConfig.EmailBuilder().build(),
//                new AuthUI.IdpConfig.GoogleBuilder().build(),
//                new AuthUI.IdpConfig.FacebookBuilder().build()
//        );

        List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.EmailBuilder().build());
        startActivityForResult(
                AuthUI.getInstance(mFireApp)
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(providers)
                        .setTheme(R.style.LoginTheme)
                        .setLogo(R.mipmap.cycler_logo)
                        .build(),
                RC_SIGN_IN);



//
//
//
//
//
//        // Choose authentication providers
//
//        List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.EmailBuilder().build());
//
//        // Create and launch sign-in intent
//        startActivityForResult(
//                AuthUI.getInstance(mFireApp)
//                        .createSignInIntentBuilder()
//                        .setAvailableProviders(providers)
//                        .setLogo(R.drawable.cycler_logo)
//                        .build(),
//                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String uid = user.getUid();
                    Log.d(TAG, "onActivityResult: register is good! uid: " + uid);
                } else {
                    Log.w(TAG, "onActivityResult: user is null !");
                }
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...

                if (response != null) {
                    Log.d(TAG, "onActivityResult: erro: " + Objects.requireNonNull(response.getError()).getMessage());
                    Log.d(TAG, "onActivityResult: erro1: " + response.getError().getErrorCode());
                } else {
                    Log.d(TAG, "onActivityResult: response is null !");
                }
            }
        }
    }

    /* ----------------------------------------------------------------------------------------------------------------------------------*/
    /* ------------------------------------------------------- Firebase Email&Password Sign-in ------------------------------------------*/
    /* ----------------------------------------------------------------------------------------------------------------------------------*/



}

