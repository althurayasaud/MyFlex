package com.example.myflex;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(List<User> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvName.setText(user.getName());
        holder.tvEmail.setText(user.getEmail());

        // Set avatar initials
        String initials = getInitials(user.getName());
        holder.tvAvatar.setText(initials);

        // Set role badge
        String role = user.getRole();
        if (role != null) {
            if (role.equalsIgnoreCase("admin")) {
                holder.tvRole.setText("ADMIN");
                holder.tvRole.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.purple));
                holder.tvRole.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
            } else if (role.equalsIgnoreCase("therapist")) {
                holder.tvRole.setText("THERAPIST");
                holder.tvRole.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.orange));
                holder.tvRole.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
            } else {
                holder.tvRole.setText("PATIENT");
                holder.tvRole.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.green));
                holder.tvRole.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
            }
        } else {
            holder.tvRole.setText("PATIENT");
            holder.tvRole.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.green));
            holder.tvRole.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
        }

        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "U";
        String[] parts = name.split(" ");
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < Math.min(parts.length, 2); i++) {
            if (!parts[i].isEmpty()) {
                initials.append(parts[i].substring(0, 1).toUpperCase());
            }
        }
        return initials.toString();
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateList(List<User> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvEmail, tvRole;
        CardView cardView;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);

            // تأكد أن itemView هو CardView
            if (itemView instanceof CardView) {
                cardView = (CardView) itemView;
            } else {
                // إذا لم يكن CardView، قم بإنشاء واحد جديد أو استخدم findViewById
                cardView = itemView.findViewById(R.id.cardUser);
            }
        }
    }
}