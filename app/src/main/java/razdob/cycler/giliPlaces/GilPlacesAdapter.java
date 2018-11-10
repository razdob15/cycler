package razdob.cycler.giliPlaces;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

import razdob.cycler.R;
import razdob.cycler.models.PlaceDetails;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.RazUtils;

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

    private GeoDataClient mGeoDataClient;

    // Adapter Vars
    private ArrayList<PlaceDetails> placeDetails;
    private Context mContext;

    public GilPlacesAdapter(Context mContext, ArrayList<PlaceDetails> placesIds) {
        this.placeDetails = placesIds;
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
        Log.d(TAG, "onBindViewHolder: called." + position + ": " + placeDetails.get(position));
        holder.placeNameTV.setVisibility(View.VISIBLE);
        holder.placeIV.setVisibility(View.VISIBLE);

        initPlacePhoto(placeDetails.get(position).getId(), holder);
        holder.placeNameTV.setText(placeDetails.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return placeDetails.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView placeIV;
        public TextView placeNameTV;


        public MyViewHolder(View itemView) {
            super(itemView);
            placeIV = itemView.findViewById(R.id.place_image);
            placeNameTV = itemView.findViewById(R.id.place_name_tv);
        }
    }


    private void initPlacePhoto(String placeId, final GilPlacesAdapter.MyViewHolder holder) {
        mGeoDataClient = Places.getGeoDataClient(mContext);
        mGeoDataClient.getPlacePhotos(placeId).addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> placePhotoMetadataResponseTask) {

                Task<PlacePhotoResponse> photoTask = RazUtils.getPlacePhotoTask(mGeoDataClient, placePhotoMetadataResponseTask, true);
                if (photoTask != null) {
                    photoTask.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                            PlacePhotoResponse photo = task.getResult();
                            if (photo != null) {
                                Bitmap bitmap = photo.getBitmap();
                                holder.placeIV.setImageBitmap(bitmap);
                            }
                        }
                    });
                }


            }
        });
    }


}
