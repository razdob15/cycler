package razdob.cycler.instShare;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Objects;

import razdob.cycler.R;
import razdob.cycler.instProfile.AccountSettingsActivity;
import razdob.cycler.myUtils.Permissions;

/**
 * Created by Raz on 28/05/2018, for project: PlacePicker2
 */
public class PhotoFragment extends Fragment {
    private static final String TAG = "PhotoFragment";
    private Context mContext;

    // Constant
    private static final int PHOTO_FRAGMENT_NUM = 1;
    private static final int GALLERY_FRAGMENT_NUM = 0;
    private static final int CAMERA_REQUEST_CODE = 5;

    private String placeId, placeName;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        Log.d(TAG, "onCreateView: started.");
        mContext = getContext();

        TextView titleTV = view.findViewById(R.id.photo_title_tv);
        TextView placeNameTV = view.findViewById(R.id.place_name_tv);
        Button openCam = view.findViewById(R.id.btnLaunchCamera);
        openCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO(!) Check this !!!
//                mContext.startActivity(new Intent(mContext, CustomCameraActivity.class));
                launchCamera();

            }
        });
//        if (launchCamera())
//            return view;

        getPlaceFromBundle();

        // Place Name Text
        if (placeName != null) {
            Log.d(TAG, "onCreateView: launchCamera for place: " + placeName);
            titleTV.setText(mContext.getString(R.string.title_upload_place_photo));
            placeNameTV.setText(placeName);
            placeNameTV.setVisibility(View.VISIBLE);
        } else {
            titleTV.setText(mContext.getString(R.string.title_upload_casual_photo));
            placeNameTV.setVisibility(View.INVISIBLE);
        }

        return view;

    }

    /**
     * Check the camera permission.
     *  If OK - Opens Camera
     *  Else - Requests Permission by ShareActivity-Flag
     * @return - True if the camera opened, otherwise = False.
     */
    private void launchCamera() {
        if (((ShareActivity) Objects.requireNonNull(getActivity())).getCurrentTabNumber() == PHOTO_FRAGMENT_NUM) {
            if (((ShareActivity) getActivity()).checkPermissions(Permissions.CAMERA_PERMISSION[0])) {
                Log.d(TAG, "onClick: starting camera.");
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            } else {
                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

    private boolean isRootTask() {
        return ((ShareActivity) getActivity()).getTask() == 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: done taking a photo.");
            Log.d(TAG, "onActivityResult: attempting to navigate to final share screen");

            Bitmap bitmap;
            if (data != null) {
                bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");

                if (isRootTask()) {
                    // Share activity (Place Photo)
                    try {
                        Log.d(TAG, "onActivityResult: recieved new bitmap from camera: " + bitmap);
                        Intent intent = new Intent(getActivity(), ShareNextActivity.class);
                        if (placeId != null && placeName != null) {
                            intent.putExtra(getString(R.string.intent_place_id), placeId);
                            intent.putExtra(mContext.getString(R.string.intent_place_name), placeName);
                        }
                        intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                        startActivity(intent);
                    } catch (NullPointerException e) {
                        Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                    }

                } else {
                    //EditProfile  (Profile Photo)
                    try {
                        Log.d(TAG, "onActivityResult: recieved new bitmap from camera: " + bitmap);

                        Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                        intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                        intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                        startActivity(intent);
                        Objects.requireNonNull(getActivity()).finish();

                    } catch (NullPointerException e) {
                        Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                    }
                }
            }


        }
    }

    /**
     * Initializes the placeId & placeNameTV
     */
    private void getPlaceFromBundle() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            placeName = bundle.getString(mContext.getString(R.string.intent_place_name));
            placeId = bundle.getString(mContext.getString(R.string.intent_place_id));

            if (placeName == null && placeId != null) {
                Log.d(TAG, "getPlaceFromBundle: ERROR!!");
                Log.e(TAG, "getPlaceFromBundle: name is null but id is not!!!\nplaceId: " +placeId);

            }
        } else {
            Log.d(TAG, "getPlaceFromBundle: no bundle");
        }
    }
}

