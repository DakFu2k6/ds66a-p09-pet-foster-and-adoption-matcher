package com.petmatch.service;

import com.petmatch.exception.PlacementRejectedException;
import com.petmatch.model.FosterFamily;
import com.petmatch.model.Pet;
import com.petmatch.model.Placement;
import com.petmatch.repository.PlacementRepository;

public class PlacementService {

    private final PlacementRepository placementRepository;
    private final com.petmatch.repository.ApplicationRepository applications;

    public PlacementService(PlacementRepository placementRepository) {
        this(placementRepository, new com.petmatch.repository.JdbcApplicationRepository());
    }

    public PlacementService(
            PlacementRepository placementRepository,
            com.petmatch.repository.ApplicationRepository applications) {
        this.placementRepository = placementRepository;
        this.applications = applications;
    }

    public Placement createPlacement(long applicationId, Pet pet, FosterFamily family) {
        if (pet == null || family == null) {
            throw new PlacementRejectedException("Thiếu thông tin thú cưng hoặc gia đình để bàn giao.");
        }
        if (applicationId <= 0
                || pet.getId() <= 0
                || family.getId() <= 0
                || pet.getStatus() != com.petmatch.model.PetStatus.AVAILABLE
                || !family.isActive()
                || family.getActivePetCount() != 0)
            throw new PlacementRejectedException("Pet or family is unavailable or already committed.");
        if (family.getCapacity() <= family.getActivePetCount()) {
            throw new PlacementRejectedException(
                    "Từ chối bàn giao: Gia đình này đã đạt tối đa sức chứa!");
        }

        var app = applications
                .findById(applicationId)
                .orElseThrow(() -> new PlacementRejectedException("Application does not exist."));
        if ((app.getStatus() != com.petmatch.model.ApplicationStatus.PENDING
                && app.getStatus() != com.petmatch.model.ApplicationStatus.APPROVED)
                || app.getPet().getId() != pet.getId()
                || app.getFamily().getId() != family.getId())
            throw new PlacementRejectedException("Application is final or links do not match.");
        var result = new MatchingService()
                .evaluateMatch(app.getPet(), app.getFamily(), com.petmatch.model.MatchOperator.AND);
        if (!result.isSatisfied())
            throw new PlacementRejectedException(result.getReason());
        long petId = pet.getId();
        long familyId = family.getId();

        try {
            return placementRepository.createPlacement(applicationId, petId, familyId);
        } catch (Exception e) {
            throw new PlacementRejectedException("Bàn giao thất bại từ cơ sở dữ liệu: " + e.getMessage());
        }
    }
}
