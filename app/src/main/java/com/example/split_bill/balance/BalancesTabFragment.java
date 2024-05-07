package com.example.split_bill.balance;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.split_bill.Group.GroupViewModel;

import com.example.split_bill.R;
import com.example.split_bill.users.User;
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
    public String groupId; // group name
    private String currency = "USD-($)";
    private List<String> members = new ArrayList<>();
    private ArrayList<User> users = new ArrayList<>();
    private List<HashMap<String,Object>> results = new ArrayList<>();
    private BalancesTabViewAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private TextView header;

    private void calculateBalances() {
        if(getActivity() == null) {
            return;
        }
        PriorityQueue<Balance> debtors = new PriorityQueue<>(users.size(),new BalanceComparator()); // debtors are members of the group who are owed money
        PriorityQueue<Balance> creditors = new PriorityQueue<>(users.size(),new BalanceComparator()); // creditors are members who have to pay money to the group



        final BigDecimal[] sum = {new BigDecimal("0")};
        List<BigDecimal> preBalances = new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference("Groups").child(groupId).child("expenses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot expenseSnapshot : snapshot.getChildren()) {
                    String itemCost = expenseSnapshot.child("itemCost").getValue(String.class);

                    BigDecimal cost = new BigDecimal(itemCost);

                    preBalances.add(cost);
                    sum[0] = sum[0].add(cost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("Firbase", "Failed to read value ");
            }
        });

        FirebaseDatabase firbase = FirebaseDatabase.getInstance();
        DatabaseReference groupRef = firbase.getReference("Groups").child(groupId);
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final BigDecimal[] totalExpenses = {BigDecimal.ZERO};
                Map<String, BigDecimal> payments = new HashMap<>();


                dataSnapshot.child("expenses").getChildren().forEach(expenseSnapshot -> {
                    BigDecimal itemCost = new BigDecimal(expenseSnapshot.child("itemCost").getValue(String.class));
                    String paidBy = expenseSnapshot.child("memberId").getValue(String.class);
                    totalExpenses[0] = totalExpenses[0].add(itemCost);
                    payments.put(paidBy, payments.getOrDefault(paidBy, BigDecimal.ZERO).add(itemCost));

                });

                BigDecimal eachPay = totalExpenses[0].divide(new BigDecimal(users.size()),2, RoundingMode.HALF_EVEN);;


                dataSnapshot.child("balances").getChildren().forEach(memberSnapshot -> {
                    String memberId = memberSnapshot.getKey();
                    BigDecimal paidAmount = payments.getOrDefault(memberId, BigDecimal.ZERO);
                    Log.i("payd by ==>", paidAmount.toString());
                    BigDecimal balance = paidAmount.subtract(eachPay);
                    Log.i("balance ==>", balance.toString());
                    User matchingUser = findUserById(memberId);

                    if (balance.compareTo(BigDecimal.ZERO) < 0) {
                        // Debtors have a negative balance (owed money)
                        debtors.add(new Balance(balance.abs(), memberId, matchingUser.getUsername()));  // Convert to positive as they are owed money
                    } else if (balance.compareTo(BigDecimal.ZERO) > 0) {
                        // Creditors have a positive balance (owe money)
                        creditors.add(new Balance(balance, memberId, matchingUser.getUsername()));
                    }
                });
                Log.i("Creditors ==>", creditors.toString());
                Log.i("debtors ==>", debtors.toString());
                calculateTransactions(debtors, creditors);
                resultEmptyCheck();
                // Now debtors and creditors queues are populated with members' balances
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
        results.clear(); // remove previously calculated transactions before calculating again
        Log.i("FINISHED calculating balance", "DONE");
        /*Algorithm: Pick the largest element from debtors and the largest from creditors. Ex: If debtors = {4,3} and creditors={2,7}, pick 4 as the largest debtor and 7 as the largest creditor.
        * Now, do a transaction between them. The debtor with a balance of 4 receives $4 from the creditor with a balance of 7 and hence, the debtor is eliminated from further
        * transactions. Repeat the same thing until and unless there are no creditors and debtors.
        *
        * The priority queues help us find the largest creditor and debtor in constant time. However, adding/removing a member takes O(log n) time to perform it.
        * Optimisation: This algorithm produces correct results but the no of transactions is not minimum. To minimize it, we could use the subset sum algorithm which is a NP problem.
        * The use of a NP solution could really slow down the app! */
        while(!creditors.isEmpty() && !debtors.isEmpty()) {
            Balance rich = creditors.peek(); // get the largest creditor
            Balance poor = debtors.peek(); // get the largest debtor
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
            values.put("amount",currency.charAt(5) + min.toString());

            results.add(values);

            // Consider a member as settled if he has an outstanding balance between 0.00 and 0.49 else add him to the queue again
            int compare = 1;
            if(poorBalance.compareTo(new BigDecimal("0.49")) == compare) {
                // if the debtor is not yet settled(has a balance between 0.49 and inf) add him to the priority queue again so that he is available for further transactions to settle up his debts
                String BalanceOwner= findUserById(poorId).getUsername();

                debtors.add(new Balance(poorBalance,poorId, BalanceOwner));
            }

            if(richBalance.compareTo(new BigDecimal("0.49")) == compare) {
                String BalanceOwner= findUserById(poorId).getUsername();

                // if the creditor is not yet settled(has a balance between 0.49 and inf) add him to the priority queue again so that he is available for further transactions
                creditors.add(new Balance(richBalance,richId, BalanceOwner));
            }
        }
        Log.i("KAMBAR==>", Integer.toString(users.size()));
        Log.i("Nursultan234==>", results.toString());

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
        if(getArguments() == null || getActivity() == null) {
            return view;
        }
        loadUsers(groupId);
        Log.i("KAMBAR==>", Integer.toString(users.size()));
        groupId = getArguments().getString("chatId");
        recyclerView = view.findViewById(R.id.balancesRecyclerView);
        emptyView = view.findViewById(R.id.no_data);
        header = view.findViewById(R.id.balancesHeader);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new BalancesTabViewAdapter();
        recyclerView.setAdapter(adapter);  // Set the adapter as soon as the RecyclerView is ready


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
    private void loadUsers(String groupId){

        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()){

                    String groups = userSnapshot.child("groups").getValue(String.class);
                    if (groups != null && isUserInGroup(groups, groupId)) {
                        String uid = userSnapshot.getKey();
                        String username = userSnapshot.child("username").getValue(String.class);
                        String profileImage = userSnapshot.child("profileImage").getValue(String.class);

                        users.add(new User(uid, username, profileImage));
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    @Override
    public void onResume() {
        GroupViewModel groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        if(getActivity() == null) {
            return;
        }
        runCalculations();
        super.onResume();
    }

    private void resultEmptyCheck() {
        // if results[] is empty display"No one is owed money"
        if(results.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            header.setVisibility(View.GONE);
        } else  {
            Log.i("We have data!", "computation finished");
            recyclerView.setVisibility(View.VISIBLE);
            header.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter.storeToList(results); // update the recycler view with the new results
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