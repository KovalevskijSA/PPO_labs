package com.example.probook455.telephone;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class UserRepository {
    private FirebaseAuth authInstance;
    private FirebaseUser user;
    private DatabaseReference profileReference;

    public UserRepository() {
        authInstance = FirebaseAuth.getInstance();
        user = authInstance.getCurrentUser();
        if (user != null) {
            profileReference = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(user.getUid());
        }
    }

    public FirebaseUser getUser(){
        return user;
    }

    public String getEmail(){
        return user.getEmail();
    }

    public Task<AuthResult> signIn(String email, String password){
        return authInstance.signInWithEmailAndPassword(email, password);
    }

    public void signOut(){
        authInstance.signOut();
    }

    public Task<AuthResult> createNewUser(String email, String password){
        return authInstance.createUserWithEmailAndPassword(email, password);
    }
    public void setUserProfile(UserProfile profile){
        profileReference.setValue(profile);
    }

    public void addProfileEventListener(ValueEventListener profileEventListener){
        profileReference.addValueEventListener(profileEventListener);
    }
}