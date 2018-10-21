package razdob.cycler.un_used__14_8;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import razdob.cycler.R;
import razdob.cycler.adapters.PlaceCommentAdapter;
import razdob.cycler.feed.DataActivity;
import razdob.cycler.models.Comment;
import razdob.cycler.myUtils.FireBaseUtils;

public class OnePlaceData extends AppCompatActivity implements View.OnClickListener {

    // GUI
    private TextView placeNameTV, placePhoneTV, placeAddressTV;
    private ListView commentsLV;
    private FloatingActionButton addCommentBtn;
    private FloatingActionButton likeBtn;

    // Strings
    private String placeName;
    private String placeAddress;
    private String placePhone;

    // Firebase Database Staff
    private DatabaseReference mDatabaseReference;
    private ValueEventListener mValueListener;
    private FirebaseUser mFireUser;
    private FirebaseAuth mAuth;

    //Call Permission code
    private final static int MY_CALL_PERMISSION = 131;

    @Override
    protected void onStart() {
        super.onStart();
        mDatabaseReference.addListenerForSingleValueEvent(mValueListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mValueListener != null)
            mDatabaseReference.removeEventListener(mValueListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_place_data);

        Intent intent = getIntent();
        placeName = FireBaseUtils.unzipName(intent.getStringExtra("place_name"));
        placeAddress = intent.getStringExtra("place_address");
        placePhone = intent.getStringExtra("place_phone");

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(placeName);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.cycler_green)));
        } else {
            setSupportActionBar(toolbar);
        }

        mAuth = FirebaseAuth.getInstance();
        mFireUser = mAuth.getCurrentUser();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String zipPlaceName = FireBaseUtils.zipName(placeName);
                if (dataSnapshot.child("places_comments").hasChild(zipPlaceName)) {
                    DataSnapshot commentsDS = dataSnapshot.child("places_comments").child(zipPlaceName);
                    ArrayList<Comment> arrayOfComments = new ArrayList<>();


                    for (DataSnapshot commentDS : commentsDS.getChildren()) {
                        Comment comment = commentDS.getValue(Comment.class);
                        if (comment != null) {
                            comment.setBusinessName(FireBaseUtils.unzipName(comment.getBusinessName()));
                            comment.setTime(Long.parseLong(commentDS.getKey().substring(2)));
                            arrayOfComments.add(comment);
                        }
                    }

                    // Connect listView to the PlaceCommentAdapter
                    Log.i("array_len", "" + arrayOfComments.size());
                    PlaceCommentAdapter adapter = new PlaceCommentAdapter(getApplicationContext(), arrayOfComments);
                    commentsLV.setAdapter(adapter);
                } else {
                    commentsLV.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("databaseError" + getLocalClassName(), "" + databaseError.getMessage());
            }
        };

        // GUI - connect to IDs
        placeNameTV = findViewById(R.id.place_name_tv);
        placeAddressTV = findViewById(R.id.place_address);
        placePhoneTV = findViewById(R.id.place_phone);
        commentsLV = findViewById(R.id.comments_lv);
        addCommentBtn = findViewById(R.id.add_comment_btn);
        likeBtn = findViewById(R.id.like_btn);

        // Set texts...
        placeNameTV.setText(placeName);
        placeAddressTV.setText(placeAddress);
        placePhoneTV.setText(placePhone);


        // Button clickListener
        addCommentBtn.setOnClickListener(this);
        placePhoneTV.setOnClickListener(this);
        likeBtn.setOnClickListener(this);
        // TODO(2): Get place's images


    }

    @Override
    public void onClick(View v) {

        if (v == likeBtn) {
            // TODO(!!): Add to Favorites !!!!
            Toast.makeText(this, "ADD TO FAVORITES !!", Toast.LENGTH_SHORT).show();
        }

        if (v == addCommentBtn) {
            Intent intent = new Intent(this, WriteCommentActivity.class);
            intent.putExtra("place_name", placeName);
            intent.putExtra("place_address", placeAddress);
            intent.putExtra("place_phone", placePhone);

            String userName;

            if (mFireUser != null)
                userName = mFireUser.getDisplayName();
            else userName = "Anonymous";
            if (userName == null)
                userName = "Anonymous";
            intent.putExtra("user_name", userName);
            startActivity(intent);
        }

        if (v == placePhoneTV) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                String phoneNum = placePhoneTV.getText().toString();
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNum));
                startActivity(callIntent);
            } else {
                requestCallPermission();
            }

            // TODO: Call to this phone
        }
    }

    @Override
    public void onBackPressed() {
        // TODO: CHeck this intent....
        Intent intent = new Intent(this, DataActivity.class);
        startActivity(intent);
    }

    private void requestCallPermission() {
        // If the permission ACCESS_FINE_LOCATION hasn't benn granted yet
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                PackageManager.PERMISSION_GRANTED) {

            //if Marshmallow or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // requestPermissions defined below
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                        MY_CALL_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_CALL_PERMISSION:
                // If the Permission hasn't granted
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "This app requires phone-call permissions to call to this place", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Please, try to call again", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


}
