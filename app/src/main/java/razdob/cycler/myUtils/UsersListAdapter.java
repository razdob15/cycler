package razdob.cycler.myUtils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import razdob.cycler.R;
import razdob.cycler.models.User;
import razdob.cycler.models.UserAccountSettings;

/**
 * Created by Raz on 18/06/2018, for project: PlacePicker2
 */
public class UsersListAdapter extends ArrayAdapter<User> {
    private static final String TAG = "UsersListAdapter";

    private LayoutInflater mInflater;
    private List<User> mUsers;
    private int layoutResource;
    private Context mContext;


    public UsersListAdapter(@NonNull Context context, int resource, @NonNull List<User> objects) {
        super(context, resource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        this.mUsers = objects;
    }

    private static class ViewHolder {
        TextView userNameTV, emailTV;
        CircleImageView profileIV;
        ProgressBar profileImagePB;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();
            holder.userNameTV = convertView.findViewById(R.id.user_name);
            holder.emailTV = convertView.findViewById(R.id.email);
            holder.profileIV = convertView.findViewById(R.id.profile_image);
            holder.profileImagePB = convertView.findViewById(R.id.profile_image_pb);
            holder.profileImagePB.setVisibility(View.GONE);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.userNameTV.setText(Objects.requireNonNull(getItem(position)).getName());
        holder.emailTV.setText(Objects.requireNonNull(getItem(position)).getEmail());

        User user = getItem(position);
        Log.d(TAG, "getView: userssssedrer: " +user.toString());
        if (user.getProfile_photo() != null)
            UniversalImageLoader.setImage(mContext, user.getProfile_photo(), holder.profileIV, holder.profileImagePB, "");
        else
            holder.profileImagePB.setVisibility(View.GONE);

        return convertView;
    }
}
