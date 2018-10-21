package razdob.cycler.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import razdob.cycler.R;
import razdob.cycler.myUtils.FirebaseMethods;

/**
 * Created by Raz on 15/04/2018, for project: PlacePicker2
 */

public class CurrentSubjectsAdapter extends RecyclerView.Adapter<CurrentSubjectsAdapter.ViewHolder> {
    private static final String TAG = "CurrentSubjectsAdapter";

    // TODO(!): If the user already choose this tag - mark it !


    private List<String> tagsList;
    private Context mContext;

    // Vars
    long counter = 0;
    private FirebaseMethods mFirebaseMethods;
    private ArrayList<String> currentSubjects;

    public CurrentSubjectsAdapter(Context context, ArrayList<String> tagsList,
                                  ArrayList<String> markedSubjects) {
        Log.d(TAG, "TagsAdapter: tags: " + tagsList);
        this.tagsList = tagsList;
        this.mContext = context;
        currentSubjects = markedSubjects != null ? markedSubjects : new ArrayList<String>();
        mFirebaseMethods = new FirebaseMethods(context);
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
        if (currentSubjects.contains(tagText)) {
            holder.tagCM.setVisibility(View.VISIBLE);
        } else {holder.tagCM.setVisibility(View.GONE); }

        setListeners(holder, tagText);
    }

    @Override
    public int getItemCount() {
        return tagsList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tagTV;
        public ImageView subjectIV, tagCM;


        public ViewHolder(View itemView) {
            super(itemView);
            tagTV = itemView.findViewById(R.id.tag_tv);
            subjectIV = itemView.findViewById(R.id.tag_image_view);
            tagCM = itemView.findViewById(R.id.tag_check_mark);
        }
    }

    private void setListeners(final ViewHolder holder, final String subject) {
        holder.subjectIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.tagCM.getVisibility() == View.VISIBLE) {
                    removeSub(subject);
                    holder.tagCM.setVisibility(View.GONE);
                } else {
                    addSub(subject);
                    holder.tagCM.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "onClick: Tag IV");
            }
        });
    }

    public ArrayList<String> getCurrentSubjects() {
        return currentSubjects;
    }

    private void addSub(String subjectText) {
        Log.d(TAG, "addSub: add the subject: "+subjectText);
        currentSubjects.add(subjectText);
    }

    private void removeSub(String subjectText) {
        Log.d(TAG, "removeSub: remove subjects: " +subjectText);
        currentSubjects.remove(subjectText);

    }

}