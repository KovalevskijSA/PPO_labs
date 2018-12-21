package com.example.probook455.telephone;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import static android.app.Activity.RESULT_OK;


public class ProfileEditFragment extends Fragment {
    private static final int RC_PICK_IMAGE_REQUEST = 1234;
    private static final int RC_TAKE_PHOTO_REQUEST = 4321;

    private Uri selectedImageUri;
    private Bitmap takenImageBitmap = null;

    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private ImageView imageView;
    private ProgressBar progressBar;
    private Button saveButton;
    private UserProfile user;

    private Boolean isPhotoChanged = false;
    private UserRepository rep = new UserRepository();;
    private ImageRepository.OnImageDownloadedListener onImageDownloadedListener;

    public ProfileEditFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_edit, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firstNameEditText = view.findViewById(R.id.profileFirstName);
        lastNameEditText = view.findViewById(R.id.profileLastName);
        phoneEditText = view.findViewById(R.id.profilePhone);
        imageView = view.findViewById(R.id.profileImage);
        saveButton = view.findViewById(R.id.saveButton);
        progressBar = view.findViewById(R.id.progressBar);
        selectedImageUri = null;

        if (savedInstanceState != null) {
            firstNameEditText.setText(savedInstanceState.getString("name"));
            lastNameEditText.setText(savedInstanceState.getString("surname"));

            phoneEditText.setText(savedInstanceState.getString("phone"));
            String imageUri = savedInstanceState.getString("selectedImageUri");
            takenImageBitmap = savedInstanceState.getParcelable("takenImageBitmap");
            if (imageUri != null) {
                selectedImageUri = Uri.parse(imageUri);
            }
        }
        if (selectedImageUri != null) {
            imageView.setImageURI(selectedImageUri);
        }else if (takenImageBitmap != null) {
            imageView.setImageBitmap(takenImageBitmap);
        } else {
            setFileAsImage(ImageRepository.getInstance().getImageFile());
        }
        onImageDownloadedListener = new ImageRepository.OnImageDownloadedListener() {
            @Override
            public void onImageDownloaded(File image) {
                setFileAsImage(image);
                progressBar.setVisibility(ProgressBar.INVISIBLE);
            }

            @Override
            public void onImageDownloadFailure(Exception e) {
                e = e;
                progressBar.setVisibility(ProgressBar.INVISIBLE);
            }
        };

        ValueEventListener profileEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                if (userProfile!= null && savedInstanceState == null){
                    setUI(userProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        rep.addProfileEventListener(profileEventListener);

        ImageRepository.getInstance().addOnImageDownloadedListener(onImageDownloadedListener);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfile user = new UserProfile();
                user.firstName = firstNameEditText.getText().toString();
                user.lastName = lastNameEditText.getText().toString();
                user.phone = phoneEditText.getText().toString();
                rep.setUserProfile(user);
                if (isPhotoChanged){
                    if (selectedImageUri != null) {
                        ImageRepository.getInstance().setImage(selectedImageUri);
                    } else if (takenImageBitmap != null) {
                        ImageRepository.getInstance().setImage(takenImageBitmap);
                    }
                }

                Navigation.findNavController(view).navigate(R.id.profileFragment);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePhotoSourceSelectionDialog();
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", firstNameEditText.getText().toString());
        outState.putString("surname", lastNameEditText.getText().toString());
        outState.putString("phone", phoneEditText.getText().toString());
        outState.putString("selectedImageUri", selectedImageUri == null? null : selectedImageUri.toString());
        outState.putParcelable("takenImageBitmap", takenImageBitmap);
    }

    private void setUI(UserProfile user) {
        if (user == null) {
            return;
        }
        firstNameEditText.setText(user.firstName);
        lastNameEditText.setText(user.lastName);
        phoneEditText.setText(user.phone);
    }

    private void setFileAsImage(File file) {
        if (file == null)
            return;
        Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        imageView.setImageBitmap(myBitmap);
        if (!ImageRepository.isChange){
            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
    }

    private void showImagePhotoSourceSelectionDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.photo_selection_dialog);
        dialog.setTitle("Set image");
        Button makePhotoButton = dialog.findViewById(R.id.pickPhotoSourceSelectionDialog_takePhoto_button);
        makePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePictureIntent, RC_TAKE_PHOTO_REQUEST);
                dialog.hide();
            }
        });
        Button chooseExistingButton = dialog.findViewById(R.id.pickPhotoSourceSelectionDialog_chooseExisting_button);
        chooseExistingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), RC_PICK_IMAGE_REQUEST);
                dialog.hide();
            }
        });
        dialog.show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ImageRepository.getInstance().removeOnImageDownloadedListener(onImageDownloadedListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_PICK_IMAGE_REQUEST:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    selectedImageUri = data.getData();
                    imageView.setImageURI(selectedImageUri);
                    takenImageBitmap = null;
                    isPhotoChanged = true;
                }
                break;
            case RC_TAKE_PHOTO_REQUEST:
                if (resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imageView.setImageBitmap(imageBitmap);
                    takenImageBitmap = imageBitmap;
                    selectedImageUri = null;
                    isPhotoChanged = true;
                }
        }
    }


}
