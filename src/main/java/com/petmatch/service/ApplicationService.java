package main.java.com.petmatch.service;

import main.java.com.petmatch.exception.InvalidApplicationException;
import main.java.com.petmatch.model.*;
import main.java.com.petmatch.repository.*;
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
        return applications.save(petId, familyId, notes);
    }
}
