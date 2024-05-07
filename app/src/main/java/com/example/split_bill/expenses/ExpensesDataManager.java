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

    public void retrieveExpensesData() {
        expensesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<BillEntity> billEntities = new ArrayList<>();
                // Iterate through each expense data
                for (DataSnapshot BillSnapshot : dataSnapshot.getChildren()) {
                    // Parse expense data into ExpenseEntity object
                    BillEntity bill = BillSnapshot.getValue(BillEntity.class);
                    if (bill != null) {
                        billEntities.add(bill);
                        System.out.println("Expense item name: " + bill.getItem());
                        System.out.println("Expense item cost: " + bill.getCost());
                        System.out.println("Paid by: " + bill.getPaidBy());
                    }
                }
                adapter.updateExpensesList(billEntities);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
                System.out.println("Error retrieving expenses data: " + databaseError.getMessage());
            }
        });
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

