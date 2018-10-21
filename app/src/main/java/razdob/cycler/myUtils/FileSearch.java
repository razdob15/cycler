package razdob.cycler.myUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Raz on 08/06/2018, for project: PlacePicker2
 */
public class FileSearch {

    /**
     * Search a directory and returns a list of all **directories** contained inside
     * @param directory
     * @return
     */
    public static ArrayList<String> getDirectoryPaths(String directory) {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            for (File listFile : listFiles) {
                if (listFile.isDirectory()) {
                    pathArray.add(listFile.getAbsolutePath());
                }
            }
        }
        return pathArray;
    }

    /**
     * Search a directory and returns a list of all **files** contained inside
     * @param directory
     * @return
     */
    public static ArrayList<String> getFilePaths(String directory) {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            for (File listFile : listFiles) {
                if (listFile.isFile()) {
                    pathArray.add(listFile.getAbsolutePath());
                }
            }
        }
        return pathArray;
    }
}
