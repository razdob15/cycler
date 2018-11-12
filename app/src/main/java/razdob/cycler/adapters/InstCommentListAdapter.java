package razdob.cycler.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
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
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import razdob.cycler.R;
import razdob.cycler.dialogs.CustomDialog;
import razdob.cycler.models.InstComment;
import razdob.cycler.models.Photo;
import razdob.cycler.models.User;
import razdob.cycler.myUtils.FirebaseMethods;

/**
 * Created by Raz on 17/06/2018, for project: PlacePicker2
 */
public class InstCommentListAdapter extends ArrayAdapter<InstComment> {
    private static final String TAG = "InstCommentListAdapter";

    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;
    private Photo mPhoto;
    private ArrayList<InstComment> comments;

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    /* ---------------------- FIREBASE ----------------------- */

    private FirebaseMethods mFireMethods;


    public InstCommentListAdapter(@NonNull Context context, int resource,
                                  @NonNull ArrayList<InstComment> comments, Photo photo) {
        super(context, resource, comments);

        initFirebase();

        this.comments = comments;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mFireMethods = new FirebaseMethods(mContext);
        mPhoto = photo;
        layoutResource = resource;
    }

    private void initFirebase() {
        Log.d(TAG, "initFirebase: called.");

        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();
    }

    private static class ViewHolder {
        TextView commentTv, userNameTv, timestampTV, replyTV, likesTV;
        CircleImageView profileImage;
        ImageView blankHeartIV, redHeartIV, deleteIV;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.commentTv = convertView.findViewById(R.id.comment_tv);
            holder.userNameTv = convertView.findViewById(R.id.comment_user_name);
            holder.timestampTV = convertView.findViewById(R.id.comment_time_posted);
            holder.replyTV = convertView.findViewById(R.id.comment_reply);
            holder.blankHeartIV = convertView.findViewById(R.id.comment_heart_blank);
            holder.redHeartIV = convertView.findViewById(R.id.comment_heart_red);
            holder.likesTV = convertView.findViewById(R.id.comment_likes);
            holder.profileImage = convertView.findViewById(R.id.comment_profile_image);
            holder.deleteIV = convertView.findViewById(R.id.delete_comment_iv);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final InstComment comment = getItem(position);
        if (comment.getUser_id().equals(mAuth.getCurrentUser().getUid())
                && !comment.getComment_id().equals("0")) {
            holder.deleteIV.setVisibility(View.VISIBLE);
            holder.deleteIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO(!) Delete comment !
                    final CustomDialog deleteDialog = CustomDialog.createDeleteDialog(mContext, mContext.getString(R.string.del_comment_dialog_title),
                            mContext.getString(R.string.del_comment_text));

                    deleteDialog.setClick1(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: delete comment: " + comment.getComment_id());
                            mFireMethods.removePhotoCommentDB(mPhoto, comment.getComment_id());
                            comments.remove(comment);
                            notifyDataSetChanged();
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

        // Set the comment
        holder.commentTv.setText(Objects.requireNonNull(comment).getComment());

        // Set the timestamp
        String timestampDiff = getTimeStampDifference(comment);
        if (!timestampDiff.equals("0")) {
            // TODO (2): Change 'd' to 'w' OR 'y' [Or even 'h' and 'm'] according the timeDiff
            holder.timestampTV.setText(timestampDiff + " d");
        } else {
            holder.timestampTV.setText("today");
        }


        // Set the userName and profileImage
        Query query = mRef
                .child(mContext.getString(R.string.db_persons))
                .orderByChild(mContext.getString(R.string.db_field_user_id))
                .equalTo(comment.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    holder.userNameTv.setText(singleSnapshot.getValue(User.class).getName());

                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(singleSnapshot.getValue(User.class).getProfile_photo(),
                            holder.profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled: " + databaseError.getMessage());
            }
        });

        if (position == 0) {
            holder.blankHeartIV.setVisibility(View.GONE);
            holder.likesTV.setVisibility(View.GONE);
            holder.replyTV.setVisibility(View.GONE);
        } else {
            if (comment.getLikes() != null && comment.getLikes().size() > 0) {
                holder.likesTV.setText(String.valueOf(comment.getLikes().size()));
                holder.likesTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO(!) Send to there !
//                        Intent intent = new Intent(mContext, UsersListActivity.class);

                    }
                });
            }
            holder.replyTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO(1): Add listener...
                }
            });
        }


        return convertView;
    }


    private void likeComment(ViewHolder holder) {
        holder.redHeartIV.setVisibility(View.VISIBLE);
        holder.blankHeartIV.setVisibility(View.GONE);

        // TODO(!) LIKE OCMMENT !
    }

    /**
     * Returns a string representing the number of days ago the post was uploaded.
     *
     * @return String
     */
    private String getTimeStampDifference(InstComment comment) {
        Log.d(TAG, "getTimeStampDifference: getting timestamp difference");

        String difference = "";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss'z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Israel"));
        Date today = calendar.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = comment.getDate_creates();
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
