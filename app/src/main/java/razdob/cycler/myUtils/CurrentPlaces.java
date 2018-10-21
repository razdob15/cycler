package razdob.cycler.myUtils;

import android.graphics.Bitmap;

import com.google.android.gms.location.places.Place;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Raz on 12/04/2018, for project: PlacePicker2
 */

public class CurrentPlaces {

    private HashMap<String, Place> places;
    private HashMap<String, Bitmap> bitmaps;

    public CurrentPlaces(HashMap<String, Place> places, HashMap<String, Bitmap> bitmaps) {
        this.places = places;
        this.bitmaps = bitmaps;
    }

    public HashMap<String, Place> getPlaces() {
        return places;
    }

    public void setPlaces(HashMap<String, Place> places) {
        this.places = places;
    }

    public HashMap<String, Bitmap> getBitmaps() {
        return bitmaps;
    }

    public void setBitmaps(HashMap<String, Bitmap> bitmaps) {
        this.bitmaps = bitmaps;
    }
}
