package com.example.nhom7vexeapp.adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom7vexeapp.R;
import com.example.nhom7vexeapp.models.CarType;

import java.util.List;

public class CarTypeAdapter extends RecyclerView.Adapter<CarTypeAdapter.CarTypeViewHolder> {

    private List<CarType> carList;
    private Context context;

    public CarTypeAdapter(List<CarType> carList, Context context) {
        this.carList = carList;
        this.context = context;
    }

    @NonNull
    @Override
    public CarTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_car_type, parent, false);
        return new CarTypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarTypeViewHolder holder, int position) {
        CarType car = carList.get(position);

        // Hiển thị dữ liệu lên danh sách chính
        holder.tvName.setText(car.getName());
        holder.tvSeats.setText(car.getSeats() + " chỗ");
        holder.tvPrice.setText(car.getPrice());
        holder.tvDate.setText(car.getLastUpdate());
        holder.vLine.setBackgroundColor(car.getColor());
        holder.tvIcon.setText(car.getName().substring(car.getName().length() - 1));

        // Nút Cập nhật giá vé
        holder.btnEdit.setOnClickListener(v -> showUpdateDialog(car));
    }

    private void showUpdateDialog(CarType car) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_update_price);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // 1. Ánh xạ các View trong Dialog
        TextView tvCarInfo = dialog.findViewById(R.id.tvDialogCarName);
        TextView tvCurrentPrice = dialog.findViewById(R.id.tvDialogCurrentPrice);
        TextView tvUnsetLabel = dialog.findViewById(R.id.tvUnsetLabel);
        EditText edtPrice = dialog.findViewById(R.id.edtNewPrice);
        TextView tvError = dialog.findViewById(R.id.tvErrorPrice);
        Button btnSave = dialog.findViewById(R.id.btnSavePrice);
        Button btnCancel = dialog.findViewById(R.id.btnCancelUpdate);

        // 2. Đổ dữ liệu động dựa trên xe người dùng chọn
        tvCarInfo.setText(car.getName() + " (" + car.getSeats() + " chỗ)");

        // KIỂM TRA: Nếu giá là "Chưa thiết lập" thì hiện nhãn cam, ngược lại hiện số tiền
        if (car.getPrice().equals("Chưa thiết lập") || car.getPrice().isEmpty()) {
            tvUnsetLabel.setVisibility(View.VISIBLE); // Hiện nhãn màu cam
            tvCurrentPrice.setVisibility(View.GONE);  // Ẩn text giá tiền
        } else {
            tvUnsetLabel.setVisibility(View.GONE);    // Ẩn nhãn màu cam
            tvCurrentPrice.setVisibility(View.VISIBLE); // Hiện text giá tiền
            tvCurrentPrice.setText(car.getPrice());
        }

        // 3. Xử lý nút Lưu
        btnSave.setOnClickListener(vSave -> {
            String input = edtPrice.getText().toString().trim();
            if (input.isEmpty() || Integer.parseInt(input) <= 0) {
                tvError.setVisibility(View.VISIBLE);
            } else {
                tvError.setVisibility(View.GONE);
                dialog.dismiss();
                showSuccessDialog(); // Hiện thông báo thành công
            }
        });

        // 4. Xử lý nút Hủy
        btnCancel.setOnClickListener(vCancel -> dialog.dismiss());

        dialog.show();
    }

    private void showSuccessDialog() {
        Dialog successDialog = new Dialog(context);
        successDialog.setContentView(R.layout.dialog_success);
        successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        successDialog.setCanceledOnTouchOutside(true);

        successDialog.show();
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public static class CarTypeViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSeats, tvPrice, tvDate, tvIcon;
        View vLine;
        Button btnEdit;

        public CarTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCarTypeName);
            tvSeats = itemView.findViewById(R.id.tvSeats);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDate = itemView.findViewById(R.id.tvLastUpdate);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            vLine = itemView.findViewById(R.id.viewHeaderColor);
            btnEdit = itemView.findViewById(R.id.btnUpdatePrice);
        }
    }
}