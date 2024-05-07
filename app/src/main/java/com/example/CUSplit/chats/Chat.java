package com.example.CUSplit.chats;

import java.util.List;

public class Chat {
    private String chats;
    private String chatName;
    private List<String> members;  // Stores user IDs of all members in the chat
    // Constructor for individual chats
    public Chat(String chatId, String chatName, List<String> members) {
        this.chats = chatId;
        this.chatName = chatName;
        this.members = members;
    }

    // Getters and setters
    public String getChats() {
        return chats;
    }

    public void setChats(String chats) {
        this.chats = chats;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

}
