package com.example.split_bill.expenses;

import com.example.split_bill.BillEntity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExpensesDataManager {

    private DatabaseReference expensesRef;
    private ExpensesTabViewAdapter adapter;
    private OnDataChangeListener onDataChangeListener;
    private List<BillEntity> expensesList;

    public ExpensesDataManager(String groupId, ExpensesTabViewAdapter adapter) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        System.out.println("groupIDsd" + groupId);
        expensesRef = database.getReference("Groups").child(groupId).child("expenses");
        this.adapter = adapter;
        this.expensesList = new ArrayList<>();
    }

    public List<BillEntity> getExpensesList() {
        return expensesList;
    }
    public void setOnDataChangeListener(OnDataChangeListener listener) {
        this.onDataChangeListener = listener;
    }

    public interface OnDataChangeListener {
        void onDataChange(List<BillEntity> billEntities);
    }

}

