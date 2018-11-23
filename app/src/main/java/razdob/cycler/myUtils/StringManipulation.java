package razdob.cycler.myUtils;

import java.util.ArrayList;

/**
 * Created by Raz on 10/06/2018, for project: PlacePicker2
 */
public class StringManipulation {
    private static final String TAG = "StringManipulation";

    /**
     * @param text - photo's description Or a Text contains '#'
     * @return - String contains all the tags as a list.
     * for example: "abc #def #xyz#cool --> #def,#xyz,#cool
     */
    public static String getTagsFromDescription(String text) {
        if (text.contains("#")) {
            StringBuilder sb = new StringBuilder();
            char[] charArray = text.toCharArray();
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

    /**
     * Counts the number of the c in string.
     * @param string
     * @param ch
     * @return (int) how many times c exists in string?
     */
    public static int count(String string, char ch) {
        int count = 0;
        for (char temp : string.toCharArray()) {
            if (temp == ch) count++;
        }
        return count;
    }

    /**
     * Checks if the place is a coordinates and not a real business place.
     *
     * @param placeName - he place's name.
     * @return true if the place is a coordinates location, false otherwise.
     */
    public static boolean isMapCoordinates(String placeName) {
        //      32°47'10.9"N 34°58'19.0"E

        return count(placeName, '°') == 2 &&
                count(placeName, '\"') == 2 &&
                count(placeName, 'N') == 1 &&
                count(placeName, 'E') == 1;
    }

    /**
     * delete unnecessary spaces and put a capital letter in each word.
     *
     * @param tag - tag text.
     * @return - tag with capitalLetter in each word.
     */
    public static String tagFormat(String tag) {
        if (tag.length() == 0) {
            return tag;
        }
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < tag.length(); i++) {
            char ch = tag.charAt(i);
            if (i == 0)
                result.append(charToCap(ch));
            else if (tag.charAt(i - 1) == ' ') {
                if (tag.charAt(i) != ' ')
                    result.append(charToCap(ch));
            } else {
                result.append(charToReg(ch));
            }
        }
        return result.toString();
    }

    /**
     * shows the place tags with '#'
     * @param tags - tags list.
     * @return String represents the tags as text, with " #"
     */
    public static String placeTagsFormat(ArrayList<String> tags) {
        if (tags != null && tags.size() > 0) {
            StringBuilder tagsString = new StringBuilder("#" + tags.get(0));
            for (String tag : tags.subList(1, tags.size())) {
                tagsString.append(" #").append(tag);
            }
            return tagFormat(tagsString.toString());
        } else {
            return null;
        }
    }

    /**
     *
     * @param ch a char
     * @return char in capital letter.
     */
    private static char charToCap(char ch) {
        if (ch >= 97 && ch <= 122)
            return (char) (ch - 32);
        return ch;
    }

    /**
     *
     * @param ch - a char
     * @return char in regular letter.
     */
    private static char charToReg(char ch) {
        if (ch >= 65 && ch <= 90)
            return (char) (ch + 32);
        return ch;
    }

}