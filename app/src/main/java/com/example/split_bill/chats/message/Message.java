package com.example.split_bill.chats.message;

public class Message {

    private String id, ownerId, text, date, ownerName;

    public Message(String id, String ownerId, String text, String date, String name) {
        this.id = id;
        this.ownerId = ownerId;
        this.text = text;
        this.date = date;
        this.ownerName = name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
