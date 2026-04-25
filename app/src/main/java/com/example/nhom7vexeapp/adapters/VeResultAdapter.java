package com.example.nhom7vexeapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom7vexeapp.R;
import com.example.nhom7vexeapp.models.TripSearchResult;

import java.util.List;

public class VeResultAdapter extends RecyclerView.Adapter<VeResultAdapter.ViewHolder> {

    private List<TripSearchResult> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onSelectClick(TripSearchResult item);
    }

    public VeResultAdapter(List<TripSearchResult> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ve_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TripSearchResult item = list.get(position);

        if (item != null) {
            holder.tvNhaXe.setText(item.getNhaXeName());
            holder.tvPrice.setText(item.getPrice() + "đ");
            holder.tvSeats.setText(item.getSeats() + " chỗ trống");
            holder.tvCar.setText(item.getCarType());

            // Gán riêng biệt Giờ đi và Giờ đến
            holder.tvTime.setText(item.getTime()); // Giờ đi (GioDi)
            if (item.getEndTime() != null) {
                holder.tvEndTime.setText(item.getEndTime()); // Giờ đến (GioDen)
            }
            
            holder.imgNhaXe.setImageResource(R.drawable.kh_home_1);

            holder.btnSelect.setOnClickListener(v -> {
                if (listener != null) listener.onSelectClick(item);
            });
        }
    }

    @Override
    public int getItemCount() { return list != null ? list.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvEndTime, tvNhaXe, tvPrice, tvSeats, tvCar;
        ImageView imgNhaXe;
        Button btnSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTimeResult); // Ô Giờ đi
            tvEndTime = itemView.findViewById(R.id.tvEndTimeResult); // Ô Giờ đến
            tvNhaXe = itemView.findViewById(R.id.tvNhaXeName);
            tvPrice = itemView.findViewById(R.id.tvPriceResult);
            tvSeats = itemView.findViewById(R.id.tvEmptySeats);
            tvCar = itemView.findViewById(R.id.tvCarType);
            btnSelect = itemView.findViewById(R.id.btnSelectSeat);
            imgNhaXe = itemView.findViewById(R.id.imgNhaXe);
        }
    }
}
