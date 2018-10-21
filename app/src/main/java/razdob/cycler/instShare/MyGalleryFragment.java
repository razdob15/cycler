package razdob.cycler.instShare;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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

import java.io.IOException;

import razdob.cycler.R;

/**
 * Created by Raz on 04/08/2018, for project: PlacePicker2
 */
public class MyGalleryFragment extends Fragment {
    private static final String TAG = "MyGalleryFragment";
    private Context mContext;

    // Constants
    private int PICK_IMAGE_REQUEST = 8;

    // Vars
    private String placeId, placeName;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_gallery, container, false);
        mContext = getContext();
        TextView photoTitleTV = view.findViewById(R.id.photo_title_tv);
        Button choosePhotoBtn = view.findViewById(R.id.choose_photo_btn);
        TextView placeNameTV = view.findViewById(R.id.place_name_tv);

        getPlaceFromBundle();

        if (placeName != null) {
            Log.d(TAG, "onCreateView: launchCamera for place: " + placeName);
            photoTitleTV.setText(mContext.getString(R.string.title_upload_place_photo));
            placeNameTV.setText(placeName);
            placeNameTV.setVisibility(View.VISIBLE);
        } else {
            photoTitleTV.setText(mContext.getString(R.string.title_upload_casual_photo));
            placeNameTV.setVisibility(View.INVISIBLE);
        }

        choosePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select place picture"), PICK_IMAGE_REQUEST);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST) {
            Log.d(TAG, "onActivityResult: data:" + data);
            if (data == null || data.getData() == null) return;
            Uri selectedPhotoUri = data.getData();


            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),
                        selectedPhotoUri);

            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(getActivity(), ShareNextActivity.class);
            if (placeId != null && placeName != null) {
                intent.putExtra(getString(R.string.intent_place_id), placeId);
                intent.putExtra(mContext.getString(R.string.intent_place_name), placeName);
            }
            intent.putExtra(getString(R.string.intent_selected_photo_uri), selectedPhotoUri.toString());
            intent.putExtra(getString(R.string.selected_image), selectedPhotoUri);

            startActivity(intent);

        }
    }


    /**
     * Initializes the placeId & placeNameTV
     */
    private void getPlaceFromBundle() {
        Bundle bundle = this.getArguments();
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
