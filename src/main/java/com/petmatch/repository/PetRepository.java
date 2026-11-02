package main.java.com.petmatch.repository;

import main.java.com.petmatch.model.*;
import java.util.*;

public interface PetRepository {
    List<Pet> findAll();

    Optional<Pet> findById(long id);

    Pet save(Pet pet);

    void update(Pet pet);

    void delete(long id);
}
