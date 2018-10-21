package razdob.cycler.myUtils;

import android.Manifest;

/**
 * Created by Raz on 07/06/2018, for project: PlacePicker2
 */
public class Permissions {

    public static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static final String[] CAMERA_PERMISSION = {Manifest.permission.CAMERA};
    public static final String[] WRITE_STORAGE_PERMISSION = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final String[] READ_STORAGE_PERMISSION = {Manifest.permission.READ_EXTERNAL_STORAGE};
    public static final String[] CALL_PHONE_PERMISSION = {Manifest.permission.CALL_PHONE};
    public static final String[] ACCESS_FINE_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION};



}
