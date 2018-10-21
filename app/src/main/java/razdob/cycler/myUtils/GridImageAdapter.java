package razdob.cycler.myUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

import razdob.cycler.R;

/**
 * Created by Raz on 02/06/2018, for project: PlacePicker2
 */
public class GridImageAdapter extends ArrayAdapter<String> {
    private static final String TAG = "GridImageAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResource;

    private String mAppend;
    private ArrayList<String> imgURLs;
    private ArrayList<String> placesIds;

    // Get place Name
    private GeoDataClient mGeoDataClient;

    public GridImageAdapter(Context context, int layoutResource, String append, ArrayList<String> imgURLs, ArrayList<String> placesNames) {
        super(context, layoutResource, imgURLs);
        Log.d(TAG, "GridImageAdapter: imgURLs: " + imgURLs);
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        this.layoutResource = layoutResource;
        this.mAppend = append;
        this.imgURLs = imgURLs;
        this.placesIds = placesNames;

        if (placesIds != null) mGeoDataClient = Places.getGeoDataClient(mContext);
    }

    private static class ViewHolder {
        SquareImageView image;
        ProgressBar mProgressBar;
        TextView placeName;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;

        /*
         * ViewHolder hold pattern (Similar to recycleView).
         * */

        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();
            holder.mProgressBar = convertView.findViewById(R.id.gridImage_progressbar);
            holder.image = convertView.findViewById(R.id.grid_imageView);
            holder.placeName = convertView.findViewById(R.id.place_name_tv);

            if (placesIds != null && placesIds.get(position) != null) {
                    mGeoDataClient.getPlaceById(placesIds.get(position)).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                        @SuppressLint("RestrictedApi")
                        @Override
                        public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                            if (task.isSuccessful()) {
                                PlaceBufferResponse places = task.getResult();
                                Place place = places.get(0);

                                holder.placeName.setText(place.getName().toString());
                                holder.placeName.setVisibility(View.VISIBLE);
                            }

                        }
                    });
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String imgUrl = getItem(position);
        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(mAppend + imgUrl, holder.image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if (holder.mProgressBar != null) holder.mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (holder.mProgressBar != null) holder.mProgressBar.setVisibility(View.GONE);

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (holder.mProgressBar != null) holder.mProgressBar.setVisibility(View.GONE);
                if (holder.placeName.getVisibility() == View.VISIBLE &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.placeName.setTextColor(mContext.getColor(R.color.colorWhite));
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if (holder.mProgressBar != null) holder.mProgressBar.setVisibility(View.GONE);

            }
        });


        return convertView;
    }
}

