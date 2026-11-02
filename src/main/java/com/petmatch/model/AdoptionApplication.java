package com.petmatch.model;

import java.time.LocalDate;
public class AdoptionApplication {
    
    private Integer id;                    
    private Pet pet;                     
    private FosterFamily family;        
    private ApplicationStatus status;   
    private LocalDate applicationDate; 

    public AdoptionApplication() {
    }

    public AdoptionApplication(Integer id, Pet pet, FosterFamily family, ApplicationStatus status, LocalDate applicationDate) {
        this.id = id;
        this.pet = pet;
        this.family = family;
        this.status = status;
        this.applicationDate = applicationDate;
    }
    
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Pet getPet() {
        return pet;
    }
    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public FosterFamily getFamily() {
        return family;
    }
    public void setFamily(FosterFamily family) {
        this.family = family;
    }

    public ApplicationStatus getStatus() {
        return status;
    }
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }
    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }
}