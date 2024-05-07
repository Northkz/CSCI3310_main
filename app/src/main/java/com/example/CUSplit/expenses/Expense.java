package com.example.CUSplit.expenses;

public class Expense {
    private String memberId;
    private String itemName;
    private String itemCost;
    private String groupName;
    private String paidBy;

    private String currency;
    // Default constructor (required for Firestore)
    public Expense() {
    }

    public Expense(String memberId, String itemName, String itemCost, String groupName, String paidBy, String currency) {
        this.memberId = memberId;
        this.itemName = itemName;
        this.itemCost = itemCost;
        this.groupName = groupName;
        this.paidBy = paidBy;
        this.currency = currency;
    }

    // Getters and setters
    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemCost() {
        return itemCost;
    }

    public void setItemCost(String itemCost) {
        this.itemCost = itemCost;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(String paidBy) {
        this.paidBy = paidBy;
    }
    public String getCurrency(){
        return currency;
    }
    public void setCurrency(){
        this.currency = currency;
    }
}

