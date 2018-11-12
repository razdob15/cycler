package razdob.cycler.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import razdob.cycler.R;

/**
 * Created by Raz on 28/02/2018, for project: PlacePicker2
 */

public class User implements Parcelable {

    private String name, address, profile_photo;
    private String gender;  // Male\ Female\ Other
    private String phone, email;
    private String status;
    private String aboutUser;
    private String userType;  // "person" OR "business"
    private HashMap<String, Boolean> preferences;  // TODO(1) Change to ArrayList<String>  [DELETE !!! ?]
    private ArrayList<String> favoritePlacesIDs;
    private String user_id;


    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public User(Parcel in) {
        this.name = in.readString();
        this.address = in.readString();
        this.profile_photo = in.readString();
        this.phone = in.readString();
        this.email = in.readString();
        this.status = in.readString();
        this.aboutUser = in.readString();
        this.userType = in.readString();
        this.preferences = new HashMap<>();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            this.preferences.put(in.readString(), Boolean.valueOf(in.readString()));
        }
        this.favoritePlacesIDs = in.createStringArrayList();
        this.user_id = in.readString();
    }

    public User(String name, String uid) {
        this.name = name;
        this.user_id = uid;
    }

    public User() {}

    public User(Context context, DataSnapshot dataSnapshot, String uid) {
        if (dataSnapshot.getKey().equals(context.getString(R.string.db_persons))) {
            for (DataSnapshot attrDS : dataSnapshot.child(uid).getChildren()) {
                switch (attrDS.getKey()) {
                    case "address":
                        this.address = attrDS.getValue(String.class);
                        break;
                    case "name":
                        this.name = attrDS.getValue(String.class);
                        break;
                    case "favoritePlacesIDs":
                        ArrayList<String> favIds = new ArrayList<>();
                        for (DataSnapshot favDS : attrDS.getChildren()) {
                            favIds.add(favDS.getValue(String.class));
                        }
                        this.favoritePlacesIDs = favIds;
                        break;
                    case "preferences":
                        HashMap<String, Boolean> userPreferences = new HashMap<>();
                        for (DataSnapshot prefDS : attrDS.getChildren()) {
                            userPreferences.put(prefDS.getKey(), prefDS.getValue(Boolean.class));
                        }
                        this.preferences = userPreferences;
                        break;
                    case "profile_photo":
                        this.profile_photo = attrDS.getValue(String.class);
                        break;
                    case "userType":
                        this.userType = attrDS.getValue(String.class);
                        break;
                    case "gender":
                        this.gender = attrDS.getValue(String.class);
                        break;
                    case "phone":
                        this.phone = attrDS.getValue(String.class);
                        break;
                    case "email":
                        this.email = attrDS.getValue(String.class);
                        break;
                    case "status":
                        this.status = attrDS.getValue(String.class);
                        break;
                    case "aboutUser":
                        this.aboutUser = attrDS.getValue(String.class);
                        break;
                    case "user_id":
                        this.user_id = attrDS.getValue(String.class);
                        break;
//                    case "exemption_places_ids":
//                        ArrayList<String> tempIds = new ArrayList<>();
//                        for (DataSnapshot exemPlaceDS: attrDS.getChildren()) {
//                            tempIds.add(exemPlaceDS.getValue(String.class));
//                        }
//                        exemption_places_ids = tempIds;
                }

            }
        }
    }

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getProfile_photo() {
        return profile_photo;
    }
    public String getUser_id() {
        return user_id;
    }
    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getAboutUser() {
        return aboutUser;
    }
    public void setAboutUser(String aboutUser) {
        this.aboutUser = aboutUser;
    }
    public List<String> getFavoritePlacesIDs() {
        return favoritePlacesIDs;
    }
    public void setFavoritePlacesIDs(ArrayList<String> favoritePlacesIDs) {
        this.favoritePlacesIDs = favoritePlacesIDs;
    }
    public String getUserType() {
        return userType;
    }
    public void setUserType(String userType) {
        this.userType = userType;
    }
    public HashMap<String, Boolean> getPreferences() {
        return preferences;
    }
    public void setPreferences(HashMap<String, Boolean> preferences) { this.preferences = preferences; }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(profile_photo);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeString(status);
        dest.writeString(aboutUser);
        dest.writeString(userType);
        if (preferences != null) {
            dest.writeInt(preferences.size());
            for (String pref: preferences.keySet()) {
                dest.writeString(pref);
                dest.writeString(String.valueOf(preferences.get(pref)));
            }
        } else dest.writeInt(0);
        if (favoritePlacesIDs != null)
            dest.writeStringArray(arrFromArrListStrings(favoritePlacesIDs));
        else dest.writeStringArray(null);
        dest.writeString(user_id);
    }

    private String[] arrFromArrListStrings(ArrayList<String> lst) {
        String [] arr = new String[lst.size()];
        int i=0;
        for (String s: lst) {
            arr[i] = s;
            i++;
        }
        return arr;
    }

    public boolean hasFavorites() {
        return favoritePlacesIDs != null && favoritePlacesIDs.size() > 0;
    }
}
