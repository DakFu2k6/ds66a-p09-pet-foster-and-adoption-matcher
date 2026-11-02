package com.petmatch.service;

import com.petmatch.model.*;
import com.petmatch.repository.FosterFamilyRepository;
import java.util.*;

public class FosterFamilyService {
    private final FosterFamilyRepository repository;

    public FosterFamilyService(FosterFamilyRepository repository) {
        this.repository = repository;
    }

    public List<FosterFamily> findAll() {
        return repository.findAll();
    }

    public FosterFamily save(FosterFamily f) {
        if (f.getFamilyName() == null || f.getFamilyName().isBlank())
            throw new IllegalArgumentException("Family name is required.");
        if (f.getCapacity() <= 0 || f.getActivePetCount() > f.getCapacity())
            throw new IllegalArgumentException("Capacity must be positive and cannot be below current occupancy.");
        return repository.save(f);
    }
}
