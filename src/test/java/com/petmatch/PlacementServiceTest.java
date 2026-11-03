package com.petmatch;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import com.petmatch.exception.PlacementRejectedException;
import com.petmatch.repository.PlacementRepository;
import com.petmatch.service.PlacementService;

class PlacementServiceTest {

    static class FakePlacementRepository implements PlacementRepository {
        @Override
        public void createPlacement(long applicationId, long petId, long familyId) {
        }
    }

    @Test
    void missingInfoRejectsPlacement() {
        PlacementService service = new PlacementService(new FakePlacementRepository());
        
        assertNotNull(assertThrows(PlacementRejectedException.class,
            () -> service.createPlacement(1L, null, null)));
    }
}