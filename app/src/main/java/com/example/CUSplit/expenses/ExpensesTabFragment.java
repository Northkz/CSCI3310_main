package com.example.CUSplit.expenses;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CUSplit.Group.GroupListActivity;
import com.example.CUSplit.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExpensesTabFragment extends Fragment {
    private String gName; // group name
    private List<Expense> bills = new ArrayList<>(); // maintain a list of all the existing bills of the group from the database
    private List<ExpenseEntity> members = new ArrayList<>(); // maintain a list of all the existing members of the group from the database
    private ExpensesTabViewAdapter adapter;
    private StringBuilder currency = new StringBuilder();

    private String groupId;

    public static ExpensesTabFragment newInstance(String gName, String groupId) {
        Bundle args = new Bundle();
        args.putString("group_name", gName);
        args.putString("group_id", groupId);
        ExpensesTabFragment f = new ExpensesTabFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.expenses_fragment, container, false);
        gName = getArguments().getString("group_name");
        groupId = getArguments().getString("group_id");
        if(getArguments() == null) {
            return view;
        }


        RecyclerView recyclerView = view.findViewById(R.id.expensesRecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // get all the existing members from the database using the group ID
        DatabaseReference membersRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("members");
        membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                    String memberId = memberSnapshot.getValue(String.class);
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(memberId);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userDataSnapshot) {
                            if (userDataSnapshot.exists()) {
                                String username = userDataSnapshot.child("username").getValue(String.class);
                                ExpenseEntity member = new ExpenseEntity(gName, username, memberId);
                                members.add(member); // Add member to the list
                            } else {
                                Toast.makeText(getActivity(), "Failed to retrieve group members based on id. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle errors
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(getActivity(), "Failed to retrieve group members. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });

        if(getActivity() != null) {
            adapter = new ExpensesTabViewAdapter(groupId, gName, getActivity().getApplication(), this, members);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("expenses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Expense> expensesList = new ArrayList<>();
                // Iterate through each expense data
                for (DataSnapshot expenseSnapshot : dataSnapshot.getChildren()) {

                    Expense expense = expenseSnapshot.getValue(Expense.class);
                    if (expense != null) {
                        expensesList.add(expense);
                    }
                }
                adapter.updateExpensesList(expensesList);
                adapter.storeToList(expensesList, currency.toString());
                bills = expensesList;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
                System.out.println("Error retrieving expenses data: " + databaseError.getMessage());
            }
        });


        // Implement Add new expense function
        FloatingActionButton addFloating = view.findViewById(R.id.expensesFragmentAdd);
        addFloating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch AddEditBillActivity only if members are not empty
                if (!members.isEmpty() && getActivity() != null) {
                    Intent intent = new Intent(getActivity(), AddEditBillActivity.class);
                    intent.putExtra(GroupListActivity.EXTRA_TEXT_GNAME, gName);
                    intent.putExtra("groupId",groupId );
                    intent.putExtra("groupCurrency", currency.toString());
                    intent.putExtra("requestCode", 1); // using requestCode(value = 1) to identify add expense intent
                    getActivity().startActivityFromFragment(ExpensesTabFragment.this, intent, 1);
                } else {
                    Toast.makeText(getActivity(), "No members found. Please add some members.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.expenses_fragment_menu, menu);
    }




}
