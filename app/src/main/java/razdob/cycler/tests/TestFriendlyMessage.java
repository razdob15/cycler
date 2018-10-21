package razdob.cycler.tests;

/**
 * Created by Raz on 11/03/2018, for project: PlacePicker2
 */

public class TestFriendlyMessage {

    private String text;
    private String name;
    private String photoUrl;

    public TestFriendlyMessage() {
    }

    public TestFriendlyMessage(String text, String name, String photoUrl) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}

