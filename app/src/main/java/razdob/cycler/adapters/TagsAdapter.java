package razdob.cycler.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import razdob.cycler.R;
import razdob.cycler.models.Tag;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.RazUtils;

/**
 * Created by Raz on 15/04/2018, for project: PlacePicker2
 */

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.ViewHolder> {
    private static final String TAG = "TagsAdapter";

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private String uid;
    /* ---------------------- FIREBASE ----------------------- */


    // Constructor Vars
    private List<String> tagsList;
    private Context mContext;
    private String place_id;
    private ArrayList<String> chosenTags;

    // Vars
    private long counter = 0;
    private FirebaseMethods mFirebaseMethods;

    public TagsAdapter(Context mContext, List<String> tagsList, String place_id, ArrayList<String> chosenTags) {
        Log.d(TAG, "TagsAdapter: tags: " + tagsList);
        this.mContext = mContext;
        initFirebase();
        this.tagsList = tagsList;
        this.place_id = place_id;
        this.chosenTags = chosenTags != null ? chosenTags : new ArrayList<String>();
        mFirebaseMethods = new FirebaseMethods(mContext);
    }

    private void initFirebase() {
        Log.d(TAG, "initFirebase: called.");

        mFireApp = FirebaseApp.getInstance("mFireApp");
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();
        mAuth = FirebaseAuth.getInstance(mFireApp);
        uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called.");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_grid_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        final String tagText = tagsList.get(position);
        holder.tagTV.setText(tagText);
        if (chosenTags.contains(tagText)) {
            holder.tagCM.setVisibility(View.VISIBLE);
        } else { holder.tagCM.setVisibility(View.GONE); }

        setListeners(holder, tagText);
    }

    @Override
    public int getItemCount() {
        return tagsList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tagTV;
        public ImageView tagIv, tagCM;


        public ViewHolder(View itemView) {
            super(itemView);
            tagTV = itemView.findViewById(R.id.tag_tv);
            tagIv = itemView.findViewById(R.id.tag_image_view);
            tagCM = itemView.findViewById(R.id.tag_check_mark);
        }
    }

    private void setListeners(final ViewHolder holder, final String tagText) {
        holder.tagIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Tag IV");
                final Tag tag = new Tag(tagText, place_id, uid, -1);

                // Set Tag's counter
                setupTag(tag, holder, tagText);


            }
        });
    }

    public ArrayList<String> getChosenTags() { return chosenTags; }

    private void setupTag(final Tag tag, final ViewHolder holder, final String tagText) {
        Log.d(TAG, "setupTag: setting up counter according DB");
        mRef.child(mContext.getString(R.string.db_field_places_tag_counters))
                .child(place_id).child(tagText).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    counter = dataSnapshot.getValue(long.class);

                    if (holder.tagCM.getVisibility() == View.GONE) {
                        Log.d(TAG, "onClick: Add tag");

                        holder.tagCM.setVisibility(View.VISIBLE);
                        addTag(tag, tagText);
                    } else {
                        Log.d(TAG, "onClick: Remove tag");

                        holder.tagCM.setVisibility(View.GONE);

                        removeTag(tag, tagText);
                    }
                } catch (NullPointerException e) {
                    Log.e(TAG, "onDataChange: No counter yet: " + e.getMessage());
                    if (holder.tagCM.getVisibility() == View.GONE) {
                        Log.d(TAG, "onClick: Add tag");

                        holder.tagCM.setVisibility(View.VISIBLE);
                        addTag(tag, tagText);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: DatabaseError: " + databaseError.getMessage());
            }
        });
    }

    private void addTag(final Tag tag, String tagText) {
        Log.d(TAG, "addTag: add the tag !");

        chosenTags.add(tagText);

        tag.setTagName(tagText);

        // TODO(2): Move this to FirebaseMethods
        mRef.child(mContext.getString(R.string.db_field_places)).child(place_id)
                .child(mContext.getString(R.string.db_field_tags))
                .child(String.valueOf(counter))
                .child(tagText).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> usersIds = new ArrayList<>();
                boolean currnetUserSign = false;
                for (DataSnapshot uidDS : dataSnapshot.getChildren()) {
                    String uid = uidDS.getValue(String.class);
                    usersIds.add(uid);
                    if (uid.equals(uid))
                        currnetUserSign = true;
                }
                if (!currnetUserSign) {
                    usersIds.add(uid);
                    mFirebaseMethods.clearAndSaveTag(tag, place_id, usersIds);
                }
                counter--;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: DatabaseError: " + databaseError.getMessage());
            }
        });

        mRef.child(mContext.getString(R.string.db_tag_places))
                .child(tagText)
                .child(place_id)
                .setValue(true);

        mRef.child(mContext.getString(R.string.db_users_tag_places))
                .child(uid)
                .child(place_id)
                .child(tagText)
                .setValue(true);
    }

    private void removeTag(final Tag tag, String tagText) {
        tag.setCounter(counter);
        tag.setTagName(tagText);

        chosenTags.remove(tagText);

        mRef.child(mContext.getString(R.string.db_field_places)).child(place_id)
                .child(mContext.getString(R.string.db_field_tags))
                .child(String.valueOf(counter))
                .child(tagText).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> usersIds = new ArrayList<>();
                boolean currnetUserSign = false;
                for (DataSnapshot uidDS : dataSnapshot.getChildren()) {
                    String uid = uidDS.getValue(String.class);
                    usersIds.add(uid);
                    if (uid.equals(uid))
                        currnetUserSign = true;
                }
                if (currnetUserSign) {
                    usersIds.remove(uid);
                    mFirebaseMethods.removeTag(tag, place_id, usersIds);
                }
                counter++;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: DatabaseError: " + databaseError.getMessage());
            }
        });

        mRef.child(mContext.getString(R.string.db_tag_places))
                .child(tagText)
                .child(place_id)
                .setValue(null);

        mRef.child(mContext.getString(R.string.db_users_tag_places))
                .child(uid)
                .child(place_id)
                .child(tagText)
                .setValue(null);

    }

    public static void createGridTagsAdapter(Context context, RecyclerView recyclerView, List<String> tagsList, String placeId, ArrayList<String> chosenTags) {
        TagsAdapter adapter = new TagsAdapter(context, tagsList, placeId, chosenTags);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(context, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

}