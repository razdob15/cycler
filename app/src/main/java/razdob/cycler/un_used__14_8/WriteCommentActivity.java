package razdob.cycler.un_used__14_8;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Calendar;

import razdob.cycler.R;
import razdob.cycler.models.Comment;
import razdob.cycler.myUtils.FireBaseUtils;
import razdob.cycler.myUtils.FirebaseInserts;

public class WriteCommentActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PICK_IMAGE_REQUEST = 25;
    // GUI
    private ImageButton[] starsArr;
    private EditText content_et;
    private Button submitBtn, addImgBtn;
    private TextView placeName_tv, numOfStars;
    private ImageView placeImg;

    private Uri uploadedUri;
    private String imgLoc;

    String placeName;

    // Firebase Database Staff
    private DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser mFireUser;
    private FirebaseAuth mFirebaseAuth;

    // Can continue
    int starCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_comment);


        // Get firebaseAuth instance and mFireUser
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth.getCurrentUser() == null) {
            mFireUser = null;
        } else {
            mFireUser = mFirebaseAuth.getCurrentUser();
        }


        // Gets extras
        Intent intent = getIntent();
        placeName = intent.getStringExtra("place_name");

        // Sets Title
        placeName_tv = findViewById(R.id.place_name_tv);
        placeName_tv.setText(placeName);

        content_et = findViewById(R.id.user_comment);
        submitBtn = findViewById(R.id.submit_btn);
        numOfStars = findViewById(R.id.num_of_stars);
        addImgBtn = findViewById(R.id.add_image);
        placeImg = findViewById(R.id.place_img);
        starsArr = new ImageButton[]{
                findViewById(R.id.one_star),
                findViewById(R.id.two_star),
                findViewById(R.id.three_star),
                findViewById(R.id.four_star),
                findViewById(R.id.five_star)};

        for (ImageButton star : starsArr) {

            star.setImageResource(android.R.drawable.btn_star_big_off);
            star.setBackgroundColor(Color.TRANSPARENT);
            star.setOnClickListener(this);
        }
        submitBtn.setOnClickListener(this);
        addImgBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v == submitBtn) {
            if (starCounter == 0) {
                Toast.makeText(this, "Choose a rank, please", Toast.LENGTH_SHORT).show();
                return;
            }

            String content = content_et.getText().toString();
            long calender = Calendar.getInstance().getTime().getTime();

            // TODO(2): Check validate of the placeNameTV (without . , \ /    ...)
            // TODO(3): Remove similar comments from the same mFireUser.
            Comment comment = new Comment(content, PersonProfileActivity.mUserName, mFireUser.getUid(), calender, starCounter, placeName, imgLoc);
            mDatabaseReference.child("places_comments").child(FireBaseUtils.zipName(placeName)).child("t" + mFireUser.getUid().toCharArray()[0] + calender).setValue(comment);

            Intent intent = new Intent(getApplicationContext(), OnePlaceData.class);
            // Extras
            intent.putExtra("place_name", placeName);  // Name
            intent.putExtra("place_address", getIntent().getStringExtra("place_address"));  // Address
            intent.putExtra("place_phone", getIntent().getStringExtra("place_phone"));  // Phone
            finish();
            startActivity(intent);

        } else if (v == addImgBtn) {
            showFileChooser();

            // TODO(!): Add image to the place's storage
        } else {  // Stars...
            // Sets the stars colors, by the click.
            int index = -1;
            for (int i = 0; i < 5; i++)
                if (starsArr[i] == v) {
                    index = i;
                    break;
                }
            if (index != -1) {  // v in the array.
                starCounter = index + 1;
                for (int i = 0; i < 5; i++) {
                    if (i <= index)
                        starsArr[i].setImageResource(android.R.drawable.btn_star_big_on);
                    else
                        starsArr[i].setImageResource(android.R.drawable.btn_star_big_off);
                }
                numOfStars.setText(String.format("(%d)", index + 1));
            }

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            uploadedUri = data.getData();
            String url = uploadedUri.toString();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uploadedUri);
                placeImg.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (uploadedUri != null && mFireUser != null) {

                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Uploading image...");
                progressDialog.show();

                imgLoc = "images/business_comments/" + placeName;
                FirebaseInserts.uploadFile(this, imgLoc, uploadedUri, mFireUser.getUid()+".jpg", progressDialog);
            }
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle("Cancel comment");
        alertDialogBuilder
                .setMessage("Are you sure tou want to cancel this comment?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getApplicationContext(), OnePlaceData.class);

                        // Extras
                        intent.putExtra("place_name", getIntent().getStringExtra("place_name"));  // Name
                        intent.putExtra("place_address", getIntent().getStringExtra("place_address"));  // Address
                        intent.putExtra("place_phone", getIntent().getStringExtra("place_phone"));  // Phone
                        startActivity(intent);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(WriteCommentActivity.this, "Nice, keep writing :)", Toast.LENGTH_SHORT).show();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select your profile image"), PICK_IMAGE_REQUEST);
    }

}
