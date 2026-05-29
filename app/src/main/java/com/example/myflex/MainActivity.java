package com.example.myflex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myflex.adapters.ExerciseAdapter;
import com.example.myflex.databinding.ActivityMainBinding;
import com.example.myflex.models.Exercise;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private List<Exercise> exercises = new ArrayList<>();
    private ExerciseAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        setupGreeting();
        NotificationHelper.showNotification(
                this,
                "Test Notification",
                "MyFlex notifications are working!"
        );
        setupStats();
        setupExerciseList();
        setupNavigation();

        binding.btnStartSession.setOnClickListener(v -> {
            NotificationHelper.showNotification(
                    this,
                    "Exercise Session Started",
                    "Stay consistent and complete your session today!"
            );
            startActivity(new Intent(this, ExerciseSessionActivity.class));
        });
    }

    // ================= GREETING =================
    private void setupGreeting() {
        // ✅ استخدم SessionManager بدل SharedPreferences مباشرة
        SessionManager sm = new SessionManager(this);
        String name = sm.getName();
        if (name == null || name.isEmpty()) name = "User";

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greet;
        if (hour < 12)      greet = "Good morning";
        else if (hour < 17) greet = "Good afternoon";
        else                greet = "Good evening";

        String firstName = name.split(" ")[0];
        binding.tvGreeting.setText(greet + ", " + firstName);

        // Initials للأفاتار
        String[] parts = name.split(" ");
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < Math.min(parts.length, 2); i++) {
            if (!parts[i].isEmpty())
                initials.append(parts[i].substring(0, 1).toUpperCase());
        }
        binding.tvAvatar.setText(initials.toString());

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH);
        binding.tvDate.setText(sdf.format(new Date()));
        NotificationHelper.showNotification(
                this,
                "Welcome to MyFlex 💪",
                "Time to complete your therapy exercises."
        );
    }

    // ================= STATS =================
    private void setupStats() {
        binding.tvTotalReps.setText("0");
        binding.tvFormScore.setText("0%");
        binding.tvFatigue.setText("Low");
    }

    // ================= EXERCISE LIST =================
    private void setupExerciseList() {

        binding.rvExercises.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ExerciseAdapter(exercises, exercise -> {
            Intent intent = new Intent(this, ExerciseSessionActivity.class);

            intent.putExtra("exercise_name", exercise.getName());

            // حماية من null
            if (exercise.getExerciseId() != null) {
                intent.putExtra("exercise_id", exercise.getExerciseId());
            }

            intent.putExtra("target_reps", exercise.getTargetReps());

            startActivity(intent);
        });

        binding.rvExercises.setAdapter(adapter);

        loadPlans();
    }

    // ================= FIREBASE LOAD =================
    private void loadPlans() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        Log.d("MainActivity", "🔄 Loading ALL exercises from Firebase");

        // ✅ جلب جميع الوثائق من مجموعة exercise_plans
        db.collection("exercise_plans")
                .get()
                .addOnSuccessListener(query -> {
                    exercises.clear();

                    Log.d("MainActivity", "📊 Total documents found: " + query.size());

                    if (query.isEmpty()) {
                        Log.w("MainActivity", "⚠️ No documents found in exercise_plans");
                        binding.tvPlanDescription.setText("No exercises available");
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    // ✅ تصفح جميع الوثائق بغض النظر عن patientId
                    for (QueryDocumentSnapshot doc : query) {
                        Log.d("MainActivity", "📄 Document ID: " + doc.getId());
                        Log.d("MainActivity", "📄 Full Data: " + doc.getData().toString());

                        // ✅ جلب البيانات بغض النظر عن وجود patientId أو لا
                        String name = doc.getString("exerciseName");
                        String desc = doc.getString("description");
                        String duration = doc.getString("duration");

                        // إذا كانت القيم null، استخدم قيماً افتراضية
                        if (name == null || name.isEmpty()) {
                            name = doc.getString("name"); // جرب اسم آخر
                        }
                        if (name == null || name.isEmpty()) {
                            name = "Exercise " + (exercises.size() + 1);
                        }

                        if (desc == null) desc = "No description available";
                        if (duration == null) duration = "10";

                        int reps;
                        try {
                            reps = Integer.parseInt(duration);
                        } catch (Exception e) {
                            reps = 10;
                        }

                        Exercise ex = new Exercise(
                                doc.getId(),
                                name,
                                desc,
                                "easy",
                                reps,
                                "",
                                0
                        );

                        ex.setDetails(desc);
                        exercises.add(ex);
                        Log.d("MainActivity", "✅ Added exercise: " + name);
                    }

                    adapter.notifyDataSetChanged();

                    binding.tvPlanDescription.setText(
                            exercises.size() + " exercises available"
                    );
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "❌ Firestore error: " + e.getMessage());
                    binding.tvPlanDescription.setText("Error: " + e.getMessage());
                });
    }



    // ================= BOTTOM NAV =================
    private void setupNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            }
            else if (id == R.id.nav_progress) {
                startActivity(new Intent(this, ProgressReportActivity.class));
                return true;
            }
            return false;
        });
        binding.btnChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupStats();
        loadPlans();
    }
}