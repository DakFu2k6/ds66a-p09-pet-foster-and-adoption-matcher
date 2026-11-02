package main.java.com.petmatch.model;

public class CareProfile {
    private long id;
    private int dailyExerciseMinutes;
    private String feedingNotes;
    private String medicalNotes;

    public CareProfile(long id, int dailyExerciseMinutes, String feedingNotes, String medicalNotes) {
        this.id = id;
        this.dailyExerciseMinutes = dailyExerciseMinutes;
        this.feedingNotes = feedingNotes;
        this.medicalNotes = medicalNotes;
    }

    public long getId() {
        return id;
    }

    public int getDailyExerciseMinutes() {
        return dailyExerciseMinutes;
    }

    public String getFeedingNotes() {
        return feedingNotes;
    }

    public String getMedicalNotes() {
        return medicalNotes;
    }
}
