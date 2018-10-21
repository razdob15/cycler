package razdob.cycler;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import razdob.cycler.myUtils.SquareImageView;
import razdob.cycler.myUtils.UniversalImageLoader;

/**
 * Created by Raz on 10/08/2018, for project: PlacePicker2
 */
public class BigPhotoFragment extends Fragment {
    private static final String TAG = "BigPhotoFragment";
    private Context mContext;

    // GUI
    private SquareImageView imageView;
    private ProgressBar photoPB;
    private RelativeLayout mainRL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_big_photo, container, false);
        mContext = getActivity();
        imageView = view.findViewById(R.id.photo);
        photoPB = view.findViewById(R.id.photo_pb);
        mainRL = view.findViewById(R.id.main_relativeLayout);

        mainRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFragment();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            String photoUrl = bundle.getString(mContext.getString(R.string.bundle_photo));
            Bitmap bitmap = bundle.getParcelable(mContext.getString(R.string.selected_bitmap));

            if (bitmap != null) {   //Bitmap
                imageView.setImageBitmap(bitmap);
                photoPB.setVisibility(View.GONE);
            } else if (photoUrl != null) {  // String (url)
//                UniversalImageLoader.setImage(getContext(), photoUrl, imageView, photoPB, ""); TODO(0): choose: this or with bitmap..?
                ImageLoader imageLoader = ImageLoader.getInstance();
                // TODO(BETA): Make this load faster if possible.
                imageLoader.loadImage(photoUrl, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        // loaded bitmap is here (loadedImage)
                        Log.d(TAG, "onLoadingComplete: loadedImageeeee: " + loadedImage.toString());
                        imageView.setImageBitmap(loadedImage);
                        photoPB.setVisibility(View.GONE);
                    }
                });

            } else {
                Log.w(TAG, "onCreateView: no bundle-arguments: " + bundle.toString());
                closeFragment();
            }
        } else {
            Log.d(TAG, "onCreateView: no bundle");
            closeFragment();
        }


        return view;
    }

    /**
     * Clears the args (the bundle) and remove the fragment.
     */
    private void closeFragment() {
        if (getArguments() != null) {
            getArguments().clear();
        }
        getActivity().getSupportFragmentManager().beginTransaction().remove(BigPhotoFragment.this).commit();
    }


}