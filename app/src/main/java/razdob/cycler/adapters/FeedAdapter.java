package razdob.cycler.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import razdob.cycler.MainRegisterActivity;
import razdob.cycler.R;
import razdob.cycler.UsersListActivity;
import razdob.cycler.dialogs.DeleteDialog;
import razdob.cycler.feed.HomeActivity;
import razdob.cycler.instProfile.InstProfileActivity;
import razdob.cycler.models.Photo;
import razdob.cycler.models.User;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.MyFonts;
import razdob.cycler.myUtils.RazUtils;
import razdob.cycler.myUtils.SquareImageView;
import razdob.cycler.myUtils.UniversalImageLoader;
import razdob.cycler.myUtils.ViewInstCommentsFragment;


public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {
    private static final String TAG = "FeedAdapter";

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private String mUid;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    /* ---------------------- FIREBASE ----------------------- */

    // vars
    private Context mContext;
    private ArrayList<Photo> photos;
    private HashMap<String, User> users;
    private FirebaseMethods mFireMethods;
    private FragmentActivity fragmentActivity;
    private ArrayList<Boolean> userCanDeletePost;

    // Constructor
    public FeedAdapter(Context context, FragmentActivity fragmentActivity, ArrayList<Photo> photos, HashMap<String, User> users) {
        this.mContext = context;
        setupFirebaseStaff();
        this.userCanDeletePost = new ArrayList<>();
        this.photos = photos;
        for (Photo photo : photos) {
            if (photo.getUser_id().equals(mUid)) {
                userCanDeletePost.add(true);
            } else {
                userCanDeletePost.add(false);
            }
        }

        this.users = users;
        this.fragmentActivity = fragmentActivity;


        mFireMethods = new FirebaseMethods(mContext);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called.");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_mainfeed_list_item, parent, false);
        return new FeedAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        final Photo photo = photos.get(holder.getAdapterPosition());
        final User uploadUser = users.get(photo.getUser_id());

        setupRate(photo, holder);
        setupPlaceName(photo, holder);

        UniversalImageLoader.setImage(mContext, uploadUser.getProfile_photo(), holder.profileIV, null, "");
        UniversalImageLoader.setImage(mContext, photo.getImage_path(), holder.postIV, holder.postPB, "");
        holder.userNameTV.setText(uploadUser.getName());

        final String timestampDiff = getTimeStampDifference(photo.getDate_creates());
        if (!timestampDiff.equals("0")) {
            holder.timePostedTV.setText(timestampDiff + " DAYS AGO");
        } else {
            holder.timePostedTV.setText("TODAY");
        }
        if (photo.getCaption() != null && photo.getCaption().length() > 0) {
            holder.captionTV.setText(photo.getCaption());
            holder.captionTV.setVisibility(View.VISIBLE);
        } else holder.captionTV.setVisibility(View.GONE);

        if (photo.getComments().size() > 0) {
            holder.commentsTV.setText("View all " + photo.getComments().size() + " comments");
            holder.commentsTV.setVisibility(View.VISIBLE);
        } else {
            holder.commentsTV.setText("");
            holder.commentsTV.setVisibility(View.GONE);
        }
        setupLikesTV(photo, holder);
//        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (photo.getLikes() != null && photo.getLikes().size() > 0) {
//                    Log.d(TAG, "onDataChange: loading photo: "+photo.getPhoto_id()+" likes.");
//                    String[] splitUsers = new String[photo.getLikes().size()];
//                    int i = 0;
//                    for (Like like : photo.getLikes()) {
//                        if (like.getUser_id().equals(mAuth.getCurrentUser().getUid())) {
//                            guiLikePost(holder);
//                            myLike = like;
//                        }
//                        String user_name = dataSnapshot.child(mContext.getString(R.string.db_user_account_settings))
//                                .child(like.getUser_id()).child(mContext.getString(R.string.db_field_display_name)).getValue(String.class);
//                        if (user_name == null || user_name.length() == 0) {
//                            user_name = dataSnapshot.child(mContext.getString(R.string.db_persons))
//                                    .child(like.getUser_id()).child(mContext.getString(R.string.db_field_username)).getValue(String.class);
//                        }
//                        splitUsers[i] = user_name;
//                        i++;
//                    }
//                    setupLikesTV(photo, holder);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.d(TAG, "onCancelled: DBError: " + databaseError.getMessage());
//            }
//        });

        // Clicks:
        holder.userNameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigate to user-profile: " + uploadUser.getUser_id());
                Intent intent = new Intent(mContext, InstProfileActivity.class);
                intent.putExtra(mContext.getString(R.string.calling_activity), mContext.getString(R.string.feed_activity));
                intent.putExtra(mContext.getString(R.string.intent_user), uploadUser);
                mContext.startActivity(intent);
            }
        });
        holder.profileIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Big profile photo.");
                UniversalImageLoader.bigPhoto(mContext, fragmentActivity, uploadUser.getProfile_photo());
            }
        });
        holder.blankHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: like post");

                guiLikePost(holder);
                addPhotoLikeDB(photo);
                setupLikesTV(photo, holder);
            }
        });
        holder.redHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: like post");
                if (photo.getLikes().contains(mUid))
                    photo.getLikes().remove(mUid);

                guiUnlikePost(holder);
                removePhotoLikeDB(photo);
                setupLikesTV(photo, holder);
            }
        });
        holder.likesTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (photo.getLikes() != null && photo.getLikes().size() > 0) {
                    Log.d(TAG, "onClick: navigate to UsersListActivity with the likers");
                    Intent intent = new Intent(mContext, UsersListActivity.class);
                    intent.putExtra(mContext.getString(R.string.intent_title), "LIKES");

                    ArrayList<String> likedUsersIds = new ArrayList<>(photo.getLikes());

                    intent.putExtra(mContext.getString(R.string.intent_users), likedUsersIds);
                    mContext.startActivity(intent);
                } else {
                    Log.d(TAG, "onClick: NO LIKES");
                }
            }
        });
        holder.moreIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userCanDeletePost.get(holder.getAdapterPosition())) {
                    holder.moreIV.setVisibility(View.GONE);
                    holder.deleteTv.setVisibility(View.VISIBLE);
                }
            }
        });
        holder.deleteTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: open 'delete photo' dialog for photo: " + photo.getPhoto_id());
                final DeleteDialog deleteDialog = new DeleteDialog(mContext, mContext.getString(R.string.del_post_text));
                deleteDialog.setYesClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "onClick: delete post: " + photo.getPhoto_id());
                        HashMap<String, String> extras = new HashMap<>();
                        extras.put(mContext.getString(R.string.return_to_fragment), mContext.getString(R.string.feed_fragment));
                        mFireMethods.deletePhoto(photo, mContext.getString(R.string.home_activity), extras);
                        photos.remove(photo);
                        deleteDialog.dismiss();


//                        notifyDataSetChanged();  TODO: check without this line

                    }
                });
                deleteDialog.setNoClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteDialog.dismiss();
                    }
                });
                deleteDialog.show();
            }
        });

        holder.postIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.deleteTv.setVisibility(View.GONE);
                holder.moreIV.setVisibility(View.VISIBLE);
            }
        });
        View.OnClickListener commentClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: show comments for photo: " + photo.getPhoto_id());
                ViewInstCommentsFragment fragment = new ViewInstCommentsFragment();
                Bundle args = new Bundle();
                args.putParcelable(mContext.getString(R.string.bundle_photo), photo);
                fragment.setArguments(args);

                FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, fragment);
                transaction.addToBackStack(mContext.getString(R.string.view_inst_comment_fragment));
                transaction.commit();
            }
        };

        holder.commentsTV.setOnClickListener(commentClick);
        holder.commentBubble.setOnClickListener(commentClick);

    }

    /**
     * Shows the place name if needed and add a click listener for it.
     * @param photo -  Current Photo.
     * @param holder - Current Holder.
     */
    private void setupPlaceName(final Photo photo, ViewHolder holder) {
        if (photo.isPlacePhoto()) {
            Log.d(TAG, "onCreateView: Place Photo");
            holder.placeNameTV.setText(photo.getPlace_name());
            holder.placeNameTV.setVisibility(View.VISIBLE);
            holder.placeNameTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RazUtils.viewPlace(mContext, photo.getPlace_id(), 0);
                }
            });
        } else {
            holder.placeNameTV.setVisibility(View.GONE);
        }
    }

    /**
     * Returns a string representing the number of days ago the post was uploaded.
     *
     * @return String
     */
    private String getTimeStampDifference(String dateCreates) {
        // TODO(2): Make it more accurate
        Log.d(TAG, "getTimeStampDifference: getting timestamp difference");

        String difference;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss'z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Israel"));
        Date today = calendar.getTime();
        sdf.format(today);
        Date timestamp;
        try {
            timestamp = sdf.parse(dateCreates);
            difference = String.valueOf(Math.round((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24));
        } catch (ParseException e) {
            Log.e(TAG, "getTimeStampDifference: ParseException: " + e.getMessage());
            difference = "0";
        }
        return difference;
    }


    @Override
    public int getItemCount() {
        return photos.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView profileIV;
        public SquareImageView postIV;
        public TextView userNameTV, deleteTv, placeNameTV;
        public ImageView moreIV, redHeart, blankHeart, commentBubble;
        public TextView likesTV, captionTV, commentsTV, timePostedTV;
        public ProgressBar postPB;
        public LinearLayout rankingLL;
        public ImageView[] stars;

        public ViewHolder(View itemView) {
            super(itemView);
            profileIV = itemView.findViewById(R.id.profile_image);
            postIV = itemView.findViewById(R.id.post_image);
            userNameTV = itemView.findViewById(R.id.user_name);
            moreIV = itemView.findViewById(R.id.post_more_iv);
            redHeart = itemView.findViewById(R.id.image_heart_red);
            blankHeart = itemView.findViewById(R.id.image_heart_blank);
            commentBubble = itemView.findViewById(R.id.comment_bubble);
            likesTV = itemView.findViewById(R.id.image_likes_tv);
            captionTV = itemView.findViewById(R.id.caption_tv);
            commentsTV = itemView.findViewById(R.id.image_comments_link);
            timePostedTV = itemView.findViewById(R.id.image_time_posted);
            postPB = itemView.findViewById(R.id.post_pb);
            deleteTv = itemView.findViewById(R.id.delete_tv);
            rankingLL = itemView.findViewById(R.id.ranking_ll);
            placeNameTV = itemView.findViewById(R.id.place_name_tv);

            stars = new ImageView[]{itemView.findViewById(R.id.star1),
                    itemView.findViewById(R.id.star2),
                    itemView.findViewById(R.id.star3),
                    itemView.findViewById(R.id.star4),
                    itemView.findViewById(R.id.star5)};
        }
    }

    /**
     * Gets the users who like this post and use 'getLikesString' method to setup the likesTV.
     * @param photo - Current Photo.
     * @param holder - Current Holder.
     */
    private void setupLikesTV(final Photo photo, final ViewHolder holder) {
        Log.d(TAG, "setupLikesTV: photo: " + photo);


        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (photo.getLikes() != null && photo.getLikes().size() > 0) {
                    Log.d(TAG, "onDataChange: loading photo: " + photo.getPhoto_id() + " likes.");
                    String[] splitUsers = new String[photo.getLikes().size()];
                    int i = 0;
                    for (String likedUserId : photo.getLikes()) {
                        if (likedUserId.equals(mAuth.getCurrentUser().getUid())) {
                            guiLikePost(holder);
                        }
                        String user_name = dataSnapshot.child(mContext.getString(R.string.db_user_account_settings))
                                .child(likedUserId).child(mContext.getString(R.string.db_field_username)).getValue(String.class);
                        if (user_name == null || user_name.length() == 0) {
                            user_name = dataSnapshot.child(mContext.getString(R.string.db_persons))
                                    .child(likedUserId).child(mContext.getString(R.string.db_field_name)).getValue(String.class);
                        }
                        splitUsers[i] = user_name;
                        i++;
                    }
                    holder.likesTV.setText(getLikesString(splitUsers));
                } else {
                    holder.likesTV.setText(getLikesString(null));
                }
                holder.likesTV.setTypeface(new MyFonts(mContext).getLightFont());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: DBError: " + databaseError.getMessage());
            }
        });


    }

    /**
     * @param splitUsers - Names of the users that liked this post
     * @return - Text for holder.likesTV
     */
    private String getLikesString(String[] splitUsers) {
        if (splitUsers == null) {
            return "no likes yet";
        }
        int length = splitUsers.length;
        Log.d(TAG, "setupLikesTV: likes_count: " + length);

        String likeString;
        if (length == 1) {
            likeString = "Liked by " + splitUsers[0];
        } else if (length == 2) {
            likeString = "Liked by " + splitUsers[0]
                    + " and " + splitUsers[1];
        } else if (length == 3) {
            likeString = "Liked by " + splitUsers[0]
                    + ", " + splitUsers[1]
                    + " and " + splitUsers[2];

        } else if (length == 4) {
            likeString = "Liked by " + splitUsers[0]
                    + ", " + splitUsers[1]
                    + ", " + splitUsers[2]
                    + " and " + splitUsers[3];
        } else if (length > 4) {
            likeString = "Liked by " + splitUsers[0]
                    + ", " + splitUsers[1]
                    + ", " + splitUsers[2]
                    + " and " + (splitUsers.length - 3) + " others";
        } else {
            likeString = "no likes yet";
        }
        return likeString;
    }

    /**
     * Shows the redHeart; dismisses the blankHeart.
     * @param holder - Current holder.
     */
    private void guiLikePost(ViewHolder holder) {
        holder.redHeart.setVisibility(View.VISIBLE);
        holder.blankHeart.setVisibility(View.GONE);
    }

    /**
     * Shows the blankHeart; dismisses the redHeart.
     * @param holder - Current holder.
     */
    private void guiUnlikePost(ViewHolder holder) {
        holder.redHeart.setVisibility(View.GONE);
        holder.blankHeart.setVisibility(View.VISIBLE);
    }

    /**
     * If the photo is a PlacePhoto AND The photo's owner rated this place:
     *      Shows the ranking LinearLayout.
     * Else:
     *      Dismiss the ranking LinearLayout
     * @param photo - Current Photo.
     * @param holder - Current Holder.
     */
    private void setupRate(final Photo photo, final ViewHolder holder) {
        if (photo.getPlace_id() == null) {
            Log.d(TAG, "setupRate: not placePhoto: " + photo.getPhoto_id());
            holder.rankingLL.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "setupRate: placePhoto");
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(mContext.getString(R.string.db_places_rates)).hasChild(photo.getPlace_id())
                            && dataSnapshot.child(mContext.getString(R.string.db_places_rates)).child(photo.getPlace_id()).hasChild(photo.getUser_id())) {
                        // Photo's owner rated the place
                        Log.d(TAG, "onDataChange: Photo's owner rated the place :-) ");

                        holder.rankingLL.setVisibility(View.VISIBLE);
                        Long rate = dataSnapshot.child(mContext.getString(R.string.db_places_rates))
                                .child(photo.getPlace_id())
                                .child(photo.getUser_id())
                                .getValue(Long.class);
                        if (rate != null) {
                            for (int i = 0; i < holder.stars.length; i++) {
                                if (i < rate) {
                                    holder.stars[i].setBackgroundResource(R.drawable.ic_cycler_star_green);
                                    holder.stars[i].setVisibility(View.VISIBLE);
                                } else {
                                    holder.stars[i].setBackgroundResource(R.drawable.ic_cycler_star_blank);
                                    holder.stars[i].setVisibility(View.GONE);
                                }
                            }
                        }

                    } else {
                        holder.rankingLL.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w(TAG, "onCancelled: DBError: " + databaseError.getMessage());
                    holder.rankingLL.setVisibility(View.GONE);
                }
            });

        }
    }

    /**
     * Adds a like to this post from the current user to DB:
     * a) 'photos' -> photo_id -> 'likes' -> like_id -> REMOVE
     * b) 'user_photos' -> user_id -> photo_id -> 'likes' -> like_id -> REMOVE
     * if photo is a place_photo:
     * c) 'places_photos' -> place_id -> photo_id -> 'likes' -> like_id -> REMOVE
     */
    private void removePhotoLikeDB(Photo photo) {
        Log.d(TAG, "removePhotoLikeDB: remove a like from photo:  "+ photo.getPhoto_id());
        mFireMethods.unlikePhotoDB(photo);
    }

    private void addPhotoLikeDB(Photo photo) {
        Log.d(TAG, "addPhotoLike: adding new like to photo: " + photo.getPhoto_id());

        mFireMethods.likePhotoDB(photo);
    }


    // --------------------- FIREBASE ---------------------------- //
    private void setupFirebaseStaff() {
        Log.d(TAG, "setupFirebaseStaff: called.");
        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mUid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();

        // Init mAuthStateListener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                // Check if user is logged in

                if (user == null) {
                    Log.d(TAG, "onAuthStateChanged: User log-out");
                    mContext.startActivity(new Intent(mContext, MainRegisterActivity.class));
                } else {
                    if (mUid == null) mUid = user.getUid();
                    Log.d(TAG, "onAuthStateChanged: User is logged-in :) uid = " + firebaseAuth.getCurrentUser().getUid());
                }
            }
        };
    }


}