package razdob.cycler.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import razdob.cycler.R;

import static android.graphics.Typeface.BOLD;

/**
 * Created by Raz on 07/01/2018, for project: PlacePicker2
 */

public class SubjectAdapter extends ArrayAdapter<HashMap<Integer, String>> {


    // Object Attributes
    private HashMap<Integer, String> subjects;
    private Context context;
    private FirebaseUser mFireUser;
    private HashMap<String, Boolean> mHashMap;// = PersonProfileActivity.myUser.getPreferences();
    private boolean person;


    // Constructor
    public SubjectAdapter (Context context, int resource, HashMap<Integer,String> map, @NonNull FirebaseUser user, HashMap<String, Boolean> userPrefOrTags, boolean person) {
        super(context, resource);

        subjects = map;
        this.context = context;
        this.mFireUser = user;
        this.mHashMap = userPrefOrTags;
        this.person = person;
    }

    @Override
    public int getCount() {
        return subjects.size();
    }

    // Shows the view in the right way according to the Firebase.
    // Add click listener to every list row.
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return super.getView(position, convertView, parent);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View row = inflater.inflate(R.layout.subject_obj, null, true);
        final TextView subjectTextView = row.findViewById(R.id.subject_name);
        final String subjectName = subjects.get(position);

        subjectTextView.setText(subjectName);

        // Initialize subject's color
        if (mHashMap.keySet().contains(subjectName)) {
            if (mHashMap.get(subjectName)) {
                subjectTextView.setTypeface(null, BOLD);
                subjectTextView.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            }
        }
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHashMap.containsKey(subjectName)) {
                    Log.i("adapter_click", "boolean:" + !mHashMap.get(subjectName));
                    changeSubjectPref(subjectTextView, !mHashMap.get(subjectName));
                } else changeSubjectPref(subjectTextView, true);
            }
        });
        return row;
    }

    // Change the view and the DB
    private void changeSubjectPref(TextView textView, boolean toTrue) {
        final int blackColor = context.getResources().getColor(R.color.colorBlack);
        final int primaryDarkColor = context.getResources().getColor(R.color.colorPrimaryDark);

        mHashMap.put(textView.getText().toString(), toTrue);

        if (toTrue) {
            textView.setTypeface(null, BOLD);
            textView.setTextColor(primaryDarkColor);

            if (person) {
                FirebaseDatabase.getInstance().getReference().child("persons").child(mFireUser.getUid()).child("preferences").child(textView.getText().toString()).setValue(true);
            } else {
                FirebaseDatabase.getInstance().getReference().child("business_users").child(mFireUser.getUid()).child("tags").child(textView.getText().toString()).setValue(true);
            }
        } else {
            textView.setTypeface(null, Typeface.NORMAL);
            textView.setTextColor(blackColor);
            Log.i("pref_name_f", textView.getText().toString());

            if (person) {
                FirebaseDatabase.getInstance().getReference().child("persons").child(mFireUser.getUid()).child("preferences").child(textView.getText().toString()).setValue(false);
            }
            else {
                FirebaseDatabase.getInstance().getReference().child("business_users").child(mFireUser.getUid()).child("tags").child(textView.getText().toString()).setValue(false);
            }
        }
    }

}
