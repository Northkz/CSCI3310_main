package com.example.CUSplit.Group;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupViewModel extends AndroidViewModel {
    private MutableLiveData<List<Group>> allGroups = new MutableLiveData<>();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");

    public GroupViewModel(Application application) {
        super(application);
        loadGroups();
    }



    private void loadGroups() {
        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userGroupsRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserID).child("groups");
        ArrayList<String> groupIdArray = new ArrayList<>();

        userGroupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String groupIds = dataSnapshot.getValue(String.class); // Assuming the group IDs are stored as a single string
                if (groupIds != null && !groupIds.isEmpty()) {
                    groupIdArray.addAll(Arrays.asList(groupIds.split(","))); // Split and store the group IDs
                }

                // Now fetch and filter groups based on the obtained IDs
                databaseReference.addValueEventListener(new ValueEventListener()  {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Group> groupList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (groupIdArray.contains(snapshot.getKey())) { // Check if the group is in the user's group list
                                Group group = snapshot.getValue(Group.class);
                                if (group != null) {
                                    group.setGroupId(snapshot.getKey());  // Set the groupId here
                                    groupList.add(group);
                                }
                            }
                        }
                        allGroups.postValue(groupList); // Update the LiveData only with filtered groups
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Log or handle the cancellation
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Log or handle the cancellation
            }
        });
    }



    public LiveData<List<Group>> getAllGroups() {
        return allGroups;
    }

    public void delete(Group group) {
        databaseReference.child(group.getGroupName()).removeValue();
    }

    public void deleteAll() {
        databaseReference.removeValue();
    }

    public void insert(Group group) {
        databaseReference.child(group.getGroupName()).setValue(group);
    }
}
