package razdob.cycler.adapters;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import razdob.cycler.models.Comment;
import razdob.cycler.myUtils.FireBaseUtils;
import razdob.cycler.R;

/**
 * Created by Raz on 22/02/2018, for project: PlacePicker2
 */

public class PlaceCommentAdapter extends ArrayAdapter<Comment> {

    // UNUSED CLASS !!

    private Context context;
    private ArrayList<Comment> comments;
    private View line;

    private ImageView imageView;


    public PlaceCommentAdapter(Context context, ArrayList<Comment> comments) {
        super(context, 0, comments);
        this.context = context;
        this.comments = comments;
        Log.i("comments_lennnn", "size" + comments.size());
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View row, @NonNull ViewGroup parent) {
        // Get the data item for this position
        final Comment comment = getItem(position);
        if (comment == null)
            return new View(context);
        // Check if an existing view is being reused, otherwise inflate the view
        if (row == null) {
            row = LayoutInflater.from(getContext()).inflate(R.layout.item_comment, parent, false);
        }
        if (row != null) line = row;

        // Lookup view for data population
        TextView authorName_tv = row.findViewById(R.id.auther_name);
        final TextView content_tv = row.findViewById(R.id.content);
        TextView date_tv = row.findViewById(R.id.date);
        final ImageButton del_ib = row.findViewById(R.id.delete_ib);
        TextView editCommentTV = row.findViewById(R.id.edit_tv);
        imageView = row.findViewById(R.id.place_image);
        if (comment.getImageUri() != null) {
            FirebaseStorage.getInstance().getReference().child(comment.getImageUri() + "/" + comment.getAuthorUid() + "jgg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    loadImageFromUrl(uri.toString());
                }
            });
        }


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.i("comment authNAme", "" + comment.getAuthorName());
        Log.i("comment authUid", "" + comment.getAuthorUid());
        Log.i("comment Time", "" + comment.getTime());

        if (user != null && comment.getAuthorUid().equals(user.getUid())) {
            del_ib.setVisibility(View.VISIBLE);
            editCommentTV.setVisibility(View.VISIBLE);
            del_ib.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    FirebaseDatabase.getInstance().getReference().child("places_comments").child(FireBaseUtils.zipName(comment.getBusinessName())).child("t" + comment.getAuthorUid().toCharArray()[0] + comment.getTime()).setValue(null);
                    if (comments.contains(comment)) comments.remove(comment);
                    line.setVisibility(View.GONE);
                    // TODO(!): Create a Dialog before deleting the comment  !
                }
            });

            editCommentTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Edit the Comment !!", Toast.LENGTH_SHORT).show();//                Intent intent = new Intent(context, OnePlaceData.class);
//                intent.putExtra("place_name", comment.getBusinessName());
//                intent.putExtra("content", comment.getContent());
//                intent.putExtra("rank", comment.getRank());

                }
            });
        }
        // Populate the data into the template view using the data object
//        if (comment.getAuthorName().equals(comment.getAuthorUid()))
//            comment.setAuthorName(MainActivity.fromFirebase.getName());
        authorName_tv.setText(comment.getAuthorName());
        content_tv.setText(comment.getContent());

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        date_tv.setText(dateFormat.format(new Date(comment.getTime())));

        ImageButton[] starsArr = new ImageButton[]{
                row.findViewById(R.id.one_star),
                row.findViewById(R.id.two_star),
                row.findViewById(R.id.three_star),
                row.findViewById(R.id.four_star),
                row.findViewById(R.id.five_star)};


        // Sets stars color to OFF
        for (ImageButton star : starsArr) {
            star.setImageResource(android.R.drawable.btn_star_big_off);
            star.setBackgroundColor(Color.TRANSPARENT);
        }

        // Puts the correct star in the correct places.
        if (comment.getRank() != -1) {
            if (comment.getRank() >= 1 && comment.getRank() <= 5) {
                for (int i = 0; i < comment.getRank(); i++) {
                    starsArr[i].setImageResource(android.R.drawable.btn_star_big_on);
                }
            }
        }

        // Return the completed view to render on screen
        return row;
    }


    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    private void loadImageFromUrl(String url) {
        Log.i("comment_uri", url);
        Picasso.get().load(url).placeholder(R.mipmap.cycler_launcher)
                .error(R.mipmap.cycler_launcher)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.i("user_profile_image", "The user's profile image was loaded successfully ! ");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.i("user_profile_image", "The user's profile image was not loaded successfully !:( ");
                    }
                });

    }
}
