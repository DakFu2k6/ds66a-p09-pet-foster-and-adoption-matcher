package com.petmatch.service;

import com.petmatch.exception.InvalidApplicationException;
import com.petmatch.model.*;
import com.petmatch.repository.*;
import java.util.*;

public class ApplicationService {
    private final ApplicationRepository applications;
    private final PetRepository pets;
    private final FosterFamilyRepository families;

    public ApplicationService(ApplicationRepository a, PetRepository p, FosterFamilyRepository f) {
        applications = a;
        pets = p;
        families = f;
    }

    public List<AdoptionApplication> findAll() {
        return applications.findAll();
    }

    public AdoptionApplication create(long petId, long familyId, String notes) {
        Pet pet = pets.findById(petId).orElseThrow(() -> new InvalidApplicationException("Pet does not exist."));
        FosterFamily family = families.findById(familyId)
                .orElseThrow(() -> new InvalidApplicationException("Family does not exist."));
        if (pet.getStatus() != PetStatus.AVAILABLE)
            throw new InvalidApplicationException("Only available pets can receive applications.");
        if (!family.isActive())
            throw new InvalidApplicationException("Family is inactive.");
        if (!family.hasCapacity())
            throw new InvalidApplicationException("Family is at capacity.");
        if (applications.findAll().stream().anyMatch(a -> a.getPetId() == petId && a.getFamilyId() == familyId
                && (a.getStatus() == ApplicationStatus.PENDING || a.getStatus() == ApplicationStatus.APPROVED)))
            throw new InvalidApplicationException("An open application already exists for this pet and family.");
        return applications.save(petId, familyId, notes);
    }
}
