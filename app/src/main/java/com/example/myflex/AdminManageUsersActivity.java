package com.example.myflex;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myflex.databinding.ActivityAdminManageUsersBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminManageUsersActivity extends AppCompatActivity {

    private ActivityAdminManageUsersBinding binding;
    private FirebaseFirestore db;
    private List<User> userList;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminManageUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();

        setupRecyclerView();
        loadUsers();

        binding.btnBack.setOnClickListener(v -> finish());

        // Add User button
        if (binding.btnAddUser != null) {
            binding.btnAddUser.setOnClickListener(v -> {
                Toast.makeText(this, "Add User - coming soon", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupRecyclerView() {
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(userList, user -> {
            showEditRoleDialog(user);
        });
        binding.rvUsers.setAdapter(userAdapter);
    }

    private void loadUsers() {
        db.collection("users").get()
                .addOnSuccessListener(query -> {
                    userList.clear();
                    int patientCount = 0;
                    int therapistCount = 0;

                    for (QueryDocumentSnapshot doc : query) {
                        String role = doc.getString("role");
                        if ("patient".equalsIgnoreCase(role)) {
                            patientCount++;
                        } else if ("therapist".equalsIgnoreCase(role)) {
                            therapistCount++;
                        }

                        User user = new User(
                                doc.getId(),
                                doc.getString("name"),
                                doc.getString("email"),
                                role
                        );
                        userList.add(user);
                    }

                    // Update stats
                    if (binding.tvTotalUsers != null) {
                        binding.tvTotalUsers.setText("Total: " + userList.size());
                    }
                    if (binding.tvTotalPatients != null) {
                        binding.tvTotalPatients.setText("Patients: " + patientCount);
                    }
                    if (binding.tvTotalTherapists != null) {
                        binding.tvTotalTherapists.setText("Therapists: " + therapistCount);
                    }

                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showEditRoleDialog(User user) {
        Toast.makeText(this, "Edit: " + user.getName(), Toast.LENGTH_SHORT).show();
    }
}