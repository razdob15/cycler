package razdob.cycler.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.places.Place;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Raz on 08/07/2018, for project: PlacePicker2
 */
public class PlaceDetails {
    private String id;
    private String name;
    private String address;
    private String phone;
    private String website;
    private Bitmap img;
    private ArrayList<String> tags;
    private boolean favorite;

    public PlaceDetails(String id) {
        this.id = id;
        favorite = false;
    }

    public PlaceDetails(Place place) {
        this.id = place.getId();
        this.name = place.getName().toString();
        if (place.getAddress() != null) this.address = place.getAddress().toString();
        if (place.getPhoneNumber() != null) this.phone = place.getPhoneNumber().toString();
        if (place.getWebsiteUri() != null) this.website = place.getWebsiteUri().toString();
        this.favorite = false;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap img) {
        this.img = img;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public String toString() {
        return "PlaceDetails{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", website='" + website + '\'' +
                ", img=" + img +
                '}';
    }

}