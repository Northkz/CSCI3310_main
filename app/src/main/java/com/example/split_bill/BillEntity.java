package com.example.split_bill;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.split_bill.Members.MemberEntity;

@Entity(foreignKeys = {
        @ForeignKey(entity = MemberEntity.class,
                parentColumns = "Id",
                childColumns = "MemberId",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE
        ),
        @ForeignKey(entity = GroupEntity.class,
                parentColumns = "GroupName",
                childColumns = "GroupName",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE
        )},
        indices = {
                @Index(name="MemberIdIndex",value = {"MemberId"}),
                @Index(name="GroupNameIndexBill",value = {"GroupName"})
        })
public class BillEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    private int id;

    @ColumnInfo(name = "MemberId")
    private int mid;

    @ColumnInfo(name = "Item")
    public String itemName;

    @ColumnInfo(name = "PaidBy")
    public String paidBy;

    @ColumnInfo(name = "Cost")
    public String itemCost;

    @ColumnInfo(name = "GroupName")
    private String groupName; // consistent case for property name

    public BillEntity() {
        // Default constructor required by Firebase
    }

    public BillEntity(int mid, String itemName, String itemCost, String groupName, String paidBy) {
        this.mid = mid;
        this.itemName = itemName;
        this.itemCost = itemCost;
        this.groupName = groupName;
        this.paidBy = paidBy;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public String getItem() {
        return itemName;
    }

    public void setItem(String itemName) {
        this.itemName = itemName;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(String paidBy) {
        this.paidBy = paidBy;
    }

    public String getCost() {
        return itemCost;
    }

    public void setCost(String itemCost) {
        this.itemCost = itemCost;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
