package com.example.nhom7vexeapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom7vexeapp.R;
import com.example.nhom7vexeapp.models.Ticket;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<Ticket> ticketList;
    private Context context;
    private OnTicketClickListener listener;

    public interface OnTicketClickListener {
        void onTicketClick(Ticket ticket);
        void onCancelClick(Ticket ticket);
    }

    public TicketAdapter(Context context, List<Ticket> ticketList, OnTicketClickListener listener) {
        this.context = context;
        this.ticketList = ticketList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);
        holder.tvTime.setText(ticket.getGioDi());
        holder.tvDate.setText(ticket.getNgayKhoiHanh());
        holder.tvRoute.setText(ticket.getTenTuyen());
        holder.tvCar.setText(ticket.getTenNhaXe());
        holder.tvSeatCount.setText("Số lượng ghế: " + String.format("%02d", ticket.getSoLuongGhe()));
        holder.tvSeats.setText("Mã số ghế: " + ticket.getFormattedSeats());

        // Cấu hình nút dựa trên trạng thái (Tiếng Việt khớp với Backend)
        String status = ticket.getTrangThai();
        if ("Đã đặt".equals(status)) {
            holder.btnAction.setText("Hủy");
            holder.btnAction.setBackgroundColor(Color.parseColor("#EF2A39"));
            holder.btnAction.setEnabled(true);
            holder.btnAction.setOnClickListener(v -> {
                if (listener != null) listener.onCancelClick(ticket);
            });
        } else if ("Đã đi".equals(status)) {
            holder.btnAction.setText("Đã đi");
            holder.btnAction.setBackgroundColor(Color.parseColor("#FFC107"));
            holder.btnAction.setEnabled(false);
        } else if ("Đã hủy".equals(status)) {
            holder.btnAction.setText("Đã hủy");
            holder.btnAction.setBackgroundColor(Color.parseColor("#34B5F1"));
            holder.btnAction.setEnabled(false);
        } else {
            holder.btnAction.setText(status);
            holder.btnAction.setEnabled(false);
        }

        holder.itemView.setOnClickListener(v -> listener.onTicketClick(ticket));
    }

    @Override
    public int getItemCount() {
        return ticketList != null ? ticketList.size() : 0;
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvDate, tvRoute, tvCar, tvSeatCount, tvSeats;
        Button btnAction;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvRoute = itemView.findViewById(R.id.tvRoute);
            tvCar = itemView.findViewById(R.id.tvCar);
            tvSeatCount = itemView.findViewById(R.id.tvSeatCount);
            tvSeats = itemView.findViewById(R.id.tvSeats);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}
