package com.example.myflex;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myflex.adapters.RecentSessionsAdapter;   // ← هذا السطر مهم جداً
import com.example.myflex.databinding.ActivityProgressReportBinding;
import com.example.myflex.models.Session;
import java.util.ArrayList;
import java.util.List;

public class ProgressReportActivity extends AppCompatActivity {

    private ActivityProgressReportBinding binding;
    private RecentSessionsAdapter adapter;
    private final List<Session> sessionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProgressReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        loadStats();
        loadRecentSessions();
    }

    private void setupRecyclerView() {
        binding.rvSessions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecentSessionsAdapter(sessionList);
        binding.rvSessions.setAdapter(adapter);
    }

    private void loadStats() {
        SharedPreferences prefs = getSharedPreferences("myflex", MODE_PRIVATE);
        binding.tvTotalSessions.setText("12");
        binding.tvAvgForm.setText("74%");
    }

    private void loadRecentSessions() {
        sessionList.clear();

        sessionList.add(new Session("Knee Extension", "12 reps", "92%", "Today"));
        sessionList.add(new Session("Shoulder Raise", "10 reps", "81%", "Yesterday"));
        sessionList.add(new Session("Hip Flexor Stretch", "8 reps", "88%", "2 days ago"));
        sessionList.add(new Session("Back Row", "12 reps", "76%", "3 days ago"));

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}