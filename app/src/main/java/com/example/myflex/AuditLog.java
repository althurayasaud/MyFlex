package com.example.myflex;

public class AuditLog {
    private String action;
    private String user;
    private String details;
    private String time;

    public AuditLog(String action, String user, String details, String time) {
        this.action = action;
        this.user = user;
        this.details = details;
        this.time = time;
    }

    public String getAction() { return action; }
    public String getUser() { return user; }
    public String getDetails() { return details; }
    public String getTime() { return time; }
}