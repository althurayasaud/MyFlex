package com.example.myflex.models;

public class Session {
    private String exerciseName;
    private String reps;
    private String formScore;
    private String date;

    public Session(String exerciseName, String reps, String formScore, String date) {
        this.exerciseName = exerciseName;
        this.reps = reps;
        this.formScore = formScore;
        this.date = date;
    }

    public String getExerciseName() { return exerciseName; }
    public String getReps() { return reps; }
    public String getFormScore() { return formScore; }
    public String getDate() { return date; }
}