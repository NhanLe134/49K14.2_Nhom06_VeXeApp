package com.example.nhom7vexeapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.nhom7vexeapp.R;
import com.example.nhom7vexeapp.models.Driver;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverViewHolder> {
    private List<Driver> driverList;
    private OnDriverListener listener;

    public interface OnDriverListener {
        void onDriverClick(Driver driver);
        void onDriverDelete(Driver driver);
    }

    public DriverAdapter(List<Driver> driverList, OnDriverListener listener) {
        this.driverList = driverList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        Driver driver = driverList.get(position);
        
        if (holder.tvName != null) {
            holder.tvName.setText(driver.getName() != null ? driver.getName() : "Không rõ tên");
        }
        
        if (holder.tvPhone != null) {
            holder.tvPhone.setText(driver.getPhone() != null ? driver.getPhone() : "N/A");
        }

        if (holder.imgDriver != null) {
            Glide.with(holder.itemView.getContext())
                .load(driver.getAvatarUrl())
                .placeholder(R.drawable.account_circle)
                .error(R.drawable.account_circle)
                .into(holder.imgDriver);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDriverClick(driver);
        });

        if (holder.btnDelete != null) {
            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDriverDelete(driver);
            });
        }
    }

    @Override
    public int getItemCount() {
        return driverList != null ? driverList.size() : 0;
    }

    static class DriverViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone;
        ImageView btnDelete;
        CircleImageView imgDriver;

        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDriverName);
            tvPhone = itemView.findViewById(R.id.tvDriverPhone);
            btnDelete = itemView.findViewById(R.id.btnDeleteDriver);
            imgDriver = itemView.findViewById(R.id.imgDriver);
        }
    }
}
