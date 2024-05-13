package com.example.CUSplit.utils;

import android.app.Activity;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ImageUtil {

    // Utility method to upload the image from the user to Firebase
    public static Task<Uri> uploadImageToFirebase(Uri fileUri, Activity activity, String directoryName) {
        if (fileUri != null) {
            // File will be saved in Firebase Storage: "Users/:userId"
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference userFolderRef = storage.getReference().child("Users").child(userId).child(directoryName);

            // Upload the file to Firebase Storage
            UploadTask uploadTask = userFolderRef.putFile(fileUri);
            return uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return userFolderRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Upload successful
                    Uri downloadUri = task.getResult();
                    Toast.makeText(activity, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                } else {
                    // Upload failed
                    Toast.makeText(activity, "Upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(activity, "No file selected", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
