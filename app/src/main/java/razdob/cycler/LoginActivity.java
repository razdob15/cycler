package razdob.cycler;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import razdob.cycler.myUtils.FireBaseUtils;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LoginActivity";

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseAuth mAuth;
    /* ---------------------- FIREBASE ----------------------- */

    // Views
    private Button logInBtn;
    private AutoCompleteTextView email_actv, password_actv;
    private TextView signUpTV;
    private ProgressDialog mProgressDialog;

    // Errors in login
    private final String NOT_CORRECT_PASSWORD = "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The password is invalid or the user does not have a password.";
    private final String INVALID_EMAIL = "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The email address is badly formatted.";
    private final String EMAIL_NOT_EXIST = "com.google.firebase.auth.FirebaseAuthInvalidUserException: There is no mFireUser record corresponding to this identifier. The mFireUser may have been deleted.";
    private final String USER_NOT_EXIST = "com.google.firebase.auth.FirebaseAuthInvalidUserException: There is no user record corresponding to this identifier. The user may have been deleted.";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: created.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initFirebaseStaff();

        if (mAuth.getCurrentUser() != null) {
            //If the mFireUser already registered in this phone AND his account is exist in the FirebaseAuth
            Log.i("user_typeee", "" + FireBaseUtils.mUserType);

            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
        // else

        // Create the ProgressBar
        mProgressDialog = new ProgressDialog(this);

        // Matches view to IDs
        logInBtn = findViewById(R.id.log_in_btn);
        email_actv = findViewById(R.id.user_email_actv);
        password_actv = findViewById(R.id.user_password_actv);
        signUpTV = findViewById(R.id.signup_tv);

        // Click Listeners
        logInBtn.setOnClickListener(this);
        signUpTV.setOnClickListener(this);
    }

    /**
     * Log in the mFireUser
     If login was successful -> Start Profile Activity
     Else -> Show Correct error Toast
     */
    private void userLogin() {
        // Mail and password strings
        String emailText = email_actv.getText().toString().trim();
        String passwordText = password_actv.getText().toString().trim();

        // If email or password empty -> Sends the correct Toast
        if (emailText.isEmpty()) {
            Toast.makeText(this, "Fill The Mail", Toast.LENGTH_SHORT).show();
            return;
        }
        if (passwordText.isEmpty()) {
            Toast.makeText(this, "Fill the Password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show the ProgressBar
        mProgressDialog.setMessage("Sign-In User...");
        mProgressDialog.show();


        mAuth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmailAndPassword:onComplete: " + task.isSuccessful());
                        mProgressDialog.dismiss();
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (task.isSuccessful()){   // Good login
                            if (user != null) {
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                                // TODO(3): Do the Email Verified !!

//                                if (user.isEmailVerified()) {
//                                    Log.d(TAG, "onComplete: syccess. Email in verified.");
//
//                                } else {
//                                    Toast.makeText(LoginActivity.this, "Email is not verifies.\nPlease check your inbox.", Toast.LENGTH_SHORT).show();
////                                    FireBaseUtils.sendVerificationEmail(LoginActivity.this);
////                                    mAuth.signOut();
//                                }
                            }

                        } else {   // Authentication weren't successfully

                            // Identify the exception
                            String taskException = task.getException().toString();

                            // Show a matching toast
                            switch (taskException) {
                                case NOT_CORRECT_PASSWORD:
                                    Toast.makeText(LoginActivity.this, "The password is incorrect", Toast.LENGTH_SHORT).show();
                                    break;
                                case INVALID_EMAIL:
                                    Toast.makeText(LoginActivity.this, "Invalid email", Toast.LENGTH_SHORT).show();
                                    break;
                                case EMAIL_NOT_EXIST:
                                    Toast.makeText(LoginActivity.this, "The email isn't found. try to register", Toast.LENGTH_LONG).show();
                                    break;
                                case USER_NOT_EXIST:
                                    Toast.makeText(LoginActivity.this, "The email isn't found. try to register", Toast.LENGTH_LONG).show();
                                    break;
                                default:
                                    Toast.makeText(LoginActivity.this, "An error occur... Try again", Toast.LENGTH_SHORT).show();
                                    Log.i("register_error", task.getException().toString());
                            }
                        }
                    }
                });
    }


    //on click to: logInBtn AND signInTv
    @Override
    public void onClick(View view) {
        if (view == logInBtn) {userLogin();}

        if (view == signUpTV) {startActivity(new Intent(this, RegisterActivity.class));}
    }


    private void initFirebaseStaff() {
        Log.d(TAG, "initFirebaseStaff: called.");

        // Retrieve mFireApp app.
        FirebaseApp mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
    }
}
