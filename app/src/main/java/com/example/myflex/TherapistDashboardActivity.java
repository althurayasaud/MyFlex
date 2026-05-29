package com.example.myflex;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myflex.databinding.ActivityTherapistDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TherapistDashboardActivity extends AppCompatActivity {

    private ActivityTherapistDashboardBinding binding;
    private SessionManager sm;
    private FirebaseFirestore db;
    private UserAdapter userAdapter;
    private List<User> patientList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Protection
        try {
            binding = ActivityTherapistDashboardBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading interface", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            sm = new SessionManager(this);
            db = FirebaseFirestore.getInstance();
            patientList = new ArrayList<>();

            // Read Name
            String name = sm.getName();
            if (name == null || name.isEmpty() || name.equals("User")) {
                binding.tvTherapistName.setText("Therapist");
            } else {
                // حماية ضد name.split إذا كان الاسم بدون مسافات
                String firstName = name;
                if (name.contains(" ")) {
                    firstName = name.split(" ")[0];
                }
                binding.tvTherapistName.setText("Dr. " + firstName);
            }

            // ✅ تهيئة RecyclerView بالطريقة الصحيحة
            setupRecyclerView();

            // Loading patients
            loadPatients();

            //Buttons
            binding.btnCreatePlan.setOnClickListener(v -> {

                NotificationHelper.showNotification(
                        this,
                        "New Exercise Plan",
                        "A new therapy exercise has been assigned."
                );

                startActivity(new Intent(this, CreateExerciseActivity.class));

            });

            binding.btnViewReports.setOnClickListener(v ->
                    startActivity(new Intent(this, ProgressReportActivity.class))
            );

            binding.btnLogout.setOnClickListener(v -> logout());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "mistake: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        try {
            binding.rvPatients.setLayoutManager(new LinearLayoutManager(this));

            // Pass both the list and the click listener (2 arguments)
            userAdapter = new UserAdapter(patientList, this::openChat);
            binding.rvPatients.setAdapter(userAdapter);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error setting menu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //Separate function to open the conversation
    private void openChat(User patient) {
        if (patient == null) return;
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("other_user_id", patient.getUserId());
        intent.putExtra("other_user_name", patient.getName());
        intent.putExtra("other_user_role", "patient");
        startActivity(intent);
    }

    private void loadPatients() {
        db.collection("users")
                .whereEqualTo("role", "patient")
                .get()
                .addOnSuccessListener(query -> {
                    patientList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        try {
                            //Protection
                            String id = doc.getId();
                            String name = doc.getString("name");
                            String email = doc.getString("email");
                            String role = doc.getString("role");

                            if (name == null) name = "patient";
                            if (email == null) email = "";
                            if (role == null) role = "patient";

                            User user = new User(id, name, email, role);
                            patientList.add(user);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    userAdapter.notifyDataSetChanged();

                    if (patientList.isEmpty()) {
                        Toast.makeText(this, "لا يوجد مرضى", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "خطأ في تحميل المرضى: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void logout() {
        try {
            sm.logout();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}