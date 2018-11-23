package razdob.cycler.instProfile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import razdob.cycler.ChooseFavoritePlacesActivity;
import razdob.cycler.MainRegisterActivity;
import razdob.cycler.R;
import razdob.cycler.dialogs.ConfirmPasswordDialog;
import razdob.cycler.dialogs.CustomDialog;
import razdob.cycler.models.User;
import razdob.cycler.models.UserAccountSettings;
import razdob.cycler.models.UserSettings;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.MyFonts;
import razdob.cycler.myUtils.Permissions;
import razdob.cycler.myUtils.RemoteConfigConsts;
import razdob.cycler.myUtils.UniversalImageLoader;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Raz on 31/05/2018, for project: PlacePicker2
 */
public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.OnConfirmPasswordListener {
    private static final String TAG = "EditProfileFragment";

    // Constants
    private static final int PICK_IMAGE_REQUEST = 12;
    private static final int STORAGE_REQUEST_CODE = 9;

    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: got the password");

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(Objects.requireNonNull(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail()), password);

        ///////////////// Prompt the user to re-provide their sign-in credentials
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Log.d(TAG, "User re-authenticated.");

                            //////////////////// Check to see if the email is not presents in the DB.
                            if (mEmail.getText() == null || mEmail.getText().toString().length() == 0) {
                                Toast.makeText(mContext, "Please fill the email section.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mAuth.fetchProvidersForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                    if (task.isSuccessful()) {
                                        try {


                                            if (task.getResult().getProviders().size() == 1) {
                                                Log.d(TAG, "onComplete: That email is already in use.");
                                                Toast.makeText(getActivity(), "That email is already in use", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Log.d(TAG, "onComplete: That email is available");

                                                ///////////// The email is available so update it
                                                mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "onComplete: User email address updated");
                                                                    Toast.makeText(getActivity(), "email updated", Toast.LENGTH_SHORT).show();
                                                                    mFireMethods.updateEmailDB(mEmail.getText().toString());
                                                                }
                                                            }
                                                        });
                                            }
                                        } catch (NullPointerException e) {
                                            Log.e(TAG, "onComplete: NullPointerException: " + e.getMessage());
                                        }

                                    }
                                }


                            });


                        } else {
                            Log.d(TAG, "onComplete: re-authentication failed");
                        }
                    }
                });

    }

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mRef;
    private StorageReference mStorageReference;
    private FirebaseMethods mFireMethods;
    private String userID;
    /* ---------------------- FIREBASE ----------------------- */

    // GUI
    private EditText mDisplayNameET, mUserName, mWebsite, mDescription, mEmail, mPhoneNum;
    private TextView mChangeProfileImage;
    private CircleImageView mProfileImage;
    private String mImgUrl;
    private ProgressBar mProfileImagePB;

    private Context mContext;

    // Vars
    private UserSettings mUserSettings;
    private Bitmap profileBitmap;
    private MyFonts mFonts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        mContext = getActivity();
        mFireMethods = new FirebaseMethods(mContext);
        mFonts = new MyFonts(mContext);

        CheckIfFirstTime();

        mProfileImage = view.findViewById(R.id.profile_image);
        mDisplayNameET = view.findViewById(R.id.display_name);
        mUserName = view.findViewById(R.id.user_name);
        mWebsite = view.findViewById(R.id.website);
        mDescription = view.findViewById(R.id.description);
        mEmail = view.findViewById(R.id.email);
        mPhoneNum = view.findViewById(R.id.phone_number);
        mChangeProfileImage = view.findViewById(R.id.change_profile_photo);
        mProfileImagePB = view.findViewById(R.id.profile_image_pb);
        TextView privateInfoTV = view.findViewById(R.id.text_private_info);
        privateInfoTV.setTypeface(mFonts.getBoldFont());

        setupFirebaseStaff();

        mStorageReference.child("images/persons/" + userID + "/profile.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (uri == null)
                    return;
                mImgUrl = uri.toString();
//                setProfileImage();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "onFailure: Storage failed with the error: " + e.getMessage());
                    }
                });

        // Back arrow for navigating back to "InstProfileActivity"
        ImageView backArrow = view.findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to InstProfileActivity");
                Objects.requireNonNull(getActivity()).finish();
            }
        });

        ImageView saveChangesIV = view.findViewById(R.id.save_changes);
        saveChangesIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to save changes");
                // TODO(): Make saveProfileSettings() boolean and if returns true- close this fragment.
                if (mUserName.getText().toString().length() == 0) {
                    Toast.makeText(mContext, "user-name field cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    saveProfileSettings();
                    Toast.makeText(mContext, "Saved !", Toast.LENGTH_SHORT).show();

                    final ArrayList<String> favoritesIds = mFireMethods.getFavoritePlacesIds();
                    if (favoritesIds.size() < RemoteConfigConsts.MIN_FAVORITES_COUNT) {
                        Log.d(TAG, "onClick: need to choose more favorites.");


                        final CustomDialog dialog = CustomDialog.createTwoButtonsDialog(mContext, null, "Now you must choose " +
                                        (RemoteConfigConsts.MIN_FAVORITES_COUNT - favoritesIds.size()) +
                                        " more restaurants you like. " + "So we can offer you the best restaurants in yor area.",
                                "OK", "LATER");

                        dialog.setClick1(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "onClick: Choose more favorites.");
                                Intent intent = new Intent(mContext, ChooseFavoritePlacesActivity.class);
                                intent.putStringArrayListExtra(mContext.getString(R.string.intent_favorite_places), favoritesIds);
                                startActivity(intent);
                            }
                        });
                        dialog.setClick2(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "onClick: go to profile");
                                dialog.dismiss();
                                InstProfileActivity.start(mContext);
                            }
                        });

                        dialog.show();

                    } else {
                        Log.d(TAG, "onClick: goto profile");
                        InstProfileActivity.start(mContext);
                    }
                }
            }
        });
        return view;
    }

    /**
     * Checks the incoming bundle and shows the dialog if needed.
     */
    private void CheckIfFirstTime() {
        Log.d(TAG, "CheckIfFirstTime: called.");
        Bundle args = getArguments();
        if (args != null && args.getBoolean(mContext.getString(R.string.intent_first_time), false)) {
            Log.d(TAG, "onCreateView: first time");
            final CustomDialog dialog = CustomDialog.createOneButtonDialog(mContext, "Create Your Profile",
                    "Please, fill your profile details.", "OK");
            dialog.setClick1(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    /**
     * Catches Result for PICK_IMAGE_REQUEST
     *
     * @param requestCode - good if  equal to PICK_IMAGE_REQUEST
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Log.d(TAG, "onActivityResult: PICK IMAGE; OK. ");

            Uri selectedImage = data.getData();

            try {
                profileBitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), selectedImage);
                mProfileImage.setImageBitmap(profileBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Retrieves the data contained on the widgets and submits it to the DB.
     * (Not Really): Before doing so it checks to make sure the username chosen is unique.
     */
    private void saveProfileSettings() {
        final String displayName = mDisplayNameET.getText().toString();
        final String userName = mUserName.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        final String phoneNum = mPhoneNum.getText().toString();

        User tempUser = mUserSettings.getUser() != null ? mUserSettings.getUser() : new User(userName, userID);
        UserAccountSettings tempAccountSettings = mUserSettings.getSettings();

        if (tempAccountSettings != null) {
            // Update userAccountSettings
            if (tempAccountSettings.getDisplayName() == null || !displayName.equals(tempAccountSettings.getDisplayName())) {
                tempAccountSettings.setDisplayName(displayName);
            }
            if (tempAccountSettings.getWebsite() == null || !website.equals(tempAccountSettings.getWebsite())) {
                tempAccountSettings.setWebsite(website);
            }
            if (tempAccountSettings.getDescription() == null || !description.equals(tempAccountSettings.getDescription())) {
                tempAccountSettings.setDescription(description);
            }
        }
        mFireMethods.updateUserAccountSettingsDB(displayName, website, description);
        if (tempAccountSettings == null)
            tempAccountSettings = new UserAccountSettings(description, displayName, userName, website, 0, 0, 0, userID);

        // Update User (object)
        if (!email.equals(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail())) {
            if (tempUser.getEmail() == null || !email.equals(tempUser.getEmail())) {
                ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
                if (getFragmentManager() != null) {
                    dialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));
                }
                dialog.setTargetFragment(EditProfileFragment.this, 1);
                tempUser.setEmail(email);
                mFireMethods.updateEmailDB(email);
            }
        } else {
            tempUser.setEmail(email);
            mFireMethods.updateEmailDB(email);
        }
        if (tempUser.getPhone() == null || !phoneNum.equals(tempUser.getPhone())) {
            tempUser.setPhone(phoneNum);
            mFireMethods.updateUserPhone(phoneNum);
        }

        // Check & Update UserName
        //case1: if the user made a change to their userName
        if (tempUser.getName() == null || (tempUser.getName() != null && !tempUser.getName().equals(userName))) {
            Log.d(TAG, "saveProfileSettings: change tha name !");
            checkIfUserNameExist(userName);
        }

        // Update Profile Photo
        if (profileBitmap != null) {
            mFireMethods.uploadNewPhoto(getString(R.string.profile_photo), "", -1, mImgUrl, profileBitmap, null);
        }


        //case2: if the user made a change to their email
//        if (!tempUser.getEmail().equals(email)) {
        // step1) reAuthenticate
        //          -Confirm the password and the email

        // step2) Check if the user email already is registered
        //          -'fetchProviderForEmail(String name)'
        // step3) Change the email
        //          -Submit the new email to the DB and Auth
//        }

        // Update UID !
        if (tempUser.getUser_id() == null)
            tempUser.setUser_id(mAuth.getCurrentUser().getUid());
        if (tempAccountSettings.getUser_id() == null)
            tempAccountSettings.setUser_id(mAuth.getCurrentUser().getUid());

        mFireMethods.updateUserDB(mAuth.getCurrentUser().getUid(), tempUser, tempAccountSettings);

    }

    /**
     * Checks if @param userName already exists in the DB.
     *
     * @param newUserName
     */
    private void checkIfUserNameExist(final String newUserName) {
        Log.d(TAG, "checkIfUserNameExist: Checking if " + newUserName + " already exists.");
        Query query = mRef
                .child(getString(R.string.db_user_account_settings))
                .orderByChild(getString(R.string.db_field_username))
                .equalTo(newUserName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    mFireMethods.updateUserName(newUserName);
                }
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        Log.d(TAG, "checkIfUserNameExist:onDataChange: FOUND A MATCH: " + singleSnapshot.getValue(UserAccountSettings.class).getUserName());
                        Toast.makeText(mContext, "That username already exist", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void setupProfileWidgets(UserSettings userSettings) {
        Log.d(TAG, "setupProfileWidgets: setting widgets with data retrieving from firebase: " + userSettings.toString());
        mUserSettings = userSettings;

        User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();


        if (user != null) {
            if (user.getEmail() != null && user.getEmail().length() > 0) {
                Log.d(TAG, "setupProfileWidgets: use user mail");
                mEmail.setText(user.getEmail());
            } else {
                Log.d(TAG, "setupProfileWidgets: Use Auth mail.");
                mEmail.setText(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail());
            }
            if (user.getName() != null && user.getName().length() > 0)
                mUserName.setText(user.getName());
            if (user.getPhone() != null && user.getPhone().length() > 0)
                mPhoneNum.setText(user.getPhone());
            if (user.getProfile_photo() != null && user.getProfile_photo().length() > 0) {
//                UniversalImageLoader.setImage(mContext, user.getProfile_photo(), mProfileImage, mProfileImagePB, "");
                UniversalImageLoader.setImagePicasso(user.getProfile_photo(), mProfileImage, mProfileImagePB);
            }
        }
        if (settings != null) {
            if (settings.getDescription() != null && settings.getDescription().length() > 0)
                mDescription.setText(settings.getDescription());
            if (settings.getDisplayName() != null && settings.getDisplayName().length() > 0)
                mDisplayNameET.setText(settings.getDisplayName());
            else {
                String nameFromFireAuth = mAuth.getCurrentUser().getDisplayName();
                if (nameFromFireAuth != null && nameFromFireAuth.length() > 0) {
                    mDisplayNameET.setText(nameFromFireAuth);
                }
            }
            // Override userName
            if (mUserName.getText() == null || mUserName.getText().length() == 0)
                if (settings.getUserName() != null && settings.getUserName().length() > 0)
                    mUserName.setText(settings.getUserName());
            if (settings.getWebsite() != null && settings.getWebsite().length() > 0)
                mWebsite.setText(settings.getWebsite());
        }

        mChangeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Change profile Photo Click");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (mContext.checkSelfPermission(Permissions.READ_STORAGE_PERMISSION[0]) == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "onClick: open Chooser");
                        openPhotoChooser();
                    } else {
                        Log.d(TAG, "onClick: request Permission");
                        requestPermissions(Permissions.CAMERA_PERMISSION, STORAGE_REQUEST_CODE);
                    }
                } else {
                    openPhotoChooser();
                }
            }
        });
    }

    /**
     * Sends a request [OnActivityResult] to open photos app (GooglePhotos, Gallery...)
     */
    private void openPhotoChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecet Profile Photo"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_REQUEST_CODE) {
            for (int gr : grantResults) {
                if (gr == PackageManager.PERMISSION_DENIED) {
                    Log.d(TAG, "onRequestPermissionsResult: permission Denied !");
                    Toast.makeText(mContext, "permission denied.. can't choose the photo", Toast.LENGTH_SHORT).show();
                    requestPermissions(permissions, STORAGE_REQUEST_CODE);
                    return;

                }
            }
            openPhotoChooser();

        }
    }

    /*
     * ------------------------- Firebase ------------------------------------------
     */

    private void setupFirebaseStaff() {
        Log.d(TAG, "setupFirebaseStaff: called.");

        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();
        FirebaseStorage storage = FirebaseStorage.getInstance(mFireApp, "gs://cyclerproject.appspot.com");
        mStorageReference = storage.getReference();

        userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();


        // Init mAuthStateListener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                // Check if user is logged in

                if (user == null) {
                    Log.d(TAG, "onAuthStateChanged: User log-out");
                    startActivity(new Intent(mContext, MainRegisterActivity.class));
                } else {
                    Log.d(TAG, "onAuthStateChanged: User is in. uid = " + firebaseAuth.getCurrentUser().getUid());
                }
            }
        };

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Retrieve user info from the database.
                setupProfileWidgets(mFireMethods.getUserSettings(dataSnapshot, userID));

                // Retrieve images for the user in question
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: DBError: " + databaseError.getMessage());
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }
}
