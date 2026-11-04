package com.petmatch.model;

import java.time.LocalDateTime;

public class Placement {
    private long id;
    private long applicationId;
    private long petId;
    private long familyId;
    private LocalDateTime placementDate;
    private String status;

    public Placement() {
    }

    public Placement(
            long id,
            long applicationId,
            long petId,
            long familyId,
            LocalDateTime placementDate,
            String status) {
        this.id = id;
        this.applicationId = applicationId;
        this.petId = petId;
        this.familyId = familyId;
        this.placementDate = placementDate;
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

    public void setId(long id) {
        this.id = id;
    }

    public void setApplicationId(long applicationId) {
        this.applicationId = applicationId;
    }

    public void setPetId(long petId) {
        this.petId = petId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public void setPlacementDate(LocalDateTime placementDate) {
        this.placementDate = placementDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
