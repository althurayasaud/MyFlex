package com.example.myflex.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myflex.databinding.ItemSessionBinding;
import com.example.myflex.models.Session;
import java.util.List;

public class RecentSessionsAdapter extends RecyclerView.Adapter<RecentSessionsAdapter.ViewHolder> {

    private final List<Session> sessions;

    public RecentSessionsAdapter(List<Session> sessions) {
        this.sessions = sessions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSessionBinding binding = ItemSessionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Session session = sessions.get(position);

        holder.binding.tvExerciseName.setText(session.getExerciseName());
        holder.binding.tvReps.setText(session.getReps());
        holder.binding.tvFormScore.setText(session.getFormScore());
        holder.binding.tvDate.setText(session.getDate());
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSessionBinding binding;

        ViewHolder(ItemSessionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}