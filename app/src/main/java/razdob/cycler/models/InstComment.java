package razdob.cycler.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raz on 17/06/2018, for project: PlacePicker2
 */
public class InstComment {

    private String comment;
    private String user_id;
    private ArrayList<String> likes;
    private String date_creates;
    private String comment_id;

    public InstComment() { }

    public InstComment(String comment_id) { this.comment_id = comment_id; }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public ArrayList<String> getLikes() {
        return likes;
    }

    public void setLikes(ArrayList<String> likes) {
        this.likes = likes;
    }

    public String getDate_creates() {
        return date_creates;
    }

    public void setDate_creates(String date_creates) {
        this.date_creates = date_creates;
    }

    public String getComment_id() { return comment_id; }

    public void setComment_id(String comment_id) { this.comment_id = comment_id; }

    @Override
    public String toString() {
        return "InstComment{" +
                "comment='" + comment + '\'' +
                ", user_id='" + user_id + '\'' +
                ", likes=" + likes +
                ", date_creates='" + date_creates + '\'' +
                '}';
    }
}
