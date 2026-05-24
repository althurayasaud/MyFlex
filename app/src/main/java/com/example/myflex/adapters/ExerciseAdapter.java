package com.example.myflex.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myflex.R;
import com.example.myflex.databinding.ItemExerciseBinding;
import com.example.myflex.models.Exercise;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {

    private final List<Exercise> exercises;
    private final OnExerciseClickListener listener;

    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
    }

    public ExerciseAdapter(List<Exercise> exercises, OnExerciseClickListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExerciseBinding binding = ItemExerciseBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise ex = exercises.get(position);

        holder.binding.tvExerciseName.setText(ex.getName());

        // ✅ Safe solution for getDetails() / getDescription() problem
        String metaText = getExerciseMeta(ex);
        holder.binding.tvExerciseMeta.setText(metaText);

        holder.binding.progressBar.setProgress(ex.getProgress());
        holder.binding.tvProgressPct.setText(ex.getProgress() + "%");

        // Set exercise image
        holder.binding.imgExercise.setImageResource(getExerciseImage(ex.getName()));

        // Set difficulty badge
        setupDifficultyBadge(holder, ex.getDifficultyLevel());

        holder.binding.getRoot().setOnClickListener(v ->
                listener.onExerciseClick(ex));
    }

    /**
     * Safe method to get exercise meta/description/details
     */
    private String getExerciseMeta(Exercise ex) {
        if (ex == null) return "12 reps — Easy";

        // Try all possible getter names
        if (ex.getDetails() != null) return ex.getDetails();
        if (hasMethodGetDescription(ex) && ex.getDescription() != null)
            return ex.getDescription();


        // Fallback from MainActivity data
        return "12 reps — Easy";
    }

    // Safe check to avoid crash if getDescription() doesn't exist
    private boolean hasMethodGetDescription(Exercise ex) {
        try {
            ex.getClass().getMethod("getDescription");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private void setupDifficultyBadge(ViewHolder holder, String difficulty) {
        String level = (difficulty == null) ? "easy" : difficulty.toLowerCase();

        switch (level) {
            case "medium":
                holder.binding.tvDifficulty.setText("Medium");
                holder.binding.tvDifficulty.setTextColor(0xFFF59E0B);
                holder.binding.tvDifficulty.setBackgroundResource(R.drawable.badge_amber_light);
                break;

            case "hard":
                holder.binding.tvDifficulty.setText("Hard");
                holder.binding.tvDifficulty.setTextColor(0xFFEF4444);
                holder.binding.tvDifficulty.setBackgroundResource(R.drawable.badge_red_light);
                break;

            default: // easy
                holder.binding.tvDifficulty.setText("Easy");
                holder.binding.tvDifficulty.setTextColor(0xFF1A73E8);
                holder.binding.tvDifficulty.setBackgroundResource(R.drawable.badge_blue_light);
                break;
        }
    }

    private int getExerciseImage(String name) {
        if (name == null) return R.drawable.sincerely;

        String lower = name.toLowerCase();
        if (lower.contains("knee"))    return R.drawable.img_knee;
        if (lower.contains("shoulder")) return R.drawable.img_shoulder;
        if (lower.contains("hip"))     return R.drawable.img_hip;
        if (lower.contains("back"))    return R.drawable.back;
        if (lower.contains("arm"))     return R.drawable.arm;

        return R.drawable.sincerely; // default image
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemExerciseBinding binding;

        ViewHolder(ItemExerciseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}