package razdob.cycler;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import razdob.cycler.R;
import razdob.cycler.adapters.InstCommentListAdapter;
import razdob.cycler.instHome.InstHomeActivity;
import razdob.cycler.models.InstComment;
import razdob.cycler.models.Photo;
import razdob.cycler.myUtils.FirebaseMethods;

/**
 * Created by Raz on 14/06/2018, for project: PlacePicker2
 */
public class ViewInstCommentsFragment extends Fragment {
    private static final String TAG = "ViewInstCommentsFrag";

    public ViewInstCommentsFragment() {
        super();
        setArguments(new Bundle());
    }

    /* ---------------------- FIREBASE ----------------------- */
    private FirebaseApp mFireApp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mRef;
    private FirebaseMethods mFirebaseMethods;
    /* ---------------------- FIREBASE ----------------------- */

    // Widgets
    private ImageView mBackArrow, mCheckMark;
    private EditText mCommentET;
    private ListView mListView;

    //Vars
    private Photo mPhoto;
    private ArrayList<InstComment> mComments;
    private Context mContext;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inst_view_comments, container, false);
        mBackArrow = view.findViewById(R.id.back_arrow_iv);
        mCheckMark = view.findViewById(R.id.post_comment_iv);
        mCommentET = view.findViewById(R.id.comment_et);
        mListView = view.findViewById(R.id.list_view);
        mComments = new ArrayList<>();
        mContext = getActivity();
        mFirebaseMethods = new FirebaseMethods(mContext);
        try {
            mPhoto = getPhotoFromBundle();
            setupWidgets();
            setupFirebaseStaff();
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: Photo was null from bundle" + e.getMessage());
        }

        return view;
    }

    private void setupWidgets() {
        Log.d(TAG, "setupWidgets: setting up the widgets");

        // Comments
        mComments = (ArrayList<InstComment>) mPhoto.getComments();
        if (mComments == null) mComments = new ArrayList<>();

        if (mComments.size() == 0 || !Objects.equals(mComments.get(0).getComment(), mPhoto.getCaption())) {
            InstComment firstComment = new InstComment("0");

            firstComment.setComment(mPhoto.getCaption());
            firstComment.setUser_id(mPhoto.getUser_id());
            firstComment.setDate_creates(mPhoto.getDate_creates());
            mComments.add(0, firstComment);
        }

        InstCommentListAdapter adapter = new InstCommentListAdapter(mContext, R.layout.layout_inst_comment, mComments, mPhoto);
        mListView.setAdapter(adapter);

        // CheckMark
        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCommentET.getText().toString().equals("")) {
                    Log.d(TAG, "onClick: attempting to submit a new comment");
                    addNewComment(mCommentET.getText().toString());

                    mCommentET.setText("");
                    closeKeyBoard();
                } else {
                    Toast.makeText(mContext, "you can't post a blank comment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // BackArrow
        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");

                if (getCallingActivity() != null && getCallingActivity().equals(getString(R.string.home_activity))) {
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStack();
                    ((InstHomeActivity) getActivity()).showLayout();
                } else {
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStack();
                }
            }
        });
    }

    private void closeKeyBoard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addNewComment(String newComment) {
        Log.d(TAG, "addNewComment: adding new comment: " + newComment);



        // New comment's details
        String commentID = mRef.push().getKey();
        InstComment comment = new InstComment(commentID);
        comment.setComment(newComment);
        comment.setDate_creates(getTimeStamp());
        comment.setUser_id(mAuth.getCurrentUser().getUid());

        mComments.add(comment);

        mFirebaseMethods.addPhotoCommentDB(mPhoto, commentID, comment);

//        // Insert into photos nose
//        mRef.child(mContext.getString(R.string.db_photos))
//                .child(mPhoto.getPhoto_id())
//                .child(mContext.getString(R.string.db_field_comments))
//                .child(commentID)
//                .setValue(comment);
//
//        // Insert into user_photos nose
//        mRef.child(mContext.getString(R.string.db_user_photos))
//                .child(mPhoto.getUser_id())
//                .child(mPhoto.getPhoto_id())
//                .child(mContext.getString(R.string.db_field_comments))
//                .child(commentID)
//                .setValue(comment);

        setupWidgets();
    }

    private String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss'z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Israel"));
        Log.d(TAG, "getTimeStamp: Israel: " + sdf.format(new Date()));
        return sdf.format(new Date());
    }

    /**
     * retrieve the photo from the incoming bundle from profileActivity interface/
     *
     * @return Photo
     */
    private String getCallingActivity() {
        Log.d(TAG, "getCallingActivity: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getString(mContext.getString(R.string.home_activity));
        } else {
            return null;
        }

    }

    /**
     * retrieve the photo from the incoming bundle from profileActivity interface/
     *
     * @return Photo
     */
    private Photo getPhotoFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(mContext.getString(R.string.bundle_photo));
        } else {
            return null;
        }

    }

    /*
     * ----------------------------- Firebase ------------------------------------------
     */


    private void setupFirebaseStaff() {
        Log.d(TAG, "setupFirebaseStaff: called.");

        mFireApp = FirebaseApp.getInstance("mFireApp");
        mAuth = FirebaseAuth.getInstance(mFireApp);
        mRef = FirebaseDatabase.getInstance(mFireApp).getReference();

        // Init mAuthStateListener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                // Check if user is logged in

                if (user == null) {
                    Log.d(TAG, "onAuthStateChanged: User log-out");
                } else {
                    Log.d(TAG, "onAuthStateChanged: User is logged-in :) uid = " + firebaseAuth.getCurrentUser().getUid());
                }
            }
        };

//        if (mPhoto.getComments() == null || mPhoto.getComments().size() == 0) {
//            mComments.clear();
//            InstComment firstComment = new InstComment();
//            firstComment.setComment(mPhoto.getCaption());
//            firstComment.setUser_id(mPhoto.getUser_id());
//            firstComment.setDate_creates(mPhoto.getDate_creates());
//            mComments.add(firstComment);
//            mPhoto.setComments(mComments);
//            setupWidgets();
//        }
//
//        mRef.child(mContext.getString(R.string.db_photos))
//                .child(mPhoto.getPhoto_id())
//                .child(mContext.getString(R.string.db_field_comments))
//                .addChildEventListener(new ChildEventListener() {
//                    @Override
//                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                        Query query = mRef
//                                .child(mContext.getString(R.string.db_photos))
//                                .orderByChild(mContext.getString(R.string.db_field_photo_id))
//                                .equalTo(mPhoto.getPhoto_id());
//                        query.addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
//
//                                    Photo photo = new Photo();
//                                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
//
//                                    photo.setCaption(objectMap.get(mContext.getString(R.string.db_field_caption)).toString());
//                                    photo.setTags(objectMap.get(mContext.getString(R.string.db_field_tags)).toString());
//                                    photo.setPhoto_id(objectMap.get(mContext.getString(R.string.db_field_photo_id)).toString());
//                                    photo.setUser_id(objectMap.get(mContext.getString(R.string.db_field_user_id)).toString());
//                                    photo.setDate_creates(objectMap.get(mContext.getString(R.string.db_field_date_creates)).toString());
//                                    photo.setImage_path(objectMap.get(mContext.getString(R.string.db_field_image_path)).toString());
//
//                                    mComments.clear();
//                                    InstComment firstComment = new InstComment();
//                                    firstComment.setComment(mPhoto.getCaption());
//                                    firstComment.setUser_id(mPhoto.getUser_id());
//                                    firstComment.setDate_creates(mPhoto.getDate_creates());
//
//                                    mComments.add(firstComment);
//
//                                    for (DataSnapshot ds : singleSnapshot.child(mContext.getString(R.string.db_field_comments)).getChildren()) {
//                                        InstComment comment = new InstComment();
//                                        comment.setUser_id(ds.getValue(InstComment.class).getUser_id());
//                                        comment.setComment(ds.getValue(InstComment.class).getComment());
//                                        comment.setDate_creates(ds.getValue(InstComment.class).getDate_creates());
//                                        mComments.add(comment);
//                                    }
//
//                                    photo.setComments(mComments);
//                                    mPhoto = photo;
//
////                    List<Like> likeList = new ArrayList<>();
////                    for (DataSnapshot ds : singleSnapshot.child(mContext.getString(R.string.db_field_likes)).getChildren()) {
////                        Like like = new Like();
////                        like.setUser_id(ds.getValue(Like.class).getUser_id());
////                        likeList.add(like);
////                    }
//                                }
//                                setupWidgets();
//
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//                                Log.d(TAG, "onCancelled: query cancelled: " + databaseError.getMessage());
//                            }
//                        });
//
//
//                    }
//
//                    @Override
//                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//                    }
//
//                    @Override
//                    public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//                    }
//
//                    @Override
//                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//

    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAuthStateListener != null)
            mAuth.removeAuthStateListener(mAuthStateListener);
        try {
            Objects.requireNonNull(getActivity()).notifyAll();
        } catch (IllegalMonitorStateException e) {
            Log.e(TAG, "onPause: THE ERROR: IllegalMonitorStateException" + e.getMessage());
        }

    }

}
