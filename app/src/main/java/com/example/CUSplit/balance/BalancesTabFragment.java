package com.example.CUSplit.balance;

import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.CUSplit.R;
import com.example.CUSplit.users.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;


public class BalancesTabFragment extends Fragment {
    private TextView currentBalanceTextView; // Reference to the TextView

    public String groupId; // group name
    private String currency = "HKD ";
    private List<String> members = new ArrayList<>();
    private ArrayList<User> users = new ArrayList<>();
    private String curBalance = "0.0 HKD";
    private ArraySet<String> uids = new ArraySet<>();
    private List<HashMap<String,Object>> results = new ArrayList<>();
    private BalancesTabViewAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private TextView header;
    private Double mainExchangeRate = 1.0;
    private TextView currentBalance;
    private void calculateBalances() {
        if(getActivity() == null) {
            return;
        }
        PriorityQueue<Balance> debtors = new PriorityQueue<>(users.size(),new BalanceComparator()); // debtors are members of the group who are owed money
        PriorityQueue<Balance> creditors = new PriorityQueue<>(users.size(),new BalanceComparator()); // creditors are members who have to pay money to the group

        String curUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase firebase = FirebaseDatabase.getInstance();
        DatabaseReference groupRef = firebase.getReference("Groups").child(groupId);
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final BigDecimal[] totalExpenses = {BigDecimal.ZERO};
                Map<String, BigDecimal> payments = new HashMap<>();


                dataSnapshot.child("expenses").getChildren().forEach(expenseSnapshot -> {
                    BigDecimal itemCost = new BigDecimal(expenseSnapshot.child("itemCost").getValue(String.class));
                    String cur = (expenseSnapshot.child("currency").getValue(String.class));

                    if (!cur.equals("HKD")) {
                        FirebaseDatabase.getInstance().getReference("FXRates").child(cur).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot rateSnapshot) {
                                mainExchangeRate = rateSnapshot.getValue(Double.class); // Get the rate as Double
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("Firebase", "Error loading rate for currency: " + cur, databaseError.toException());
                            }
                        });
                    }
                    else {
                        mainExchangeRate = 1.0;
                    }

                    String paidBy = expenseSnapshot.child("memberId").getValue(String.class);
                    totalExpenses[0] = totalExpenses[0].add(itemCost.multiply(BigDecimal.valueOf(mainExchangeRate)));
                    payments.put(paidBy, payments.getOrDefault(paidBy, BigDecimal.ZERO).add(itemCost));

                });

                BigDecimal eachPay = totalExpenses[0].divide(new BigDecimal(users.size()),2, RoundingMode.HALF_EVEN);;

                dataSnapshot.child("balances").getChildren().forEach(memberSnapshot -> {
                    String memberId = memberSnapshot.getKey();

                    BigDecimal paidAmount = payments.getOrDefault(memberId, BigDecimal.ZERO);
                    BigDecimal balance = paidAmount.subtract(eachPay);
                    User matchingUser = findUserById(memberId);
                    if (memberId.equals(curUID)){
                        curBalance = balance.toString();
                        // Update the TextView
                        if (currentBalanceTextView != null) {
                            currentBalanceTextView.post(() -> currentBalanceTextView.setText(curBalance + " HKD"));
                        }
                    }
                    if (balance.compareTo(BigDecimal.ZERO) < 0) {
                        debtors.add(new Balance(balance.abs(), memberId, matchingUser.getUsername()));  // Convert to positive as they are owed money
                    } else if (balance.compareTo(BigDecimal.ZERO) > 0) {
                        creditors.add(new Balance(balance, memberId, matchingUser.getUsername()));
                    }
                });

                calculateTransactions(debtors, creditors);
                resultEmptyCheck();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }
    public User findUserById(String userId) {
        for (User user : users) {
            if (user.getUid().equals(userId)) {
                return user;
            }
        }
        return null;  // Return null if no user matches the provided userId
    }
    private void calculateTransactions(PriorityQueue<Balance> debtors, PriorityQueue<Balance> creditors) {
        results.clear();

        while(!creditors.isEmpty() && !debtors.isEmpty()) {
            Balance rich = creditors.peek();
            Balance poor = debtors.peek();
            if(rich == null || poor == null) {
                return;
            }
            String richId = rich.userId;
            BigDecimal richBalance = rich.balance;
            String RichBalanceOwner= findUserById(richId).getUsername();

            creditors.remove(rich);

            String poorId = poor.userId;
            BigDecimal poorBalance = poor.balance;
            debtors.remove(poor);
            String PoorBalanceOwner= findUserById(poorId).getUsername();


            BigDecimal min = richBalance.min(poorBalance);

            // calculate the amount to be send from creditor to debtor
            richBalance = richBalance.subtract(min);
            poorBalance = poorBalance.subtract(min);

            HashMap<String,Object> values = new HashMap<>(); // record the transaction details in a HashMap
            values.put("sender",PoorBalanceOwner);
            values.put("recipient", RichBalanceOwner);
            values.put("amount",currency + min.toString());

            results.add(values);

            int compare = 1;
            if(poorBalance.compareTo(new BigDecimal("0.49")) == compare) {
                String BalanceOwner= findUserById(poorId).getUsername();

                debtors.add(new Balance(poorBalance,poorId, BalanceOwner));
            }

            if(richBalance.compareTo(new BigDecimal("0.49")) == compare) {
                String BalanceOwner= findUserById(poorId).getUsername();

                creditors.add(new Balance(richBalance,richId, BalanceOwner));
            }
        }

    }

    public static BalancesTabFragment newInstance(String chatId) {
        Bundle args = new Bundle();
        args.putString("chatId", chatId);
        BalancesTabFragment f = new BalancesTabFragment();
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.balances_fragment,container,false);
        currentBalanceTextView = view.findViewById(R.id.currentBalanceValue); // Make sure ID matches that in your balances_fragment.xml

        if(getArguments() == null || getActivity() == null) {
            return view;
        }
        groupId = getArguments().getString("chatId");
        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()){

                    String groups = userSnapshot.child("groups").getValue(String.class);
                    if (groups != null && isUserInGroup(groups, groupId)) {
                        String uid = userSnapshot.getKey();
                        String username = userSnapshot.child("username").getValue(String.class);
                        String profileImage = userSnapshot.child("profileImage").getValue(String.class);
                        if(uids.contains(uid)){
                            continue;
                        }
                        uids.add(uid);
                        users.add(new User(uid, username, profileImage));
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });


        recyclerView = view.findViewById(R.id.balancesRecyclerView);
        emptyView = view.findViewById(R.id.no_data);
        header = view.findViewById(R.id.balancesHeader);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new BalancesTabViewAdapter();
        recyclerView.setAdapter(adapter);

        return view;
    }

    private boolean isUserInGroup(String groups, String groupId) {
        String[] groupArray = groups.split(",");
        for (String group : groupArray) {
            if (group.trim().equals(groupId)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onResume() {
        if(getActivity() == null) {
            return;
        }
        super.onResume();
        runCalculations();
    }


    private void resultEmptyCheck() {
        if(results.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            header.setVisibility(View.GONE);

        } else  {
            recyclerView.setVisibility(View.VISIBLE);
            header.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter.storeToList(results);
        }
    }

    private void runCalculations() {
        if(!users.isEmpty()) {

            calculateBalances();
        } else {
            results.clear();
            resultEmptyCheck();
        }
    }
}