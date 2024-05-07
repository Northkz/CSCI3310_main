package com.example.CUSplit.Group;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Group {
    private String groupName;
    private String currency;
    private List<String> members; // List of member IDs
    private Long createdAt; // Timestamp of creation
    private String groupId;

    public Group() {
        // Default constructor required for calls to DataSnapshot.getValue(Group.class)
    }

    public Group(String groupId, String groupName, String currency, List<String> members, Long createdAt) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.currency = currency;
        this.members = members;
        this.createdAt = createdAt;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("groupName", groupName);
        result.put("currency", currency);
        result.put("members", members);
        result.put("createdAt", createdAt);
        return result;
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupName='" + groupName + '\'' +
                ", currency='" + currency + '\'' +
                ", members=" + members +
                ", createdAt=" + createdAt +
                '}';
    }
}
