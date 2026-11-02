package com.petmatch.service;

import com.petmatch.exception.PlacementRejectedException;
import com.petmatch.model.*;
import com.petmatch.matching.CriterionResult;
import com.petmatch.repository.*;
import java.util.*;

public class PlacementService {
    private final PlacementRepository placements;
    private final ApplicationRepository applications;
    private final PetRepository pets;
    private final FosterFamilyRepository families;
    private final MatchingService matching;

    public PlacementService(PlacementRepository p, ApplicationRepository a, PetRepository pets,
            FosterFamilyRepository f, MatchingService m) {
        placements = p;
        applications = a;
        this.pets = pets;
        families = f;
        matching = m;
    }

    public void confirm(long applicationId) {
        AdoptionApplication app = applications.findById(applicationId)
                .orElseThrow(() -> new PlacementRejectedException("Application not found."));
        Pet pet = pets.findById(app.getPetId()).orElseThrow(() -> new PlacementRejectedException("Pet not found."));
        FosterFamily family = families.findById(app.getFamilyId())
                .orElseThrow(() -> new PlacementRejectedException("Family not found."));
        if (app.getStatus() != ApplicationStatus.PENDING && app.getStatus() != ApplicationStatus.APPROVED)
            throw new PlacementRejectedException("Application must be pending or approved.");
        List<CriterionResult> results = matching.evaluate(pet, family);
        if (!matching.isPassed(results, MatchOperator.AND))
            throw new PlacementRejectedException("Matching requirements are not all satisfied.");
        try {
            placements.createPlacement(applicationId, pet.getId(), family.getId());
        } catch (RuntimeException ex) {
            throw new PlacementRejectedException(ex.getMessage());
        }
    }
}
