package com.example.split_bill;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.split_bill.Group.GroupListActivity;
import com.example.split_bill.balance.BalancesTabFragment;
import com.example.split_bill.chats.ChatFragment;
import com.example.split_bill.Members.MembersTabFragment;
import com.google.android.material.tabs.TabLayout;

// This activity is initiated if the user clicks on any group while on the GroupList activity.
public class HandleOnGroupClickActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.handle_on_group_click_activity);

        /* get extra data(name of the group the user clicked on) from the intent that started this activity
         * Hence, we can load all the members and bills of the group the user clicked on in GroupList Activity*/
        Intent intent = getIntent();
        String gName = intent.getStringExtra(GroupListActivity.EXTRA_TEXT_GNAME);
        String chatId = intent.getStringExtra("groupId");
        TabLayout tabLayout = findViewById(R.id.tablayout_id);
        ViewPager viewPager = findViewById(R.id.viewpager_id);

        // set toolbar
        Toolbar toolbar = findViewById(R.id.handleOnGroupClickToolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        setTitle(gName);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(MembersTabFragment.newInstance(gName, chatId),"Members");
        adapter.addFragment(ExpensesTabFragment.newInstance(gName, chatId),"Expenses");
        adapter.addFragment(BalancesTabFragment.newInstance(chatId),"Balances");
        adapter.addFragment(ChatFragment.newInstance(chatId),"Chat");



        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            // if user clicks on back button initiate finish to close activity
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
