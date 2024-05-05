package com.example.split_bill.chats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.split_bill.chats.Chat;
import com.example.split_bill.chats.ChatsAdapter;
import com.example.split_bill.databinding.FragmentChatsBinding;

public class Chats extends Fragment {
    private FragmentChatsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);

        loadChats();

        return binding.getRoot();
    }

    private void loadChats() {
        ArrayList<Chat> chats = new ArrayList<>();

        // Get a reference to the user's chats
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(getContext(), "No chats found.", Toast.LENGTH_SHORT).show();
                    return; // No chats for this user
                }
                String chatsStr = Objects.requireNonNull(snapshot.child("Users").child(uid).child("chats").getValue()).toString();
                String[] chatsIds = chatsStr.split(",");
                if (chatsIds.length==0) return;

                for (String chatId : chatsIds) {
                    // Assuming chat details are stored directly under each chat ID
                    FirebaseDatabase.getInstance().getReference().child("Chats").child(chatId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot chatDetailSnapshot) {
                                    if (!chatDetailSnapshot.exists()) return; // Skip if no details
                                    String chatName = chatDetailSnapshot.child("chat_name").getValue(String.class);
                                    List<String> members = new ArrayList<>();
                                    for (DataSnapshot memberSnapshot : chatDetailSnapshot.child("members").getChildren()) {
                                        members.add(memberSnapshot.getValue(String.class));
                                    }

                                    // Create a new chat object and add it to the list
                                    Chat chat = new Chat(chatId, chatName, members);
                                    chats.add(chat);
                                    // Update the adapter
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            binding.chatsRv.setLayoutManager(new LinearLayoutManager(getContext()));
                                            binding.chatsRv.setAdapter(new ChatsAdapter(chats));
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getContext(), "Failed to load chat details", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to get user chats", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
