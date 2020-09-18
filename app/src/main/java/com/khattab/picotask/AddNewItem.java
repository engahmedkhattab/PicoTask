package com.khattab.picotask;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.databinding.DataBindingUtil;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.khattab.picotask.databinding.AddNewItemBinding;
import com.khattab.picotask.pojo.Item;

public class AddNewItem extends AppCompatDialogFragment implements View.OnClickListener {

    AddNewItemBinding binding;
    private DatabaseReference mDatabaseRef;

    public AddNewItem() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.add_new_item, null, false);
        initViews();
        return binding.getRoot();
    }

    private void initViews() {
        binding.save.setOnClickListener(this);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Items");
    }

    @Override
    public void onResume() {
        super.onResume();
        int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
        int height = getResources().getDimensionPixelSize(R.dimen.dialog_height);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_solid_shape);
        getDialog().getWindow().setLayout(width, height);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.save) {

            String title = binding.title.getText().toString();
            String description = binding.description.getText().toString();
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description)) {
                Toast.makeText(getContext(), R.string.missing, Toast.LENGTH_SHORT).show();
            } else {
                Item item = new Item(title, description);
                mDatabaseRef.push().setValue(item);
                dismiss();
            }

        }
    }
}
