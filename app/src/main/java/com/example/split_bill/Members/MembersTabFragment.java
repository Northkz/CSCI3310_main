package com.example.split_bill.Members;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.split_bill.users.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.split_bill.databinding.FragmentGroupMembersBinding;

import java.util.ArrayList;

public class MembersTabFragment extends Fragment {
    private FragmentGroupMembersBinding binding;

    private String gName; // group name
    private String groupId; // group Id




    public static MembersTabFragment newInstance(String gName, String groupId) {
        Bundle args = new Bundle();
        args.putString("group_name", gName);
        args.putString("group_id", groupId);
        MembersTabFragment f = new MembersTabFragment();
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGroupMembersBinding.inflate(inflater, container, false);

        loadUsers(groupId);

        gName = getArguments().getString("group_name"); // get group name from bundle
        groupId = getArguments().getString("group_id");

        return binding.getRoot();
    }


    private void loadUsers(String groupId){
        ArrayList<User> users = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()){

                    String groups = userSnapshot.child("groups").getValue(String.class);
                    if (groups != null && isUserInGroup(groups, groupId)) {
                        String uid = userSnapshot.getKey();
                        String username = userSnapshot.child("username").getValue(String.class);
                        String profileImage = userSnapshot.child("profileImage").getValue(String.class);

                        users.add(new User(uid, username, profileImage));
                    }
                }

                binding.usersRv.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.usersRv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
                binding.usersRv.setAdapter(new MemberAdapter(users));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    /**
     * Helper method to check if the user is in the specific group.
     * It splits the groups string by commas and checks if the list contains the groupId.
     * @param groups Comma-separated string of group IDs.
     * @param groupId The groupId to check.
     * @return true if the groupId is in the list of groups, false otherwise.
     */
    private boolean isUserInGroup(String groups, String groupId) {
        String[] groupArray = groups.split(",");
        for (String group : groupArray) {
            if (group.trim().equals(groupId)) {
                return true;
            }
        }
        return false;
    }

}