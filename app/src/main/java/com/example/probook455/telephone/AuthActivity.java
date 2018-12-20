package com.example.probook455.telephone;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startMainActivity();
        }
    }

    public void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
