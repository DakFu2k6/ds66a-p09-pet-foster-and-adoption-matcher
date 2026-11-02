package main.java.com.petmatch.service;

import main.java.com.petmatch.exception.InvalidPetException;
import main.java.com.petmatch.model.*;
import main.java.com.petmatch.repository.PetRepository;
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
        if (pet.getName() == null || pet.getName().isBlank())
            throw new InvalidPetException("Pet name is required.");
        if (pet.getAgeMonths() < 0)
            throw new InvalidPetException("Age cannot be negative.");
        return repository.save(pet);
    }

    public void delete(long id) {
        repository.delete(id);
    }
}
