package razdob.cycler.myUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

import razdob.cycler.BigPhotoFragment;
import razdob.cycler.R;

/**
 * Created by Raz on 02/06/2018, for project: PlacePicker2
 */
public class UniversalImageLoader {

    private static final String TAG = "UniversalImageLoader";
    private static final int DEFAULT_IMAGE = R.drawable.cycler_logo;
    private Context mContext;

    public UniversalImageLoader(Context mContext) {
        this.mContext = mContext;
    }

    public ImageLoaderConfiguration getConfig() {

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(DEFAULT_IMAGE)
                .showImageForEmptyUri(DEFAULT_IMAGE)
                .showImageOnFail(DEFAULT_IMAGE)
                .considerExifParams(true)
                .cacheOnDisc(true).cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        return new ImageLoaderConfiguration.Builder(mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100 * 1024 * 1024).build();
    }


    /***
     * This method can be used to set images that are static. It can't be used if the images
     * are being changes in theFragment/Activity - OR if they are being set in a list or
     * a gridView.
     * @param imgUrl
     * @param imageView
     * @param mProgressBar
     * @param append
     */
    public static void setImage(Context context, String imgUrl, ImageView imageView, final ProgressBar mProgressBar, String append) {
        File cacheDir = StorageUtils.getCacheDirectory(context);
        long cacheAge = 10L;

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .diskCache(new LimitedAgeDiskCache(cacheDir, cacheAge)) // this will make the cache to remain for 10 seconds only
                .build();
        ImageLoader.getInstance().init(config);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(append + imgUrl, imageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                Log.d(TAG, "onLoadingStarted: image_url: " + imageUri);
                if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                Log.d(TAG, "onLoadingFailed: FAILED TO UPLOAD THE IMAGE !");
                if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                Log.d(TAG, "onLoadingComplete: loading completed.");
                if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);

            }
        });
    }

}
