package com.example.myflex;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myflex.databinding.ActivityAdminPanelBinding;
import com.google.firebase.auth.FirebaseAuth;

public class AdminPanelActivity extends AppCompatActivity {

    private ActivityAdminPanelBinding binding;
    private SessionManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminPanelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sm = new SessionManager(this);

        // ✅ إدارة المستخدمين - تفتح صفحة جديدة
        binding.btnManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminManageUsersActivity.class);
            startActivity(intent);
        });

        // ✅ سجل التدقيق - تفتح صفحة جديدة
        binding.btnAuditLogs.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminAuditLogsActivity.class);
            startActivity(intent);
        });

        // ✅ تقارير الامتثال - تفتح صفحة جديدة
        binding.btnCompliance.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminComplianceActivity.class);
            startActivity(intent);
        });

        // ✅ إعدادات النظام - تفتح صفحة جديدة
        binding.btnSysConfig.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminSystemConfigActivity.class);
            startActivity(intent);
        });

        // ✅ تسجيل الخروج
        binding.btnAdminLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        sm.logout();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}