package razdob.cycler.un_used__14_8;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import razdob.cycler.MainRegisterActivity;
import razdob.cycler.R;
import razdob.cycler.myUtils.FirebaseInserts;
import razdob.cycler.models.Business;


/**
 * A business profile details
 */

public class BusinessProfileActivity extends AppCompatActivity implements OnClickListener {

    private static final int PICK_IMAGE_REQUEST = 77;
    // texts
    private EditText businessName_et, businessAddress_et, businessPhone_et, businessMail_et;
    private AutoCompleteTextView tag_actv;
    private EditText moreDetailsET;
    private Button createBusinessBtn, useAccountMailBtn, chooseLogoBtn;
    private ImageView logoIV;
    private ProgressBar progressBar;

    public static LatLng placeLatLng = null;
    public static Business myBusiness;

    private Uri logoUri;
    private String logoString;


    // Firebase Staff
    private DatabaseReference mRef;
    private ValueEventListener mValueListener;
    private FirebaseUser mFireUser;
    private StorageReference mStorageReference = FirebaseStorage.getInstance().getReference();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    private HashMap<String, Boolean> placeTags;


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
        setContentView(R.layout.activity_business_profile);

        progressBar = findViewById(R.id.business_pb);
        progressBar.setVisibility(View.VISIBLE);

        mRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    mAuth.removeAuthStateListener(mAuthListener);
                    Toast.makeText(BusinessProfileActivity.this, "You require to sign-in firstly", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(BusinessProfileActivity.this, MainRegisterActivity.class));
                }
            }
        };

        // Get firebase User
        if (mAuth.getCurrentUser() != null) {
            mFireUser = mAuth.getCurrentUser();
        } else {
            Log.w(getLocalClassName(), "The user is null !!");
        }
        mValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myBusiness = new Business();
                if (dataSnapshot.child("business_users").hasChild(mFireUser.getUid())) {
                    DataSnapshot businessDS = dataSnapshot.child("business_users").child(mFireUser.getUid());
                    if (businessDS.hasChild("tags")) {
                        placeTags = new HashMap<>();
                        for (DataSnapshot tagDS : dataSnapshot.child("business_users").child(mFireUser.getUid()).child("tags").getChildren()) {
                            placeTags.put(tagDS.getKey(), tagDS.getValue(boolean.class));
                        }
                        myBusiness.setTags(placeTags);
                    }
                    if (businessDS.hasChild("address")) {
                        myBusiness.setAddress(businessDS.child("address").getValue(String.class));
                    }
                    if (businessDS.hasChild("businessMail")) {
                        myBusiness.setBusinessMail(businessDS.child("businessMail").getValue(String.class));
                    }
                    if (businessDS.hasChild("logoImgUrl")) {
                        myBusiness.setLogoImgUrl(businessDS.child("logoImgUrl").getValue(String.class));
                    }
                    if (businessDS.hasChild("name")) {
                        myBusiness.setName(businessDS.child("name").getValue(String.class));
                    }
                    if (businessDS.hasChild("phone")) {
                        myBusiness.setPhone(businessDS.child("phone").getValue(String.class));
                    }
                    if (businessDS.hasChild("uid")) {
                        myBusiness.setUid(businessDS.child("uid").getValue(String.class));
                    }
                    if (businessDS.hasChild("latLng")) {
                        DataSnapshot latLngDS = businessDS.child("latLng");
                        myBusiness.setLatLng(new LatLng(latLngDS.child("latitude").getValue(Double.class), latLngDS.child("longitude").getValue(Double.class)));
                    }
                    // TODO!!! Add latLng details!
                }
                businessName_et.setText(myBusiness.getName());
                businessAddress_et.setText(myBusiness.getAddress());
                businessPhone_et.setText(myBusiness.getPhone());
                businessMail_et.setText(myBusiness.getBusinessMail());

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        // connect GUI to IDs
        businessName_et = findViewById(R.id.place_name_et);
        businessAddress_et = findViewById(R.id.place_address_et);
        businessPhone_et = findViewById(R.id.place_phone_et);
        businessMail_et = findViewById(R.id.place_email_et);
//        moreDetailsET = findViewById(R.id.more_details_et);
        createBusinessBtn = findViewById(R.id.create_place_btn);
        useAccountMailBtn = findViewById(R.id.use_account_mail_btn);
        chooseLogoBtn = findViewById(R.id.choose_logo_img_btn);
        logoIV = findViewById(R.id.logo_iv);

        findViewById(R.id.choose_on_map_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BusinessProfileActivity.this, TestGoogleMaps.class);
                intent.putExtra(getString(R.string.intent_map_type), getString(R.string.intent_business_map_place));

                if (placeLatLng != null) {
                    intent.putExtra("longitude", placeLatLng.longitude);
                    intent.putExtra("latitude", placeLatLng.latitude);
                }
                intent.putExtra("user_uid", mFireUser.getUid());
                startActivity(intent);
            }
        });


        businessName_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mRef.child("business_users").child(mFireUser.getUid()).child("name").setValue(s.toString());
                if (count > 0) createBusinessBtn.setOnClickListener(BusinessProfileActivity.this);
                else createBusinessBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(BusinessProfileActivity.this, "Name field is must!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
                mRef.child("business_users").child(mFireUser.getUid()).child("name").setValue(s.toString());
            }
        });
        businessAddress_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mRef.child("business_users").child(mFireUser.getUid()).child("address").setValue(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                mRef.child("business_users").child(mFireUser.getUid()).child("address").setValue(s.toString());
            }
        });

        // Set click listeners
        createBusinessBtn.setOnClickListener(this);
        chooseLogoBtn.setOnClickListener(this);
        useAccountMailBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFireUser != null) businessMail_et.setText(mFireUser.getEmail());
            }
        });

        // Load LOGO to the imageView
        mStorageReference.child("images/business/" + mFireUser.getUid() + "/logo.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                logoString = uri.toString();
                loadImageFromUrl(logoString);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            logoUri = data.getData();

            StorageReference photosRef = mStorageReference.child(logoUri.getLastPathSegment());

            // Upload file to firebase Storage
            photosRef.putFile(logoUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getUploadSessionUri();
                    if (downloadUri != null) {
                        logoString = downloadUri.toString();
                        Log.i("logoString", logoString);
                    }
                }
            });

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), logoUri);
                logoIV.setImageBitmap(bitmap);
                Log.i("YYYYYYYY", "TYTHTGTG");
            } catch (IOException e) {
                e.printStackTrace();
            }
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {

        if (v == createBusinessBtn) {
            if (isValidEmail(businessMail_et.getText().toString()) && mFireUser != null) {


                myBusiness = new Business(businessName_et.getText().toString(), "business", placeLatLng, FirebaseAuth.getInstance().getCurrentUser().getUid(), businessAddress_et.getText().toString(),
                        logoString, businessPhone_et.getText().toString(), businessMail_et.getText().toString(), placeTags, null);

                myBusiness.setLogoImgUrl(logoString);
                FirebaseInserts.saveBusinessInfo(mFireUser.getUid(), myBusiness);
                Intent intent = new Intent(this, BusinessMoreInfo.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Email is Must !", Toast.LENGTH_SHORT).show();
            }
            uploadFile();

        }

        if (v == chooseLogoBtn) {
            showFileChooser();
        }


    }

    // Get an email address.
    // Returns true if the email is valid.
    public boolean isValidEmail(String mail) {
        if (mail.length() < 2)
            return false;
        boolean[] booleans = {false, false, false};
        if (mail != null) {
            char first_ch = mail.toCharArray()[0];
            if (first_ch > 122 || first_ch < 65 ||
                    (first_ch > 90 && first_ch < 97)) {
                Log.i("not_valid_mail", "" + 1);
                return false;
            }

            for (char ch : mail.toCharArray()) {
                if (ch == '@')
                    Log.i("not_valid_mail", "" + 4);
                booleans[0] = true;
                if (ch == '.')
                    Log.i("not_valid_mail", "" + 5);
                booleans[1] = true;
                if (booleans[1] &&
                        ((ch >= 97 && ch <= 1622) ||
                                (ch >= 65 && ch <= 90)))
                    Log.i("not_valid_mail", "" + 6);
                booleans[2] = true;
            }
            for (boolean bool : booleans) {
                if (!bool) {
                    Log.i("not_valid_mail", "" + 2);
                    return false;
                }
            }
            return true;
        } else {
            Log.i("not_valid_mail", "" + 3);
            return false;
        }
    }

    // Menu options.. Sign out
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mAuth.signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        // set title
        alertDialogBuilder.setTitle("Sign out");

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you want to sign out?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseAuth.getInstance().signOut();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void uploadFile() {
        if (logoUri == null) {
//            Toast.makeText(this, "You didn't choose an image !", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mFireUser == null) {
            Toast.makeText(this, "Sorry, You need to log-in first", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        String imageLoc = "images/business/" + mFireUser.getUid();

        FirebaseInserts.uploadFile(this, imageLoc, logoUri, "logo.jpg", progressDialog);
    }

    private void showFileChooser() {
        progressBar.setVisibility(View.VISIBLE);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select your logo image"), PICK_IMAGE_REQUEST);
    }

    private void loadImageFromUrl(final String url) {
        Log.i("person_uri", url);
        Picasso.get().load(url).placeholder(R.mipmap.cycler_launcher)
                .error(R.mipmap.cycler_launcher)
                .into(logoIV, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.i("business_image", "The user's profile image was loaded successfully ! ");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.i("business_image", "The user's profile image was not loaded successfully !:( ");
                    }
                });

    }

    private ArrayList<String> hashMapToTrueArrayList(HashMap<String, Boolean> hashMap) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (String name : hashMap.keySet()) {
            if (hashMap.get(name)) {
                arrayList.add(name);
            }
        }
        return arrayList;
    }
}
