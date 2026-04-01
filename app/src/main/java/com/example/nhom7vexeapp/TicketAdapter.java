package com.example.nhom7vexeapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<TicketModel> ticketList;
    private Context context;
    private OnTicketClickListener listener;

    public interface OnTicketClickListener {
        void onTicketClick(TicketModel ticket);
    }

    public TicketAdapter(Context context, List<TicketModel> ticketList, OnTicketClickListener listener) {
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
        TicketModel ticket = ticketList.get(position);
        holder.tvTime.setText(ticket.getTime());
        holder.tvDate.setText(ticket.getDate());
        holder.tvRoute.setText(ticket.getRoute());
        holder.tvCar.setText(ticket.getCar());
        holder.tvSeatCount.setText(ticket.getSeatCount());
        holder.tvSeats.setText(ticket.getSeats());

        // Cấu hình nút dựa trên trạng thái
        if (ticket.getStatus().equals("Booked")) {
            holder.btnAction.setText("Hủy");
            holder.btnAction.setBackgroundColor(Color.parseColor("#EF2A39"));
        } else if (ticket.getStatus().equals("Completed")) {
            holder.btnAction.setText("Đã hoàn thành");
            holder.btnAction.setBackgroundColor(Color.parseColor("#FFC107"));
        } else if (ticket.getStatus().equals("Cancelled")) {
            holder.btnAction.setText("Đã hủy");
            holder.btnAction.setBackgroundColor(Color.parseColor("#34B5F1"));
        }

        holder.itemView.setOnClickListener(v -> listener.onTicketClick(ticket));
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
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
