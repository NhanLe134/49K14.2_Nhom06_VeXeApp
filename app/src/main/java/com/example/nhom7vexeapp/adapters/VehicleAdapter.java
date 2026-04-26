package com.example.nhom7vexeapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom7vexeapp.R;
import com.example.nhom7vexeapp.VehicleDetailActivity;
import com.example.nhom7vexeapp.models.Vehicle;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    private List<Vehicle> vehicleList;

    public VehicleAdapter(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_phuong_tien, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicleList.get(position);
        
        holder.tvIndex.setText(String.valueOf(position + 1));
        holder.tvPlate.setText(vehicle.getPlateNumber());
        holder.tvType.setText(vehicle.getType());
        holder.tvStatus.setText(vehicle.getStatus());

        // Đổi màu nhãn trạng thái tùy theo nội dung
        if (vehicle.getStatus().equals("Đang bảo trì")) {
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_yellow);
        } else if (vehicle.getStatus().equals("Tạm dừng")) {
            holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E"));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_input_gray);
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_green);
        }

        // Chuyển sang màn hình chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), VehicleDetailActivity.class);
            intent.putExtra("vehicle_data", vehicle);
            v.getContext().startActivity(intent);
        });

        // Xử lý khi nhấn vào icon thùng rác
        holder.btnDelete.setOnClickListener(v -> {
            showDeleteDialog(holder.itemView.getContext(), vehicle, holder.getAdapterPosition());
        });
    }

    private void showDeleteDialog(Context context, Vehicle vehicle, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_vehicle_confirmation, null);
        builder.setView(dialogView);

        TextView tvMessage = dialogView.findViewById(R.id.tvDeleteMessage);
        if (tvMessage != null) {
            tvMessage.setText("Bạn có chắc muốn xóa xe " + vehicle.getPlateNumber() + " này không?\nHành động này không thể hoàn tác.");
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        // Căn giữa popup và làm trong suốt nền
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.CENTER;
            window.setAttributes(lp);
        }

        MaterialButton btnNo = dialogView.findViewById(R.id.btnNo);
        MaterialButton btnYes = dialogView.findViewById(R.id.btnYes);

        btnNo.setOnClickListener(v -> dialog.dismiss());
        btnYes.setOnClickListener(v -> {
            // Xóa item khỏi danh sách và cập nhật UI
            if (position != RecyclerView.NO_POSITION) {
                vehicleList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, vehicleList.size());
                dialog.dismiss();
                // Hiển thị popup thành công
                showDeleteSuccessPopup(context);
            }
        });
    }

    private void showDeleteSuccessPopup(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_success, null);
        builder.setView(dialogView);

        TextView tvMessage = dialogView.findViewById(R.id.tvSuccessMessage);
        if (tvMessage != null) {
            tvMessage.setText("Đã xóa thông tin xe thành công.");
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        // Căn giữa popup và làm trong suốt nền
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.CENTER;
            window.setAttributes(lp);
        }

        // Tự động đóng sau 1.5 giây
        new Handler().postDelayed(dialog::dismiss, 1500);
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {
        TextView tvIndex, tvPlate, tvType, tvStatus;
        ImageView btnDelete;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tvVehicleIndex);
            tvPlate = itemView.findViewById(R.id.tvVehiclePlate);
            tvType = itemView.findViewById(R.id.tvVehicleType);
            tvStatus = itemView.findViewById(R.id.tvVehicleStatus);
            btnDelete = itemView.findViewById(R.id.btnDeleteVehicle);
        }
    }
}
