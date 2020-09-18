package com.khattab.picotask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.khattab.picotask.common.Common;
import com.khattab.picotask.databinding.ActivityMainBinding;
import com.khattab.picotask.pojo.Item;
import com.khattab.picotask.service.BroadcastManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ItemAdapter.OnItemClickListener {

    ActivityMainBinding iBinding;
    private List<Item> items;
    private ItemAdapter mAdapter;
    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        iBinding.fab.setOnClickListener(this);
        initViews();
    }

    private void initViews() {

        iBinding.rvItems.setHasFixedSize(true);
        iBinding.rvItems.setLayoutManager(new LinearLayoutManager(this));
        items = new ArrayList<>();
        mAdapter = new ItemAdapter(this, items);
        iBinding.rvItems.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Items");
        Common common = new Common();
        if (common.checkNetWork(MainActivity.this)) {
            mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    items.clear();

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Item item = postSnapshot.getValue(Item.class);
                        item.setId(postSnapshot.getKey());
                        items.add(item);
                    }

                    mAdapter.notifyDataSetChanged();

                    iBinding.progressCircle.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    iBinding.progressCircle.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            iBinding.progressCircle.setVisibility(View.INVISIBLE);
        }


        iBinding.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Search(s.toString());
            }
        });
    }

    private void Search(String text) {
        items.clear();

        mDatabaseRef.orderByChild("title").startAt(text)
                .endAt(text + "\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    Item item = userSnapshot.getValue(Item.class);
                    item.setId(userSnapshot.getKey());
                    items.add(item);
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
    }

    @Override
    public void onClick(View v) {

        if (v == iBinding.fab) {
            AddNewItem addNewItem = new AddNewItem();
            addNewItem.setCancelable(false);
            addNewItem.show(getSupportFragmentManager(), "fragment");
        }
    }

    @Override
    public void onItemClick(int position, Item item) {
    }

    @Override
    public void onDeleteClick(int position) {
        Item selectedItem = items.get(position);
        final String selectedKey = selectedItem.getId();

        mDatabaseRef.child(selectedKey).removeValue();
        Toast.makeText(MainActivity.this, R.string.delete_successful, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onNotifyClick(int position) {
        Item selectedItem = items.get(position);
        String title = selectedItem.getTitle();
        String description = selectedItem.getDescription();
        createNotification(title, description);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatabaseRef.removeEventListener(mDBListener);
    }


    public void createNotification(final String title, final String description) {

        final Calendar c = Calendar.getInstance();
        final int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                c.set(Calendar.SECOND, 0);


                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent intent = new Intent(MainActivity.this, BroadcastManager.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 1, intent, 0);
                if (c.before(Calendar.getInstance())) {
                    Toast.makeText(MainActivity.this, R.string.choose, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, R.string.remind, Toast.LENGTH_SHORT).show();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
                        } else {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
                        }
                    }
                }

            }
        }, hour, minute, false);

        timePickerDialog.show();

    }

}