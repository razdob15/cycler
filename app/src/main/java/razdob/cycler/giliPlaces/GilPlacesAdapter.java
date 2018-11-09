package razdob.cycler.giliPlaces;

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

import razdob.cycler.R;
import razdob.cycler.myUtils.FirebaseMethods;

/**
 * Created by Raz on 09/11/2018, for project: Cycler
 */
public class GilPlacesAdapter extends RecyclerView.Adapter<GilPlacesAdapter.MyViewHolder> {
    private static final String TAG = "GilPlacesAdapter";

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private FirebaseMethods mFireMethods;
    /* ---------------------- FIREBASE ----------------------- */

    // Adapter Vars
    private ArrayList<String> placesIds;
    private Context mContext;

    public GilPlacesAdapter(Context mContext, ArrayList<String> placesIds) {
        this.placesIds = placesIds;
        this.mContext = mContext;
        initFirebase();
    }

    private void initFirebase() {
        Log.d(TAG, "initFirebase: called.");
        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();
        mFireMethods = new FirebaseMethods(mContext);
    }

    @NonNull
    @Override
    public GilPlacesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called.");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_gili_place, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GilPlacesAdapter.MyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called." + position + ": " + placesIds.get(position));
        holder.placeNameTV.setVisibility(View.VISIBLE);
        holder.placeIV.setVisibility(View.VISIBLE);

        holder.placeNameTV.setText(placesIds.get(position));


    }

    @Override
    public int getItemCount() {
        return placesIds.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView placeIV;
        public TextView placeNameTV;


        public MyViewHolder(View itemView) {
            super(itemView);
            placeIV = itemView.findViewById(R.id.place_image);
            placeNameTV = itemView.findViewById(R.id.place_name_tv);


//            placeIV.setImageResource(R.drawable.ic_launcher_background);
//            placeNameTV.setText("Place Name");
        }
    }







}
