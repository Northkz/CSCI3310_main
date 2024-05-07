package com.example.CUSplit.expenses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.CUSplit.Group.GroupViewModel;
import com.example.CUSplit.MainActivity;
import com.example.CUSplit.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* Note that this activity can act as a Add Bill Activity or Edit Bill Activity based on the intent data we receive*/
public class AddEditBillActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private EditText editTextItem;
    private EditText editTextCost;
    private String currency;
    private String gName;
    private String groupId;
    private String paidBy;
    private String memberId;
    private int requestCode;
    private int billId;
    List<ExpenseEntity> members = new ArrayList<>();

    private void saveExpense() {
        String item = editTextItem.getText().toString();
        String cost = editTextCost.getText().toString();

        // check if the item name or cost is empty
        if (item.trim().isEmpty() || cost.trim().isEmpty()) {
            Toast.makeText(this, "Please enter a valid input", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");



        if (requestCode == 1) { // 1 for Add Bill Activity
            // Round up the cost of the bill to 2 decimal places
            BigDecimal decimal = new BigDecimal(cost);
            BigDecimal res = decimal.setScale(2, RoundingMode.HALF_EVEN);

            // Create a new expense object
            Expense expense = new Expense(memberId, item, res.toString(), gName, paidBy, currency);

            // Get a reference to the "expenses" node within the group in the Firebase Realtime Database
            DatabaseReference expensesRef = groupsRef.child(groupId).child("expenses").push();

            // Push the expense to the expenses node
            expensesRef.setValue(expense)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AddEditBillActivity.this, "Expense added successfully", Toast.LENGTH_SHORT).show();
                            finish(); // Close the activity after adding the expense
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddEditBillActivity.this, "Failed to add expense", Toast.LENGTH_SHORT).show();
                        }
                    });
        }


        // updates the group currency
        GroupViewModel groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);

//        groupViewModel.update(group);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_bill);

        // set toolbar
        Toolbar toolbar = findViewById(R.id.addBillToolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editTextItem = findViewById(R.id.addBillItemName);

        editTextCost = findViewById(R.id.addBillItemCost);

        editTextCost.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

            public void afterTextChanged(Editable arg0) {
                String str = editTextCost.getText().toString();
                if (str.isEmpty()) return;
                String str2 = PerfectDecimal(str, 20, 2);

                if (!str2.equals(str)) {
                    editTextCost.setText(str2);
                    int pos = editTextCost.getText().length();
                    editTextCost.setSelection(pos);
                }
            }
        });

        // get data from the intent that started this activity
        Intent intent = getIntent();
        // requestCode == 1 identifies an add bill intent and requestCode == 2 identifies an edit Bill intent
        requestCode = intent.getIntExtra("requestCode",0);
        memberId = intent.getStringExtra("billMemberId");
        groupId = intent.getStringExtra("groupId");
        billId = intent.getIntExtra("billId",-1);
        currency = intent.getStringExtra("groupCurrency");

        // spinner for select currency
        Spinner spinner = findViewById(R.id.addBillItemCurrencySpinner);

        List<CharSequence> currencySymbols = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.currencySymbols)));
        ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencySymbols);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        String cur = MainActivity.currencyCode;
        Log.i("Nursultan==>", cur);

        if (!currencySymbols.contains(cur)){
            spinnerAdapter.add(cur);
        }
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);
        // set default spinner currency
        int spinnerPositionCurrency = spinnerAdapter.getPosition(currency);
        spinner.setSelection(spinnerPositionCurrency); // set spinner default selection


        // spinner for selecting paidBy Member
        final Spinner spinnerPaidBy = findViewById(R.id.addBillItemPaidBy);
        final AllMembersSpinnerAdapter allMembersSpinnerAdapter = new AllMembersSpinnerAdapter(this,new ArrayList<ExpenseEntity>());
        allMembersSpinnerAdapter.setDropDownViewResource(0);
        spinnerPaidBy.setAdapter(allMembersSpinnerAdapter);
        spinnerPaidBy.setOnItemSelectedListener(this);



        // get all current members of the group
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DatabaseReference membersRef = FirebaseDatabase.getInstance()
                .getReference().child("Groups").child(groupId).child("members");

        membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> memberIds = new ArrayList<>();
                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                    String memberId = memberSnapshot.getValue(String.class);
                    if (memberId != null) {
                        memberIds.add(memberId);
                    }
                }
                // Now fetch member entities based on member IDs
                fetchMemberEntities(memberIds, allMembersSpinnerAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Toast.makeText(AddEditBillActivity.this, "Failed to retrieve group members. Please try again later.", Toast.LENGTH_SHORT).show();

            }
        });

        if(intent.hasExtra("billId")) {
            setTitle("Edit expense");
            editTextItem.setText(intent.getStringExtra("billName")); // set default text received from the intent
            editTextCost.setText(intent.getStringExtra("billCost")); // set default text received from the intent
            paidBy = intent.getStringExtra("billPaidBy");
        } else {
            setTitle("Add an Expense");
        }

    }
    private void fetchMemberEntities(List<String> memberIds, final AllMembersSpinnerAdapter allMembersSpinnerAdapter) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    memberId = userSnapshot.getKey();
                    if (memberIds.contains(memberId)) {
                        String username = userSnapshot.child("username").getValue(String.class);
                        ExpenseEntity member = new ExpenseEntity(gName, username, memberId);
                        members.add(member);
                    }
                }
                allMembersSpinnerAdapter.clear();
                allMembersSpinnerAdapter.addAll(members);
                allMembersSpinnerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle failure
                Toast.makeText(AddEditBillActivity.this, "Failed to retrieve group members. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    public String PerfectDecimal(String str, int MAX_BEFORE_POINT, int MAX_DECIMAL){
        if(str.charAt(0) == '.') str = "0"+str;
        int max = str.length();

        StringBuilder rFinal = new StringBuilder();
        boolean after = false;
        int i = 0, up = 0, decimal = 0; char t;
        while(i < max){
            t = str.charAt(i);
            if(t != '.' && !after){
                up++;
                if(up > MAX_BEFORE_POINT) return rFinal.toString();
            }else if(t == '.'){
                after = true;
            }else{
                decimal++;
                if(decimal > MAX_DECIMAL)
                    return rFinal.toString();
            }
            rFinal.append(t);
            i++;
        }return rFinal.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_bill_action_bar_menu,menu);
        return true;
    }

    // call this method when an option in the menu is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.addBillToolbarMenu) {
            saveExpense();
        }
        finish(); // if the user clicks on back button close this activity
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.addBillItemCurrencySpinner:
                currency = parent.getItemAtPosition(position).toString();
                break;
            case R.id.addBillItemPaidBy:
                Log.d("t", "selected paidBy");
                ExpenseEntity member = (ExpenseEntity) parent.getItemAtPosition(position);
                paidBy = member.name;
                System.out.println("selected paidBy"+ paidBy);
                memberId = member.id;
                break;
            default:break;
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
