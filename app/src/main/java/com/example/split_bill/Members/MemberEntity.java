package com.example.split_bill.Members;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.split_bill.GroupEntity;

// Column "GroupName" has a foreign key reference to column "GroupName" of GroupEntity
@Entity(foreignKeys = @ForeignKey(entity = GroupEntity.class,
        parentColumns = "GroupName",
        childColumns = "GroupName",
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE),
        indices = {
            @Index(name="GroupNameIndexMember",value = {"GroupName"})
        })
public class MemberEntity {

    public MemberEntity(String name, String gName) {
        this.name = name;
        this.gName = gName;
    }
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    public int id;

    @ColumnInfo(name = "GroupName")
    public String gName;

    @ColumnInfo(name = "MemberName")
    public String name;

    @ColumnInfo(name = "MemberAvatar")
    public
    int mAvatar;

    public void setId(int id) {
        this.id = id;
    }

    public void setMAvatar(int mAvatar) {
        this.mAvatar = mAvatar;
    }

    public String getName() {
        return this.name;
    }
    public int getId(){
        return this.id;
    }
}