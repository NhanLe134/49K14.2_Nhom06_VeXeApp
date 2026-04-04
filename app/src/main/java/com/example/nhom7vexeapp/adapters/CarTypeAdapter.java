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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom7vexeapp.R;
import com.example.nhom7vexeapp.models.Loaixe;

import java.util.List;
import java.util.Locale;

public class CarTypeAdapter extends RecyclerView.Adapter<CarTypeAdapter.CarTypeViewHolder> {

    private List<Loaixe> carList;
    private Context context;

    public CarTypeAdapter(List<Loaixe> carList, Context context) {
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
        Loaixe car = carList.get(position);

        holder.tvName.setText(car.getLoaixeID());
        holder.tvSeats.setText(car.getSoCho() + " chỗ");
        
        // Hiển thị giá vé an toàn
        try {
            double gia = Double.parseDouble(car.getGiaVe());
            holder.tvPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", gia));
        } catch (Exception e) {
            holder.tvPrice.setText(car.getGiaVe() + " đ");
        }

        holder.tvDate.setText(car.getNgayCapNhatGia() != null ? car.getNgayCapNhatGia() : "Chưa cập nhật");

        if (car.getLoaixeID() != null && !car.getLoaixeID().isEmpty()) {
            // Lấy ký tự cuối làm Icon đại diện
            holder.tvIcon.setText(car.getLoaixeID().substring(car.getLoaixeID().length() - 1));
        }

        holder.btnEdit.setOnClickListener(v -> showUpdateDialog(car));
    }

    private void showUpdateDialog(Loaixe car) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_update_price);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvCarInfo = dialog.findViewById(R.id.tvDialogCarName);
        TextView tvCurrentPrice = dialog.findViewById(R.id.tvDialogCurrentPrice);
        EditText edtPrice = dialog.findViewById(R.id.edtNewPrice);
        Button btnSave = dialog.findViewById(R.id.btnSavePrice);
        Button btnCancel = dialog.findViewById(R.id.btnCancelUpdate);

        tvCarInfo.setText(car.getLoaixeID() + " (" + car.getSoCho() + " chỗ)");
        tvCurrentPrice.setText(car.getGiaVe() + " đ");

        btnSave.setOnClickListener(vSave -> dialog.dismiss());
        btnCancel.setOnClickListener(vCancel -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return carList != null ? carList.size() : 0;
    }

    public static class CarTypeViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSeats, tvPrice, tvDate, tvIcon;
        Button btnEdit;

        public CarTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCarTypeName);
            tvSeats = itemView.findViewById(R.id.tvSeats);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDate = itemView.findViewById(R.id.tvLastUpdate);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            btnEdit = itemView.findViewById(R.id.btnUpdatePrice);
        }
    }
}
