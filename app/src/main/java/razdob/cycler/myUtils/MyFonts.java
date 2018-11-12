package razdob.cycler.myUtils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

/**
 * Created by Raz on 12/09/2018, for project: PlacePicker2
 */
public class MyFonts {
    private static final String TAG = "MyFonts";

    private Typeface mBoldFont, mBoldItalicFont, mLightFont, mLightItalicFont;


    public MyFonts(Context context) {
        Log.d(TAG, "MyFonts: Called.");

        mBoldFont = Typeface.createFromAsset(context.getAssets(), "fonts/Panton-ExtraBold.otf");
        mBoldItalicFont = Typeface.createFromAsset(context.getAssets(), "fonts/Panton-ExtraBoldItalic.otf");
        mLightFont = Typeface.createFromAsset(context.getAssets(), "fonts/Panton-ExtraLight.otf");
        mLightItalicFont = Typeface.createFromAsset(context.getAssets(), "fonts/Panton-ExtraLightItalic.otf");
    }

    public Typeface getBoldFont() {
        return mBoldFont;
    }

    public Typeface getBoldItalicFont() {
        return mBoldItalicFont;
    }

    public Typeface getLightFont() {
        return mLightFont;
    }

    public Typeface getLightItalicFont() {
        return mLightItalicFont;
    }
}
