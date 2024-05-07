package com.example.CUSplit.Group;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CUSplit.HandleOnGroupClickActivity;
import com.example.CUSplit.R;

import java.util.ArrayList;
import java.util.List;

public class GroupListActivity extends AppCompatActivity {
    public static final String EXTRA_TEXT_GNAME = "com.example.split_bill.EXTRA_TEXT_GNAME";
    private List<Group> groupNames = new ArrayList<>();
    private GroupListActivityViewAdapter adapter;
    private GroupViewModel groupViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_list_activity);

        Toolbar toolbar = findViewById(R.id.groupListToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle("Group List");

        RecyclerView recyclerView = findViewById(R.id.group_list_recycler_view);
        recyclerView.setHasFixedSize(true);
        adapter = new GroupListActivityViewAdapter(new ArrayList<>(), this::onGroupClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        groupViewModel.getAllGroups().observe(this, groups -> {
            groupNames = groups;
            adapter.setGroups(groups);
            TextView emptyListMsgTV = findViewById(R.id.noGroupsMsg);
            if (groups.isEmpty()) {
                emptyListMsgTV.setText("No groups found :(\nPlease create a new group");
            } else {
                emptyListMsgTV.setText("");
            }
        });
    }

    private void onGroupClick(Group group) {
        Intent intent = new Intent(GroupListActivity.this, HandleOnGroupClickActivity.class);
        intent.putExtra(EXTRA_TEXT_GNAME, group.getGroupName());
        intent.putExtra("groupId", group.getGroupId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.deleteAllGroups) {
            if (!groupNames.isEmpty()) {
                groupViewModel.deleteAll();
                Toast.makeText(this, "All Groups Deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Nothing To Delete", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
