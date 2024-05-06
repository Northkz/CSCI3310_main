package com.example.split_bill.Members;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.split_bill.AddEditMemberActivity;
import com.example.split_bill.R;
import com.example.split_bill.users.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.split_bill.databinding.FragmentGroupMembersBinding;
import com.example.split_bill.Members.MemberAdapter;

import java.util.ArrayList;
import java.util.List;

public class MembersTabFragment extends Fragment {
    private FragmentGroupMembersBinding binding;

    private MemberViewModel memberViewModel;
    private String gName; // group name
    private String groupId; // group Id
    private MembersTabViewAdapter adapter;
    private List<MemberEntity> members = new ArrayList<>(); // maintain a list of all the existing members of the group from the database



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
        binding.membersFragmentAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an intent to start the AddEditMemberActivity
                Intent intent = new Intent(getActivity(), AddEditMemberActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.members_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.deleteAllMembers) {
            if(!members.isEmpty()) { // condition prevents initiating a deleteAll operation if there are no members to delete
                memberViewModel.deleteAll(gName);
                Toast.makeText(getActivity(), "All Members Deleted", Toast.LENGTH_SHORT).show();
                return true;
            }
            Toast.makeText(getActivity(), "Nothing To Delete", Toast.LENGTH_SHORT).show();
            return super.onOptionsItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
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