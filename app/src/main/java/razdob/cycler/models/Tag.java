package razdob.cycler.models;

/**
 * Created by Raz on 02/07/2018, for project: PlacePicker2
 */
public class Tag {

    private String tagName;
    private String place_id;
    private String user_id;
    private long counter;

    public Tag() { }

    public Tag(String tagName, String place_id, String user_id, long counter) {
        this.tagName = tagName;
        this.place_id = place_id;
        this.user_id = user_id;
        this.counter = counter;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "tagName='" + tagName + '\'' +
                ", place_id='" + place_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", counter=" + counter +
                '}';
    }
}
