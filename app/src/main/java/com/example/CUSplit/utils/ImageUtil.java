package com.example.CUSplit.utils;
import android.app.Activity;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.CUSplit.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class ImageUtil {

    //utility method to upload the image from user to Firebase
    public static void uploadImageToFirebase(Uri fileUri, Activity activity) {
        if (fileUri != null) {
            //File will be saved in Firebase Storage:
            //"Users/:userId", can be any file, but only tested with jpg
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference userFolderRef = storage.getReference().child("Users/"+ userId);

            userFolderRef.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(activity, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    activity.runOnUiThread(() -> {
                        // Update profile pic with fileUri directly
                        CircleImageView profilePic = activity.findViewById(R.id.profile_image);
                        profilePic.setImageURI(fileUri);
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(activity, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(activity, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }
}
