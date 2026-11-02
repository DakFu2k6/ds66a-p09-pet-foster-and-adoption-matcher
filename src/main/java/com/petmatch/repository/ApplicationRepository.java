package com.petmatch.repository;

import java.util.List;
import java.util.Optional;

import com.petmatch.model.AdoptionApplication;

public interface ApplicationRepository {
    AdoptionApplication save(long petId, long familyId, String status);

    List<AdoptionApplication> findAll();

    Optional<AdoptionApplication> findById(long id);

    void update(AdoptionApplication application);
}