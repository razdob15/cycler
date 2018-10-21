package razdob.cycler.models;

import android.support.annotation.Nullable;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Raz on 22/02/2018, for project: PlacePicker2
 */

public class Comment {

    private String content;  // The comment
    private String authorName, authorUid;  // The author TODO(!): Delete author name. use authorUid getName()  - FirebaseUtils !
    private long time;
    private long rank = -1;
    private String businessName, imageUri;

    public Comment() { }
    public Comment(String content, String authorName, String authorUid, long rank, long time) {
        this.content = content;
        this.authorName = authorName;
        this.authorUid = authorUid;
        this.time = time;
        this.rank = rank;
    }

    public Comment(String content, String authorName, String authorUid, long time, long rank, String businessName, @Nullable String imageUri) {
        this.content = content;
        this.authorName = authorName;
        this.authorUid = authorUid;
        this.time = time;
        this.rank = rank;
        this.businessName = businessName;
        this.imageUri = imageUri;
    }

    public Comment(String content, String authorName, String authorUid, long time, long rank, String businessName) {
        this.content = content;
        this.authorName = authorName;
        this.authorUid = authorUid;
        this.time = time;
        this.rank = rank;
        this.businessName = businessName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorUid() {
        return authorUid;
    }

    public void setAuthorUid(String authorUid) {
        this.authorUid = authorUid;
    }

    public long getRank() {
        return rank;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }

    public String getBusinessName() { return businessName; }

    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getImageUri() { return imageUri; }

    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
}
