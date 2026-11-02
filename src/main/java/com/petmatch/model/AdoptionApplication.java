package com.petmatch.model;

import java.time.LocalDateTime;

public class AdoptionApplication {
    private long id, petId, familyId;
    private LocalDateTime applicationDate;
    private ApplicationStatus status;
    private String notes;

    public AdoptionApplication(long id, long petId, long familyId, LocalDateTime date, ApplicationStatus status,
            String notes) {
        this.id = id;
        this.petId = petId;
        this.familyId = familyId;
        this.applicationDate = date;
        this.status = status;
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public long getPetId() {
        return petId;
    }

    public long getFamilyId() {
        return familyId;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }
}
