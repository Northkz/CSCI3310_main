package com.example.split_bill.chats;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import com.example.split_bill.chats.ChatActivity;
import com.example.split_bill.R;

public class ChatsAdapter extends RecyclerView.Adapter<ChatViewHolder>{

    private ArrayList<Chat> chats;
    private FirebaseAuth firebaseAuth;
    public ChatsAdapter(ArrayList<Chat> chats){
        this.chats = chats;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.person_item_rv, parent, false);
        return new ChatViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chats.get(position);
        String currentUserUid = firebaseAuth.getCurrentUser().getUid();
        // Set chat name
        holder.chat_name_tv.setText(chat.getChatName());

        // Determine profile image to display
        String displayUserId = null;
        for (String memberId : chat.getMembers()) {
            if (!memberId.equals(currentUserUid)) {
                displayUserId = memberId;
                break;
            }
        }

        if (displayUserId != null) {
            FirebaseDatabase.getInstance().getReference().child("Users").child(displayUserId)
                    .child("profileImage").get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().getValue() != null) {
                            String profileImageUrl = task.getResult().getValue(String.class);
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(holder.itemView.getContext()).load(profileImageUrl).into(holder.chat_iv);
                            }
                        } else {
                            // Handle failure or no image found
                            Glide.with(holder.itemView.getContext()).load(R.drawable.username_icon).into(holder.chat_iv);
                        }
                    });
        } else {
            // Set default image or hide ImageView if no user is available to show
            Glide.with(holder.itemView.getContext()).load(R.drawable.username_icon).into(holder.chat_iv);
        }

        // Set click listener to open chat
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ChatActivity.class);

            intent.putExtra("chatId", chats.get(position).getChats());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }
}
