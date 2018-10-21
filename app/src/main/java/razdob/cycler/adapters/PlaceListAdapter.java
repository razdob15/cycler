package razdob.cycler.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import razdob.cycler.ChoosePlaceTagsActivity;
import razdob.cycler.R;
import razdob.cycler.fivePlaces.FivePlacesActivity;
import razdob.cycler.fivePlaces.ViewOnePlaceActivity;
import razdob.cycler.models.PlaceDetails;
import razdob.cycler.myUtils.FirebaseMethods;
import razdob.cycler.myUtils.Permissions;
import razdob.cycler.myUtils.RazUtils;

/**
 * Created by Raz on 24/04/2018, for project: PlacePicker2
 */
public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.MyViewHolder> {
    private static final String TAG = "PlaceListAdapter";

    // TODO(0): See al lth TODOs in this file !

    // Call Permission
    public static final int CALL_PERMISSION_CODE = 5;
    // Constants
    private final int PD_LOAD = 0;
    private final int PHOTO_LOAD = 1;
    private final int LOADINGS_COUNT = 2;
    // Vars
    private boolean[][] mBooleans;
    private FirebaseMethods mFireMethods;


    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    /* ---------------------- FIREBASE ----------------------- */

    // Adapter constructor vars
    private HashMap<Integer, PlaceDetails> posPlaceHM;
    private List<String> placesIds;
    private List<String> favoritePlacesIds;
    private ArrayList<Boolean> opens;
    private Context mContext;
    private String activityName;
    private HashMap<String, PlaceDetails> idDetailsHM;
    private boolean load;

    private GeoDataClient mGeoDataClient;

    // Loading constructor
    public PlaceListAdapter(Context context, List<String> places, List<String> favorites, String activityName, ArrayList<Boolean> opens) {
        Log.d(TAG, "PlaceListAdapter: places: " + places);
        Log.d(TAG, "PlaceListAdapter: opens: " + opens);
        this.mContext = context;
        initFirebase();
        this.placesIds = places;
        this.favoritePlacesIds = favorites;
        this.posPlaceHM = new HashMap<>();
        this.activityName = activityName;
        this.load = true;
        this.opens = opens;
        mFireMethods = new FirebaseMethods(mContext);
        setupBooleans();
    }

    // No loading constructor
    public PlaceListAdapter(Context context, HashMap<String, PlaceDetails> idDetailsHM, List<String> favorites, String activityName) {
        Log.d(TAG, "PlaceListAdapter: places: " + idDetailsHM.keySet());
        this.mContext = context;
        initFirebase();
        this.idDetailsHM = idDetailsHM;
        this.placesIds = new ArrayList<>();
        this.placesIds.addAll(idDetailsHM.keySet());
        this.favoritePlacesIds = favorites;
        this.posPlaceHM = new HashMap<>();
        this.activityName = activityName;
        this.load = false;
        mFireMethods = new FirebaseMethods(mContext);
        setupBooleans();
    }

    private void initFirebase() {
        Log.d(TAG, "initFirebase: called.");
        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();
    }


    @NonNull
    @Override
    public PlaceListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called.");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_custom_place, parent, false);
//        checkPhonePermission();
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PlaceListAdapter.MyViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        // init geo client
        mGeoDataClient = Places.getGeoDataClient(mContext);
        Log.d(TAG, "onBindViewHolder: position: " + position);
        // Close / Open
        if (opens == null || opens.size() == 0 || opens.get(position) == null) {
            Log.d(TAG, "onBindViewHolder: case(1)");
            holder.closeTV.setVisibility(View.GONE);
            holder.openTV.setVisibility(View.GONE);
        } else if (opens.get(position)) {
            Log.d(TAG, "onBindViewHolder: case(2    )");
            holder.openTV.setVisibility(View.VISIBLE);
            holder.closeTV.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "onBindViewHolder: case(3)");
            holder.openTV.setVisibility(View.GONE);
            holder.closeTV.setVisibility(View.VISIBLE);
        }

        // Current placeID
        final String placeId = placesIds.get(position);
        final int pos1 = position;
        final PlaceDetails placeDetails = posPlaceHM.containsKey(pos1) ? posPlaceHM.get(pos1) : new PlaceDetails(placeId);


        Log.d(TAG, "onBindViewHolder: placeId (" + position + "): " + placeId);

        // Check in favorites
        if (favoritePlacesIds.contains(placeId)) placeDetails.setFavorite(true);

        // If place existed before
        if (posPlaceHM.containsKey(position) && posPlaceHM.get(position).getName() != null) {
            PlaceDetails pd = posPlaceHM.get(position);
            Log.d(TAG, "onBindViewHolder: place is in: " + pd.getName());
            // TODO(!) Check this crash (Mom's phone)
            mBooleans[PD_LOAD][pos1] = true;
            setupHolder(holder, pos1);
        } else if (load || true) {
            Log.d(TAG, "onBindViewHolder: load place: " + placeId);
            mGeoDataClient.getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                    if (task.isSuccessful()) {
                        PlaceBufferResponse places = task.getResult();
                        Place place = places.get(0);

                        Log.d(TAG, "onComplete: place(" + pos1 + "): " + place.getName());

                        // Setting up placeDetails object
                        if (place.getName() != null)
                            placeDetails.setName(place.getName().toString());
                        if (place.getAddress() != null)
                            placeDetails.setAddress(place.getAddress().toString());
                        if (place.getPhoneNumber() != null)
                            placeDetails.setPhone(place.getPhoneNumber().toString());
                        if (place.getWebsiteUri() != null)
                            placeDetails.setWebsite(place.getWebsiteUri().toString());

                        if (activityName.equals(mContext.getString(R.string.five_places_activity))) {
                            if (FivePlacesActivity.idDetailsHM.containsKey(placeId)) {
                                FivePlacesActivity.idDetailsHM.get(placeId).setName(placeDetails.getName());
                                FivePlacesActivity.idDetailsHM.get(placeId).setAddress(placeDetails.getAddress());
                                FivePlacesActivity.idDetailsHM.get(placeId).setPhone(placeDetails.getPhone());
                                FivePlacesActivity.idDetailsHM.get(placeId).setWebsite(placeDetails.getWebsite());
                            } else {
                                FivePlacesActivity.idDetailsHM.put(placeId, placeDetails);
                            }
                        }

                        // Insert to HashMap
                        posPlaceHM.put(pos1, placeDetails);
                        mBooleans[PD_LOAD][pos1] = true;
                        setupHolder(holder, pos1);

                        Log.d(TAG, "onComplete: finish all text init");

                        places.release();

                    } else {
                        Log.e(TAG, "Place not found.");
                    }
                }
            });
            getPhotos(pos1, holder);

        } else {
            Log.d(TAG, "onBindViewHolder: not need to load !");
            posPlaceHM.put(pos1, idDetailsHM.get(placeId));
            mBooleans[PD_LOAD][pos1] = true;
            mBooleans[PHOTO_LOAD][pos1] = true;
            setupHolder(holder, pos1);
        }

        setupClickListeners(holder, placeId);

    }

    @Override
    public int getItemCount() {
        return placesIds.size();
    }

    public void removeItem(int position) {
        placesIds.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(String placeId, int position) {
        placesIds.add(position, placeId);
        notifyItemInserted(position);
    }

    private void getPhotos(final int pos, final MyViewHolder holder) {
        final PlaceDetails placeDetails = posPlaceHM.containsKey(pos) ? posPlaceHM.get(pos) : new PlaceDetails(placesIds.get(pos));

        Log.d(TAG, "getPhotos: get: " + placeDetails.getId() + " first photo");
        if (placeDetails.getImg() != null || (posPlaceHM.containsKey(pos) && posPlaceHM.get(pos).getImg() != null)) {
            Log.d(TAG, "getPhotos: photo not needed...");
            return;
        }
        mGeoDataClient.getPlacePhotos(placeDetails.getId()).addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                final PlacePhotoMetadataBuffer photoMetadataBuffer = Objects.requireNonNull(task.getResult()).getPhotoMetadata();
                // Get the first photo in the list.
                if (photoMetadataBuffer != null && photoMetadataBuffer.getCount() > 0) {
                    PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                    Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                    photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                            Log.d(TAG, "onComplete: put photo to: " + placeDetails.getId());
                            PlacePhotoResponse photo = task.getResult();
                            Bitmap bitmap = photo.getBitmap();

                            // TODO(!): Check this code! (Why uses static variables?)

                            if (posPlaceHM.containsKey(pos)) {
                                PlaceDetails temp = posPlaceHM.get(pos);
                                temp.setImg(bitmap);
                                posPlaceHM.put(pos, temp);
                            } else {
                                placeDetails.setImg(bitmap);
                                if (activityName.equals(mContext.getString(R.string.data_activity))) {
                                    FivePlacesActivity.idDetailsHM.put(placeDetails.getId(), placeDetails);
                                }
                                posPlaceHM.put(pos, placeDetails);
                            }
                            mBooleans[PHOTO_LOAD][pos] = true;
                            setupHolder(holder, pos);
                        }
                    });
                } else {
                    mBooleans[PHOTO_LOAD][pos] = true;
                    setupHolder(holder, pos);
                }
            }
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTV, addressTV, phoneTV, websiteTv, tagsExplanation, openTV, closeTV;
        public ImageView choose_place_iv, tagsWarn, greenHeartIV, blankHeartIV;
        public CircleImageView circleImage;
        public RelativeLayout viewBackground, viewForeground;
        public LinearLayout addressLL, phoneLL, websiteLL;
        public ProgressBar progressBar;
        public FrameLayout frameLayout;
        public CardView mainCardView;
        public String placeID = "";

        public MyViewHolder(View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.place_name);
            addressTV = itemView.findViewById(R.id.place_address);
            phoneTV = itemView.findViewById(R.id.place_phone);
            websiteTv = itemView.findViewById(R.id.place_website);
            choose_place_iv = itemView.findViewById(R.id.choose_place_iv);
            circleImage = itemView.findViewById(R.id.place_image);
            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);
            addressLL = itemView.findViewById(R.id.place_address_ll);
            phoneLL = itemView.findViewById(R.id.place_phone_ll);
            websiteLL = itemView.findViewById(R.id.place_website_ll);
            tagsWarn = itemView.findViewById(R.id.tags_warn);
            tagsExplanation = itemView.findViewById(R.id.tags_explanation);
            progressBar = itemView.findViewById(R.id.progress_bar);
            frameLayout = itemView.findViewById(R.id.main_frame_layout);
            mainCardView = itemView.findViewById(R.id.main_card_view);
            openTV = itemView.findViewById(R.id.open_tv);
            closeTV = itemView.findViewById(R.id.close_tv);
            blankHeartIV = itemView.findViewById(R.id.blank_heart_iv);
            greenHeartIV = itemView.findViewById(R.id.green_heart_iv);

            tagsWarn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tagsExplanation.getVisibility() == View.GONE) {
                        Log.d(TAG, "onClick: show tagsExplanation");
                        tagsExplanation.setVisibility(View.VISIBLE);
                    } else {
                        Log.d(TAG, "onClick: dismiss tagsExplanation");
                        tagsExplanation.setVisibility(View.GONE);
                    }
                }
            });

            phoneLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: phone click.");
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + phoneTV.getText().toString()));

                    if (ActivityCompat.checkSelfPermission(mContext, Permissions.CALL_PHONE_PERMISSION[0]) == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "onClick: permission granted. make a call.");
                        mContext.startActivity(callIntent);
                    } else {
                        requestPhonePermission1();
                    }
                }
            });

            blankHeartIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: Like Place: " + placeID);
                    mFireMethods.likePlaceDB(placeID);
                    blankHeartIV.setVisibility(View.GONE);
                    greenHeartIV.setVisibility(View.VISIBLE);
                }
            });
            greenHeartIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: Unlike Place:  " + placeID);
                    mFireMethods.unlikePlaceDB(placeID);
                    blankHeartIV.setVisibility(View.VISIBLE);
                    greenHeartIV.setVisibility(View.GONE);
                }
            });
        }


    }

    /**
     * Check in the FirebaseDB if the user need to tag this place.
     *
     * @param placeId - current PlaceId.
     * @param holder  - Current Holder
     */
    private void checkExemptions(final String placeId, final MyViewHolder holder) {
        Log.d(TAG, "checkExemptions: checking exemptions... for place: " + placeId);
        mRef.child(mContext.getString(R.string.db_tags_exemptions))
                .child(placeId)
                .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            if (dataSnapshot.exists() && dataSnapshot.getValue(Boolean.class)) {
                                holder.tagsWarn.setVisibility(View.GONE);
                            }
                        } catch (NullPointerException e) {
                            Log.w(TAG, "onDataChange: NullPointerException:  " + e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "onCancelled: DBError: " + databaseError.getMessage());
                    }
                });
    }

    private void setupClickListeners(final MyViewHolder holder, final String placeId) {

        View.OnClickListener openPlaceClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "openPlaceClick: place_id: " + placeId);
                if (holder.tagsWarn.getVisibility() == View.VISIBLE) {
                    Log.d(TAG, "openPlaceClick: navigate toChoosePlaceTagsActivity.");
                    Intent intent = new Intent(mContext, ChoosePlaceTagsActivity.class);
                    intent.putExtra(mContext.getString(R.string.intent_place_id), placeId);
                    mContext.startActivity(intent);
                } else {
                    Log.d(TAG, "openPlaceClick: navigate to ViewOnePlaceActivity.");
                    Intent intent = new Intent(new Intent(mContext, ViewOnePlaceActivity.class));

                    int activityNum = 0;
                    if (activityName.equals(mContext.getString(R.string.profile_activity))) {
                        activityNum = 4;
                    } else if (activityName.equals(mContext.getString(R.string.five_places_activity))) {
                        activityNum = 3;
                    } else if (activityName.equals(mContext.getString(R.string.search_activity))) {
                        activityNum = 1;
                    } else if (activityName.equals(mContext.getString(R.string.data_activity))
                            || activityName.equals(mContext.getString(R.string.feed_activity))
                            || activityName.equals(mContext.getString(R.string.home_activity))) {
                        activityNum = 0;
                    }
                    intent.putExtra(mContext.getString(R.string.activity_number), activityName);
                    intent.putExtra(mContext.getString(R.string.activity_number), activityNum);
                    intent.putExtra(mContext.getString(R.string.intent_place_id), placeId);
                    mContext.startActivity(intent);
                }
            }
        };

        holder.choose_place_iv.setOnClickListener(openPlaceClick);
        holder.nameTV.setOnClickListener(openPlaceClick);
        holder.mainCardView.setOnClickListener(openPlaceClick);

        holder.websiteLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO(!): Add counter in the DB:      'places_click' -> place_id -> ++
                RazUtils.openUrl(mContext, holder.websiteTv.getContentDescription().toString());
            }
        });

    }

    /**
     * Take the PlaceDetails from the HashMap
     *
     * @param holder current Holder
     * @param pos    in the HashMap and the PlacesList
     */
    private void setupHolder(final MyViewHolder holder, int pos) {
        // TODO(!): minimize the calls to this method to prevent double-loading AND other problems.
        final PlaceDetails placeDetails = posPlaceHM.get(pos);
        Log.d(TAG, "setupHolder: setting up holder: " + placeDetails.toString());
        Log.d(TAG, "setupHolder: pos " + pos + ") " + placeDetails.getName());

        checkExemptions(placeDetails.getId(), holder);

        holder.placeID = placeDetails.getId();

        holder.nameTV.setText(placeDetails.getName());  // Name
        // Address
        if (placeDetails.getAddress() == null) {
            holder.addressLL.setVisibility(View.GONE);
        } else {
            holder.addressTV.setText(placeDetails.getAddress());
            holder.addressLL.setVisibility(View.VISIBLE);
        }
        // Phone
        if (placeDetails.getPhone() == null || placeDetails.getPhone().length() < 1) {
            holder.phoneLL.setVisibility(View.GONE);
        } else {
            holder.phoneTV.setText(placeDetails.getPhone());
            holder.phoneLL.setVisibility(View.VISIBLE);
        }
        // Website
        if (placeDetails.getWebsite() == null || placeDetails.getWebsite().length() < 1) {
            holder.websiteLL.setVisibility(View.GONE);
        } else {
            holder.websiteTv.setContentDescription(placeDetails.getWebsite());
            holder.websiteTv.setText(placeDetails.getWebsite());  // limited to one line
            holder.websiteLL.setVisibility(View.VISIBLE);
        }
        // Image (bitmap)
        if (placeDetails.getImg() != null) holder.circleImage.setImageBitmap(placeDetails.getImg());
        // ProgressBar
        if (checkBooleans(pos)) {
            holder.progressBar.setVisibility(View.GONE);
        }
        if (checkAllBooleans() && activityName.equals(mContext.getString(R.string.five_places_activity))) {
            FivePlacesActivity.load = false;
        }
        if (activityName.equals(mContext.getString(R.string.five_places_activity))) {
            holder.blankHeartIV.setVisibility(View.GONE);
            holder.greenHeartIV.setVisibility(View.GONE);

        } else if (placeDetails.isFavorite()) {   // Favorite
            holder.blankHeartIV.setVisibility(View.GONE);
            holder.greenHeartIV.setVisibility(View.VISIBLE);
        } else {    // Not Favorite
            holder.blankHeartIV.setVisibility(View.VISIBLE);
            holder.greenHeartIV.setVisibility(View.GONE);

            holder.tagsWarn.setVisibility(View.GONE);
            holder.tagsExplanation.setVisibility(View.GONE);
        }
    }

    private void setupBooleans() {
        if (placesIds != null)
            mBooleans = new boolean[LOADINGS_COUNT][placesIds.size()];
        else if (idDetailsHM != null)
            mBooleans = new boolean[LOADINGS_COUNT][idDetailsHM.size()];
        else
            Log.d(TAG, "setupBooleans: Error!!!! NUll");
        for (int i = 0; i < mBooleans.length; i++) {
            for (int j = 0; j < mBooleans[0].length; j++)
                mBooleans[i][j] = false;
        }
    }

    private boolean checkBooleans(int pos) {
        for (boolean[] b : mBooleans) {
            if (!b[pos])
                return false;
        }
        return true;
    }

    private boolean checkAllBooleans() {
        for (boolean[] booleans : mBooleans) {
            for (boolean b : booleans)
                if (!b)
                    return false;
        }
        return true;
    }

    private void requestPhonePermission1() {
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Permissions.CALL_PHONE_PERMISSION[0])) {
            new AlertDialog.Builder(mContext)
                    .setTitle("Call Permission needed")
                    .setMessage("This permission is required to call to restaurants")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) mContext, Permissions.CALL_PHONE_PERMISSION, CALL_PERMISSION_CODE);
                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions((Activity) mContext, Permissions.CALL_PHONE_PERMISSION, CALL_PERMISSION_CODE);
        }
    }

    private void requestPhonePermission() {
        // TODO(!): Check Call Permission request and Action !
        if (ActivityCompat.checkSelfPermission(mContext, Permissions.CALL_PHONE_PERMISSION[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, Permissions.CALL_PHONE_PERMISSION, CALL_PERMISSION_CODE);
        }
    }


}

