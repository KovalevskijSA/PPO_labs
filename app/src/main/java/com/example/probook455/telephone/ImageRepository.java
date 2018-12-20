package com.example.probook455.telephone;

import android.graphics.Bitmap;
import android.net.Uri;

import com.example.probook455.telephone.rss.OnProgressListener;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;

public class ImageRepository {

    private static final String TAG = "ImageRepository";
    private final String imageFilename = "image";
    private final String imageFileExtention = ".jpg";

    private static ImageRepository instance;

    private StorageReference storageRef;

    private Uri downloadUrl;
    private File imageFile;

    private ArrayList<OnImageUploadedListener> onImageUploadedListeners;
    private ArrayList<OnImageDownloadedListener> onImageDownloadedListeners;

    private ImageRepository(){
        storageRef = FirebaseStorage.getInstance().getReference();
        onImageDownloadedListeners = new ArrayList<>();
        onImageUploadedListeners = new ArrayList<>();
        imageFile = null;
        downloadUrl = null;
    }

    public static ImageRepository getInstance()
    {
        if (instance == null){
            instance = new ImageRepository();
            instance.downloadImage();
        }
        return instance;
    }

    public static void logOut(){
//        instance = null;
    }

    public void setImage(Uri file) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) {
            notifyOnImageDownloadedListenersAboutFailure(new FirebaseAuthException("not authorized", "user is not authorized"));
            return;
        }
        String uid = user.getUid();
        final StorageReference fileRef = storageRef.child(uid+"/"+imageFilename+imageFileExtention);
        fileRef.putFile(file).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return fileRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadUrl = task.getResult();
                    downloadImage();
                } else {
                    // Handle failures
                }
            }
        });
    }

    public void setImage(Bitmap bitmap) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) {
            notifyOnImageDownloadedListenersAboutFailure(new FirebaseAuthException("not authorized", "user is not authorized"));
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        String uid = user.getUid();
        final StorageReference fileRef = storageRef.child(uid+"/"+imageFilename+imageFileExtention);
        fileRef.putBytes(data).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return fileRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadUrl = task.getResult();
                    downloadImage();

                } else {
                    // Handle failures
                }
            }
        });
    }

    public File getImageFile() {
        return imageFile;
    }

    public void downloadImage() {
//        notifyOnProgressStarted();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) {
            notifyOnImageDownloadedListenersAboutFailure(new FirebaseAuthException("not authorized", "user is not authorized"));
            return;
        }
        String uid = user.getUid();
        final StorageReference fileRef = storageRef.child( uid+"/"+imageFilename+imageFileExtention);
        try {
            final File localFile = File.createTempFile(imageFilename, "jpg");
            fileRef.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Successfully downloaded data to local file
                            imageFile = localFile;
                            notifyOnImageDownloadedListenersAboutSuccess(imageFile);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle failed download
                    notifyOnImageDownloadedListenersAboutFailure(exception);
                }
            });
        } catch (IOException e) {

        }
//        notifyOnProgressEnded();
    }

    public void addOnImageUploadedListener(OnImageUploadedListener listener) {
        if (!onImageUploadedListeners.contains(listener)) {
            onImageUploadedListeners.add(listener);
        }
    }

    public void removeOnImageUploadedListener(OnImageUploadedListener listener){
        onImageUploadedListeners.remove(listener);
    }

    public void addOnImageDownloadedListener(OnImageDownloadedListener listener) {
        if (!onImageDownloadedListeners.contains(listener)) {
            onImageDownloadedListeners.add(listener);
        }
    }

    public void removeOnImageDownloadedListener(OnImageDownloadedListener listener) {
        onImageDownloadedListeners.remove(listener);
    }

    public void notifyOnImageUploadedListenersAboutSuccess() {
        for (OnImageUploadedListener listener:onImageUploadedListeners) {
            listener.onImageUploaded();
        }
    }

    public void notifyOnImageDownloadedListenersAboutSuccess(File image) {
        for (OnImageDownloadedListener listener:onImageDownloadedListeners) {
            listener.onImageDownloaded(image);
        }
    }

    public void notifyOnImageUploadedListenersAboutFailure(Exception e) {
        for (OnImageUploadedListener listener:onImageUploadedListeners) {
            listener.onImageUploadFailure(e);
        }
    }

    public void notifyOnImageDownloadedListenersAboutFailure(Exception e) {
        for (OnImageDownloadedListener listener:onImageDownloadedListeners) {
            listener.onImageDownloadFailure(e);
        }
    }

    public interface OnImageUploadedListener {
        void onImageUploaded();
        void onImageUploadFailure(Exception e);
    }

    public interface OnImageDownloadedListener {
        void onImageDownloaded(File image);
        void onImageDownloadFailure(Exception e);
    }

    private ArrayList<OnProgressListener> onProgressListeners;
    public void addOnProgressListener(OnProgressListener listener) {
        if (!onProgressListeners.contains(listener)) {
            onProgressListeners.add(listener);
        }
    }

    public void notifyOnProgressStarted() {
        for (OnProgressListener listener:onProgressListeners) {
            listener.onProgressStarted();
        }
    }

    public void notifyOnProgressEnded() {
        for (OnProgressListener listener:onProgressListeners) {
            listener.onProgressEnded();
        }
    }

}
