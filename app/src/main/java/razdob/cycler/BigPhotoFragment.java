package razdob.cycler;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.Objects;

import razdob.cycler.myUtils.SquareImageView;
import razdob.cycler.myUtils.UniversalImageLoader;

/**
 * Created by Raz on 10/08/2018, for project: PlacePicker2
 */
public class  BigPhotoFragment extends Fragment {
    private static final String TAG = "BigPhotoFragment";

    // Bundle Extras
    private static final String PHOTO_EXTRA = "bundle_photo";
    private static final String BITMAP_EXTRA = "selected_bitmap";

    // GUI
    private SquareImageView imageView;
    private ProgressBar photoPB;
    private RelativeLayout mainRL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_big_photo, container, false);
        imageView = view.findViewById(R.id.photo);
        photoPB = view.findViewById(R.id.photo_pb);
        mainRL = view.findViewById(R.id.main_relativeLayout);

        mainRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFragment();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) { Log.d(TAG, "onClick: big image view Click !"); }});

        getDataFromBundle();

        return view;
    }

    private void getDataFromBundle() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            String photoUrl = bundle.getString(PHOTO_EXTRA);
            Bitmap bitmap = bundle.getParcelable(BITMAP_EXTRA);

            if (bitmap != null) {   //Bitmap

                imageView.setImageBitmap(bitmap);
                photoPB.setVisibility(View.GONE);

            } else if (photoUrl != null) {  // String (url)
                UniversalImageLoader.setImagePicasso(photoUrl, imageView, photoPB);
//                UniversalImageLoader.setImage(getContext(), photoUrl, imageView, photoPB, "");

            } else {
                Log.w(TAG, "onCreateView: no bundle-arguments: " + bundle.toString());
                closeFragment();
            }
        } else {
            Log.d(TAG, "onCreateView: no bundle");
            closeFragment();
        }
    }

    /**
     * Clears the args (the bundle) and remove the fragment.
     */
    private void closeFragment() {
        if (getArguments() != null) {
            getArguments().clear();
        }
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().remove(BigPhotoFragment.this).commit();
    }

    /**
     * Opens the photo (from bitmap) as big with BigPhotoFragment.
     *
     * @param activity - current Activity
     * @param bitmap - Bitmap to make big.
     */
    public static void createBigPhoto(FragmentActivity activity, Bitmap bitmap) {
        Log.d(TAG, "bigPhoto: called with bitmap");
        BigPhotoFragment bigPhotoFrag = new BigPhotoFragment();
        Bundle bundle = new Bundle();

        if (bitmap != null) {
            Log.d(TAG, "bigPhoto: bitmap: " + bitmap.toString());
            bundle.putParcelable(BITMAP_EXTRA, bitmap);

            bigPhotoFrag.setArguments(bundle);

            FragmentManager manager = activity.getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.container, bigPhotoFrag, "bigFragment");
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            Log.w(TAG, "bigPhoto: Bitmap is NULL !");
        }
    }


    /**
     * Open the photo (from url) as big with BigPhotoFragment.
     *
     * @param activity - current Activity
     * @param photoUrl - url of the photo to make big.
     */
    public static void createBigPhoto(FragmentActivity activity, String photoUrl) {
        Log.d(TAG, "bigPhoto: called with url");
        BigPhotoFragment bigPhotoFrag = new BigPhotoFragment();
        Bundle bundle = new Bundle();

        if (photoUrl != null) {
            Log.d(TAG, "bigPhoto: url: " + photoUrl);
            bundle.putString(PHOTO_EXTRA, photoUrl);

            bigPhotoFrag.setArguments(bundle);

            FragmentManager manager = activity.getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.container, bigPhotoFrag, "bigFragment");
            transaction.addToBackStack(null);
            transaction.commit();
        }

    }

}