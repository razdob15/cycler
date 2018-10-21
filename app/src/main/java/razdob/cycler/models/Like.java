package razdob.cycler.models;

/**
 * Created by Raz on 15/06/2018, for project: PlacePicker2
 */
public class Like {

    private String user_id;
    private String like_id;

    public Like(String user_id, String like_id) {
        this.user_id = user_id;
        this.like_id = like_id;
    }

    public Like() { }

    @Override
    public String toString() {
        return "Like{" +
                "user_id='" + user_id + '\'' +
                '}';
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getLike_id() {
        return like_id;
    }

    public void setLike_id(String like_id) {
        this.like_id = like_id;
    }
}
