package razdob.cycler.myUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

import razdob.cycler.models.User;
import razdob.cycler.models.Business;
import razdob.cycler.models.UserAccountSettings;

/**
 * Created by Raz on 23/03/2018, for project: PlacePicker2
 */

public class FirebaseInserts implements Transaction.Handler {
    private static final String TAG = "FirebaseInserts";

    private static FirebaseStorage storage = FirebaseStorage.getInstance(FirebaseApp.getInstance("mFireApp"), "gs://cyclerproject.appspot.com");
    private static DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance("mFireApp")).getReference();
    private static StorageReference mStorageReference = storage.getReference();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance("mFireApp"));

    public static void savePersonInfo(String uid, User user, UserAccountSettings accountSettings) {
        if (user.getEmail() == null) user.setEmail(mAuth.getCurrentUser().getEmail());
        mDatabaseRef.child("persons").child(uid).setValue(user);
        mDatabaseRef.child("user_account_settings").child(uid).setValue(accountSettings);
    }
    public static void saveBusinessInfo(String uid, Business business) {
        mDatabaseRef.child("business_users").child(uid).setValue(business);
    }

    private static HashMap<String, Boolean> arrayListToHashMapAllTrue(ArrayList<String> arrayList) {
        HashMap<String, Boolean> hashMap = new HashMap<>();
        for (String s: arrayList) {
            hashMap.put(s, true);
        }
        return hashMap;
    }

    public static void checkPushBusiness (Business business){
        mDatabaseRef.child("tests").child("busniessssss").push().setValue(business);
    }


    private static void myPutFile(final Context context, StorageReference storageReference, Uri fileUri, final ProgressDialog progressDialog) {
        storageReference.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "upload successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressDialog.dismiss();
                        Toast.makeText(context, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage(((int) progress + "% Uploaded..."));

                    }
                });
    }


    public static void uploadFile(final Context context, String path, Uri fileUri, String fileName, final ProgressDialog progressDialog) {
        if (fileUri == null)
            return;

        StorageReference tempStorageRef = mStorageReference.child(path + "/" + fileName);
        myPutFile(context, tempStorageRef, fileUri, progressDialog);

        Log.i("uploadFile", "File: " + fileName + " uploaded to path: " + path);
    }





    @Override
    public Transaction.Result doTransaction(MutableData mutableData) {
        return null;
    }

    @Override
    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

    }
}
