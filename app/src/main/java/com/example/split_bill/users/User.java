package com.example.split_bill.users;

public class User {
    public String uid, username, profileImage;
    public boolean isSelected = false;  // Default value
    public User(String uid, String username, String profileImage) {
        this.uid = uid;
        this.username = username;
        this.profileImage = profileImage;
    }


    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public boolean isSelected() {
        return isSelected;
    }
}
