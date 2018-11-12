package razdob.cycler.myUtils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import razdob.cycler.R;
import razdob.cycler.algorithms.MyAlgorithm;
import razdob.cycler.feed.HomeActivity;
import razdob.cycler.instProfile.AccountSettingsActivity;
import razdob.cycler.instProfile.InstProfileActivity;
import razdob.cycler.models.InstComment;
import razdob.cycler.models.Photo;
import razdob.cycler.models.Tag;
import razdob.cycler.models.User;
import razdob.cycler.models.UserAccountSettings;
import razdob.cycler.models.UserSettings;

/**
 * Created by Raz on 04/06/2018, for project: PlacePicker2
 */
public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private String mUserID;
    private DatabaseReference mRef;
    private StorageReference mStorageReference;
    /* ---------------------- FIREBASE ----------------------- */

    // Vars
    private Context mContext;
    private double mPhotoUploadProgress = 0;
    private MyAlgorithm mAlgorithm;
    private User myUser;
    // Get place Name
    private GeoDataClient mGeoDataClient;

    // Progress
    private ProgressDialog mProgressDialog;

    // -------------------------- Constructor --------------------------------- //

    /**
     * Constructor. initiate:
     * mAuth
     * mRef (Main)
     * Storage (Main)
     * UserID (if mAuth!= null)
     * mAlgorithm (Use the mRef DataSnapshot)
     * mFavoritePlacesIds - According the mRef-DS
     * <p>
     * creates ProgressDialog
     *
     * @param context current Context
     */
    public FirebaseMethods(Context context) {
        mContext = context;

        // Retrieve mFireApp app.
        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();
        FirebaseStorage storage = FirebaseStorage.getInstance(mFireApp, "gs://cyclerproject.appspot.com");
        mStorageReference = storage.getReference();

        if (mAuth.getCurrentUser() != null) mUserID = mAuth.getCurrentUser().getUid();

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mFireApp = FirebaseApp.getInstance("mFireApp");
                mAlgorithm = new MyAlgorithm(mContext, dataSnapshot, mUserID);
                myUser = dataSnapshot
                        .child(mContext.getString(R.string.db_persons))
                        .child(mUserID).getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: DatabaseError: " + databaseError.getMessage());
            }
        });

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setCanceledOnTouchOutside(false);
    }


    // -------------------------- Current Time Stamp --------------------------------- //

    /**
     * @return String representing the time in this moment in Israel
     */
    private String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss'z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Israel"));
        Log.d(TAG, "getTimeStamp: Israel: " + sdf.format(new Date()));
        return sdf.format(new Date());
    }


   /*
    -------------------------------------------------------------------------------
    ----------------------------------- Photos ------------------------------------
    -------------------------------------------------------------------------------
    */

    // -------------------------- New Photo Object ----------------------------------- //

    /**
     * Returns Photo object according the caption and the url.
     * with the attrs:
     * - caption
     * - image_path(url)
     * - date_creates (current time)
     * - tags (according the caption)
     * - user_id (mUserID)
     * - photo_id (from the push() function)
     *
     * @param caption - photo's caption
     * @param url     - url from the Firebase Storage
     * @return - new Photo Object.
     */
    private Photo createPhoto(String caption, String url) {
        Log.d(TAG, "createPhoto: caption: " + caption + "; url: " + url);
        String user_id = mUserID != null ? mUserID : mAuth.getCurrentUser().getUid();
        String tags = StringManipulation.getTags(caption);
        String newPhotoKey = mRef.child(mContext.getString(R.string.db_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setImage_path(url);
        photo.setDate_creates(getTimeStamp());
        photo.setTags(tags);
        photo.setUser_id(user_id);
        photo.setPhoto_id(newPhotoKey);

        return photo;
    }


    // -------------------------- Photos uploading ------------------------------ //

    /**
     * Main Upload-Photo-Method.
     * Calls to the correct method:
     *
     * @param photoType - decides which method to call:
     *                  R.string.place_photo -> uploadPlacePhoto(...)
     *                  R.string.new_photo -> uploadCasualPhoto(...)
     *                  R.string.profile_photo -> uploadProfilePhoto(...)
     * @param caption   - photo's caption.
     * @param count     - only if this is a 'new_photo'
     * @param imgUrl    - the image path in the device
     * @param bm        - bitmap (unnecessary)
     * @param placeId   - Only gor place_photo
     */
    public void uploadNewPhoto(String photoType, final String caption, long count, final String imgUrl, Bitmap bm, String placeId) {
        uploadNewPhoto(photoType, caption, count, imgUrl, bm, placeId, null);
    }

    public void uploadNewPhoto(String photoType, final String caption, long count, final String imgUrl, Bitmap bm, String placeId, String placeTags) {
        Log.d(TAG, "uploadNewPhoto: Attempting to upload new photo: " + imgUrl);
        FilePaths filePaths = new FilePaths();
        if (photoType.equals(mContext.getString(R.string.place_photo))) {
            if (placeId == null) {
                Toast.makeText(mContext, "Something went wrong. please try again", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "uploadNewPhoto: place ID is Null !!!!");
                Log.d(TAG, "uploadNewPhoto: place ID is Null !!!!");
            } else
                uploadPlacePhoto(filePaths, caption, count, bm, imgUrl, placeId, placeTags);
        } else if (photoType.equals(mContext.getString(R.string.casual_photo))) {
            uploadCasualPhoto(filePaths, imgUrl, bm, caption);

        } else if (photoType.equals(mContext.getString(R.string.profile_photo))) {
            uploadProfilePhoto(filePaths, imgUrl, bm);
        }
    }

    // -------------------------- Profile Photo ----------------------------------- //

    /**
     * Upload a photo as a profile Photo.
     * Storage path:   'images' -> 'persons' -> user_id -> 'profile_photo'
     * <p>
     * Uses uploadProfilePhotoTask(...)
     *
     * @param filePaths - MY_OBJECT...
     * @param imgUrl    - Url in the device
     * @param bm        - if there is not bitmap --> there is an URL
     */
    private void uploadProfilePhoto(FilePaths filePaths, String imgUrl, Bitmap bm) {
        Log.d(TAG, "uploadProfilePhoto: uploading new PROFILE photo.");
        if (mUserID == null) mUserID = mAuth.getCurrentUser().getUid();
        StorageReference storageReference = mStorageReference
                .child(filePaths.FIREBASE_IMAGE_STORAGE_PERSONS + "/" + mUserID + "/profile_photo");

        // Convert image url to bitmap
        if (bm == null) {
            bm = ImageManager.getBitmap(imgUrl);
        }
        byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

        UploadTask uploadTask;
        if (bytes != null) {
            uploadTask = storageReference.putBytes(bytes);

            uploadProfilePhotoTask(uploadTask, "Profile photo uploading...");
        }
    }

    /**
     * Uploads the photo as a profile photo to the storage.
     * Save in the DB with putProfilePhotoDB(...)
     *
     * @param uploadTask     - The uploading task (to Firebase Storage)
     * @param loadingMessage - Message that will showed when the photo upload is loading.
     */
    private void uploadProfilePhotoTask(UploadTask uploadTask, final String loadingMessage) {
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: photo upload success !");

                Uri firebaseUrl = taskSnapshot.getUploadSessionUri();
                taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            putProfilePhotoDB(task.getResult().toString());
                        }

                    }
                });
//                if (firebaseUrl != null) {
//                    putProfilePhotoDB(firebaseUrl.toString());
//                } else {
//                    Log.d(TAG, "onSuccess: OOPS !");
//                }

                ((AccountSettingsActivity) mContext).setViewPager(AccountSettingsActivity.EDIT_PROFILE_FRAGMENT_NUM);
                if (mProgressDialog.isShowing())
                    mProgressDialog.cancel();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.d(TAG, "onProgress: upload progress: " + progress + "% done.");

                mProgressDialog.setMessage(loadingMessage + (int) progress + " %");
                mProgressDialog.show();
                mPhotoUploadProgress = progress;

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: photo upload failed. " + e.getMessage());
                if (mProgressDialog.isShowing()) mProgressDialog.cancel();
                Toast.makeText(mContext, "photo upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Saves a profile photo in the DB:
     * 'persons' -> user_id -> 'profile_photo' -> url
     *
     * @param url - Firebase Storage url for the photo.
     */
    private void putProfilePhotoDB(String url) {
        Log.d(TAG, "putProfilePhotoDB: setting new profile image: " + url);

        if (mUserID == null) mUserID = mAuth.getCurrentUser().getUid();

        mRef.child(mContext.getString(R.string.db_persons))
                .child(mUserID)
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);
    }

    // -------------------------- Casual Photo ----------------------------------- //

    /**
     * Uploads a casual photo (Not connected to a place).
     * Storage path:   'images' -> 'persons' -> user_id -> 'photos' -> (count+1)
     * <p>
     * Uses uploadCasualPhotoTask(...)
     *
     * @param filePaths - MyObject...
     * @param imgUrl    - Url in the device
     * @param bm        - if there is not bitmap --> there is an URL
     * @param caption   - the photo caption.
     */
    private void uploadCasualPhoto(FilePaths filePaths, String imgUrl, Bitmap bm, final String caption) {
        Log.d(TAG, "uploadCasualPhoto: uploading NEW photo.");
        String user_id = FirebaseAuth.getInstance(mFireApp).getCurrentUser().getUid();
        Photo photo = createPhoto(caption, imgUrl);

        StorageReference storageReference = mStorageReference
                .child(filePaths.FIREBASE_IMAGE_STORAGE_PERSONS + "/" + user_id + "/" + photo.getDate_creates());

        // Convert image url to bitmap
        if (bm == null) {
            bm = ImageManager.getBitmap(imgUrl);
        }
        byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

        UploadTask uploadTask = storageReference.putBytes(bytes);

        uploadCasualPhotoTask(uploadTask, photo);

    }

    /**
     * Uploads a casual photo to the storage.
     * Saves in the DB with putCasualPhotoDb(...)
     *
     * @param uploadTask - The uploading task (to Firebase Storage)
     * @param photo      - The uploaded photo.
     */
    private void uploadCasualPhotoTask(UploadTask uploadTask, final Photo photo) {
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri firebaseUrl = taskSnapshot.getUploadSessionUri();

                //add the new photo to 'photo' node and 'user_photos' node
                photo.setImage_path(Objects.requireNonNull(firebaseUrl).toString());
                putCasualPhotoDb(photo);

                // navigate to the main feed so the user can see their photos
                HomeActivity.start(mContext);
                if (mProgressDialog.isShowing()) mProgressDialog.dismiss();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.d(TAG, "onProgress: upload progress: " + progress + "% done.");

                mProgressDialog.setMessage("Uploading Photo..." + (int) progress + "%");
                mProgressDialog.show();

                mPhotoUploadProgress = progress;
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (mProgressDialog.isShowing()) mProgressDialog.cancel();
                Log.d(TAG, "onFailure: photo upload failed. " + e.getMessage());
                Toast.makeText(mContext, "photo upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Saves a photo in the DB:
     * 1) 'user_photos' -> user_id -> photo_id -> PhotoObject
     * 2) 'photos' -> photo_id -> PhotoObject
     *
     * @param photo - the uploaded photo.
     */
    private void putCasualPhotoDb(Photo photo) {
        Log.d(TAG, "putCasualPhotoDb: adding photo to database .");

        String photoId = photo.getPhoto_id();

        // Insert into DB
        mRef.child(mContext.getString(R.string.db_user_photos))
                .child(FirebaseAuth.getInstance(mFireApp).getCurrentUser().getUid())
                .child(photoId).setValue(photo);
        mRef.child(mContext.getString(R.string.db_photos))
                .child(photoId).setValue(photo);
    }


    // -------------------------- Place Photo ----------------------------------- //

    /**
     * Uploads a place-photo.
     * Storage path:   'images' -> 'places' -> place_id -> 'photos' -> count
     * <p>
     * Uses uploadPlacePhotoTask(...)
     *
     * @param filePaths - MyObject...
     * @param caption   - the photo caption.
     * @param count     - How many photos this user uploads to this place?
     * @param bm        - if there is not bitmap --> there is an URL
     * @param imgUrl    - Url in the device
     * @param placeId   - place photo
     */
    private void uploadPlacePhoto(final FilePaths filePaths, final String caption, long count, Bitmap bm, String imgUrl, final String placeId, String placeTags) {
        Log.d(TAG, "uploadPlacePhoto: uploading place photo.");


        if (bm == null) {
            bm = ImageManager.getBitmap(imgUrl);
        }
        final byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);


        final Photo photo = createPhoto(caption, imgUrl);
        if (placeTags != null)
            photo.setTags(photo.getTags() + StringManipulation.getTags(placeTags));


        mGeoDataClient = Places.getGeoDataClient(mContext);
        mGeoDataClient.getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    Place place = places.get(0);

                    photo.setTags(StringManipulation.getTags(photo.getCaption()));
                    photo.setPlace_id(placeId);
                    photo.setPlace_name(place.getName().toString());

                    StorageReference placeStorageRef = mStorageReference
                            .child(filePaths.FIREBASE_PLACE_IMAGE + "/" + placeId + "/" + photo.getDate_creates());
                    if (bytes != null) {
                        UploadTask uploadTask = placeStorageRef.putBytes(bytes);
                        Log.d(TAG, "onComplete: " + placeStorageRef.getDownloadUrl().toString());
                        uploadPlacePhotoTask(uploadTask, photo);
                    }
                }
            }
        });


    }

    private void uploadPlacePhoto(final FilePaths filePaths, final String caption, long count, Bitmap bm, String imgUrl, final String placeId) {
        uploadPlacePhoto(filePaths, caption, count, bm, imgUrl, placeId, null);
    }

    /**
     * Uploads the a photo to the storage as a place's photo.
     * Saves in the DB with putPlacePhotoDB.
     *
     * @param uploadTask - The uploading task (to Firebase Storage).
     */
    private void uploadPlacePhotoTask(UploadTask uploadTask, final Photo photo) {
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri firebaseUrl = taskSnapshot.getUploadSessionUri();

                if (taskSnapshot.getTask().isSuccessful()) {
                    taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                String photoPath = task.getResult().toString();
                                Log.d(TAG, "onComplete: photoPath: " + photoPath);
                                photo.setImage_path(photoPath);


                                putPlacePhotoDB(photo.getPlace_id(), photo);

                                HomeActivity.start(mContext, null, null, mContext.getString(R.string.feed_activity));

                                if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
                            }
                        }
                    });
                }

                Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onSuccess: mStorageReference.getPath(): + " + mStorageReference.getPath());

//               photo.setImage_path(taskSnapshot.getStorage().getPath());

                //add the new photo to 'photo' node and 'user_photos' node
//                if (firebaseUrl != null) {
//                    photo.setImage_path(firebaseUrl.toString());
//                } else {
//                    Log.w(TAG, "onSuccess: URL IS NULL!!:  ");
//                }


            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                mProgressDialog.setMessage("Uploading Photo..." + (int) progress + "%");
                mProgressDialog.show();

                if (progress - 5 > mPhotoUploadProgress) {
                    mPhotoUploadProgress = progress;
                }
                Log.d(TAG, "onProgress: upload progress: " + progress + "% done.");
            }


        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
                Log.d(TAG, "onFailure: photo upload failed. " + e.getMessage());
                Toast.makeText(mContext, "photo upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Saves a place's photo in the DB:
     * 1) 'places_photos' -> place_id -> photo_id -> PhotoObject
     * 2) 'user_photos' -> user_id -> photo_id -> PhotoObject
     *
     * @param placeId - place's id
     */
    private void putPlacePhotoDB(final String placeId, final Photo photo) {
        Log.d(TAG, "putPlacePhotoDB: adding photo for place: " + placeId);

        // to DB
        String photoId = photo.getPhoto_id();
        mRef.child(mContext.getString(R.string.db_places_photos))
                .child(placeId)
                .child(photoId).setValue(photo);

        mRef.child(mContext.getString(R.string.db_user_photos))
                .child(mUserID)
                .child(photoId).setValue(photo);


    }


    // ------------------------- Photos Counters -------------------------------- //

    /**
     * @param mainDS - mainDS.
     * @return How many photos did the user upload till now?
     */
    public long getUserPhotosCount(DataSnapshot mainDS) {
        Log.d(TAG, "getUserPhotosCount: getChildrenCount(): " + mainDS);
        String user_id = mUserID != null ? mUserID : mAuth.getCurrentUser().getUid();
        return mainDS
                .child(mContext.getString(R.string.db_user_photos))
                .child(user_id).getChildrenCount();
    }

    /**
     * @param mainDS  - mainDS
     * @param placeId - place's id
     * @return - how many photos of this place was uploaded?
     */
    public long getPlaceImageCount(DataSnapshot mainDS, String placeId) {

        return mainDS.child(mContext.getString(R.string.db_places_photos))
                .child(placeId)
                .getChildrenCount();
    }


    // -------------------------- Delete Photo ----------------------------------- //

    /**
     * Delete the photo from Firebase. Uses the methods:
     * deletePhotoDB(photo).
     * deletePhotoStorage(photo, ...)
     *
     * @param photo - The photo for delete
     */
    public void deletePhoto(Photo photo, String activityAfter, HashMap<String, String> extras) {
        deletePhotoDB(photo);

        FilePaths filePaths = new FilePaths();
        deletePhotoStorage(photo, filePaths, activityAfter, extras);
    }

    /**
     * Deletes the photo from the Firebase Storage.
     * if the photo is a Place-Photo:
     * goes to FIREBASE_PLACE_IMAGE and delete the photo from the correct place.
     * else:
     * goes to FIREBASE_IMAGE_STORAGE_PERSONS and delete the photo from the correct user (Person).
     *
     * @param photo     - The photo to delete.
     * @param filePaths - MyObject.
     */
    private void deletePhotoStorage(final Photo photo, final FilePaths filePaths, final String activityAfter, final HashMap<String, String> extras) {
        if (photo.getPlace_id() != null) {
            // ----------------------- Place Photo ------------------------ //
            Log.d(TAG, "deletePhotoStorage: Place-Photo");
            final String pl_id = photo.getPlace_id();
            StorageReference placeStorageRef = mStorageReference
                    .child(filePaths.FIREBASE_PLACE_IMAGE + "/" + pl_id + "/" + photo.getDate_creates());
            placeStorageRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: Photo deleted: " + photo.getPhoto_id());
                        if (activityAfter != null) {
                            Intent intent;
                            if (activityAfter.equals(mContext.getString(R.string.profile_activity))) {
                                intent = new Intent(mContext, InstProfileActivity.class);
                            } else {
                                intent = new Intent(mContext, HomeActivity.class);
                            }
                            if (extras != null && extras.size() > 0) {
                                for (String ext : extras.keySet()) {
                                    intent.putExtra(ext, extras.get(ext));
                                }
                            }
                            mContext.startActivity(intent);
                        } else {
                            HomeActivity.start(mContext);
                        }

                        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: photo delete FAIL! " + e.getMessage());
                    if (mProgressDialog.isShowing()) mProgressDialog.cancel();
                }
            });


        } else {
            // ----------------------- CasualPhoto ----------------------- //
            Log.d(TAG, "deletePhotoStorage: Casual-Photo");
            String uid = photo.getUser_id();
            StorageReference personStorageRef = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE_PERSONS + "/" + uid + "/" + photo.getDate_creates());
            personStorageRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: Photo deleted: " + photo.getPhoto_id());
                        InstProfileActivity.start(mContext);
                        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: photo delete FAIL! " + e.getMessage());
                    if (mProgressDialog.isShowing()) mProgressDialog.cancel();
                }
            });
        }
    }

    /**
     * Delete the photo from DB:
     * 1) 'user_photos' -> user_id -> photo_id -> PhotoObject
     * 2) if photo is PlacePhoto:
     * 'places_photos' -> place_id -> photo_id -> NULL
     * else:
     * 'photos' -> photo_id -> NULL
     *
     * @param photo - PhotoObject to delete.
     */
    private void deletePhotoDB(Photo photo) {
        String uid = photo.getUser_id();
        if (uid == null || !uid.equals(mUserID)) return;
        String pl_id = photo.getPlace_id();
        String ph_id = photo.getPhoto_id();

        mRef.child(mContext.getString(R.string.db_user_photos))
                .child(mUserID)
                .child(ph_id).setValue(null);

        if (pl_id != null) {
            mRef.child(mContext.getString(R.string.db_places_photos))
                    .child(pl_id)
                    .child(ph_id).setValue(null);
        } else {
            mRef.child(mContext.getString(R.string.db_photos))
                    .child(ph_id).setValue(null);
        }
    }


    // ------------------------------ Photo From DB ------------------------------- //

    /**
     * Creates a photoObjecs from its DB.
     *
     * @param photoDS - DataSnapshot contains photo.
     * @return PhotoObject.
     */
    public Photo getPhotoFromDB(DataSnapshot photoDS) {
        Photo photo = new Photo();
        photo.setCaption(photoDS.child(mContext.getString(R.string.db_field_caption)).getValue(String.class));
        photo.setDate_creates(photoDS.child(mContext.getString(R.string.db_field_date_creates)).getValue(String.class));
        photo.setImage_path(photoDS.child(mContext.getString(R.string.db_field_image_path)).getValue(String.class));
        photo.setPhoto_id(photoDS.child(mContext.getString(R.string.db_field_photo_id)).getValue(String.class));
        photo.setTags(StringManipulation.getTags(photo.getCaption()));
        photo.setUser_id(photoDS.child(mContext.getString(R.string.db_field_user_id)).getValue(String.class));
        photo.setPlace_id(photoDS.child(mContext.getString(R.string.db_field_place_id)).getValue(String.class));
        photo.setPlace_name(photoDS.child(mContext.getString(R.string.db_field_place_name)).getValue(String.class));
        // Likes
        ArrayList<String> likes = new ArrayList<>();
        for (DataSnapshot likeDS : photoDS.child(mContext.getString(R.string.db_field_likes)).getChildren()) {
            likes.add(likeDS.getValue(String.class));
        }
        photo.setLikes(likes);

        // Comments
        ArrayList<InstComment> comments = new ArrayList<>();
        for (DataSnapshot commentDS : photoDS.child(mContext.getString(R.string.db_field_comments)).getChildren()) {
            comments.add(commentDS.getValue(InstComment.class));
        }
        photo.setComments(comments);

        return photo;
    }

    /**
     * @param mainDS - mainDataSnapshot.
     * @return List of Photos, place's photos FIRST.
     */
    public ArrayList<Photo> getUserPhotos(DataSnapshot mainDS, String user_id) {
        ArrayList<Photo> userPhotos = new ArrayList<>();
        DataSnapshot userAllPhotosDS = mainDS
                .child(mContext.getString(R.string.db_user_photos))
                .child(user_id);
        for (DataSnapshot userPhotoDS : userAllPhotosDS.getChildren()) {
            Photo photo = getPhotoFromDB(userPhotoDS);

            Log.d(TAG, "getUserPhotos: checkkkkk" + userPhotoDS.getValue());
            Log.d(TAG, "getUserPhotos: checkkkkk" + userPhotoDS.getValue().getClass());

            if (photo.getPlace_id() != null) {
                Log.d(TAG, "getUserPhotos: placePhoto: " + photo.getPhoto_id());
                userPhotos.add(0, photo);
            } else {
                Log.d(TAG, "getUserPhotos: casualPhoto: " + photo.getPhoto_id());
                userPhotos.add(photo);
            }
        }
        return userPhotos;
    }


    // ------------------------------ Like Photo DB ----------------------------- //

    // TODO(!):  use those functions yet !!! Need to change the LikesAlgorithm !
    // TODO(!) Continue from here! check like&Unlike

    /**
     * validate that the current user exists in the photo's favorites.
     * call to 'updatePhotoLikesDB(photo)'
     *
     * @param photo - Photo to like.
     */
    public void likePhotoDB(Photo photo) {
        // Input Check
        if (photo.getLikes().contains(mUserID)) {
            Log.d(TAG, "likePhotoDB: user already likes this photo !");
        } else {
            Log.d(TAG, "likePhotoDB: add like to photo: " + photo.getPhoto_id());
            photo.getLikes().add(mUserID);
        }

        // Updates likes
        updatePhotoLikesDB(photo);
    }

    public void unlikePhotoDB(Photo photo) {
        // Input Check
        if (!photo.getLikes().contains(mUserID)) {
            Log.d(TAG, "unlikePhotoDB: user doesn't like this photo: " + photo.getPhoto_id());
        } else {
            Log.d(TAG, "unlikePhotoDB: unlike photo: " + photo.getPhoto_id());
            photo.getLikes().remove(mUserID);
        }

        // Updates Likes
        updatePhotoLikesDB(photo);
    }

    private void updatePhotoLikesDB(Photo photo) {
        mRef.child(mContext.getString(R.string.db_user_photos))
                .child(photo.getUser_id())
                .child(photo.getPhoto_id())
                .child(mContext.getString(R.string.db_field_likes))
                .setValue(photo.getLikes());

        String place_id = photo.getPlace_id();
        if (place_id != null) {     // Place Photo
            mRef.child(mContext.getString(R.string.db_places_photos))
                    .child(place_id)
                    .child(photo.getPhoto_id())
                    .child(mContext.getString(R.string.db_field_likes))
                    .setValue(photo.getLikes());

        } else {                    // Casual Photo
            mRef.child(mContext.getString(R.string.db_photos))
                    .child(photo.getPhoto_id())
                    .child(mContext.getString(R.string.db_field_likes))
                    .setValue(photo.getLikes());
        }
    }

    public boolean isUserLikePhoto(DataSnapshot mainDS, Photo photo) {
        DataSnapshot photoLikes = mainDS.child(mContext.getString(R.string.db_user_photos))
                .child(photo.getUser_id())
                .child(photo.getPhoto_id())
                .child(mContext.getString(R.string.db_field_likes));
        for (DataSnapshot likeDS : photoLikes.getChildren()) {
            if (likeDS.getValue(String.class).equals(mUserID)) {
                return true;
            }
        }
        return false;
    }


    // ------------------------------ Photo's Comments DB  ------------------------ //

    /**
     * Add a photo's comment to Firebase Database:
     * 1) 'user_photos' -> photo's owner_id -> photo_id -> 'comments' -> commentPushID -> COMMENT
     * IF photo is PlacePhoto:
     * 'places_photos' -> place_id -> photo_id -> 'comments' -> commentPushID -> COMMENT
     * ELSE:
     * 'photos' -> photo_id -> 'comments' -> commentPushID -> COMMENT
     *
     * @param photo         - The photo the comment was written for.
     * @param commentPushID - The comment id after push it.
     * @param comment       - InstComment Object.
     */
    public void addPhotoCommentDB(Photo photo, String commentPushID, InstComment comment) {
        // Insert into photos node

        //db_user_photos
        mRef.child(mContext.getString(R.string.db_user_photos))
                .child(photo.getUser_id())
                .child(photo.getPhoto_id())
                .child(mContext.getString(R.string.db_field_comments))
                .child(commentPushID)
                .setValue(comment);


        if (photo.getPlace_id() != null) { // Place's photo

            mRef.child(mContext.getString(R.string.db_places_photos))
                    .child(photo.getPlace_id())
                    .child(photo.getPhoto_id())
                    .child(mContext.getString(R.string.db_field_comments))
                    .child(commentPushID)
                    .setValue(comment);

        } else { // Casual photo

            mRef.child(mContext.getString(R.string.db_photos))
                    .child(photo.getPhoto_id())
                    .child(mContext.getString(R.string.db_field_comments))
                    .child(commentPushID)
                    .setValue(comment);
        }
    }

    /**
     * remove comment from the photo.
     *
     * @param photo
     * @param comment_id
     */
    public void removePhotoCommentDB(Photo photo, String comment_id) {
        Log.d(TAG, "removePhotoCommentDB: comment: " + comment_id);

        //db_user_photos
        mRef.child(mContext.getString(R.string.db_user_photos))
                .child(photo.getUser_id())
                .child(photo.getPhoto_id())
                .child(mContext.getString(R.string.db_field_comments))
                .child(comment_id)
                .setValue(null);


        if (photo.getPlace_id() != null) { // Place's photo

            mRef.child(mContext.getString(R.string.db_places_photos))
                    .child(photo.getPlace_id())
                    .child(photo.getPhoto_id())
                    .child(mContext.getString(R.string.db_field_comments))
                    .child(comment_id)
                    .setValue(null);

        } else { // Casual photo

            mRef.child(mContext.getString(R.string.db_photos))
                    .child(photo.getPhoto_id())
                    .child(mContext.getString(R.string.db_field_comments))
                    .child(comment_id)
                    .setValue(null);
        }
    }

    // ------------------------------ Like & unlike Comments --------------------- //
    public void likePhotoCommentDB(Photo photo, String comment_id) {
        mRef.child(mContext.getString(R.string.db_user_photos))
                .child(photo.getUser_id())
                .child(photo.getPhoto_id())
                .child(mContext.getString(R.string.db_field_comments))
                .child(comment_id)
                .child(mContext.getString(R.string.db_field_likes))
                .child(mUserID)
                .setValue(true);
        if (photo.getPlace_id() != null) { // Place Photo
            mRef.child(mContext.getString(R.string.db_places_photos))
                    .child(photo.getPlace_id())
                    .child(photo.getPhoto_id())
                    .child(mContext.getString(R.string.db_field_comments))
                    .child(comment_id)
                    .child(mContext.getString(R.string.db_field_likes))
                    .child(mUserID)
                    .setValue(true);
        } else {  // Casual Photo
            mRef.child(mContext.getString(R.string.db_photos))
                    .child(photo.getPhoto_id())
                    .child(mContext.getString(R.string.db_field_comments))
                    .child(comment_id)
                    .child(mContext.getString(R.string.db_field_likes))
                    .child(mUserID)
                    .setValue(true);
        }
    }

    public void unlikePhotoCommentDB(Photo photo, String comment_id) {
        mRef.child(mContext.getString(R.string.db_user_photos))
                .child(photo.getUser_id())
                .child(photo.getPhoto_id())
                .child(mContext.getString(R.string.db_field_comments))
                .child(comment_id)
                .child(mContext.getString(R.string.db_field_likes))
                .child(mUserID)
                .setValue(null);
        if (photo.getPlace_id() != null) { // Place Photo
            mRef.child(mContext.getString(R.string.db_places_photos))
                    .child(photo.getPlace_id())
                    .child(photo.getPhoto_id())
                    .child(mContext.getString(R.string.db_field_comments))
                    .child(comment_id)
                    .child(mContext.getString(R.string.db_field_likes))
                    .child(mUserID)
                    .setValue(null);
        } else {  // Casual Photo
            mRef.child(mContext.getString(R.string.db_photos))
                    .child(photo.getPhoto_id())
                    .child(mContext.getString(R.string.db_field_comments))
                    .child(comment_id)
                    .child(mContext.getString(R.string.db_field_likes))
                    .child(mUserID)
                    .setValue(null);
        }
    }






    /*
    -------------------------------------------------------------------------------
    -------------------------- Retrieve User Details ------------------------------
    -------------------------------------------------------------------------------
    */

    /**
     * Checks:
     * 1) User in the DB in the Persons & UserAccountSettings.
     * 2) User has name (in the persons & UserAccountSettings) [not null & Length > 0].
     *
     * @param mainDS - Main Database's Snapshot.
     * @return - true if the current user has name in the DB, else false.
     */
    public boolean userHasName(DataSnapshot mainDS) {
        Log.d(TAG, "userHasName: checks if the current user has name in the DB.");

        DataSnapshot personsDS = mainDS.child(mContext.getString(R.string.db_persons));
        DataSnapshot usersAccountSettingsDS = mainDS.child(mContext.getString(R.string.db_user_account_settings));

        if (!personsDS.hasChild(mUserID)) {
            Log.d(TAG, "userHasName: user not exists in the PERSONS-DB !");
            return false;
        }
        if (!usersAccountSettingsDS.hasChild(mUserID)) {
            Log.d(TAG, "userHasName: user not exists in the USER_ACCOUNT_SETTINGS-DB !");
            return false;
        }
        if (!personsDS.child(mUserID).hasChild(mContext.getString(R.string.db_field_name))) {
            Log.d(TAG, "userHasName: user has no name [DB: 'persons' -> 'name']");
            return false;
        }
        if (Objects.requireNonNull(personsDS.child(mUserID).child(mContext.getString(R.string.db_field_name)).getValue(String.class)).length() == 0) {
            Log.d(TAG, "userHasName: name's length is 0! [DB: 'persons' -> 'name']");
            return false;
        }
        if (!usersAccountSettingsDS.child(mUserID).hasChild(mContext.getString(R.string.db_field_username))) {
            Log.d(TAG, "userHasName: user has no name [DB: 'user_account_settings' -> userName]");
            return false;
        }
        if (Objects.requireNonNull(usersAccountSettingsDS.child(mUserID).child(mContext.getString(R.string.db_field_username)).getValue(String.class)).length() == 0) {
            Log.d(TAG, "userHasName: name's length is 0! [DB: 'user_account_settings' -> userName]");
            return false;
        }

        return true;
    }

    /**
     * Current userAccountSettings
     *
     * @param mainDS
     * @return
     */
    public UserSettings getUserSettings(DataSnapshot mainDS, String uid) {
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from firebase");

        User user = getUserById(mainDS, uid);
        UserAccountSettings settings = new UserAccountSettings();
        if (mainDS.child(mContext.getString(R.string.db_user_account_settings)).hasChild(uid))
            settings = mainDS.child(mContext.getString(R.string.db_user_account_settings)).child(uid).getValue(UserAccountSettings.class);

        return new UserSettings(user, settings);
    }

    /**
     * Gets a user_id and returns its followings (the users that he follows after).
     *
     * @param uid    - The user to get his followings.
     * @param mainDS - main DataSnapshot.
     * @return List of the user's followings.
     */
    public ArrayList<String> getUserFollowings(String uid, DataSnapshot mainDS) {
        Log.d(TAG, "getUserFollowings: uid: " + uid);
        ArrayList<String> followingsIds = new ArrayList<>();

        for (DataSnapshot followingDS : mainDS.child(mContext.getString(R.string.db_field_following))
                .child(uid).getChildren()) {
            followingsIds.add(followingDS.getKey());
        }
        return followingsIds;
    }


    /*
    -------------------------------------------------------------------------------
    ----------------------------- User Details Updates ----------------------------
    -------------------------------------------------------------------------------
    */

    // -------------------------- Account Settings -------------------------------- //

    public void updateUserDB(String uid, User user, UserAccountSettings accountSettings) {
        if (user.getEmail() == null) {
            String email = "";
            if (myUser.getEmail() != null) {
                email = myUser.getEmail();
            } else if (Objects.requireNonNull(mAuth.getCurrentUser()).getEmail() != null)
                email = mAuth.getCurrentUser().getEmail();
            else {
                Log.w(TAG, "updateUserDB: NO EMAIL !! Check it out !!");
            }
            user.setEmail(email);
        }
        mRef.child(mContext.getString(R.string.db_persons)).child(uid).setValue(user);
        mRef.child(mContext.getString(R.string.db_user_account_settings)).child(uid).setValue(accountSettings);
        myUser = user;
    }

    /**
     * Updates 'user_account_settings' for current user in the DB:
     * 'user_account_settings' -> user_id -> itemName -> itemValue
     *
     * @param displayName
     * @param website
     * @param description
     */
    public void updateUserAccountSettingsDB(String displayName, String website, String description) {

        Log.d(TAG, "updateUserAccountSettingsDB: updating user_account_settings");

        if (displayName == null) Log.d(TAG, "updateUserAccountSettingsDB: Delete displayName");
        if (website == null) Log.d(TAG, "updateUserAccountSettingsDB: Delete website");
        if (description == null) Log.d(TAG, "updateUserAccountSettingsDB: Delete description");

        mRef.child(mContext.getString(R.string.db_user_account_settings))
                .child(mUserID)
                .child(mContext.getString(R.string.db_field_display_name))
                .setValue(displayName);
        mRef.child(mContext.getString(R.string.db_user_account_settings))
                .child(mUserID)
                .child(mContext.getString(R.string.db_field_website))
                .setValue(website);
        mRef.child(mContext.getString(R.string.db_user_account_settings))
                .child(mUserID)
                .child(mContext.getString(R.string.db_field_description))
                .setValue(description);
    }

    // User top details
    public void updatePosts(long posts) {
        mRef.child(mContext.getString(R.string.db_user_account_settings))
                .child(mUserID)
                .child(mContext.getString(R.string.db_field_posts))
                .setValue(posts);
    }

    public void updateFollowers(long followers) {
        mRef.child(mContext.getString(R.string.db_user_account_settings))
                .child(mUserID)
                .child(mContext.getString(R.string.db_field_followers))
                .setValue(followers);
    }

    public void updateFollowings(long followings) {
        mRef.child(mContext.getString(R.string.db_user_account_settings))
                .child(mUserID)
                .child(mContext.getString(R.string.db_field_following))
                .setValue(followings);
    }

    // ---------------------------------- Phone ----------------------------------- //

    /**
     * Updates the user's phone in the DB:
     * 'persons' -> user_id -> 'phone' -> phone
     *
     * @param phone
     */
    public void updateUserPhone(String phone) {
        Log.d(TAG, "updateUserPhone: phone: " + phone);
        if (phone == null) Log.d(TAG, "updateUserPhone: delete phone");
        mRef.child(mContext.getString(R.string.db_persons))
                .child(mUserID)
                .child(mContext.getString(R.string.db_field_phone))
                .setValue(phone);
    }


    // ---------------------------------- User Name ------------------------------- //

    /**
     * update the userName in the DB:
     * 1) 'persons' -> user_id -> 'name' -> userName
     * 2) 'user_account_settings' -> user_id -> 'userName' -> userName
     *
     * @param userName - new UserName
     */
    public void updateUserName(String userName) {
        Log.d(TAG, "updateUserName: updating userName to: " + userName);
        if (userName == null || userName.length() == 0) {
            Log.d(TAG, "updateUserName: userName is null !");
            Toast.makeText(mContext, "please, write a userName", Toast.LENGTH_SHORT).show();
        } else {
            mRef.child(mContext.getString(R.string.db_persons))
                    .child(mUserID)
                    .child(mContext.getString(R.string.db_field_name))
                    .setValue(userName);
            mRef.child(mContext.getString(R.string.db_user_account_settings))
                    .child(mUserID)
                    .child(mContext.getString(R.string.db_field_username))
                    .setValue(userName);
        }
    }


    // ------------------------------------ Email --------------------------------- //

    /**
     * update the email in the DB:
     * 'persons' -> user_id -> 'email' -> email
     *
     * @param email - new User's email.
     */
    public void updateEmailDB(String email) {
        Log.d(TAG, "updateEmailDB: updating email to: " + email);
        // TODO(2): Validate email...

        mRef.child(mContext.getString(R.string.db_persons))
                .child(mUserID)
                .child(mContext.getString(R.string.db_field_email))
                .setValue(email);

    }


    public void registerNewEmail(final String email, String password, final String userName) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmailAndPassword:onComplete: " + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Toast.makeText(mContext, "auth Failed", Toast.LENGTH_SHORT).show();
                        } else if (task.isSuccessful()) {
                            mUserID = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "onComplete: AuthState changed: " + mUserID);

                        }
                    }
                });
    }






    /*
    -------------------------------------------------------------------------------
    ------------------------------------ Places -----------------------------------
    -------------------------------------------------------------------------------
    */

    /**
     *
     * @return myUser's favorites places.
     */
    public ArrayList<String> getFavoritePlacesIds() {
        if (myUser == null) return null ;
        return (ArrayList<String>) myUser.getFavoritePlacesIDs();
    }

    /**
     *
     * @param mainDS - Main DataSnapshot.
     * @param uid - User's id to get his favorites places.
     * @return - user's (with uid) favorite placesIds.
     */
    public ArrayList<String> getFavoritePlacesIds(DataSnapshot mainDS, String uid) {
        ArrayList<String> favoritePlacesIds = new ArrayList<>();
        for (DataSnapshot ds : mainDS.child(mContext.getString(R.string.db_persons))
                .child(uid)
                .child(mContext.getString(R.string.db_field_favorite_places_ids))
                .getChildren()) {
            favoritePlacesIds.add(ds.getValue(String.class));
        }
        return favoritePlacesIds;
    }

    /**
     *
     * @param mainDS - Main DataSnapshot.
     * @return - current user's favorite places from the DB.
     */
    public ArrayList<String> getFavoritePlacesIds(DataSnapshot mainDS) {
        return getFavoritePlacesIds(mainDS, Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
    }



    /**
     * Marks the place as restaurant:
     * 'Restaurants' -> place_id -> true
     *
     * @param place_id - place's id to mark.
     */
    public void markPlaceAsRestaurantDB(String place_id) {
        mRef.child(mContext.getString(R.string.db_restaurants)).child(place_id).setValue(true);
    }

    /**
     * Checks if the place is showed as a restaurant in the DB.
     *
     * @param mainDS   - mainDataSnapshot
     * @param place_id - placeId the check.
     * @return if the place is restaurant according the DB.
     */
    public boolean isRestaurant(DataSnapshot mainDS, String place_id, List<Integer> placeTypes) {
        Log.d(TAG, "isRestaurant: check place: " + place_id);
        if (RazUtils.isRestaurant(placeTypes)) {
            Log.d(TAG, "isRestaurant: place: " + place_id + " according its types... [RazUtils.isRestaurant()]");
            return true;
        }

        Boolean boo = mainDS.child(mContext.getString(R.string.db_restaurants)).hasChild(place_id);

        if (boo) {
            boo = mainDS
                    .child(mContext.getString(R.string.db_restaurants))
                    .child(place_id)
                    .getValue(Boolean.class);
            Log.d(TAG, "isRestaurant: boo:  " + boo);
            if (boo == null)
                return false;
            return boo;
        } else
            return false;
    }

    // ---------------------------- Place's Tags ------------------------------- //
    public void clearAndSaveTag(Tag tag, String place_id, ArrayList<String> users_ids) {
        // TODO(!) Explain this method (add a documentation).
        Log.d(TAG, "clearAndSaveTag: tag: " + tag.getTagName() + " place_id" + place_id);
        if (users_ids.size() != tag.getCounter()) tag.setCounter(users_ids.size() * -1);
        if (tag.getCounter() < -1) {
            mRef.child(mContext.getString(R.string.db_field_places)).child(place_id)
                    .child(mContext.getString(R.string.db_field_tags))
                    .child(String.valueOf(tag.getCounter() + 1)).child(tag.getTagName())
                    .removeValue();
        }
        mRef.child(mContext.getString(R.string.db_field_places)).child(place_id)
                .child(mContext.getString(R.string.db_field_tags))
                .child(String.valueOf(tag.getCounter())).child(tag.getTagName())
                .setValue(users_ids);
        mRef.child(mContext.getString(R.string.db_field_places_tag_counters))
                .child(place_id).child(tag.getTagName()).setValue(tag.getCounter());
    }

    /**
     * 1) Removes the current user's id from the users_ids (users that like this place).
     * 2) update the tag's counter.
     * 3) Remove the tag from the place in DB:
     * ...
     *
     * @param tag       - TagObject
     * @param place_id  - place's id
     * @param users_ids users that liked the place
     */
    public void removeTag(Tag tag, String place_id, ArrayList<String> users_ids) {
        if (users_ids.contains(mUserID)) users_ids.remove(mUserID);
        if (tag.getCounter() != users_ids.size() * -1) tag.setCounter(users_ids.size() * -1);

        mRef.child(mContext.getString(R.string.db_field_places))
                .child(place_id)
                .child(mContext.getString(R.string.db_field_tags))
                .child(String.valueOf(tag.getCounter() - 1))
                .child(tag.getTagName())
                .removeValue();
        if (tag.getCounter() < 0) {
            mRef.child(mContext.getString(R.string.db_field_places)).child(place_id)
                    .child(mContext.getString(R.string.db_field_tags))
                    .child(String.valueOf(tag.getCounter())).child(tag.getTagName())
                    .setValue(users_ids);
            mRef.child(mContext.getString(R.string.db_field_places_tag_counters))
                    .child(place_id).child(tag.getTagName()).setValue(tag.getCounter());
        } else {
            mRef.child(mContext.getString(R.string.db_field_places_tag_counters))
                    .child(place_id).child(tag.getTagName()).removeValue();
        }
    }

    /**
     * Uploads the tag to the FirebaseDB:
     * 1) 'tags' -> ONLY ENGLISH !
     * 2) 'places' -> PLACE_ID -> 'tags' -> -1 -> TAG_TEXT -> UID (in a list with size 0)       [0: UID]
     * 3) 'places_tags_counters' -> PLACE_ID -> [TAG_TEXT: -1]
     * <p>
     * and sends a Toast.
     *
     * @param tag:    tagName (English)
     * @param placeId
     */
    public void saveNewPlaceTagFromUser(String tag, String placeId) {
        tag = StringManipulation.placeTagFormat(tag);
        mRef.child(mContext.getString(R.string.db_field_tags))
                .child(tag).child("en").setValue(tag);
        ArrayList<String> userList = new ArrayList<>();
        userList.add(mUserID);

        mRef.child(mContext.getString(R.string.db_field_places)).child(placeId).child(mContext.getString(R.string.db_field_tags))
                .child("-1").child(tag).setValue(userList);
        mRef.child(mContext.getString(R.string.db_field_places_tag_counters)).child(placeId).child(tag).setValue(-1);
        Toast.makeText(mContext, "your tag saved !", Toast.LENGTH_SHORT).show();
    }

    public ArrayList<String> getAllTags(DataSnapshot mainDS) {
        ArrayList<String> tags = new ArrayList<>();
        for (DataSnapshot ds : mainDS.child(mContext.getString(R.string.db_field_tags)).getChildren()) {
            String tag = ds.child("en").getValue(String.class);
            tags.add(tag);
        }
        return tags;
    }


    // -------------------------- Like & Unlike Places ----------------------------------- //

    /**
     * 1) Add the placeId to the mFavoritePlacesIds.
     * 2) Add place-like to the DB:
     * a) 'persons' -> user_id -> 'favoritePlacesIDs' -> updated mFavoritePlacesIds.
     * b) 'places_likes' -> place_id -> user_id -> true
     * 3) Like place in to the mAlgorithm.
     *
     * @param placeId - place's id
     */
    public void likePlaceDB(String placeId) {
        Log.d(TAG, "likePlaceDB: user: " + mUserID + " likes place: " + placeId);
        ArrayList<String> favoritePlacesIds = getFavoritePlacesIds();
        if (favoritePlacesIds.contains(placeId)) return;    // User already like this place

        // 'persons' -> UID -> 'favorites_places_ids'   [LIST]add: PLACE_ID
        favoritePlacesIds.add(placeId);
        mRef
                .child(mContext.getString(R.string.db_persons))
                .child(mUserID)
                .child(mContext.getString(R.string.db_field_favorite_places_ids))
                .setValue(favoritePlacesIds);

        // 'places_likes' -> PLACE_ID -> UID -> true
        mRef
                .child(mContext.getString(R.string.db_field_places_likes))
                .child(placeId)
                .child(mUserID)
                .setValue(true);

        mAlgorithm.likePlaceGraph(placeId);
        myUser.setFavoritePlacesIDs(favoritePlacesIds);
    }

    /**
     * 1) Removes the placeId to the mFavoritePlacesIds.
     * 2) Remove Add place-like to the DB:
     * a) 'persons' -> user_id -> 'favoritePlacesIDs' -> updated mFavoritePlacesIds.
     * b) 'places_likes' -> place_id -> user_id -> null        [Delete]
     * 3) Unlike place in the mAlgorithm.
     *
     * @param placeId - place's id
     */
    public void unlikePlaceDB(String placeId) {
        Log.d(TAG, "unlikePlaceDB: user: " + mUserID + " unlikes place: " + placeId);
        ArrayList<String> favoritePlacesIds = getFavoritePlacesIds();

        if (!favoritePlacesIds.contains(placeId)) return;  // User does not like this place

        // 'persons' -> UID -> 'favorites_places_ids'   [LIST]remove: PLACE_ID
        favoritePlacesIds.remove(placeId);
        mRef
                .child(mContext.getString(R.string.db_persons))
                .child(mUserID)
                .child(mContext.getString(R.string.db_field_favorite_places_ids))
                .setValue(favoritePlacesIds);

        // 'places_likes' -> PLACE_ID -> UID -> null
        mRef
                .child(mContext.getString(R.string.db_field_places_likes))
                .child(placeId)
                .child(mUserID)
                .setValue(null);

        mAlgorithm.unlikePlaceGraph(placeId);
        myUser.setFavoritePlacesIDs(favoritePlacesIds);
    }


    // ----------------------------- Rating Places --------------------------------------- //

    /**
     * Save the rate in the DB:
     * 'places_rates' -> place_id -> (current) user_id -> rate.
     *
     * @param place_id - the rated place's id.
     * @param rate     - rate (1 - 5)
     */
    public void ratePlaceDB(String place_id, int rate) {
        if (rate < 1 || rate > 5) {
            Log.d(TAG, "ratePlaceDB: invalid rate: " + rate);
            return;
        }
        mRef.child(mContext.getString(R.string.db_places_rates))
                .child(place_id)
                .child(mUserID)
                .setValue(rate);
    }

    /**
     * @param mainDS
     * @param place_id
     * @param user_id
     * @return The rate that user_id gave to place_id.
     */
    public int getPlaceRateDB(DataSnapshot mainDS, String place_id, String user_id) {
        Integer rate = mainDS.child(mContext.getString(R.string.db_places_rates))
                .child(place_id)
                .child(user_id).getValue(Integer.class);
        if (rate == null) return -1;
        else return rate;
    }


    // -------------------------------- Places Photos ------------------------------------ //

    /**
     * @param mainDS  - mainDS
     * @param placeId - place's id
     * @return lList of place's photos
     */
    public ArrayList<Photo> getPlacePhotos(DataSnapshot mainDS, String placeId) {
        ArrayList<Photo> placePhotos = new ArrayList<>();
        DataSnapshot placePhotosDS = mainDS
                .child(mContext.getString(R.string.db_places_photos))
                .child(placeId);
        for (DataSnapshot placePhotoDS : placePhotosDS.getChildren()) {
            Photo photo = getPhotoFromDB(placePhotoDS);
            if (photo != null) {
                placePhotos.add(photo);
            }
        }
        return placePhotos;
    }

    // -------------------------- Places Website Clicks Counter -------------------------- //
    // TODO(BETA) !
    public void websiteClickDB(final String placeId) {
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mContext.getString(R.string.db_websites_clicks_counter)) &&
                        dataSnapshot.child(mContext.getString(R.string.db_websites_clicks_counter))
                                .hasChild(placeId)) {
                    Long currentCount = dataSnapshot.child(mContext.getString(R.string.db_websites_clicks_counter))
                            .child(placeId)
                            .getValue(Long.class);
                    if (currentCount == null) currentCount = 0L;
                    mRef.child(mContext.getString(R.string.db_websites_clicks_counter))
                            .child(placeId).setValue(currentCount + 1);
                } else {
                    mRef.child(mContext.getString(R.string.db_websites_clicks_counter))
                            .child(placeId).setValue(1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                FireBaseUtils.dbErrorMessage(TAG, databaseError);
            }
        });

    }

    // ----------------------------- Place As Restaurant --------------------------------- //
    // 3 are not In used
    // TODO(BETA) !

    /*
    -------------------------------------------------------------------------------
    -------------------------------- User Object ----------------------------------
    -------------------------------------------------------------------------------
    */
    public User getCurrentUser() {
        return myUser;
    }


    /*
    -------------------------------------------------------------------------------
    -------------------------------- Unused Functions -----------------------------
    -------------------------------------------------------------------------------
    */
    public ArrayList<InstComment> getPhotoCommentsPhotosDB(DataSnapshot mainDS, String photo_id) {
        ArrayList<InstComment> comments = new ArrayList<>();
        Log.d(TAG, "getPhotoCommentsPhotosDB: loading comments of photo: " + photo_id);
        for (DataSnapshot commentDS : mainDS
                .child(mContext.getString(R.string.db_photos))
                .child(photo_id)
                .child(mContext.getString(R.string.db_field_comments)).getChildren()) {
            comments.add(commentDS.getValue(InstComment.class));
        }
        return comments;
    }

    public ArrayList<InstComment> getPlacePhotoCommentsDB(DataSnapshot mainDS, String photo_id, String place_id) {
        ArrayList<InstComment> comments = new ArrayList<>();
        Log.d(TAG, "getPhotoCommentsPhotosDB: loading comments of photo: " + photo_id);
        for (DataSnapshot commentDS : mainDS
                .child(mContext.getString(R.string.db_places_photos))
                .child(place_id)
                .child(photo_id)
                .child(mContext.getString(R.string.db_field_comments)).getChildren()) {
            comments.add(commentDS.getValue(InstComment.class));
        }
        return comments;
    }

    public User getUserById(DataSnapshot mainDS, String uid) {
        return mainDS
                .child(mContext.getString(R.string.db_persons))
                .child(uid).getValue(User.class);
    }

}


