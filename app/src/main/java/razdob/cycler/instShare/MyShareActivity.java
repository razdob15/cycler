package razdob.cycler.instShare;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.Objects;

import razdob.cycler.R;
import razdob.cycler.dialogs.CustomDialog;
import razdob.cycler.myUtils.BottomNavigationViewHelper;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.MyFonts;
import razdob.cycler.myUtils.Permissions;

/**
 * Created by Raz on 24/09/2018, for project: PlacePicker2
 */
public class MyShareActivity extends AppCompatActivity {
    private static final String TAG = "MyShareActivity";
    private static final int ACTIVITY_NUM = 2;
    private final Context mContext = MyShareActivity.this;

    // Constants
    private final int PICK_IMAGE_REQUEST = 5;
    private final int CAMERA_REQUEST_CODE = 6;
    private final int CAMERA_PERMISSION_CODE = 8;

    // Widgets
    private LinearLayout cameraLL, galleryLL;
    private ImageView cameraIV, galleryIV;
    private TextView cameraTV, galleryTV;
    private TextView titleTV, placeNameTV;

    // Click Listeners
    private View.OnClickListener cameraClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openCamera();
        }
    };
    private View.OnClickListener galleryClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openGallery();
        }
    };


    // Vars
    private MyFonts mFonts;
    private FirebaseMethods mFireMethods;

    // Place Vars
    private String placeId;
    private String placeName;
    private String placeAddress;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_share);

        mFonts = new MyFonts(mContext);
        mFireMethods = new FirebaseMethods(mContext);

        getPlaceInfoFromIntent();
        setupWidgets();
        setupClicks();

        setupBottomNavigationView();
    }

    /**
     * Set up the placeName & placeId vars, from the income-intent.
     */
    private void getPlaceInfoFromIntent(){
        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.intent_place_id))) {
            placeId = intent.getStringExtra(getString(R.string.intent_place_id));
        }
        if (intent.hasExtra(mContext.getString(R.string.intent_place_name))) {
            placeName = intent.getStringExtra(mContext.getString(R.string.intent_place_name));
        }
        if (intent.hasExtra(mContext.getString(R.string.intent_place_address))) {
            placeAddress = intent.getStringExtra(mContext.getString(R.string.intent_place_address));
        }
    }

    /**
     * Matches the widgets to their IDs.
     */
    private void setupWidgets() {
        cameraLL = findViewById(R.id.camera_ll);
        cameraIV = findViewById(R.id.camera_iv);
        cameraTV = findViewById(R.id.camera_tv);
        galleryLL = findViewById(R.id.gallery_ll);
        galleryIV = findViewById(R.id.gallery_iv);
        galleryTV = findViewById(R.id.gallery_tv);
        titleTV = findViewById(R.id.title_tv);
        placeNameTV = findViewById(R.id.place_name_tv);

        titleTV.setTypeface(mFonts.getLightFont());
        placeNameTV.setTypeface(mFonts.getBoldFont());
        if (placeId != null && placeName != null) {
            titleTV.setText(R.string.title_upload_place_photo);
            placeNameTV.setText(placeName);
        }
    }

    /**
     * Set Listeners (CameraClick, GalleryClick).
     */
    private void setupClicks() {
        cameraLL.setOnClickListener(cameraClick);
        cameraIV.setOnClickListener(cameraClick);
        cameraTV.setOnClickListener(cameraClick);
        galleryLL.setOnClickListener(galleryClick);
        galleryIV.setOnClickListener(galleryClick);
        galleryTV.setOnClickListener(galleryClick);
    }


    /**
     * Sends a request [OnActivityResult] to open photos app (GooglePhotos, Gallery...)
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select place picture"), PICK_IMAGE_REQUEST);
    }

    // TODO(!): Use Camera2 API !
    /**
     * Check the camera permissions and open the camera if granted.
     */
    private void openCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Permissions.CAMERA_PERMISSION[0]) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onClick: launching camera.");
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            } else {
                requestPermissions(Permissions.CAMERA_PERMISSION, CAMERA_PERMISSION_CODE);
            }
        }
    }

    /**
     * work for CAMERA_PERMISSION_CODE. Show dialog if needed.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            Log.d(TAG, "onRequestPermissionsResult: sizes: " + (permissions.length == grantResults.length) + permissions.length);
            for (int i=0; i<permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {

                    final CustomDialog dialog = new CustomDialog(mContext, "This permission is required to share a photo",
                            2, "OK", "Cancel");
                    dialog.setClick1(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(Permissions.CAMERA_PERMISSION, CAMERA_PERMISSION_CODE);
                            }
                        }
                    });
                    dialog.setClick2(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    dialog.setCancelable(false);
                return;
                }
            }
            openCamera();
        }
    }

    /**
     * IF requestCode == PICK_IMAGE_REQUEST   ->  Take the photo from the gallery.
     * IF requestCode == CAMERA_REQUEST_CODE  ->  Take the photo from camera.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST) {
            Log.d(TAG, "onActivityResult: From Gallery");
            Log.d(TAG, "onActivityResult: data:" + data);
            if (data == null || data.getData() == null) return; // No Data

            Uri selectedPhotoUri = data.getData();   // The Photos Uri
            navigateToNextShare(selectedPhotoUri);

        } else if (requestCode == CAMERA_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: From camera.");
            Log.d(TAG, "onActivityResult: attempting to navigate to final share screen");


            if (data != null) {
                Bitmap bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                Log.d(TAG, "onActivityResult: received new bitmap from camera: " + bitmap);

                navigateToNextShare(bitmap);
            }
        }
    }

    /**
     * Navigates to ShareNextActivity with the Bitmap extra
     * @param bitmap - sends to the nextActivity in the intent [extra].
     */
    private void navigateToNextShare(Bitmap bitmap) {
        Intent intent = new Intent(mContext, ShareNextActivity.class);
        if (placeId != null && placeName != null) {     // Place Details
            intent.putExtra(getString(R.string.intent_place_id), placeId);
            intent.putExtra(mContext.getString(R.string.intent_place_name), placeName);
            intent.putExtra(mContext.getString(R.string.intent_place_address), placeAddress);
        }
        // Photo Details
        intent.putExtra(getString(R.string.selected_bitmap), bitmap);
        startActivity(intent);
    }

    /**
     * Navigates to ShareNextActivity with the Photo's Uri extra.
     * @param selectedPhotoUri - sends to the nextActivity in the intent [extra].
     */
    private void navigateToNextShare(Uri selectedPhotoUri) {
        Intent intent = new Intent(mContext, ShareNextActivity.class);
        if (placeId != null && placeName != null) {
            // Place Details Extra
            intent.putExtra(getString(R.string.intent_place_id), placeId);
            intent.putExtra(mContext.getString(R.string.intent_place_name), placeName);
            intent.putExtra(mContext.getString(R.string.intent_place_address), placeAddress);
        }
        // Photo Details Extra
        intent.putExtra(getString(R.string.intent_selected_photo_uri), selectedPhotoUri.toString());
        intent.putExtra(getString(R.string.selected_image), selectedPhotoUri);

        startActivity(intent);
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(this, this, bottomNavigationViewEx, mFireMethods.getFavoritePlacesIds());
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
