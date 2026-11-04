package com.petmatch.repository;

import com.petmatch.model.AdoptionApplication;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository {
    AdoptionApplication save(long petId, long familyId, String status);

    List<AdoptionApplication> findAll();

    Optional<AdoptionApplication> findById(long id);

    void update(AdoptionApplication application);
}
