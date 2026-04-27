package com.example.nhom7vexeapp.adapters;

import android.graphics.Color;
import android.content.res.ColorStateList;
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

        holder.tvTripId.setText("Mã chuyến: " + trip.getId());
        holder.tvRouteName.setText(trip.getRouteName());
        holder.tvVehicle.setText(trip.getVehicleType());
        holder.tvStatus.setText(trip.getStatus());

        // ✅ CẬP NHẬT MÀU NỀN TRẠNG THÁI THEO HÌNH ẢNH FIGMA (Merge từ File 2)
        if (trip.getStatus() != null && trip.getStatus().equals("Hoàn thành")) {
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFF9C4")));
            holder.tvStatus.setTextColor(Color.parseColor("#FBC02D"));
        }

        // Kiểm tra null cho tvTimeDate (Merge từ File 1 & 2)
        if (holder.tvTimeDate != null) {
            holder.tvTimeDate.setText(trip.getTime() + " | " + trip.getDate());
        }

        // Xử lý sự kiện nút Sửa
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(trip, position);
            }
        });

        // Xử lý sự kiện click vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(trip, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tripList != null ? tripList.size() : 0;
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