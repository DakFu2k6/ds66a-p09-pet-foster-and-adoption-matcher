package com.petmatch.repository;

import com.petmatch.model.Placement;

public interface PlacementRepository {
    Placement createPlacement(long applicationId, long petId, long familyId);
}
