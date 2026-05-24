package com.example.myflex;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myflex.databinding.ActivityAdminSystemConfigBinding;

public class AdminSystemConfigActivity extends AppCompatActivity {

    private ActivityAdminSystemConfigBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminSystemConfigBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSaveConfig.setOnClickListener(v -> saveConfig());
    }

    private void saveConfig() {
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
    }
}