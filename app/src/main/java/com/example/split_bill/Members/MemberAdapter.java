package com.example.split_bill.Members;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.split_bill.R;
import com.example.split_bill.users.User;
import com.example.split_bill.users.UserViewHolder;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<UserViewHolder>{
    private List<String> selectedUserIds = new ArrayList<>();

    private ArrayList<User> users = new ArrayList<>();

    public MemberAdapter(ArrayList<User> users){
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.person_item_rv, parent, false);
        return new UserViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        holder.username_tv.setText(user.username);
        holder.itemView.setActivated(selectedUserIds.contains(user.uid));  // This is used to set the selected state

        if (!user.profileImage.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(user.profileImage).into(holder.profileImage_iv);
        }

        holder.itemView.setOnClickListener(view -> {
            if (selectedUserIds.contains(user.uid)) {
                selectedUserIds.remove(user.uid);
                view.setActivated(false);
            } else {
                selectedUserIds.add(user.uid);
                view.setActivated(true);
            }
            notifyDataSetChanged();  // This will refresh the RecyclerView and update the visual state
        });
    }

    public List<String> getSelectedUserIds() {
        return selectedUserIds;
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}
