package com.petmatch;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.petmatch.exception.PlacementRejectedException;
import com.petmatch.repository.PlacementRepository;
import com.petmatch.service.PlacementService;
import org.junit.jupiter.api.Test;

class PlacementServiceTest {

    static class FakePlacementRepository implements PlacementRepository {
        @Override
        public com.petmatch.model.Placement createPlacement(
                long applicationId, long petId, long familyId) {
            throw new AssertionError("Repository must not be called for invalid inputs");
        }
    }

    @Test
    void missingInfoRejectsPlacement() {
        PlacementService service = new PlacementService(new FakePlacementRepository());

        assertNotNull(
                assertThrows(
                        PlacementRejectedException.class, () -> service.createPlacement(1L, null, null)));
    }
}
