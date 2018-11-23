package razdob.cycler.giliPlaces;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
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

import razdob.cycler.R;
import razdob.cycler.models.PlaceDetails;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.RazUtils;
import razdob.cycler.myUtils.UniversalImageLoader;

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

    private static GeoDataClient mGeoDataClient;

    // Static Vars
    private static ArrayList<PlaceDetails> placeDetails;
    private String activityName;

    // Adapter Vars
    private Context mContext;
    private FragmentActivity mActivity;
    private LayoutInflater mLayoutInflater;

    private GilPlacesAdapter(Context context, FragmentActivity activity, ArrayList<PlaceDetails> items, String activityName) {
        placeDetails = items;
        this.mContext = context;
        this.mActivity = activity;
        this.activityName = activityName;
        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        View view = mLayoutInflater.inflate(R.layout.custom_gili_place, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called." + position + ": " + placeDetails.get(position));
        final PlaceDetails pd = placeDetails.get(position);
        holder.onBindViewHolder(pd);

        if (holder.placeBitmap == null) initPlacePhoto(pd.getId(), holder, position);
    }


    @Override
    public int getItemCount() {
        return placeDetails.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public final ImageView placeIV;
        public final TextView placeNameTV;
        Bitmap placeBitmap;


        MyViewHolder(View itemView) {
            super(itemView);
            placeIV = itemView.findViewById(R.id.place_image);
            placeNameTV = itemView.findViewById(R.id.place_name_tv);
        }

        void onBindViewHolder(final PlaceDetails placeDetails) {
            Bitmap bitmap = placeDetails.getImg();
            if (bitmap != null) {
                placeBitmap = bitmap;
                placeIV.setImageBitmap(placeBitmap);
            }
            if (placeDetails.getName() != null) placeNameTV.setText(placeDetails.getName());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GiliOnePlaceFragment.showOnePlace(mActivity, placeDetails);
                }
            });

        }

    }


    private void initPlacePhoto(String placeId, final GilPlacesAdapter.MyViewHolder holder, final int pos) {
        Bitmap img = holder.placeBitmap;
        if (img == null) {
            if (mGeoDataClient == null) mGeoDataClient = Places.getGeoDataClient(mContext);
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
                                    bitmap = UniversalImageLoader.getRoundedCornerBitmap(bitmap);

                                    holder.placeIV.setImageBitmap(bitmap);
                                    holder.placeBitmap = bitmap;

                                    placeDetails.get(pos).setImg(bitmap);
                                    if (placeDetails.size() - 1 == pos) {
                                        finishLoad();
                                    }
                                }
                            }
                        });
                    }
                }
            });
        } else {
            placeDetails.get(pos).setImg(img);
        }
    }

    private void finishLoad() {
        Log.d(TAG, "finishLoad: called.");
        if (activityName.equals(mContext.getString(R.string.gili_favorites_activity)))
            GiliFavoritesActivity.setPlaceDetailsList(placeDetails);
    }

    /**
     * Create an adapter for recyclerView.
     * uses Vertical LinearLayoutManager & DefaultItemAnimator.
     *
     * @param context       App Context.
     * @param recyclerView  - RecyclerView Object to contain the adapter.
     placesDetails - List od places details for the adapter.
     */
//    public static void createPlacesAdapter(Context context, FragmentActivity activity, RecyclerView recyclerView, ArrayList<PlaceDetails> placesDetails, String activityName) {
//        GilPlacesAdapter adapter = new GilPlacesAdapter(context, activity, placesDetails, activityName);
//        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(context, 3);
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        recyclerView.setAdapter(adapter);
//    }

    public static void createPlacesAdapter(final Context context, final FragmentActivity activity, final RecyclerView recyclerView, final ArrayList<?> placesList, final String activityName) {
        if (placesList == null || placesList.size() == 0) return;
        if (placesList.get(0).getClass().equals(String.class)) {
            if (mGeoDataClient == null) mGeoDataClient = Places.getGeoDataClient(context);
            final ArrayList<PlaceDetails> placeDetailsList = new ArrayList<>();
            for (Object pid : placesList) {
                mGeoDataClient.getPlaceById((String) pid).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                        if (task.isSuccessful()) {
                            PlaceBufferResponse places = task.getResult();
                            Place place = null;
                            if (places != null) {
                                place = places.get(0);
                                PlaceDetails pd = new PlaceDetails(place);
                                placeDetailsList.add(pd);
                                if (placeDetailsList.size() == placesList.size()) {
                                    Log.d(TAG, "onComplete: Last place in list");
                                    createPlacesAdapter(context, activity, recyclerView, placeDetailsList, activityName);
                                }
                            }

                        }
                    }
                });
            }
        } else if (placesList.get(0).getClass().equals(PlaceDetails.class)) {
            GilPlacesAdapter adapter = new GilPlacesAdapter(context, activity, (ArrayList<PlaceDetails>) placesList, activityName);
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(context, 3);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(adapter);
        }
    }


}
