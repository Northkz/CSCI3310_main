package com.example.split_bill.Group;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.split_bill.databinding.FragmentNewGroupBinding;

import com.example.split_bill.users.User;
import com.example.split_bill.users.UsersAdapter;


public class CreateNewGroupsActivity extends AppCompatActivity {
    private FragmentNewGroupBinding binding;
    private UsersAdapter adapter;


    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentNewGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new UsersAdapter(new ArrayList<>());
        setupRecyclerView();
        binding.createGroupButton.setOnClickListener(v -> createGroupChat());
    }
    private void setupRecyclerView() {
        binding.usersRv.setLayoutManager(new LinearLayoutManager(this));
        binding.usersRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        binding.usersRv.setAdapter(adapter);
        loadUsers();
    }
    private void loadUsers() {
        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<User> users = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (!userSnapshot.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        String uid = userSnapshot.getKey();
                        String username = userSnapshot.child("username").getValue(String.class);
                        String profileImage = userSnapshot.child("profileImage").getValue(String.class);
                        users.add(new User(uid, username, profileImage));
                    }
                }
                adapter = new UsersAdapter(users);
                binding.usersRv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateNewGroupsActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public static void addChatIdToUser(String uid, String chatId){
        FirebaseDatabase.getInstance().getReference().child("Users").child(uid)
                .child("chats").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        String chats = task.getResult().getValue().toString();
                        String chatsUpd = addIdToStr(chats, chatId);
                        FirebaseDatabase.getInstance().getReference().child("Users").child(uid)
                                .child("chats").setValue(chatsUpd);
                    }
                });
    }

    public static void addGroupIdToUser(String uid, String groupID){
        FirebaseDatabase.getInstance().getReference().child("Users").child(uid)
                .child("groups").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        String groups = task.getResult().getValue().toString();
                        String groupsUpdated = addIdToStr(groups, groupID);
                        FirebaseDatabase.getInstance().getReference().child("Users").child(uid)
                                .child("groups").setValue(groupsUpdated);
                    }
                });
    }
    private static String addIdToStr(String str, String chatId){
        str += (str.isEmpty()) ? chatId : (","+chatId);
        return str;
    }
    private void createGroupChat() {
        List<String> selectedUserIds = adapter.getSelectedUserIds();
        if (selectedUserIds.isEmpty()) {
            Toast.makeText(this, "Please select at least one user to create a group chat", Toast.LENGTH_SHORT).show();
            return;
        }

        String groupName = binding.groupChatName.getText().toString().trim();
        if (groupName.isEmpty()) {
            Toast.makeText(this, "Please enter a group chat name", Toast.LENGTH_SHORT).show();
            return;
        }
        // Create a new group
        DatabaseReference newGroupRef = FirebaseDatabase.getInstance().getReference().child("Groups").push();
        String groupId = newGroupRef.getKey(); // This is the unique ID for the new group

        Map<String, Object> groupInfo = new HashMap<>();
        groupInfo.put("groupName", groupName);
        groupInfo.put("members", selectedUserIds);
        groupInfo.put("currency", "");
        groupInfo.put("createdAt", System.currentTimeMillis()); // Storing creation timestamp

        // Initialize balance map with each member's balance set to 0
        Map<String, Integer> balances = new HashMap<>();
        for (String userId : selectedUserIds) {
            balances.put(userId, 0);
        }
        balances.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), 0);
        groupInfo.put("balances", balances);

        newGroupRef.setValue(groupInfo)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Group chat created successfully", Toast.LENGTH_SHORT).show();
                    // Use the chat ID in your addChatIdToUser method inside the success listener
                    addGroupIdToUser(FirebaseAuth.getInstance().getCurrentUser().getUid(), groupId);
                    for (String userId : selectedUserIds) {

                        addGroupIdToUser(userId, groupId);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to create group chat: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // First, create a reference for a new chat and get the unique chat ID
        DatabaseReference newChatRef = FirebaseDatabase.getInstance().getReference().child("Chats").push();
        String chatId = newChatRef.getKey(); // This is the unique ID for the new chatr

        Map<String, Object> chatInfo = new HashMap<>();
        chatInfo.put("chat_name", groupName);

        selectedUserIds.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
        chatInfo.put("members", selectedUserIds);

        // Now use the reference to set the chat info
        newChatRef.setValue(chatInfo)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Group chat created successfully", Toast.LENGTH_SHORT).show();
                    // Use the chat ID in your addChatIdToUser method inside the success listener
                    addChatIdToUser(FirebaseAuth.getInstance().getCurrentUser().getUid(), chatId);
                    for (String userId : selectedUserIds) {
                        addChatIdToUser(userId, chatId);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to create group chat: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


}
