package com.example.nhom7vexeapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom7vexeapp.R;
import com.example.nhom7vexeapp.models.Route;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    private List<Route> routeList;
    private OnRouteActionListener listener;

    public interface OnRouteActionListener {
        void onEdit(Route route);
        void onDelete(Route route);
        void onStatusChange(Route route);
    }

    public RouteAdapter(List<Route> routeList, OnRouteActionListener listener) {
        this.routeList = routeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        Route route = routeList.get(position);
        holder.tvRouteName.setText(route.getName());
        holder.tvStartPoint.setText(route.getStartPoint());
        holder.tvEndPoint.setText(route.getEndPoint());
        holder.tvDistance.setText(route.getDistance());
        holder.tvTime.setText(route.getTime());
        
        String status = route.getStatus();
        if (status == null) status = "Đang hoạt động";
        holder.tvStatus.setText(status);

        // Cập nhật màu sắc và form tương ứng từ các file drawable của bạn
        if ("Đang hoạt động".equals(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")); // Chữ xanh lá đậm
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_green); // Form nền xanh nhạt
        } else if ("Bảo trì".equals(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#FBC02D")); // Chữ vàng đậm
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_yellow); // Form nền vàng nhạt
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#D32F2F")); // Chữ đỏ đậm
            // Sử dụng bg_input_error hoặc tạo mới bg_status_red nếu cần, ở đây dùng tạm bg_input_error để có form tương ứng
            holder.tvStatus.setBackgroundResource(R.drawable.bg_input_error); 
        }

        holder.tvStatus.setOnClickListener(v -> listener.onStatusChange(route));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(route));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(route));
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView tvRouteName, tvStartPoint, tvEndPoint, tvDistance, tvTime, tvStatus;
        MaterialButton btnEdit, btnDelete;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRouteName = itemView.findViewById(R.id.tvRouteName);
            tvStartPoint = itemView.findViewById(R.id.tvStartPoint);
            tvEndPoint = itemView.findViewById(R.id.tvEndPoint);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEditRoute);
            btnDelete = itemView.findViewById(R.id.btnDeleteRoute);
        }
    }
}
