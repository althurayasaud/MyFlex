package com.example.myflex;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myflex.databinding.ActivityCreateExerciseBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateExerciseActivity extends AppCompatActivity {

    private ActivityCreateExerciseBinding binding;
    private FirebaseFirestore db;
    private String therapistId;

    // 🔥 قائمة المرضى
    private ArrayList<String> patientNames = new ArrayList<>();
    private ArrayList<String> patientIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCreateExerciseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        therapistId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Setup Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }

        // تحميل المرضى في Spinner
        loadPatients();

        // زر الحفظ
        binding.btnSavePlan.setOnClickListener(v -> savePlan());
    }

    // 🟢 جلب المرضى من Firestore
    private void loadPatients() {

        db.collection("users")
                .whereEqualTo("role", "patient")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    patientNames.clear();
                    patientIds.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        String name = doc.getString("name");

                        if (name != null) {
                            patientNames.add(name);
                            patientIds.add(doc.getId());
                        }
                    }

                    if (patientNames.isEmpty()) {
                        Toast.makeText(this, "No patients found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_dropdown_item,
                            patientNames
                    );

                    binding.spPatients.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading patients", Toast.LENGTH_SHORT).show()
                );
    }

    // 🟢 حفظ الخطة
    private void savePlan() {
        String name = binding.etExerciseName.getText().toString().trim();
        String desc = binding.etDescription.getText().toString().trim();
        String duration = binding.etDuration.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(duration)) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> plan = new HashMap<>();
        plan.put("exerciseName", name);
        plan.put("description", desc);
        plan.put("duration", duration);
        plan.put("therapistId", therapistId);
        // ❌ لا تضف patientId أبداً
        plan.put("timestamp", System.currentTimeMillis());

        db.collection("exercise_plans")
                .add(plan)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Exercise added for all patients!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}