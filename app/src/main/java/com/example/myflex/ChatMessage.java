package com.example.myflex;

/**
 * ChatMessage — Data model for a single chat message in Firestore.
 */
public class ChatMessage {
    private String messageId;
    private String senderId;
    private String senderName;
    private String senderRole;
    private String text;
    private long timestamp;
    private boolean isRead;

    // Required empty constructor for Firestore
    public ChatMessage() {}

    public ChatMessage(String messageId, String senderId, String senderName,
                       String senderRole, String text, long timestamp) {
        this.messageId  = messageId;
        this.senderId   = senderId;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.text       = text;
        this.timestamp  = timestamp;
        this.isRead     = false;
    }

    public String getMessageId()  { return messageId; }
    public String getSenderId()   { return senderId; }
    public String getSenderName() { return senderName; }
    public String getSenderRole() { return senderRole; }
    public String getText()       { return text; }
    public long   getTimestamp()  { return timestamp; }
    public boolean isRead()       { return isRead; }

    public void setMessageId(String messageId)   { this.messageId = messageId; }
    public void setSenderId(String senderId)     { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
    public void setText(String text)             { this.text = text; }
    public void setTimestamp(long timestamp)     { this.timestamp = timestamp; }
    public void setRead(boolean read)            { isRead = read; }
}
