package main.java.com.petmatch.model;

import java.time.LocalDateTime;

public class Placement {
    private long id, applicationId, petId, familyId;
    private LocalDateTime placementDate;
    private String status;

    public Placement(long id, long applicationId, long petId, long familyId, LocalDateTime date, String status) {
        this.id = id;
        this.applicationId = applicationId;
        this.petId = petId;
        this.familyId = familyId;
        this.placementDate = date;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public long getApplicationId() {
        return applicationId;
    }

    public long getPetId() {
        return petId;
    }

    public long getFamilyId() {
        return familyId;
    }

    public LocalDateTime getPlacementDate() {
        return placementDate;
    }

    public String getStatus() {
        return status;
    }
}
