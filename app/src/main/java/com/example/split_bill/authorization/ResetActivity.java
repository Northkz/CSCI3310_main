package com.example.split_bill.authorization;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.split_bill.db.DatabaseHelper;
import com.example.split_bill.databinding.ActivityResetBinding;


public class ResetActivity extends AppCompatActivity {
    
    ActivityResetBinding binding;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        databaseHelper = new DatabaseHelper(this);

        binding.btnconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.emailReset.getText().toString();
                String password = binding.passwordReset.getText().toString();
                String repassword = binding.repasswordReset.getText().toString();

                if(email.equals("")||password.equals("")||repassword.equals(""))
                    Toast.makeText(ResetActivity.this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
                else {
                    if (password.equals(repassword)) {
                        Boolean checkUserEmail = databaseHelper.checkEmail(email);

                        if (checkUserEmail == true) {
                            Boolean updatePassword = databaseHelper.updatePassword(email, password);

                            if (updatePassword == true) {
                                Toast.makeText(ResetActivity.this, "Update Password Successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(ResetActivity.this, "Update password Failed!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ResetActivity.this, "User not Available,Signup First!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ResetActivity.this, "Password Not Same!", Toast.LENGTH_SHORT).show();
                    }
                }



            }
        });
    }
}