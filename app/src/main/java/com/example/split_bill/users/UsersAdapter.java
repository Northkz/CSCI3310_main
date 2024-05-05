package com.example.split_bill.users;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import com.example.split_bill.R;

public class UsersAdapter extends RecyclerView.Adapter<UserViewHolder>{
    private List<String> selectedUserIds = new ArrayList<>();

    private ArrayList<User> users = new ArrayList<>();

    public UsersAdapter(ArrayList<User> users){
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

        // Update background based on selection state
        if (selectedUserIds.contains(user.uid)) {
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.selectedItem));  // Define this color in your colors.xml
        } else {
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.unselectedItem));  // Define this color in your colors.xml
        }

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
