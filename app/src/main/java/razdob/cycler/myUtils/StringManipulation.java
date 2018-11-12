package razdob.cycler.myUtils;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

/**
 * Created by Raz on 10/06/2018, for project: PlacePicker2
 */
public class StringManipulation {
    private static final String TAG = "StringManipulation";

    public static String getTags(String string) {
        if (string.contains("#")) {
            StringBuilder sb = new StringBuilder();
            char[] charArray = string.toCharArray();
            boolean foundTag = false;
            for (char c : charArray) {
                if (c == '#') {
                    foundTag = true;
                    sb.append(c);
                } else if (foundTag) {
                    sb.append(c);
                }
                if (c == ' ') {
                    foundTag = false;
                }
            }
            String s = sb.toString().replace(" ", "").replace("#", ",#");
            return s.substring(1, s.length());
        }
        return "";
    }

    public static int count(String string, char c) {
        int count = 0;
        for (char temp : string.toCharArray()) {
            if (temp == c) count++;
        }
        return count;
    }

    public static boolean isMapCoordinates(String placeName) {
        //      32°47'10.9"N 34°58'19.0"E

        return count(placeName, '°') == 2 &&
                count(placeName, '\"') == 2 &&
                count(placeName, 'N') == 1 &&
                count(placeName, 'E') == 1;
    }

    public static String placeTagFormat(String s) {
        if (s.length() == 0) {
            return s;
        }
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            if (i == 0)
                result.append(charToCap(s.charAt(i)));
            else if (s.charAt(i - 1) == ' ') {
                if (s.charAt(i) != ' ')
                    result.append(charToCap(s.charAt(i)));
            } else {
                result.append(charToReg(s.charAt(i)));
            }
        }
        return result.toString();
    }

    private static char charToCap(char c) {
        if (c >= 97 && c <= 122)
            return (char) (c - 32);
        return c;
    }

    private static char charToReg(char c) {
        if (c >= 65 && c <= 90)
            return (char) (c + 32);
        return c;
    }

}