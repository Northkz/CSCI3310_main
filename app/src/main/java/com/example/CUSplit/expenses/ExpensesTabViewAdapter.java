package com.example.CUSplit.expenses;

import android.app.Application;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CUSplit.R;

import java.util.ArrayList;
import java.util.List;

/* Objective: Prepare a custom adapter that could create/update the view for every item in the recycler view */
public class ExpensesTabViewAdapter extends RecyclerView.Adapter<ExpensesTabViewAdapter.ExpenseDetailViewHolder> {
    private List<Expense> list = new ArrayList<>(); // maintain a list of all the existing bills in the database
    private List<ExpenseEntity> members = new ArrayList<>();
    private String gName;
    private String currency;
    private Application application;
    private String groupdId;
    private ExpensesTabFragment thisOfExpenseFragment;
    private List<String> memberIds = new ArrayList<>();
    private List<Expense> expenses = new ArrayList<>();




    ExpensesTabViewAdapter(String groupId, String gName, Application application, ExpensesTabFragment thisOfExpenseFragment, List<ExpenseEntity> members) {
        this.gName = gName;
        this.application = application;
        this.thisOfExpenseFragment = thisOfExpenseFragment;
        this.members = members; // Initialize the members list
        this.groupdId = groupId;
        this.expenses = list;
    }

    public void updateExpensesList(List<Expense> expenses) {
        this.expenses.clear(); // Clear the existing list
        this.expenses.addAll(expenses); // Add the new expense data to the list
        this.expenses = list;
        notifyDataSetChanged(); // Notify the adapter that the data set has changed
    }


    // A holder for every item in our recycler view is created
    class ExpenseDetailViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewItem;
        private TextView textViewCost;
        private TextView textViewCurrency;
        private TextView textViewPaidBy;
        private RelativeLayout relativeLayout;

        ExpenseDetailViewHolder(@NonNull View itemView) {
            super(itemView);

            // store all the references from our layout for future use
            textViewItem = itemView.findViewById(R.id.expenseDetailItem);
            textViewCost = itemView.findViewById(R.id.expenseDetailCost);
            textViewCurrency = itemView.findViewById(R.id.expenseDetailCurrency);
            textViewPaidBy = itemView.findViewById(R.id.expenseDetailPaidBy);
            relativeLayout = itemView.findViewById(R.id.expenseDetail);
        }

    }


    // Create new viewHolder (invoked by the layout manager). Note that this method is called for creating every ExpenseDetailViewHolder required for our recycler view items
    @NonNull
    @Override
    public ExpenseDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_detail, parent, false);
        return new ExpenseDetailViewHolder(v);
    }

    // note that this method is called for every ExpenseDetailViewHolder
    @Override
    public void onBindViewHolder(@NonNull ExpenseDetailViewHolder holder, int position) {
        Expense bill = list.get(position);
        System.out.println("wehere");
        Log.d("ExpenseDetails11", "Item Name: " + bill.getItemName());
        Log.d("ExpenseDetails11", "Cost: " + bill.getItemCost());


        holder.textViewItem.setText(bill.getItemName()); // Assuming getItemName() returns the item name
        holder.textViewCost.setText(String.valueOf(bill.getItemCost())); // Assuming getCost() returns the cost as a double or float
        holder.textViewCurrency.setText(currency); // Set currency symbol
        holder.textViewPaidBy.setText("Paid By: " + bill.getPaidBy()); // Assuming getPaidBy() returns the name of the person who paid
//        StringBuilder justText = new StringBuilder();

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    void storeToList(List<Expense> billEntities, String currency) {
        list = billEntities;
        this.currency = currency;
        notifyDataSetChanged();
    }



    public interface OnItemClickListener {
        void onItemClick(Expense bill);
    }



}
