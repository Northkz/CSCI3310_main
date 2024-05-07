package com.example.split_bill.chats;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.example.split_bill.databinding.FragmentChatBinding;
import com.example.split_bill.chats.message.Message;
import com.example.split_bill.chats.message.MessagesAdapter;

public class ChatFragment extends Fragment {
    private FragmentChatBinding binding;
    private String chatId;
    private MessagesAdapter adapter;
    private List<Message> messages;


    public static ChatFragment newInstance(String chatId) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("chatId", chatId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        chatId = getArguments() != null ? getArguments().getString("chatId") : null;

        initializeRecyclerView();
        loadMessages(chatId);

        binding.sendMessageBtn.setOnClickListener(v -> {
            String message = binding.messageEt.getText().toString();
            if (message.isEmpty()){
                Toast.makeText(getContext(), "Message field cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            String date = simpleDateFormat.format(new Date());
            binding.messageEt.setText(""); // Clearing the edit text
            sendMessage(chatId, message, date);
        });
        return binding.getRoot();
    }
    private void sendMessage(String chatId, String message, String date){
        if (chatId == null) return;

        HashMap<String, String> messageInfo = new HashMap<>();
        messageInfo.put("text", message);
        messageInfo.put("ownerId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        messageInfo.put("date", date);

        FirebaseDatabase.getInstance().getReference().child("Chats").child(chatId)
                .child("messages").push().setValue(messageInfo);
    }
    private void initializeRecyclerView() {
        messages = new ArrayList<>();
        adapter = new MessagesAdapter(messages);
        binding.messagesRv.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.messagesRv.setAdapter(adapter);
    }

    private void loadMessages(String chatId){
        if (chatId == null) return;

        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(chatId).child("messages");
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                messages.clear(); // Clear existing messages
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    String messageId = messageSnapshot.getKey();
                    String ownerId = messageSnapshot.child("ownerId").getValue(String.class);
                    String text = messageSnapshot.child("text").getValue(String.class);
                    String date = messageSnapshot.child("date").getValue(String.class);

                    fetchUsernameAndAddMessage(messageId, ownerId, text, date);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to load messages", error.toException());
            }
        });
    }

    private void fetchUsernameAndAddMessage(String messageId, String ownerId, String text, String date) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(ownerId).child("username");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                String username = userSnapshot.getValue(String.class);
                if (username != null) {
                    Message message = new Message(messageId, ownerId, text, date, username);
                    messages.add(message);
                    adapter.notifyDataSetChanged(); // Notify the adapter of the change
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Firebase", "Load username onCancelled", error.toException());
            }
        });
    }
}


