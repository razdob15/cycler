package razdob.cycler.instHome;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import razdob.cycler.R;
import razdob.cycler.models.Photo;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.adapters.MainFeedListAdapter;

/**
 * Created by Raz on 28/05/2018, for project: PlacePicker2
 */
public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    // Vars
    private ArrayList<Photo> mPhotos;
    private ArrayList<Photo> mPaginatedPhotos;
    private ArrayList<String> mFollowing;
    private ListView mListView;
    private MainFeedListAdapter mAdapter;
    private int mRelults;
    private Context mContext;

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private FirebaseMethods mFireMethods;
    /* ---------------------- FIREBASE ----------------------- */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmant_inst_home, container, false);
        mListView = view.findViewById(R.id.list_view);
        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();
        mContext = getActivity();
        mFireMethods = new FirebaseMethods(mContext);

        initFirebase();

        getFollowing();


        return view;
    }

    private void initFirebase() {
        Log.d(TAG, "initFirebase: called.");
        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();
    }

    private void getFollowing() {
        Log.d(TAG, "getFollowing: Searching for following");


        Query query = mRef
                .child(getString(R.string.db_field_following))
                .child(mAuth.getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnap : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user: " +
                            singleSnap.child(getString(R.string.db_field_user_id)).getValue());

                    mFollowing.add(singleSnap.child(getString(R.string.db_field_user_id)).getValue().toString());
                }
                mFollowing.add(mAuth.getCurrentUser().getUid());
                //get the photos
                getPhotos();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPhotos() {
        Log.d(TAG, "getPhotos: getting photos");


        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int counter = 0;
                for (DataSnapshot ds : dataSnapshot
                        .child(mContext.getString(R.string.db_user_photos)).getChildren()) {
                    if (mFollowing.contains(ds.getKey()) ||
                            ds.getKey().equals(mAuth.getCurrentUser().getUid())) {
                        for (DataSnapshot photoDS : ds.getChildren()) {
                            Photo photo = getPhotoFromDS(photoDS);
                            Log.d(TAG, "onDataChange: find a photo: User: " + photo.getUser_id() + ", Photo: " + photo.getPhoto_id());

                            mPhotos.add(photo);
                        }
                        counter++;
                        if (counter == mFollowing.size() + 1) break;
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: DBError: " + databaseError.getMessage());
            }
        });

//        for (int i = 0; i < mFollowing.size(); i++) {
//            final int count = i;
//            Query query = reference
//                    .child(getString(R.string.db_user_photos))
//                    .child(mFollowing.get(i))
//                    .orderByChild(getString(R.string.db_field_user_id))
//                    .equalTo(mFollowing.get(i));
//            query.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    for (DataSnapshot singleSnap : dataSnapshot.getChildren()) {
//                        Photo photo = new Photo();
//                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnap.getValue();
//
//                        photo.setCaption(objectMap.get(getString(R.string.db_field_caption)).toString());
//                        photo.setTags(objectMap.get(getString(R.string.db_field_tags)).toString());
//                        photo.setPhoto_id(objectMap.get(getString(R.string.db_field_photo_id)).toString());
//                        photo.setUser_id(objectMap.get(getString(R.string.db_field_user_id)).toString());
//                        photo.setDate_creates(objectMap.get(getString(R.string.db_field_date_creates)).toString());
//                        photo.setImage_path(objectMap.get(getString(R.string.db_field_image_path)).toString());
//
//                        ArrayList<InstComment> comments = new ArrayList<>();
//                        for (DataSnapshot ds : singleSnap.child(getString(R.string.db_field_comments)).getChildren()) {
//                            InstComment comment = new InstComment();
//                            comment.setUser_id(ds.getValue(InstComment.class).getUser_id());
//                            comment.setComment(ds.getValue(InstComment.class).getComment());
//                            comment.setDate_creates(ds.getValue(InstComment.class).getDate_creates());
//                            comments.add(comment);
//                        }
//
//                        photo.setComments(comments);
//
//                        mPhotos.add(photo);
//                    }
//                    if (count >= mFollowing.size() - 1) {
//                        // Display our photos
//                        displayPhotos();
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        }
    }

    /**
     * take the photo as a HashMap<String, Object> and return the photo that is contains.
     * @param photoDS - DataSnapshot contains HashMap<String, Object> the representing a photoObject.
     * @return  - Photo Object.
     */
    private Photo getPhotoFromDS(DataSnapshot photoDS) {
        Photo photo = photo = mFireMethods.getPhotoFromDB(photoDS);
//        Map<String, Object> objectMap = (HashMap<String, Object>) photoDS.getValue();
//
//        photo.setCaption(objectMap.get(getString(R.string.db_field_caption)).toString());
//        photo.setTags(objectMap.get(getString(R.string.db_field_tags)).toString());
//        photo.setPhoto_id(objectMap.get(getString(R.string.db_field_photo_id)).toString());
//        photo.setUser_id(objectMap.get(getString(R.string.db_field_user_id)).toString());
//        photo.setDate_creates(objectMap.get(getString(R.string.db_field_date_creates)).toString());
//        photo.setImage_path(objectMap.get(getString(R.string.db_field_image_path)).toString());
//
//        ArrayList<InstComment> comments = new ArrayList<>();
//        for (DataSnapshot ds : photoDS.child(getString(R.string.db_field_comments)).getChildren()) {
//            InstComment comment = new InstComment(ds.getKey());
//            comment.setUser_id(ds.getValue(InstComment.class).getUser_id());
//            comment.setComment(ds.getValue(InstComment.class).getComment());
//            comment.setDate_creates(ds.getValue(InstComment.class).getDate_creates());
//            comments.add(comment);
//        }
//
//        photo.setComments(comments);
        return photo;
    }

    private void displayPhotos() {
        mPaginatedPhotos = new ArrayList<>();
        if (mPhotos != null) {
            try {
                Collections.sort(mPhotos, new Comparator<Photo>() {
                    @Override
                    public int compare(Photo o1, Photo o2) {
                        return o2.getDate_creates().compareTo(o1.getDate_creates());
                    }
                });
                int iterations = mPhotos.size();
                if (iterations > 10) iterations = 10;
                mRelults = 0;
                for (int i = 0; i < iterations; i++) {
                    mPaginatedPhotos.add(mPhotos.get(i));
                }


                mAdapter = new MainFeedListAdapter(getActivity(), R.layout.layout_mainfeed_list_item, mPaginatedPhotos);
                mListView.setAdapter(mAdapter);


            } catch (NullPointerException e) {
                Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage());
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage());
            }

        }
    }

    public void displayMorePhotos() {
        Log.d(TAG, "displayMorePhotos: displaying more photos");

        try {
            if (mPhotos.size() > mRelults && mPhotos.size() > 0) {
                int iterations;
                if (mPhotos.size() > (mRelults + 10)) {
                    Log.d(TAG, "displayMorePhotos: there are greater than 10 more photos");
                    iterations = 10;
                } else {
                    Log.d(TAG, "displayMorePhotos: there are less than 10 more photos");
                    iterations = mPhotos.size() - mRelults;
                }

                // Add the new photos to the paginated result
                for (int i = mRelults; i < mRelults + iterations; i++) {
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mRelults = mRelults + iterations;
                mAdapter.notifyDataSetChanged();
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage());
        }


    }


}
