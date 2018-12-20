package com.example.probook455.telephone;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserProfile {
    public UserProfile(){}

    public String firstName;
    public String lastName;
    public String phone;

    public UserProfile(String firstName, String lastName, String phone){
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }
}
