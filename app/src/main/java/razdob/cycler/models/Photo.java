package razdob.cycler.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Raz on 10/06/2018, for project: PlacePicker2
 */
public class Photo implements Parcelable{

    private String caption;
    private String date_creates;
    private String image_path;
    private String photo_id;
    private String user_id;
    private String tags;
    private List<String> likes;
    private List<InstComment> comments;
    private String place_id;
    private String place_name;

    public Photo(){}

    public Photo(String caption, String date_creates, String image_path, String photo_id, String user_id, String tags, List<String> likes, List<InstComment> comments, String place_id) {
        this.caption = caption;
        this.date_creates = date_creates;
        this.image_path = image_path;
        this.photo_id = photo_id;
        this.user_id = user_id;
        this.tags = tags;
        this.likes = likes;
        this.comments = comments;
        this.place_id = place_id;
    }

    public List<InstComment> getComments() {
        return comments;
    }

    public void setComments(List<InstComment> comments) {
        this.comments = comments;
    }

    public static Creator<Photo> getCREATOR() {
        return CREATOR;
    }

    protected Photo(Parcel in) {
        caption = in.readString();
        date_creates = in.readString();
        image_path = in.readString();
        photo_id = in.readString();
        user_id = in.readString();
        tags = in.readString();
        place_id = in.readString();
        place_name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(caption);
        dest.writeString(date_creates);
        dest.writeString(image_path);
        dest.writeString(photo_id);
        dest.writeString(user_id);
        dest.writeString(tags);
        dest.writeString(place_id);
        dest.writeString(place_name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };


    public boolean isPlacePhoto() {
        return (place_id != null && place_name != null);
    }
    // ------------------------- Getters & Setters --------------------------------

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDate_creates() {
        return date_creates;
    }

    public void setDate_creates(String date_creates) {
        this.date_creates = date_creates;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public String getPhoto_id() {
        return photo_id;
    }

    public void setPhoto_id(String photo_id) {
        this.photo_id = photo_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public List<String> getLikes() {
        return likes;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getPlace_name() {
        return place_name;
    }

    public void setPlace_name(String place_name) {
        this.place_name = place_name;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "caption='" + caption + '\'' +
                ", date_creates='" + date_creates + '\'' +
                ", image_path='" + image_path + '\'' +
                ", photo_id='" + photo_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", tags='" + tags + '\'' +
                ", likes=" + likes +
                ", comments=" + comments +
                ", place_id='" + place_id + '\'' +
                '}';
    }

    public boolean equals(Photo photo){
        return (photo.photo_id.equals(photo_id) && photo.date_creates.equals(date_creates) && photo.image_path.equals(image_path));
    }
}
