package com.petmatch.repository;

import java.util.List;
import java.util.Optional;

import com.petmatch.model.FosterFamily;

public interface FosterFamilyRepository {
    List<FosterFamily> findAll();

    Optional<FosterFamily> findById(long id);

    FosterFamily save(FosterFamily family);

    void update(FosterFamily family); 
}