package razdob.cycler.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DataSnapshot;

import java.util.Date;
import java.util.HashMap;

import razdob.cycler.R;

/**
 * Created by Raz on 02/06/2018, for project: PlacePicker2
 */
public class UserAccountSettings implements Parcelable{

    private String description;
    private String displayName;
    private String userName;
    private String website;
    private long followers;
    private long following;
    private long posts;
    private String user_id;

    public UserAccountSettings() { }

    public UserAccountSettings(String userName, String user_id) {
        this.userName = userName;
        this.user_id = user_id;
        this.posts = 0;
        this.following = 0;
        this.followers = 0;
        this.displayName = userName;
    }

    public UserAccountSettings(String description, String displayName, String userName, String website,
                               long followers, long following, long posts, String user_id) {
        this.description = description;
        this.displayName = displayName;
        this.userName = userName;
        this.website = website;
        this.followers = followers;
        this.following = following;
        this.posts = posts;
        this.user_id = user_id;
    }
    public UserAccountSettings(Context context, DataSnapshot dataSnapshot, String uid) {
        if (dataSnapshot.getKey().equals(context.getString(R.string.db_user_account_settings))) {
            for (DataSnapshot attrDS : dataSnapshot.child(uid).getChildren()) {
                switch (attrDS.getKey()) {
                    case "description":
                        this.description = attrDS.getValue(String.class);
                        break;
                    case "displayName":
                        this.displayName = attrDS.getValue(String.class);
                        break;
                    case "userName":
                        this.userName = attrDS.getValue(String.class);
                        break;
                    case "website":
                        this.website = attrDS.getValue(String.class);
                        break;
                    case "followers":
                        this.followers = attrDS.getValue(Long.class);
                        break;
                    case "following":
                        this.following = attrDS.getValue(Long.class);
                        break;
                    case "posts":
                        this.posts = attrDS.getValue(Long.class);
                        break;
                    case "user_id":
                        this.user_id = attrDS.getValue(String.class);
                        break;
                }

            }
        }

    }

    protected UserAccountSettings(Parcel in) {
        description = in.readString();
        displayName = in.readString();
        userName = in.readString();
        website = in.readString();
        followers = in.readLong();
        following = in.readLong();
        posts = in.readLong();
        user_id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeString(displayName);
        dest.writeString(userName);
        dest.writeString(website);
        dest.writeLong(followers);
        dest.writeLong(following);
        dest.writeLong(posts);
        dest.writeString(user_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserAccountSettings> CREATOR = new Creator<UserAccountSettings>() {
        @Override
        public UserAccountSettings createFromParcel(Parcel in) {
            return new UserAccountSettings(in);
        }

        @Override
        public UserAccountSettings[] newArray(int size) {
            return new UserAccountSettings[size];
        }
    };

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public long getFollowers() {
        return followers;
    }

    public void setFollowers(long followers) {
        this.followers = followers;
    }

    public long getFollowing() {
        return following;
    }

    public void setFollowing(long following) {
        this.following = following;
    }

    public long getPosts() {
        return posts;
    }

    public void setPosts(long posts) {
        this.posts = posts;
    }

    @Override
    public String toString() {
        return "UserAccountSettings{" +
                "description='" + description + '\'' +
                ", displayName='" + displayName + '\'' +
                ", userName='" + userName + '\'' +
                ", website='" + website + '\'' +
                ", followers=" + followers +
                ", following=" + following +
                ", posts=" + posts +
                '}';
    }
}
