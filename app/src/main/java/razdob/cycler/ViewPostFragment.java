package razdob.cycler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import razdob.cycler.dialogs.CustomDialog;
import razdob.cycler.fivePlaces.ViewOnePlaceActivity;
import razdob.cycler.instProfile.InstProfileActivity;
import razdob.cycler.models.Photo;
import razdob.cycler.models.User;
import razdob.cycler.myUtils.BottomNavigationViewHelper;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.SquareImageView;
import razdob.cycler.myUtils.UniversalImageLoader;

/**
 * Created by Raz on 14/06/2018, for project: PlacePicker2
 */
public class ViewPostFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ViewPostFragment";

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: click");
        if (v != deleteTV && deleteTV.getVisibility() != View.GONE) {
            Log.d(TAG, "onClick: don't show deleteTV");
            deleteTV.setVisibility(View.GONE);
            mMoreIV.setVisibility(View.VISIBLE);
        }
    }

    public interface OnCommentThreadSelectedListener {
        void onCommentThreadSelectedListener(Photo photo);
    }

    OnCommentThreadSelectedListener mOnCommentThreadSelectedListener;

    public ViewPostFragment() {
        super();
        setArguments(new Bundle());
    }

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mRef;
    /* ---------------------- FIREBASE ----------------------- */

    // Widgets
    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationViewEx;
    private TextView mBackLabel, mCaption, mUserName, mTimeStamp, mLikesTV, mCommentsTV, deleteTV;
    private TextView placeNameTV;
    private ImageView mBackArrow, mMoreIV, mHeartRed, mBlankHeart, mProfileImage, mCommentIV;
    private ProgressBar postPB;

    // Vars
    private Context mContext;
    private Photo mPhoto;
    private int mActivityNumber = 0;
    private User mUser;
    private boolean mLikedByCurrentUser;
    private String mLikesString = "no likes yet";
    private ArrayList<String> favoritePlacesIds;
    private FirebaseMethods mFireMethods;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called.");
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        setupFirebaseStaff();

        mContext = getContext();

        mPostImage = view.findViewById(R.id.post_image);
        bottomNavigationViewEx = view.findViewById(R.id.bottomNavViewBar);
        mBackArrow = view.findViewById(R.id.back_arrow_iv);
        mBackLabel = view.findViewById(R.id.back_label_tv);
        mCaption = view.findViewById(R.id.caption_tv);
        mUserName = view.findViewById(R.id.user_name);
        mTimeStamp = view.findViewById(R.id.image_time_posted);
        mMoreIV = view.findViewById(R.id.post_more_iv);
        mHeartRed = view.findViewById(R.id.image_heart_red);
        mBlankHeart = view.findViewById(R.id.image_heart_blank);
        mProfileImage = view.findViewById(R.id.profile_image);
        mLikesTV = view.findViewById(R.id.image_likes_tv);
        mCommentIV = view.findViewById(R.id.comment_bubble);
        mCommentsTV = view.findViewById(R.id.image_comments_link);
        deleteTV = view.findViewById(R.id.delete_tv);
        postPB = view.findViewById(R.id.post_pb);
        placeNameTV = view.findViewById(R.id.place_name_tv);

        mFireMethods = new FirebaseMethods(mContext);

        try {
            UniversalImageLoader.setImage(getContext(), Objects.requireNonNull(getPhotoFromBundle()).getImage_path(), mPostImage, postPB, "");
            mActivityNumber = getActivityNumFromBundle();
            mPhoto = getPhotoFromBundle();

            if (mPhoto.isPlacePhoto()) {
                Log.d(TAG, "onCreateView: Place Photo");
                placeNameTV.setText(mPhoto.getPlace_name());
                placeNameTV.setVisibility(View.VISIBLE);
                placeNameTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewOnePlaceActivity.start(mContext, mPhoto.getPlace_id(), mActivityNumber);
                        }
                });
            } else {
                placeNameTV.setVisibility(View.GONE);
            }

            if (mPhoto.getPlace_id() == null) {
                Log.d(TAG, "onCreateView: not place for photo: " + mPhoto.getPhoto_id());
            } else  {
                Log.d(TAG, "onCreateView: PlacePhoto: " +mPhoto.getPhoto_id());


                // TODO(!) Delete this and add a attr to PhotoObjects - 'place_name' which will be added for a PlacePhotos only.
//                mGeoDataClient.getPlaceById(mPhoto.getPlace_id()).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
//                    @Override
//                    public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
//                        if (task.isSuccessful()) {
//                            PlaceBufferResponse places = task.getResult();
//                            @SuppressLint("RestrictedApi") Place place = places.get(0);
//                            String placeNameTV = place.getName().toString();
//
//                            Log.d(TAG, "onComplete: place_name: " + placeNameTV);
//
//                            placeNameTV.setText(placeNameTV);
//                            placeNameTV.setVisibility(View.VISIBLE);
//                        }
//                    }
//                });

            }

            Log.d(TAG, "onCreateView: incoming photo: " + mPhoto.toString());
            getLikesStringDB();


            userCanDelete(mPhoto.getUser_id());

            BottomNavigationViewHelper.setupBottomNavigationView(mContext, Objects.requireNonNull(getActivity()), mActivityNumber);



            if (mUser != null)
                setupWidgets();
            Log.d(TAG, "onCreateView: mPhotooooooo: " + mPhoto.toString());
            mLikedByCurrentUser = mPhoto.getLikes().contains(mAuth.getCurrentUser().getUid());


//            Query query = FirebaseDatabase.getInstance().getReference()
//                    .child(mContext.getString(R.string.db_photos))
//                    .orderByChild(mContext.getString(R.string.db_field_photo_id))
//                    .equalTo(photo_id);
//            query.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    for (DataSnapshot singleSnap: dataSnapshot.getChildren()) {
//                        Photo newPhoto = new Photo();
//                        Map<String , Object> objectMap = (HashMap<String, Object>) singleSnap.getValue();
//
//                        newPhoto.setCaption(objectMap.get(mContext.getString(R.string.db_field_caption)).toString());
//                        newPhoto.setTags(objectMap.get(mContext.getString(R.string.db_field_tags)).toString());
//                        newPhoto.setPhoto_id(objectMap.get(mContext.getString(R.string.db_field_photo_id)).toString());
//                        newPhoto.setUser_id(objectMap.get(mContext.getString(R.string.db_field_user_id)).toString());
//                        newPhoto.setDate_creates(objectMap.get(mContext.getString(R.string.db_field_date_creates)).toString());
//                        newPhoto.setImage_path(objectMap.get(mContext.getString(R.string.db_field_image_path)).toString());
//
//                        List<InstComment> commentList = new ArrayList<>();
//                        for (DataSnapshot ds: singleSnap
//                            .child(mContext.getString(R.string.db_field_comments)).getChildren()) {
//                            InstComment comment = new InstComment();
//                            comment.setUser_id(ds.getValue(InstComment.class).getUser_id());
//                            comment.setComment(ds.getValue(InstComment.class).getComment());
//                            comment.setDate_creates(ds.getValue(InstComment.class).getDate_creates());
//                            commentList.add(comment);
//                        }
//
//                        newPhoto.setComments(commentList);
//
//                        mPhoto = newPhoto;
//
//                        getCurrentUser();
//                        getPhotoDetails();
//                        //getLikesStringDB();
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });

        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: Photo was null from bundle" + e.getMessage());
        }


        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnCommentThreadSelectedListener = (OnCommentThreadSelectedListener) getActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }

    }

//    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
//        @Override
//        public boolean onDown(MotionEvent e) {
//            return true;
//        }
//
//        @Override
//        public boolean onDoubleTap(MotionEvent e) {
//            Log.d(TAG, "onDoubleTap: double touch detected");
//
//            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//            Query query = reference
//                    .child(mContext.getString(R.string.db_photos))
//                    .child(mPhoto.getPhoto_id())
//                    .child(mContext.getString(R.string.db_field_likes));
//            query.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    for (DataSnapshot singleSnap : dataSnapshot.getChildren()) {
//                        String like_id = singleSnap.getKey();
//                        // Case1: The user already liked the photo
//                        if (mLikedByCurrentUser &&
//                                singleSnap.getValue(Like.class).getUser_id()
//                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//                            // Remove the user's like
//                            removePhotoLike();
//                        }// Case2: The user has not liked the photo
//                        else if (!mLikedByCurrentUser) {
//                            // add new like
//                            addPhotoLike();
//                            break;
//                        }
//
//                    }
//                    if (!dataSnapshot.exists()) {
//                        // Add new Like
//                        addPhotoLike();
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//
//
//            return true;
//        }
//    }



    /**
     * Returns a string representing the number of days ago the post was uploaded.
     *
     * @return String
     */
    private String getTimeStampDifference() {
        // TODO(2): Make it more accurate
        Log.d(TAG, "getTimeStampDifference: getting timestamp difference");

        String difference;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss'z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Israel"));
        Date today = calendar.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = mPhoto.getDate_creates();
        try {
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24));
        } catch (ParseException e) {
            Log.e(TAG, "getTimeStampDifference: ParseException: " + e.getMessage());
            difference = "0";
        }

        return difference;
    }


    /*
     * ---------------------------- Bundle Details --------------------------------------
     */

    /**
     * retrieve the activity number from the incoming bundle from profileActivity interface
     *
     * @return Int
     */
    private int getActivityNumFromBundle() {
        Log.d(TAG, "getActivityNumFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getInt(mContext.getString(R.string.activity_number));
        } else {
            return 0;
        }

    }


    /**
     * retrieve the photo from the incoming bundle from profileActivity interface/
     *
     * @return Photo
     */
    private Photo getPhotoFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(mContext.getString(R.string.photo));
        } else {
            return null;
        }

    }


    /*
     * ------------------------------- Setups -------------------------------------------
     */
    private void setupWidgets() {
        if (mUser == null) return;
        Log.d(TAG, "setupWidgets: setup the widget according to photo: " + mPhoto.toString());
        String timestampDiff = getTimeStampDifference();
        if (!timestampDiff.equals("0")) {
            mTimeStamp.setText(timestampDiff + " DAYS AGO");
        } else {
            mTimeStamp.setText("TODAY");
        }
        UniversalImageLoader.setImage(getContext(), mUser.getProfile_photo(), mProfileImage, null, "");
        mUserName.setText(mUser.getName());
        mLikesTV.setText(mLikesString);
        if (mPhoto.getCaption() != null && mPhoto.getCaption().length() > 0) {
            mCaption.setText(mPhoto.getCaption());
            mCaption.setVisibility(View.VISIBLE);
        } else mCaption.setVisibility(View.GONE);

        if (mPhoto.getComments().size() > 0) {
            mCommentsTV.setText("View all " + mPhoto.getComments().size() + " comments");
            mCommentsTV.setVisibility(View.VISIBLE);
        } else {
            mCommentsTV.setText("");
            mCommentsTV.setVisibility(View.GONE);
        }

        mCommentsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to comments thread");

                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto);

            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStack();
            }
        });

        mCommentIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating ");
                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto);
            }
        });

        mPostImage.setOnClickListener(this);

        if (mLikedByCurrentUser) {
            mBlankHeart.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);
//            mHeartRed.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    Log.d(TAG, "onTouch: red heart touch detected");
//                    return mGestureDetector.onTouchEvent(event);
//                }
//            });

        } else {
            mBlankHeart.setVisibility(View.VISIBLE);
            mHeartRed.setVisibility(View.GONE);
//            mBlankHeart.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    Log.d(TAG, "onTouch: white heart touch detected.");
//                    return mGestureDetector.onTouchEvent(event);
//                }
//            });
        }

        mHeartRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHeartRed.setVisibility(View.GONE);
                mBlankHeart.setVisibility(View.VISIBLE);

                removePhotoLike();
                getLikesStringDB();
            }
        });

        mBlankHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBlankHeart.setVisibility(View.GONE);
                mHeartRed.setVisibility(View.VISIBLE);

                addPhotoLike();
                getLikesStringDB();
            }
        });




    }

    private void setupLikesString(String[] splitUsers) {
        int length = splitUsers != null ? splitUsers.length : 0;
        Log.d(TAG, "setupLikesString: likes_count: " + length);
        if (length == 1) {
            mLikesString = "Liked by " + splitUsers[0];
        } else if (length == 2) {
            mLikesString = "Liked by " + splitUsers[0]
                    + " and " + splitUsers[1];
        } else if (length == 3) {
            mLikesString = "Liked by " + splitUsers[0]
                    + ", " + splitUsers[1]
                    + " and " + splitUsers[2];

        } else if (length == 4) {
            mLikesString = "Liked by " + splitUsers[0]
                    + ", " + splitUsers[1]
                    + ", " + splitUsers[2]
                    + " and " + splitUsers[3];
        } else if (length > 4) {
            mLikesString = "Liked by " + splitUsers[0]
                    + ", " + splitUsers[1]
                    + ", " + splitUsers[2]
                    + " and " + (splitUsers.length - 3) + " others";
        } else {
            mLikesString = "no likes yet";
        }
        mLikesTV.setText(mLikesString);
    }

    private void userCanDelete(String uid) {
        if (!uid.equals(mAuth.getCurrentUser().getUid())) {
            Log.d(TAG, "userCanDelete: uid: " + uid);
            Log.d(TAG, "userCanDelete: AUTH: " + mAuth.getCurrentUser().getUid());
            mMoreIV.setOnClickListener(null);
            deleteTV.setOnClickListener(null);
            return;
        }
        Log.d(TAG, "userCanDelete: this is a user's photo :)");
        mMoreIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: open Options");
                deleteTV.setVisibility(View.VISIBLE);
            }
        });

        deleteTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhoto == null) {
                    Log.w(TAG, "onClick: can't delete. photo is NULL !");
                    return;
                }
                Log.d(TAG, "onClick: open 'delete photo' dialog for photo: " + mPhoto.getPhoto_id());
                final CustomDialog deleteDialog = CustomDialog.createDeleteDialog(mContext, mContext.getString(R.string.del_post_dialog_title),
                        mContext.getString(R.string.del_post_text));
                deleteDialog.setClick1(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "onClick: delete post: " + mPhoto.getPhoto_id());

                        String activityAfter;
                        if (Objects.requireNonNull(getActivity()).getClass().equals(InstProfileActivity.class)) {
                            activityAfter = mContext.getString(R.string.profile_activity);
                        } else {
                            activityAfter = mContext.getString(R.string.feed_activity);
                        }

                        mFireMethods.deletePhoto(mPhoto, activityAfter, null);
                        deleteDialog.dismiss();
                    }
                });
                deleteDialog.setClick2(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteDialog.dismiss();
                    }
                });
                deleteDialog.show();

            }
        });
    }

    /*
     * ------------------------------- Likes --------------------------------------------
     */
    private void getLikesStringDB() {
        Log.d(TAG, "getLikesStringDB: getting likes string");
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mPhoto.getLikes() != null && mPhoto.getLikes().size() > 0) {
                    Log.d(TAG, "onDataChange: loading mPhoto: "+mPhoto.getPhoto_id()+" likes.");
                    String[] splitUsers = new String[mPhoto.getLikes().size()];
                    int i = 0;
                    for (String likedUserId : mPhoto.getLikes()) {
                        if (likedUserId.equals(mAuth.getCurrentUser().getUid())) mLikedByCurrentUser = true;
                        String user_name = dataSnapshot.child(mContext.getString(R.string.db_user_account_settings))
                                .child(likedUserId).child(mContext.getString(R.string.db_field_username)).getValue(String.class);
                        if (user_name == null || user_name.length() == 0) {
                            user_name = dataSnapshot.child(mContext.getString(R.string.db_persons))
                                    .child(likedUserId).child(mContext.getString(R.string.db_field_name)).getValue(String.class);
                        }
                        splitUsers[i] = user_name;
                        i++;
                    }
                    setupLikesString(splitUsers);
                } else
                    setupLikesString(null);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: DBError: " + databaseError.getMessage());
            }
        });

//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//        Query query = reference
//                .child(mContext.getString(R.string.db_photos))
//                .child(mPhoto.getPhoto_id())
//                .child(mContext.getString(R.string.db_field_likes));
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                mUsers = new StringBuilder();
//                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
//
//                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//                    Query query = reference
//                            .child(mContext.getString(R.string.db_persons))
//                            .orderByChild(mContext.getString(R.string.db_field_user_id))
//                            .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
//                    query.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
//                                User tempUser = singleSnapshot.getValue(User.class);
//                                Log.d(TAG, "onDataChange: found like: " + tempUser.getName());
//
//                                if (tempUser.getUser_id().equals(mAuth.getCurrentUser().getUid())) { mLikedByCurrentUser = true; }
//
//                                mUsers.append(singleSnapshot.getValue(User.class).getName());
//                                mUsers.append(",");
//                            }
//
//                            String[] splitUsers = mUsers.toString().split(",");
//                            setupLikesString(splitUsers);
////                            mLikedByCurrentUser = mUsers.toString().contains(mCurrentUser.getName() + ",");
//
//                            Log.d(TAG, "onDataChange: likes string: " + mLikesString);
//                            setupWidgets();
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//
//                        }
//                    });
//                }
//                if (!dataSnapshot.exists()) {
//                    mLikedByCurrentUser = false;
//                    setupLikesString(null);
//                    setupWidgets();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

    }

    /**
     * 1) Adds a like to this post from the current user to DB:
     * a) 'photos' -> photo_id -> 'likes' -> like_id -> REMOVE
     * b) 'user_photos' -> user_id -> photo_id -> 'likes' -> like_id -> REMOVE
     * if photo is a place_photo:
     * c) 'places_photos' -> place_id -> photo_id -> 'likes' -> like_id -> REMOVE
     * 2) updates the heart color.
     *
     */
    private void removePhotoLike() {
        Log.d(TAG, "removePhotoLike: remove the like from photo: "+ mPhoto.getPhoto_id());

        mFireMethods.unlikePhotoDB(mPhoto);

        mBlankHeart.setVisibility(View.VISIBLE);
        mHeartRed.setVisibility(View.GONE);
//        String like_id = like.getLike_id();
//        mRef.child(mContext.getString(R.string.db_user_photos))
//                .child(mPhoto.getUser_id())
//                .child(mPhoto.getPhoto_id())
//                .child(mContext.getString(R.string.db_field_likes))
//                .child(like_id)
//                .setValue(null);
//        String place_id = mPhoto.getPlace_id();
//        if (place_id != null) {
//            Log.d(TAG, "removePhotoLike: from a place photo: " + place_id);
//            mRef.child(mContext.getString(R.string.db_places_photos))
//                    .child(place_id)
//                    .child(mPhoto.getPhoto_id())
//                    .child(mContext.getString(R.string.db_field_likes))
//                    .child(like_id)
//                    .setValue(null);
//        } else{
//
//            mRef.child(mContext.getString(R.string.db_photos))
//                    .child(mPhoto.getPhoto_id())
//                    .child(mContext.getString(R.string.db_field_likes))
//                    .child(like_id)
//                    .setValue(null);
//        }
//        mLikedByCurrentUser = false;
//        mHeartUtil.toggleLike();
//        if (mPhoto.getLikes() != null && mPhoto.getLikes().size() > 1) {
//            mPhoto.getLikes().remove(like);
//        } else {
//            mPhoto.setLikes(new ArrayList<Like>());
//        }
//        getLikesStringDB();
    }

    private void addPhotoLike() {
        Log.d(TAG, "addPhotoLike: adding new like to photo: " + mPhoto.getPhoto_id());

        mFireMethods.likePhotoDB(mPhoto);

        mBlankHeart.setVisibility(View.GONE);
        mHeartRed.setVisibility(View.VISIBLE);

//        String newLikeId = mRef.push().getKey();
//        Like like = new Like();
//        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
//
//
//
//        mRef.child(mContext.getString(R.string.db_user_photos))
//                .child(mPhoto.getUser_id())
//                .child(mPhoto.getPhoto_id())
//                .child(mContext.getString(R.string.db_field_likes))
//                .child(newLikeId)
//                .setValue(like);
//
//        String place_id = mPhoto.getPlace_id();
//        if (place_id != null) {
//            Log.d(TAG, "addPhotoLike: to a place photo: " + place_id);
//            mRef.child(mContext.getString(R.string.db_places_photos))
//                    .child(place_id)
//                    .child(mPhoto.getPhoto_id())
//                    .child(mContext.getString(R.string.db_field_likes))
//                    .child(newLikeId)
//                    .setValue(like);
//        } else {
//            mRef.child(mContext.getString(R.string.db_photos))
//                    .child(mPhoto.getPhoto_id())
//                    .child(mContext.getString(R.string.db_field_likes))
//                    .child(newLikeId)
//                    .setValue(like);
//        }
//        mLikedByCurrentUser = true;
//        mHeartUtil.toggleLike();
//        if (mPhoto.getLikes() != null) mPhoto.getLikes().add(like);
//        else {
//            ArrayList<Like> likes = new ArrayList<>();
//            likes.add(like);
//            mPhoto.setLikes(likes);
//        }
//        getLikesStringDB();
    }



    /*
     * ----------------------------- Firebase ------------------------------------------
     */

    private void setupFirebaseStaff() {
        Log.d(TAG, "setupFirebaseStaff: called.");

        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();

        // Init mAuthStateListener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                // Check if user is logged in

                if (user == null) {
                    Log.d(TAG, "onAuthStateChanged: User log-out");
                    startActivity(new Intent(mContext, MainRegisterActivity.class));
                } else {
                    Log.d(TAG, "onAuthStateChanged: User is logged-in :) uid = " + firebaseAuth.getCurrentUser().getUid());
                }
            }
        };

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mPhoto != null)
                    mUser = dataSnapshot.child(mContext.getString(R.string.db_persons))
                            .child(mPhoto.getUser_id()).getValue(User.class);

                favoritePlacesIds = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.child(mContext.getString(R.string.db_persons))
                        .child(mAuth.getCurrentUser().getUid())
                        .child(mContext.getString(R.string.db_field_favorite_places_ids))
                        .getChildren()) {
                    favoritePlacesIds.add(ds.getValue(String.class));
                }
                setupWidgets();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: DBError"  + databaseError.getMessage());
            }
        });


    }



    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAuthStateListener != null)
            mAuth.removeAuthStateListener(mAuthStateListener);
    }

}



