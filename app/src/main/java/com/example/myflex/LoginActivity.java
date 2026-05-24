package com.example.myflex;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myflex.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private SessionManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sm = new SessionManager(this);

        showLoginUI();
    }

    private void showLoginUI() {
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(v -> loginUser());

        binding.tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    // استبدل الـ admin login block كله بهذا:

    private void loginUser() {
        String email    = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (binding.progressBar != null)
            binding.progressBar.setVisibility(View.VISIBLE);

        // ✅ كل المستخدمين (Admin + Therapist + Patient) عبر Firebase Auth
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();

                    db.collection("users").document(uid)
                            .get()
                            .addOnSuccessListener(doc -> {
                                if (binding.progressBar != null)
                                    binding.progressBar.setVisibility(View.GONE);

                                if (!doc.exists()) {
                                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String name = doc.getString("name");
                                String role = doc.getString("role");

                                if (role == null) {
                                    Toast.makeText(this, "Role not found", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                sm.login(uid, name, role);
                                navigateByRole(role);
                            })
                            .addOnFailureListener(e -> {
                                if (binding.progressBar != null)
                                    binding.progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    if (binding.progressBar != null)
                        binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void navigateByRole(String role) {
        Intent intent;

        if (role.equalsIgnoreCase("admin")) {
            intent = new Intent(this, AdminPanelActivity.class);
        } else if (role.equalsIgnoreCase("therapist")) {
            intent = new Intent(this, TherapistDashboardActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class); // patient
        }

        startActivity(intent);
        finish();
    }
}