package com.example.nhom7vexeapp.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
        holder.tvStatus.setText(route.getStatus());

        // Custom background for status
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(20f);
        
        if ("Đang hoạt động".equals(route.getStatus())) {
            holder.tvStatus.setText("Đang hoạt động");
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")); // Green
            shape.setColor(Color.parseColor("#E8F5E9")); // Light Green
        } else {
            holder.tvStatus.setText("Ngưng hoạt động");
            holder.tvStatus.setTextColor(Color.parseColor("#EF6C00")); // Orange
            shape.setColor(Color.parseColor("#FFF3E0")); // Light Orange
        }
        holder.tvStatus.setBackground(shape);

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
