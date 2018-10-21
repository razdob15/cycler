package razdob.cycler;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import razdob.cycler.models.User;
import razdob.cycler.models.UserAccountSettings;
import razdob.cycler.myUtils.FirebaseInserts;
import razdob.cycler.myUtils.MyFonts;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "RegisterActivity";
    private static final int RC_SIGN_IN = 12;
    private final Context mContext = RegisterActivity.this;

    // TODO (BETA-User accessibility) Make the register/login more comfortable for the user !

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    /* ---------------------- FIREBASE ----------------------- */

    // Progress Dialog
    private ProgressDialog mProgressDialog;

    // Views
    private AutoCompleteTextView mEmailET, mPasswordET, mPasswordAgainET;
    private Button registerBtn;
    private TextView mLoginTV;

    // Vars
    private MyFonts mFonts;

    // Errors in registering
    public static final String NOT_VALID_EMAIL = "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The email address is badly formatted.";
    public static final String EMAIL_USES_BY_ANOTHER_USER = "com.google.firebase.auth.FirebaseAuthUserCollisionException: The email address is already in use by another account.";
    public static final String NOT_VALID_PASSWORD = "com.google.firebase.auth.FirebaseAuthWeakPasswordException: The given password is invalid. [ Password should be at least 6 characters ]";

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Log.i(TAG, "onCreate: Start");
        mFonts = new MyFonts(mContext);
        initFirebaseStaff();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // Shouldn't get in it
                    Log.d(TAG, "onAuthStateChanged: signed-in: " + user.getUid());
//                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            User myUser = new User(user.getDisplayName(), user.getUid());
//                            myUser.setEmail(user.getEmail());
//                            FirebaseInserts.savePersonInfo(user.getUid(), myUser, new UserAccountSettings(
//                                    "",
//                                    user.getDisplayName(),
//                                    user.getDisplayName(),
//                                    "",
//                                    0, 0, 0, user.getUid()
//                            ));
//
//                            Toast.makeText(RegisterActivity.this, "Signed up successful! Sending verification email...", Toast.LENGTH_SHORT).show();
//                            mAuth.signOut();
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//                            Log.w(TAG, "onCancelled: DBError: " + databaseError.getMessage());
//                        }
//                    });
                }

            }
        };

//         Create ProgressDialog
//        mProgressDialog = new ProgressDialog(this);
//
        setupWidgets();
//
//         Click listeners
        mLoginTV.setOnClickListener(this);
        registerBtn.setOnClickListener(this);


    }

    private void setupWidgets() {
        // Views - Match to the IDs
        mEmailET = findViewById(R.id.user_email_actv);
        mPasswordET = findViewById(R.id.user_password_actv);
        mPasswordAgainET = findViewById(R.id.password_again_actv);
        mLoginTV = findViewById(R.id.login_tv);
        registerBtn = findViewById(R.id.register_btn);
        TextView titleTV = findViewById(R.id.title_tv);
        titleTV.setTypeface(mFonts.getBoldItalicFont());
    }


    // Registers The mFireUser OR shows an error toast
    // After register starts the PersonProfileActivity
    private void registerUser() {
        // Email and twice password Strings
        String email = mEmailET.getText().toString();
        String password = mPasswordET.getText().toString();
        String confirmPassword = mPasswordAgainET.getText().toString();

        if (email.isEmpty()) { //Should to fill email
            Toast.makeText(this, "Fill The Mail", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {  //Should to fill password 1
            Toast.makeText(this, "Fill the Password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {  //Should to fill password 2
            Toast.makeText(this, "The passwords don't match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Details seem valid --> Registering

        //Shows ProgressDialog
        mProgressDialog.setMessage("Register User...");
        mProgressDialog.show();

        // Registers the mFireUser by email and password

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {  // Register successfully

//                            FireBaseUtils.sendVerificationEmail(RegisterActivity.this);
//                            mAuth.signOut();

                            // Start Profile Activity
//                            startActivity(new Intent(getApplicationContext(), MainActivity.class));

                        } else {  // Task weren't successfully

                            // Identify the exception
                            String taskException = task.getException().toString();

                            // Show a matching toast
                            switch (taskException) {
                                case NOT_VALID_EMAIL:
                                    Toast.makeText(RegisterActivity.this, "Not Valid Email", Toast.LENGTH_SHORT).show();
                                    break;
                                case EMAIL_USES_BY_ANOTHER_USER:
                                    Toast.makeText(RegisterActivity.this,
                                            "This email is already used. Try to Login by click on the button below", Toast.LENGTH_LONG).show();
                                    break;
                                case NOT_VALID_PASSWORD:
                                    Toast.makeText(RegisterActivity.this,
                                            "The password is invalid. (Should be at least 6 characters)", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(RegisterActivity.this, "An error occur... Try again", Toast.LENGTH_SHORT).show();
                                    Log.i("register_error", task.getException().getMessage());
                            }
                        }
                        mProgressDialog.dismiss();
                    }
                });
    }


    //on click to:  mRegisterBtn OR mLoginTV
    @Override
    public void onClick(View view) {
        if (view == registerBtn) {
            // TODO(!): Check this...
//            registerUser();
            my_sign_in_method();
        }

        if (view == mLoginTV) {  // The mFireUser has an account and he wants to sign in
            // Log in Activity !
            startActivity(new Intent(this, LoginActivity.class));
        }

        // TODO(BETA): Add Facebook register


    }

    private void initFirebaseStaff() {
        Log.d(TAG, "initFirebaseStaff: called.");

        // Retrieve mFireApp app.
        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();
    }

    /* ----------------------------------------------------------------------------------------------------------------------------------*/
    /* ------------------------------------------------------- Firebase Email&Password Sign-in ------------------------------------------*/
    /* ----------------------------------------------------------------------------------------------------------------------------------*/

    private void my_sign_in_method() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.EmailBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance(mFireApp)
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.cycler_logo)
                        .build(),
                RC_SIGN_IN);
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
