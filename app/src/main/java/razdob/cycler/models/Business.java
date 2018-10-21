package razdob.cycler.models;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Raz on 23/03/2018, for project: PlacePicker2
 */

public class Business implements Serializable, Place {

    private String name, address, logoImgUrl, businessMail;
    private String phone;
    private HashMap<String, Boolean> tags;
    private String aboutBusiness;
    private String type;  // "person" OR "business"
    private LatLng latLng;
    private String uid;

    public Business() {
        this.name = "ANONYMOUS_BUSINESS";
    }

    public Business(String name, String type, LatLng latLng, String uid,
                    @Nullable String address, @Nullable String logoImgUrl, @Nullable String phone, @Nullable String businessMail,
                    @Nullable HashMap<String, Boolean> tags, @Nullable String aboutBusiness) {
        this.name = name;
        this.address = address;
        this.logoImgUrl = logoImgUrl;
        this.phone = phone;
        this.tags = tags;
        this.aboutBusiness = aboutBusiness;
        this.type = type;
        this.latLng = latLng;
        this.businessMail = businessMail;
        this.uid = uid;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getBusinessMail() {return businessMail;}

    public void setBusinessMail(String businessMail) {this.businessMail = businessMail;}

    @Override
    public CharSequence getName() {
        return name;

    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public List<Integer> getPlaceTypes() {
        return null;
    }

    @Override
    public CharSequence getAddress() {
        return this.address;
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLogoImgUrl() {
        return logoImgUrl;
    }

    public void setLogoImgUrl(String logoImgUrl) {
        this.logoImgUrl = logoImgUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public HashMap<String, Boolean> getTags() {
        return tags;
    }

    public void setTags(HashMap<String, Boolean> tags) {
        this.tags = tags;
    }

    public String getAboutBusiness() {
        return aboutBusiness;
    }

    public void setAboutBusiness(String aboutBusiness) {
        this.aboutBusiness = aboutBusiness;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    @Override
    public LatLngBounds getViewport() {
        return null;
    }

    @Override
    public Uri getWebsiteUri() {
        return null;
    }

    @Override
    public CharSequence getPhoneNumber() {
        return phone;
    }

    @Override
    public float getRating() {
        return 0;
    }

    @Override
    public int getPriceLevel() {
        return 0;
    }

    @Override
    public CharSequence getAttributions() {
        return null;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    @Override
    public Place freeze() {
        return this;
    }

    @Override
    public boolean isDataValid() {
        return false;
    }





}
