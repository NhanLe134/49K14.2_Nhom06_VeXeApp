package com.example.nhom7vexeapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom7vexeapp.R;
import com.example.nhom7vexeapp.models.Driver;
import java.util.List;

public class AssignDriverAdapter extends RecyclerView.Adapter<AssignDriverAdapter.ViewHolder> {

    private List<Driver> drivers;
    private OnDriverSelectedListener listener;

    public interface OnDriverSelectedListener {
        void onDriverSelected(Driver driver);
    }

    public AssignDriverAdapter(List<Driver> drivers, OnDriverSelectedListener listener) {
        this.drivers = drivers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assign_driver, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Driver driver = drivers.get(position);
        holder.tvName.setText(driver.getName());
        holder.tvPhone.setText("SĐT: " + driver.getPhone());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDriverSelected(driver);
        });
    }

    @Override
    public int getItemCount() {
        return drivers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAssignDriverName);
            tvPhone = itemView.findViewById(R.id.tvAssignDriverPhone);
        }
    }
}
