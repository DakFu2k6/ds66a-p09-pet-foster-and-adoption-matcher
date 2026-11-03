package com.petmatch.service;

import java.time.LocalDateTime;

import com.petmatch.exception.PlacementRejectedException;
import com.petmatch.model.FosterFamily;
import com.petmatch.model.Pet;
import com.petmatch.model.Placement;
import com.petmatch.repository.PlacementRepository;

public class PlacementService {

    private final PlacementRepository placementRepository;
    public PlacementService(PlacementRepository placementRepository) {
        this.placementRepository = placementRepository;
    }

    public Placement createPlacement(long applicationId, Pet pet, FosterFamily family) {
        if (pet == null || family == null) {
            throw new PlacementRejectedException("Thiếu thông tin thú cưng hoặc gia đình để bàn giao.");
        }
        if (family.getCapacity() <= family.getActivePetCount()) {
            throw new PlacementRejectedException("Từ chối bàn giao: Gia đình này đã đạt tối đa sức chứa!");
        }

        long petId = pet.getId();
        long familyId = family.getId();

        try {
            placementRepository.createPlacement(applicationId, petId, familyId);
        } catch (Exception e) {
            throw new PlacementRejectedException("Bàn giao thất bại từ cơ sở dữ liệu: " + e.getMessage());
        }
        return new Placement(0L, applicationId, petId, familyId, LocalDateTime.now(), "PLACED");
    }
}