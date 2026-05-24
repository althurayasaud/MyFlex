package com.example.myflex.models;

public class Exercise {

    private String exerciseId;
    private String name;
    private String description;
    private String difficultyLevel;
    private int targetReps;
    private String instructions;
    private int duration;

    // 🔥 خاصية جديدة للـ Progress (نسبة الإنجاز)
    private int progress;

    // 🔥 خاصية إضافية للـ Details (وصف مختصر أو ميتا)
    private String details;

    // Constructor
    public Exercise(String exerciseId, String name, String description,
                    String difficultyLevel, int targetReps,
                    String instructions, int duration) {
        this.exerciseId = exerciseId;
        this.name = name;
        this.description = description;
        this.difficultyLevel = difficultyLevel;
        this.targetReps = targetReps;
        this.instructions = instructions;
        this.duration = duration;

        this.progress = 0;   // القيمة الافتراضية
        this.details = null; // يمكن تعيينها لاحقاً
    }

    // Getters
    public String getExerciseId() { return exerciseId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public int getTargetReps() { return targetReps; }
    public String getInstructions() { return instructions; }
    public int getDuration() { return duration; }

    // ✅ Progress
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    // ✅ Details
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}


