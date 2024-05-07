package com.example.CUSplit.expenses;

public class ExpenseEntity {
    private String gName;
    public String name;
    public String id;

    public ExpenseEntity(String gName, String name, String id){
        this.gName = gName;
        this.id = id;
        this.name = name;
    }

    public String getGName() {
        return gName;
    }

    public void setGName(String gName) {
        this.gName = gName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
