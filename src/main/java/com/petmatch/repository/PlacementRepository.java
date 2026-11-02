package com.petmatch.repository;

public interface PlacementRepository {
    void createPlacement(long applicationId, long petId, long familyId);
}
