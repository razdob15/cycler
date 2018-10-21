package razdob.cycler.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import razdob.cycler.R;
import razdob.cycler.instHome.InstHomeActivity;
import razdob.cycler.instProfile.InstProfileActivity;
import razdob.cycler.models.Comment;
import razdob.cycler.models.InstComment;
import razdob.cycler.models.Like;
import razdob.cycler.models.Photo;
import razdob.cycler.models.User;
import razdob.cycler.models.UserAccountSettings;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.HeartUtil;
import razdob.cycler.myUtils.SquareImageView;

/**
 * Created by Raz on 21/06/2018, for project: PlacePicker2
 */
public class MainFeedListAdapter extends ArrayAdapter<Photo> {
    private static final String TAG = "MainFeedListAdapter";

    public interface OnLoadMoreItemsListener {
        void onLoadMoreItems();
    }
    OnLoadMoreItemsListener mOnLoadMoreItemsListener;

    private LayoutInflater mInflater;
    private int mLayoutResource;
    private Context mContext;

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    /* ---------------------- FIREBASE ----------------------- */

    private String currentUserName = "";
    private FirebaseMethods mFireMethods;

    public MainFeedListAdapter(@NonNull Context context, int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;

        initFirebase();

        mFireMethods = new FirebaseMethods(mContext);
        mLayoutResource = resource;
    }

    private void initFirebase() {
        Log.d(TAG, "initFirebase: called.");

        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();
    }

    static class ViewHolder {
        CircleImageView mProfileImage;
        TextView commentsTV, userNameTv, timestampTV, likesTV, caption;
        SquareImageView squareImageView;
        ImageView redHeartIV, whiteHeartIV, commentIV;

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        StringBuilder users;
        String likeString;
        boolean likeByCurrentUser;
        HeartUtil heartUtil;
        GestureDetector detector;
        Photo photo;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null || true) {
            convertView = mInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();
            holder.userNameTv = convertView.findViewById(R.id.user_name);
            holder.squareImageView = convertView.findViewById(R.id.post_image);
            holder.redHeartIV = convertView.findViewById(R.id.image_heart_red);
            holder.whiteHeartIV = convertView.findViewById(R.id.image_heart_blank);
            holder.commentIV = convertView.findViewById(R.id.comment_bubble);
            holder.likesTV = convertView.findViewById(R.id.image_likes_tv);
            holder.commentsTV = convertView.findViewById(R.id.image_comments_link);
            holder.caption = convertView.findViewById(R.id.caption_tv);
            holder.timestampTV = convertView.findViewById(R.id.image_time_posted);
            holder.mProfileImage = convertView.findViewById(R.id.profile_image);
            holder.heartUtil = new HeartUtil(holder.whiteHeartIV, holder.redHeartIV);
            holder.photo = getItem(position);
            holder.detector = new GestureDetector(mContext, new GestureListener(holder));
            holder.users = new StringBuilder();

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the current users userName (Need for checking likes string)
        getCurrentUserName();

        // get likes String
        getLikesString(holder);
        // Set the caption
        holder.caption.setText(getItem(position).getCaption());

        // Set the comment
        List<InstComment> comments = getItem(position).getComments();
        if (comments.size() > 0) {
            holder.commentsTV.setText("View all " + comments.size() + " comments ");
            holder.commentsTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: loading comment thread for: " + getItem(position).getPhoto_id());
                    ((InstHomeActivity) mContext).onCommentThreadSelected(getItem(position),
                            mContext.getString(R.string.home_activity));

                    // GOING TO NEED TO DO???>
                    ((InstHomeActivity)mContext).hideLayout();

                }
            });
        }
        else holder.commentsTV.setText("No Comments");


        // Set the time it was posted
        String timestampDiff = getTimeStampDifference(getItem(position));
        // TODO(2): Show also 'h' OR 'm' OR 'y'...
        if (!timestampDiff.equals("0")) {
            holder.timestampTV.setText(timestampDiff + " DAYS AGO");
        } else {
            holder.timestampTV.setText("TODAY");
        }

        // Set the profile image
        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(), holder.squareImageView);

        // Get the userName and the UserAccountSettings

        Query query = mRef
                .child(mContext.getString(R.string.db_user_account_settings))
                .orderByChild(mContext.getString(R.string.db_field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot singleSnap : dataSnapshot.getChildren()) {

//                    currentUserName = singleSnap.getValue(User.class).getName();
                    Log.d(TAG, "onDataChange: found user: " + singleSnap.getValue(UserAccountSettings.class).getUserName());

                    holder.userNameTv.setText((singleSnap.getValue(UserAccountSettings.class).getUserName()));
                    holder.userNameTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of: " +
                                    holder.user.getName());

                            Intent intent = new Intent(mContext, InstProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);
                        }
                    });
                    holder.settings = singleSnap.getValue(UserAccountSettings.class);
                    holder.commentIV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((InstHomeActivity) mContext).onCommentThreadSelected(getItem(position),
                                    mContext.getString(R.string.home_activity));

                            // another thing?
                            ((InstHomeActivity)mContext).hideLayout();
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Get the Profile Image
        Query userQuery = mRef
                .child(mContext.getString(R.string.db_persons))
                .orderByChild(mContext.getString(R.string.db_field_user_id))
                .equalTo(getItem(position).getUser_id());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)     {
                for (final DataSnapshot singleSnap : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user: " + singleSnap.getValue(User.class).getName());

                    holder.user = singleSnap.getValue(User.class);

                    imageLoader.displayImage(singleSnap.getValue(User.class).getProfile_photo(),
                            holder.mProfileImage);

                    holder.mProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of: " +
                                    holder.user.getName());

                            Intent intent = new Intent(mContext, InstProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: DBError: " + databaseError.getMessage());
            }
        });

        if (reachedEndOfList(position)) {
            loadMoreDate();
        }



        return convertView;
    }

    private boolean reachedEndOfList(int position) {
        return position == getCount() - 1;
    }
    private void loadMoreDate() {
        try {
            mOnLoadMoreItemsListener = (OnLoadMoreItemsListener) getContext();
        } catch (ClassCastException e) {
            Log.e(TAG, "loadMoreDate: ClassCastException: " +e.getMessage());
        }

        try {
            mOnLoadMoreItemsListener.onLoadMoreItems();
        } catch (NullPointerException e) {
            Log.e(TAG, "loadMoreDate: NullPointerException: " +e.getMessage());
        }

    }


    public class GestureListener extends GestureDetector.SimpleOnGestureListener {

        ViewHolder mHolder;

        public GestureListener(ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: double touch detected");

            Query query = mRef
                    .child(mContext.getString(R.string.db_photos))
                    .child(mHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.db_field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnap : dataSnapshot.getChildren()) {
                        String keyId = singleSnap.getKey();
                        // Case1: then user already liked the photo
                        if (mHolder.likeByCurrentUser &&
                                singleSnap.getValue(Like.class).getUser_id()
                                        .equals(mAuth.getCurrentUser().getUid())) {

                            mRef.child(mContext.getString(R.string.db_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.db_field_likes))
                                    .child(keyId)
                                    .removeValue();

                            mRef.child(mContext.getString(R.string.db_user_photos))
                                    .child(mHolder.photo.getUser_id())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.db_field_likes))
                                    .child(keyId)
                                    .removeValue();

                            mHolder.heartUtil.toggleLike();
                            getLikesString(mHolder);
                        }// Case2: The user has not liked the photo
                        else if (!mHolder.likeByCurrentUser) {
                            // add new like
                            addNewLike(mHolder);
                            break;
                        }

                    }
                    if (!dataSnapshot.exists()) {
                        // Add new Like
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return true;
        }
    }

    private void addNewLike(ViewHolder holder) {
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeId = mRef.push().getKey();
        Like like = new Like();
        like.setUser_id(mAuth.getCurrentUser().getUid());

        mRef.child(mContext.getString(R.string.db_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.db_field_likes))
                .child(newLikeId)
                .setValue(like);

        mRef.child(mContext.getString(R.string.db_user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.db_field_likes))
                .child(newLikeId)
                .setValue(like);

        holder.heartUtil.toggleLike();
        getLikesString(holder);
    }

    private void getCurrentUserName() {
        Log.d(TAG, "getCurrentUserName: retrieving user account settings");
        Query query = mRef
                .child(mContext.getString(R.string.db_persons))
                .orderByChild(mContext.getString(R.string.db_field_user_id))
                .equalTo(mAuth.getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnap : dataSnapshot.getChildren()) {
                    currentUserName = singleSnap.getValue(User.class).getName();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void getLikesString(final ViewHolder holder) {
        Log.d(TAG, "getLikesString: getting likes string");

        try {

            Query query = mRef
                    .child(mContext.getString(R.string.db_photos))
                    .child(holder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.db_field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    holder.users = new StringBuilder();
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                        Query query = mRef
                                .child(mContext.getString(R.string.db_persons))
                                .orderByChild(mContext.getString(R.string.db_field_user_id))
                                .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    Log.d(TAG, "onDataChange: found like: " +
                                            singleSnapshot.getValue(User.class).getName());

                                    holder.users.append(singleSnapshot.getValue(User.class).getName());
                                    holder.users.append(",");
                                }

                                String[] splitUsers = holder.users.toString().split(",");

                                holder.likeByCurrentUser = holder.users.toString().contains(currentUserName + ",");

                                int length = splitUsers.length;
                                if (length == 0) {
                                    holder.likeString = "No likes yet";
                                } else if (length == 1) {
                                    holder.likeString = "Liked by " + splitUsers[0];
                                } else if (length == 2) {
                                    holder.likeString = "Liked by " + splitUsers[0]
                                            + " and " + splitUsers[1];
                                } else if (length == 3) {
                                    holder.likeString = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1]
                                            + " and " + splitUsers[2];

                                } else if (length == 4) {
                                    holder.likeString = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1]
                                            + ", " + splitUsers[2]
                                            + " and " + splitUsers[3];
                                } else {
                                    holder.likeString = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1]
                                            + ", " + splitUsers[2]
                                            + " and " + (splitUsers.length - 3) + " others";
                                }
                                Log.d(TAG, "onDataChange: likes string: " + holder.likeString);
                                //setup likes string
                                setupLikesString(holder, holder.likeString);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    if (!dataSnapshot.exists()) {
                        holder.likeString = "";
                        holder.likeByCurrentUser = false;
                        //setup likes string
                        setupLikesString(holder, holder.likeString);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        } catch (NullPointerException e) {
            Log.e(TAG, "getLikesString: NullPointerException: " + e.getMessage());
            holder.likeString = "";
            holder.likeByCurrentUser = false;
            // setup likes string
            setupLikesString(holder, holder.likeString);
        }
    }

    private void setupLikesString(final ViewHolder holder, String likesString) {
        Log.d(TAG, "setupLikesString: likes string: " + holder.likeString);

        if (holder.likeByCurrentUser) {
            Log.d(TAG, "setupLikesString: photo is liked by current user");
            holder.whiteHeartIV.setVisibility(View.GONE);
            holder.redHeartIV.setVisibility(View.VISIBLE);
            holder.redHeartIV.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });

        } else {
            Log.d(TAG, "setupLikesString: photo is not liked by current user");
            holder.whiteHeartIV.setVisibility(View.VISIBLE);
            holder.redHeartIV.setVisibility(View.GONE);
            holder.whiteHeartIV.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        if (likesString.length() == 0) likesString = "No Likes yet";
        holder.likesTV.setText(likesString);
    }

    /**
     * Returns a string representing the number of days ago the post was uploaded.
     *
     * @return String
     */
    private String getTimeStampDifference(Photo photo) {
        Log.d(TAG, "getTimeStampDifference: getting timestamp difference");

        String difference;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss'z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Israel"));
        Date today = calendar.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = photo.getDate_creates();
        try {
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24));
        } catch (ParseException e) {
            Log.e(TAG, "getTimeStampDifference: ParseException: " + e.getMessage());
            difference = "0";
        }

        return difference;
    }


}
