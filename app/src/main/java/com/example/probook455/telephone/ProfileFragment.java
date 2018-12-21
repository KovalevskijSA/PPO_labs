package com.example.probook455.telephone;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.probook455.telephone.rss.OnProgressListener;
import com.firebase.ui.auth.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private Button editButton;
    private Button logoutButton;

    private TextView emailTextView;
    private TextView phoneTextView;
    private TextView nameTextView;
    private TextView surnameTextView;
    private ImageView imageView;
    private ProgressBar progressBar;
    private ImageRepository.OnImageDownloadedListener onImageDownloadedListener;
    private UserRepository rep = new UserRepository();

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(logout);
        progressBar = view.findViewById(R.id.progressBar);
        editButton = view.findViewById(R.id.profileEditButton);
        editButton.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_profileFragment_to_profileEditFragment));
        emailTextView = view.findViewById(R.id.profileEmail);
        phoneTextView = view.findViewById(R.id.profilePhone);
        nameTextView = view.findViewById(R.id.profileFirstName);
        surnameTextView = view.findViewById(R.id.profileLastName);
        imageView = view.findViewById(R.id.profileImage);

        progressBar.setVisibility(ProgressBar.VISIBLE);
        rep = new UserRepository();

        ValueEventListener profileEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                setUI(userProfile);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        ////
        rep.addProfileEventListener(profileEventListener);
        setFileAsImage(ImageRepository.getInstance().getImageFile());
        onImageDownloadedListener = new ImageRepository.OnImageDownloadedListener() {
            @Override
            public void onImageDownloaded(File image) {
                setFileAsImage(image);
//                if (!ImageRepository.isChange)
                progressBar.setVisibility(ProgressBar.INVISIBLE);
            }

            @Override
            public void onImageDownloadFailure(Exception e) {
                e = e;
                progressBar.setVisibility(ProgressBar.INVISIBLE);
            }
        };
        ImageRepository.getInstance().addOnImageDownloadedListener(onImageDownloadedListener);
    }

    private void setUI(UserProfile user) {
        if (user == null) {
            return;
        }
        nameTextView.setText(user.firstName);
        surnameTextView.setText(user.lastName);
        emailTextView.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        phoneTextView.setText(user.phone);
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


    private View.OnClickListener logout = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            rep.signOut();
            ImageRepository.logOut();
//            FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
//                @Override
//                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                    FirebaseUser user = firebaseAuth.getCurrentUser();
//                    if (user == null) {
            ((MainActivity)getActivity()).startAuthActivity();
//                        startActivity(new Intent(getActivity(), AuthActivity.class));
//                        getActivity().finish();
//                    }
//                }
//            };
        }
    };

    @Override
    public void onDetach() {
        super.onDetach();
        ImageRepository.getInstance().removeOnImageDownloadedListener(onImageDownloadedListener);
    }

   public interface OnFragmentInteractionListener {
       // TODO: Update argument type and name
       void onFragmentInteraction(Uri uri);
   }
}
