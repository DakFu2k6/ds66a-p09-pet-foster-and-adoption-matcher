package com.petmatch.repository;

import com.petmatch.model.*;
import java.util.*;

public interface ApplicationRepository {
    List<AdoptionApplication> findAll();

    AdoptionApplication save(long petId, long familyId, String notes);

    void updateStatus(long id, ApplicationStatus status);

    Optional<AdoptionApplication> findById(long id);
}
