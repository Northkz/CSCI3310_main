package com.example.split_bill;
public class BillMemberEntity {

    public BillMemberEntity(String name, String gName, String id) {
        this.name = name;
        this.gName = gName;
        this.id = id;
    }

    public String id;

    public String gName;

    public String name;

    public int mAvatar;

    public void setId(String id) {
        this.id = id;
    }

    public void setMAvatar(int mAvatar) {
        this.mAvatar = mAvatar;
    }

    public String getName() {
        return this.name;
    }

    public String getId(){
        return this.id;
    }
}