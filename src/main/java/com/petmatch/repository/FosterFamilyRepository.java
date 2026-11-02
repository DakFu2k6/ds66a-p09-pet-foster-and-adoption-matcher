package com.petmatch.repository;

import com.petmatch.model.FosterFamily;
import java.util.*;

public interface FosterFamilyRepository {
    List<FosterFamily> findAll();

    Optional<FosterFamily> findById(long id);

    FosterFamily save(FosterFamily family);
}
