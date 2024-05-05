package com.example.split_bill;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.split_bill.authorization.ResetActivity;
import com.example.split_bill.databinding.ActivityCheckUserBinding;
import com.example.split_bill.db.DatabaseHelper;

public class CheckUser extends AppCompatActivity {

    ActivityCheckUserBinding binding;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        binding.btnreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.email.getText().toString();

                Boolean checkCredentials = databaseHelper.checkEmail(email);

                if(checkCredentials == true){
                    Toast.makeText(CheckUser.this, "Verified Successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent  = new Intent(getApplicationContext(), ResetActivity.class);
                    intent.putExtra("email",email);
                    startActivity(intent);
                }else{
                    Toast.makeText(CheckUser.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}