package com.petmatch.service;

import com.petmatch.exception.InvalidPetException;
import com.petmatch.model.*;
import com.petmatch.repository.PetRepository;
import java.util.*;

public class PetService {
    private final PetRepository repository;

    public PetService(PetRepository repository) {
        this.repository = repository;
    }

    public List<Pet> findAll() {
        return repository.findAll();
    }

    public Pet save(Pet pet) {
        if (pet == null)
            throw new InvalidPetException("Pet is required.");
        if (pet.getStatus() == null || pet.getHousingRequirement() == null
                || !Set.of("ANY", "HOUSE", "APARTMENT", "YARD").contains(pet.getHousingRequirement()))
            throw new InvalidPetException("Valid status and housing requirement are required.");
        if (pet.getName() == null || pet.getName().isBlank())
            throw new InvalidPetException("Pet name is required.");
        if (pet.getAgeMonths() < 0)
            throw new InvalidPetException("Age cannot be negative.");
        if (pet.getName().length() > 100)
            throw new InvalidPetException("Name is too long.");
        if (pet.getCareProfile() != null && pet.getCareProfile().getDailyExerciseMinutes() < 0)
            throw new InvalidPetException("Exercise cannot be negative.");
        if (pet.getId() > 0) {
            repository.update(pet);
            return pet;
        }
        return repository.save(pet);
    }

    public void delete(long id) {
        repository.delete(id);
    }
}
