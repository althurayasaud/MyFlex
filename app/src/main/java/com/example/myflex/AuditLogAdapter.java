package com.example.myflex;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myflex.databinding.ItemLogBinding;
import java.util.List;

public class AuditLogAdapter extends RecyclerView.Adapter<AuditLogAdapter.ViewHolder> {

    private final List<AuditLog> logs;

    public AuditLogAdapter(List<AuditLog> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLogBinding binding = ItemLogBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AuditLog log = logs.get(position);

        holder.binding.tvAction.setText(log.getAction());
        holder.binding.tvUser.setText(log.getUser());
        holder.binding.tvDetails.setText(log.getDetails());
        holder.binding.tvTime.setText(log.getTime());

        // Change color based on action type
        if (log.getAction().contains("Login") && log.getAction().contains("Failed")) {
            holder.binding.tvAction.setTextColor(0xFFEF4444);
        } else if (log.getAction().contains("Login")) {
            holder.binding.tvAction.setTextColor(0xFF10B981);
        } else if (log.getAction().contains("Admin")) {
            holder.binding.tvAction.setTextColor(0xFF3B82F6);
        } else {
            holder.binding.tvAction.setTextColor(0xFF374151);
        }
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemLogBinding binding;

        ViewHolder(ItemLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}