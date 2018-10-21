package razdob.cycler.myUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Raz on 10/06/2018, for project: PlacePicker2
 */
public class ImageManager {

    private static final String TAG = "ImageManager";

    public static Bitmap getBitmap(String imgUrl) {
        File imageFile = new File(imgUrl);
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try {
            fis = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "getBitmap: FileNotFoundException" + e.getMessage());
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "getBitmap: IOException" + e.getMessage());
            }
        }
        return bitmap;
    }

    /**
     * Returns byte array from a bitmap.
     * @param bm
     * @param quality is greater than 0 but less than 100
     * @return
     */
    public static byte[] getBytesFromBitmap(Bitmap bm, int quality) {
        Log.d(TAG, "getBytesFromBitmap: bitmap: " + bm);
        // TODO(!) Check the exception here !!
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (bm == null) return null;
        bm.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }


}
