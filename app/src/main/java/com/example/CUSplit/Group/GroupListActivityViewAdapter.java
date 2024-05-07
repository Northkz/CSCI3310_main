package com.example.CUSplit.Group;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CUSplit.R;

import java.util.List;

public class GroupListActivityViewAdapter extends RecyclerView.Adapter<GroupListActivityViewAdapter.GroupViewHolder> {
    private List<Group> groups;
    private OnGroupClickListener listener;

    public GroupListActivityViewAdapter(List<Group> groups, OnGroupClickListener listener) {
        this.groups = groups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_list_detail, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.groupName.setText(group.getGroupName());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGroupClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
        notifyDataSetChanged();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;

        GroupViewHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.groupListDetailName);
        }
    }

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }
}
