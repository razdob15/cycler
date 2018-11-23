package razdob.cycler.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import razdob.cycler.R;
import razdob.cycler.giliPlaces.GilPlacesAdapter;
import razdob.cycler.models.PlaceDetails;
import razdob.cycler.myUtils.RazUtils;

/**
 * Created by Raz on 15/11/2018, for project: Cycler
 */
public class PlacesCirclesImageAdapter extends RecyclerView.Adapter<PlacesCirclesImageAdapter.MyViewHolder> {
    private static final String TAG = "PlacesCirclesAdapter";


    // Vars
    private GeoDataClient mGeoDataClient;
    // Vars for Constructor
    private Context mContext;
    private ArrayList<PlaceDetails> placeDetailsList;

    public PlacesCirclesImageAdapter(Context context, ArrayList<PlaceDetails> placeDetailsList) {
        mContext = context;
        this.placeDetailsList = placeDetailsList;
    }


    @NonNull
    @Override
    public PlacesCirclesImageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called.");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_gili_place, parent, false);
        return new PlacesCirclesImageAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlacesCirclesImageAdapter.MyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        final String placeName = placeDetailsList.get(position).getName();
        holder.placeNameTV.setText(placeName);
        initPlacePhoto(placeDetailsList.get(position).getId(), holder);

    }

    private void initPlacePhoto(String placeId, final PlacesCirclesImageAdapter.MyViewHolder holder) {
        mGeoDataClient = Places.getGeoDataClient(mContext);
        mGeoDataClient.getPlacePhotos(placeId).addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> placePhotoMetadataResponseTask) {

                Task<PlacePhotoResponse> photoTask = RazUtils.getPlaceOnePhotoTask(mGeoDataClient, placePhotoMetadataResponseTask, true);
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

    @Override
    public int getItemCount() {
        return placeDetailsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView placeIV;
        public TextView placeNameTV;


        public MyViewHolder(View itemView) {
            super(itemView);
            placeIV = itemView.findViewById(R.id.place_image);
            placeNameTV = itemView.findViewById(R.id.place_name_tv);
        }
    }

    public static void createPlacesCirclesAdapter(Context context, RecyclerView recyclerView, ArrayList<PlaceDetails> placeDetailsList) {
        PlacesCirclesImageAdapter adapter = new PlacesCirclesImageAdapter(context, placeDetailsList);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

    }



}
