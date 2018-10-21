package razdob.cycler.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import razdob.cycler.R;
import razdob.cycler.myUtils.GridImageAdapter;
import razdob.cycler.myUtils.SquareImageView;

/**
 * Created by Raz on 30/06/2018, for project: PlacePicker2
 */
public class GridPlaceImagesAdapter extends ArrayAdapter<Bitmap> {
    private static final String TAG = "GridPlaceImagesAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResource;
    private ArrayList<Bitmap> bitmaps;

    public GridPlaceImagesAdapter(Context context, int layoutResource, ArrayList<Bitmap> bitmaps) {
        super(context, layoutResource, bitmaps);
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layoutResource = layoutResource;
        this.bitmaps = bitmaps;
    }

    private static class ViewHolder {
        SquareImageView image;
        ProgressBar mProgressBar;
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

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.image.setImageBitmap(bitmaps.get(position));
        holder.mProgressBar.setVisibility(View.GONE);

        // TODO(!): Add item-click and open the ViewPostFragment. See Grid Image Adapter !
        // TODO(continue): like the profile fragment and the photos...


        return convertView;
    }
}
