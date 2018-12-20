package com.example.probook455.telephone;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class UserRepository implements ValueEventListener {
    private static final String TAG = "UserRepository";

    private static UserRepository instance = null;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private UserProfile user;

    private ArrayList<OnUserProfileUpdatedListener> userUpdatedListeners;

    private UserRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.setPersistenceEnabled(true);
        databaseReference = db.getReference();
        firebaseUser = firebaseAuth.getCurrentUser();
        user = new UserProfile();
        databaseReference.child("users").child(firebaseUser.getUid()).addValueEventListener(this);
        userUpdatedListeners = new ArrayList<OnUserProfileUpdatedListener>();
    }

    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    public void logOut(){

//        instance = null;
        firebaseAuth.signOut();
    }

    public UserProfile getUser() {
        return user;
    }

    public void setUser(UserProfile user){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            Log.e(TAG, "user is not authorized when saving profile !!!!!!!!!!!!!");
        }
        databaseReference.child("users").child(firebaseUser.getUid()).setValue(user);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        user = dataSnapshot.getValue(UserProfile.class);
        notifyUserProfileUpdated();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        databaseError = databaseError;
    }

    public interface OnUserProfileUpdatedListener {
        void OnUserUpdated(UserProfile user);
    }

    public void notifyFirebaseUser(){
        notifyUserProfileUpdated();
    }

    private void notifyUserProfileUpdated() {
        for (OnUserProfileUpdatedListener listener:userUpdatedListeners) {
            listener.OnUserUpdated(this.user);
        }
    }

    public void addOnUserProfileUpdatedListener(OnUserProfileUpdatedListener listener){
        if (!userUpdatedListeners.contains(listener)) {
            userUpdatedListeners.add(listener);
        }
    }

    public void removeOnUserProfileUpdatedListener(OnUserProfileUpdatedListener listener) {
        userUpdatedListeners.remove(listener);
    }


}
