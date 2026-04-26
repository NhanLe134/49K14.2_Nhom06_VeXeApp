package com.example.nhom7vexeapp.adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom7vexeapp.R;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.Loaixe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarTypeAdapter extends RecyclerView.Adapter<CarTypeAdapter.CarTypeViewHolder> {

    private List<Loaixe> carList;
    private Context context;

    public CarTypeAdapter(List<Loaixe> carList, Context context) {
        this.carList = carList;
        this.context = context;
    }

    private String getDisplayNameBySeats(int soCho) {
        switch (soCho) {
            case 4: return "Loại xe A";
            case 7: return "Loại xe B";
            case 9: return "Loại xe C";
            default: return "Loại xe " + soCho + " chỗ";
        }
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
        String displayName = getDisplayNameBySeats(car.getSoCho());

        holder.tvName.setText(displayName);
        if (displayName.contains(" ")) {
            holder.tvIcon.setText(displayName.substring(displayName.length() - 1));
        }

        holder.tvSeats.setText(car.getSoCho() + " chỗ");
        
        try {
            double gia = Double.parseDouble(car.getGiaVe());
            holder.tvPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", gia));
        } catch (Exception e) {
            holder.tvPrice.setText(car.getGiaVe() + " đ");
        }

        holder.tvDate.setText(car.getNgayCapNhatGia() != null ? car.getNgayCapNhatGia() : "Chưa cập nhật");
        holder.btnEdit.setOnClickListener(v -> showUpdateDialog(car, position));
    }

    private void showUpdateDialog(Loaixe car, int position) {
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
        ImageView btnCancelTop = dialog.findViewById(R.id.btnCancelUpdateTop);

        tvCarInfo.setText(getDisplayNameBySeats(car.getSoCho()) + " (" + car.getSoCho() + " chỗ)");
        
        try {
            double gia = Double.parseDouble(car.getGiaVe());
            tvCurrentPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", gia));
        } catch (Exception e) {
            tvCurrentPrice.setText(car.getGiaVe() + " đ");
        }

        View.OnClickListener cancelListener = v -> showCancelConfirmDialog(dialog);
        if (btnCancel != null) btnCancel.setOnClickListener(cancelListener);
        if (btnCancelTop != null) btnCancelTop.setOnClickListener(cancelListener);

        if (btnSave != null) {
            btnSave.setOnClickListener(vSave -> {
                String newPrice = edtPrice.getText().toString().trim();
                if (newPrice.isEmpty()) {
                    Toast.makeText(context, "Vui lòng nhập giá mới", Toast.LENGTH_SHORT).show();
                    return;
                }

                String oldPrice = car.getGiaVe();
                String oldDate = car.getNgayCapNhatGia();

                car.setGiaVe(newPrice);
                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                car.setNgayCapNhatGia(currentDate);

                ApiService apiService = ApiClient.getClient().create(ApiService.class);
                apiService.updateLoaixe(car.getLoaixeID(), car).enqueue(new Callback<Loaixe>() {
                    @Override
                    public void onResponse(Call<Loaixe> call, Response<Loaixe> response) {
                        if (response.isSuccessful()) {
                            notifyItemChanged(position);
                            dialog.dismiss();
                            showSuccessDialog("Cập nhật giá vé thành công");
                        } else {
                            car.setGiaVe(oldPrice);
                            car.setNgayCapNhatGia(oldDate);
                            Toast.makeText(context, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Loaixe> call, Throwable t) {
                        car.setGiaVe(oldPrice);
                        car.setNgayCapNhatGia(oldDate);
                        Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
        dialog.show();
    }

    private void showCancelConfirmDialog(Dialog updateDialog) {
        Dialog cancelDialog = new Dialog(context);
        cancelDialog.setContentView(R.layout.dialog_cancle_update_price);
        if (cancelDialog.getWindow() != null) {
            cancelDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnNo = cancelDialog.findViewById(R.id.btnNo);
        Button btnYes = cancelDialog.findViewById(R.id.btnYes);

        if (btnNo != null) btnNo.setOnClickListener(v -> cancelDialog.dismiss());
        if (btnYes != null) btnYes.setOnClickListener(v -> {
            cancelDialog.dismiss();
            updateDialog.dismiss();
        });

        cancelDialog.show();
    }

    private void showSuccessDialog(String message) {
        Dialog successDialog = new Dialog(this.context);
        successDialog.setContentView(R.layout.dialog_success);
        if (successDialog.getWindow() != null) {
            successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        TextView tvMessage = successDialog.findViewById(R.id.tvSuccessMessage);
        if (tvMessage != null) tvMessage.setText(message);
        successDialog.show();
        new Handler().postDelayed(successDialog::dismiss, 2000);
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
