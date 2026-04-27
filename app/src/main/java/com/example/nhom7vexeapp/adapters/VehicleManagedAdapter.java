package com.example.nhom7vexeapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom7vexeapp.R;
import com.example.nhom7vexeapp.models.VehicleManaged;
import java.util.List;

public class VehicleManagedAdapter extends RecyclerView.Adapter<VehicleManagedAdapter.ViewHolder> {

    private List<VehicleManaged> vehicleList;
    private Context context;
    private OnVehicleActionListener listener;

    public interface OnVehicleActionListener {
        void onDelete(VehicleManaged vehicle, int position);
        void onShowDetail(VehicleManaged vehicle);
    }

    public VehicleManagedAdapter(List<VehicleManaged> vehicleList, Context context, OnVehicleActionListener listener) {
        this.vehicleList = vehicleList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_phuong_tien, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VehicleManaged vehicle = vehicleList.get(position);
        
        holder.tvIndex.setText(String.valueOf(position + 1));
        holder.tvPlate.setText(vehicle.getBienSoXe());
        holder.tvType.setText(vehicle.getLoaiXeIDStr());
        holder.tvStatus.setText(vehicle.getTrangThai());

        // Cấu hình màu sắc trạng thái
        String status = vehicle.getTrangThai();
        if (status != null) {
            if (status.equalsIgnoreCase("Bảo trì")) {
                holder.tvStatus.setTextColor(Color.parseColor("#FBC02D"));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_yellow);
            } else if (status.equalsIgnoreCase("Dừng hoạt động")) {
                holder.tvStatus.setTextColor(Color.GRAY);
                holder.tvStatus.setBackgroundResource(R.drawable.bg_input_gray);
            } else {
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_green);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onShowDetail(vehicle);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(vehicle, position);
        });
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIndex, tvPlate, tvType, tvStatus;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tvVehicleIndex);
            tvPlate = itemView.findViewById(R.id.tvVehiclePlate);
            tvType = itemView.findViewById(R.id.tvVehicleType);
            tvStatus = itemView.findViewById(R.id.tvVehicleStatus);
            btnDelete = itemView.findViewById(R.id.btnDeleteVehicle);
        }
    }
}
