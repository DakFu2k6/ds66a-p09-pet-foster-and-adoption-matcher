package com.petmatch.service;

import com.petmatch.model.FosterFamily;
import com.petmatch.repository.FosterFamilyRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FosterFamilyService {
    private final FosterFamilyRepository repository;

    public FosterFamilyService(FosterFamilyRepository repository) {
        this.repository = repository;
    }

    public List<FosterFamily> findAll() {
        return repository.findAll();
    }

    public Optional<FosterFamily> findById(long id) {
        return repository.findById(id);
    }

    public FosterFamily save(FosterFamily f) {
        Objects.requireNonNull(f, "Family is required.");
        if (f.getContactName() == null
                || f.getContactName().isBlank()
                || f.getPhone() == null
                || !f.getPhone().matches("[+0-9 ()-]{7,30}")
                || f.getPhone().replaceAll("[^0-9]", "").length() < 7
                || f.getHousingType() == null
                || f.getExperienceLevel() == null)
            throw new IllegalArgumentException(
                    "Contact name, phone (7-30 characters), housing and experience are required.");
        if (f.getActivePetCount() < 0)
            throw new IllegalArgumentException("Occupancy cannot be negative.");
        if (f.getFamilyName() == null || f.getFamilyName().isBlank())
            throw new IllegalArgumentException("Family name is required.");
        if (f.getFamilyName().length() > 120 || f.getContactName().length() > 120)
            throw new IllegalArgumentException(
                    "Family and contact names must be at most 120 characters.");
        if (f.getCapacity() <= 0 || f.getActivePetCount() > f.getCapacity())
            throw new IllegalArgumentException(
                    "Capacity must be positive and cannot be below current occupancy.");
        return repository.save(f);
    }

    public void update(FosterFamily f) {
        Objects.requireNonNull(f, "Family is required.");
        if (f.getContactName() == null
                || f.getContactName().isBlank()
                || f.getPhone() == null
                || !f.getPhone().matches("[+0-9 ()-]{7,30}")
                || f.getPhone().replaceAll("[^0-9]", "").length() < 7
                || f.getHousingType() == null
                || f.getExperienceLevel() == null)
            throw new IllegalArgumentException(
                    "Contact name, phone (7-30 characters), housing and experience are required.");
        if (f.getActivePetCount() < 0)
            throw new IllegalArgumentException("Occupancy cannot be negative.");
        if (f.getFamilyName() == null || f.getFamilyName().isBlank())
            throw new IllegalArgumentException("Family name is required.");
        if (f.getFamilyName().length() > 120 || f.getContactName().length() > 120)
            throw new IllegalArgumentException(
                    "Family and contact names must be at most 120 characters.");
        if (f.getCapacity() <= 0 || f.getActivePetCount() > f.getCapacity())
            throw new IllegalArgumentException(
                    "Capacity must be positive and cannot be below current occupancy.");

        repository.update(f);
    }
}
