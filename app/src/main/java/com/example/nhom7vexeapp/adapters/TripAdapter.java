package com.example.nhom7vexeapp.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom7vexeapp.R;
import com.example.nhom7vexeapp.models.Trip;
import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {
    private List<Trip> tripList;
    private OnTripActionListener listener;

    public interface OnTripActionListener {
        void onEdit(Trip trip, int position);
        void onClick(Trip trip, int position);
    }

    public TripAdapter(List<Trip> tripList, OnTripActionListener listener) {
        this.tripList = tripList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = tripList.get(position);

        if (trip != null && holder != null) {
            if (holder.tvTripId != null) {
                holder.tvTripId.setText("Mã chuyến: " + (trip.getId() != null ? trip.getId() : "N/A"));
            }

            if (holder.tvRouteName != null) {
                holder.tvRouteName.setText(trip.getRouteName() != null ? trip.getRouteName() : "Chuyến xe mới");
            }

            // Cập nhật hiển thị Trạng thái theo yêu cầu mới
            if (holder.tvStatus != null) {
                String status = trip.getStatus() != null ? trip.getStatus() : "Chưa hoàn thành";
                
                // Chuẩn hóa tên trạng thái (loại bỏ chữ "Đã" nếu có)
                if (status.equalsIgnoreCase("Đã hoàn thành")) status = "Hoàn thành";
                holder.tvStatus.setText(status);
                
                GradientDrawable gd = new GradientDrawable();
                gd.setCornerRadius(15);
                
                if (status.equalsIgnoreCase("Hoàn thành")) {
                    gd.setColor(Color.parseColor("#E8F5E9")); // Nền xanh lá nhạt
                    holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Chữ xanh lá
                } else {
                    gd.setColor(Color.parseColor("#FFF9C4")); // Nền vàng nhạt
                    holder.tvStatus.setTextColor(Color.parseColor("#FBC02D")); // Chữ vàng đậm
                }
                holder.tvStatus.setBackground(gd);
            }

            if (holder.tvVehicle != null) {
                holder.tvVehicle.setText(trip.getVehicleType() != null ? trip.getVehicleType() : "Chưa gán xe");
            }

            if (holder.tvTimeDate != null) {
                String timeStr = trip.getTime() != null ? trip.getTime() : "--:--";
                String dateStr = trip.getDate() != null ? trip.getDate() : "Chưa có ngày";
                holder.tvTimeDate.setText(timeStr + " | " + dateStr);
                holder.tvTimeDate.setVisibility(View.VISIBLE);
            }

            if (holder.btnEdit != null) {
                holder.btnEdit.setOnClickListener(v -> {
                    if (listener != null) listener.onEdit(trip, position);
                });
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClick(trip, position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return (tripList != null) ? tripList.size() : 0;
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView tvTripId, tvRouteName, tvTimeDate, tvVehicle, tvStatus;
        Button btnEdit;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTripId = itemView.findViewById(R.id.tvTripId);
            tvRouteName = itemView.findViewById(R.id.tvRouteName);
            tvTimeDate = itemView.findViewById(R.id.tvTimeDate);
            tvVehicle = itemView.findViewById(R.id.tvVehicle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}
