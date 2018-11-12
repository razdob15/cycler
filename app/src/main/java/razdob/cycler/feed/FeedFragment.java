package razdob.cycler.feed;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import razdob.cycler.R;
import razdob.cycler.adapters.FeedAdapter;
import razdob.cycler.models.Photo;
import razdob.cycler.models.User;

/**
 * Created by Raz on 09/08/2018, for project: PlacePicker2
 */
public class FeedFragment extends Fragment{
    private static final String TAG = "FeedFragment";
    private static String PHOTOS_BUNDLE = "photos";
    private Context mContext;

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private DatabaseReference mRef;
    /* ---------------------- FIREBASE ----------------------- */

    // GUI
    private TextView noFeedMessage;
    private RecyclerView recyclerView;
    private FeedAdapter mAdapter;

    // vars
    private ArrayList<Photo> photos;
    private HashMap<String, User> users;  // Keys - UserId, Values - User Object

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        noFeedMessage = view.findViewById(R.id.no_feed_message);
        mContext = getActivity();

        getPhotosFromBundle();

        initFirebase();

        return view;
    }

    private void initFirebase() {
        Log.d(TAG, "initFirebase: called.");
        mFireApp = FirebaseApp.getInstance("mFireApp");

        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();
        mRef.child(mContext.getString(R.string.db_persons)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> pastIds = new ArrayList<>();
                users = new HashMap<>();
                for (Photo photo: photos) {
                    String uid = photo.getUser_id();
                    if (dataSnapshot.hasChild(uid) && !pastIds.contains(uid)) {
                        users.put(uid, dataSnapshot.child(uid).getValue(User.class));
                        pastIds.add(uid);
                    }
                }
                setupAdapter();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: DBError: " + databaseError.getMessage());
            }
        });
    }

    private void getPhotosFromBundle() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            photos = bundle.getParcelableArrayList(mContext.getString(R.string.bundle_photos));
            Log.d(TAG, "getPhotosFromBundle: get photos: " + photos);
        } else {
            Log.d(TAG, "getPhotosFromBundle: bundle is null !");
        }
    }

    public static FeedFragment create(ArrayList<Photo> photos){
        FeedFragment fragment = new FeedFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(PHOTOS_BUNDLE, photos);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * If there is NO photos ->
     *   setup NoFeedMessage
     * Otherwise ->
     *   1) Sorts the photos list by time (later first).
     *   2) Dismisses the NoFeedMessage.
     *   3) Setups FeedAdapter (mAdapter) with the photos & users vars.
     */
    private void setupAdapter() {
        if (photos.size() == 0){
            Log.d(TAG, "setupAdapter: no feed !");
            noFeedMessage.setVisibility(View.VISIBLE);
        }
        else {
            sortPhotosByDate_laterFirst();

            noFeedMessage.setVisibility(View.GONE);

            mAdapter = new FeedAdapter(mContext, getActivity(), photos, users);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * Sort the photos-list by time (LATER FIRST).
     */
    private void sortPhotosByDate_laterFirst() {
        ArrayList<Photo> temp = new ArrayList<>();
        while (photos != null && photos.size() > 0) {
            Photo earPhoto = getEarliestPhoto(photos);
            temp.add(0, earPhoto);
            photos.remove(earPhoto);
        }
        photos = temp;
    }

    /**
     * Sort the photos-list by time (EARLIER FIRST).
     */
    private void sortPhotosByDate_earlierFirst() {
        ArrayList<Photo> temp = new ArrayList<>();
        while (photos != null && photos.size() > 0) {
            Photo earPhoto = getEarliestPhoto(photos);
            temp.add(earPhoto);
            photos.remove(earPhoto);
        }
        photos = temp;
    }


    /**
     * Gets two photos and returns the photo that was uploaded earlier.
     * @param p1 - Photo Object
     * @param p2 - Photo Object
     * @return The earlier photo between them.
     */
    private Photo earlyPhoto(Photo p1,Photo p2) {
        if (p1.equals(p2)) return p1;
        String d1 = p1.getDate_creates();
        String d2 = p2.getDate_creates();

        if (d1.length() > d2.length()) {
            return p1;
        } else if (d1.length() < d2.length()) {
            return p2;
        } else {
            for (int i=0; i < d2.length(); i++ ){
                if (d1.charAt(i) < d2.charAt(i))
                    return p1;
                if (d2.charAt(i) < d1.charAt(i))
                    return p2;
            }
        }
        return p1;
    }

    /**
     * Gets a PhotosArrayList and returns the photo in the list that was uploaded earlier.
     * @param photos - Photos ArrayList.
     * @return - The earliest photo in the list.
     */
    private Photo getEarliestPhoto(ArrayList<Photo> photos) {
        if (photos == null || photos.size() == 0) {
            return null;
        }
        Photo result = photos.get(0);
        for (int i=1; i< photos.size(); i++) {
            result = earlyPhoto(result, photos.get(i));
        }
        return result;
    }



}


