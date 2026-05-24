package com.example.myflex;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myflex.databinding.ActivityAdminAuditLogsBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminAuditLogsActivity extends AppCompatActivity {

    private ActivityAdminAuditLogsBinding binding;
    private FirebaseFirestore db;
    private List<AuditLog> logList;
    private AuditLogAdapter logAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAuditLogsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        logList = new ArrayList<>();

        setupRecyclerView();
        loadAuditLogs();

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Filter logs - coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        binding.rvLogs.setLayoutManager(new LinearLayoutManager(this));
        logAdapter = new AuditLogAdapter(logList);
        binding.rvLogs.setAdapter(logAdapter);
    }

    private void loadAuditLogs() {
        // Add sample data first
        addSampleLogs();

        // You can also load from Firestore if needed
        loadLogsFromFirestore();
    }

    private void addSampleLogs() {
        logList.clear();

        logList.add(new AuditLog(
                "🔐 User Login",
                "john@example.com",
                "User logged in successfully",
                "Today, 09:30 AM"
        ));

        logList.add(new AuditLog(
                "🏋️ Exercise Session",
                "patient@myflex.com",
                "Completed 12 reps - Form score: 92%",
                "Today, 10:15 AM"
        ));

        logList.add(new AuditLog(
                "👑 Admin Action",
                "admin@myflex.com",
                "Updated system configuration",
                "Yesterday, 02:30 PM"
        ));

        logList.add(new AuditLog(
                "📝 Exercise Plan Created",
                "therapist@myflex.com",
                "Created new plan: Knee Exercise",
                "Yesterday, 11:00 AM"
        ));

        logList.add(new AuditLog(
                "🔐 User Login",
                "sarah@example.com",
                "User logged in successfully",
                "2 days ago, 08:45 AM"
        ));

        logList.add(new AuditLog(
                "⚠️ Failed Login Attempt",
                "unknown@email.com",
                "Invalid password attempt",
                "2 days ago, 03:20 AM"
        ));

        logAdapter.notifyDataSetChanged();
    }

    private void loadLogsFromFirestore() {
        // Optional: Load real logs from Firestore
        db.collection("audit_logs")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        String action = doc.getString("action");
                        String user = doc.getString("user");
                        String details = doc.getString("details");
                        String time = doc.getString("time");

                        if (action != null) {
                            logList.add(new AuditLog(action, user, details, time));
                        }
                    }
                    logAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // If no logs in Firestore, keep sample data
                    Toast.makeText(this, "Using sample data", Toast.LENGTH_SHORT).show();
                });
    }
}