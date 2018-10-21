package razdob.cycler.myUtils;

import android.os.Environment;

/**
 * Created by Raz on 08/06/2018, for project: PlacePicker2
 */
public class FilePaths {

    public FilePaths() { }

    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public String PICTURES = ROOT_DIR + "Pictures";
    public String CAMERA = ROOT_DIR + "/DCIM/camera";


    public String FIREBASE_IMAGE_STORAGE_PERSONS = "images/persons/";
    public String FIREBASE_IMAGE_STORAGE_BUSINESS = "images/business/";
    public String FIREBASE_PLACE_IMAGE = "images/places/";
}
