package com.example.myflex;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myflex.databinding.ActivityAdminComplianceBinding;

public class AdminComplianceActivity extends AppCompatActivity {

    private ActivityAdminComplianceBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminComplianceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnGenerateReport.setOnClickListener(v -> generateReport());
    }

    private void generateReport() {
        Toast.makeText(this, "Generating compliance report...", Toast.LENGTH_SHORT).show();
        // هنا كود إنشاء التقرير
    }
}
