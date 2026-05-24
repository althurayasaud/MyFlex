package com.example.myflex;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private static final int VIEW_SENT     = 1;
    private static final int VIEW_RECEIVED = 2;

    private final List<ChatMessage> messages;
    private final String            currentUserId;

    public ChatMessageAdapter(List<ChatMessage> messages, String currentUserId) {
        this.messages      = messages;
        this.currentUserId = currentUserId;
    }

    // ── View type: sent (right) vs received (left) ─────────────────
    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        if (msg.getSenderId() == null) return VIEW_RECEIVED;
        return msg.getSenderId().equals(currentUserId) ? VIEW_SENT : VIEW_RECEIVED;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == VIEW_SENT)
                ? R.layout.item_message_sent
                : R.layout.item_message_received;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);

        holder.tvMessage.setText(msg.getText());
        holder.tvTime.setText(formatTime(msg.getTimestamp()));

        // Received messages show sender's initials in the avatar
        if (holder.tvAvatar != null) {
            String name = msg.getSenderName();
            holder.tvAvatar.setText(getInitials(name));
        }
    }

    // ── Helpers ────────────────────────────────────────────────────
    private String formatTime(long timestamp) {
        return new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(timestamp));
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(parts.length, 2); i++) {
            if (!parts[i].isEmpty()) sb.append(parts[i].charAt(0));
        }
        return sb.toString().toUpperCase();
    }

    @Override
    public int getItemCount() { return messages.size(); }

    // ── ViewHolder ─────────────────────────────────────────────────
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMessage;
        final TextView tvTime;
        final TextView tvAvatar; // null for sent bubbles

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime    = itemView.findViewById(R.id.tvTime);
            tvAvatar  = itemView.findViewById(R.id.tvAvatar); // may be null
        }
    }
}