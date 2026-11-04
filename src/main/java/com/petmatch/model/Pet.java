package com.petmatch.model;

public abstract class Pet {
    private long id;
    private String name;
    private String breed;
    private int ageMonths;
    private String healthStatus;
    private String temperament;
    private boolean specialCareRequired;
    private String housingRequirement;
    private PetStatus status;
    private CareProfile careProfile;

    protected Pet(
            long id,
            String name,
            String breed,
            int ageMonths,
            String healthStatus,
            String temperament,
            boolean specialCareRequired,
            String housingRequirement,
            PetStatus status,
            CareProfile careProfile) {
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.ageMonths = ageMonths;
        this.healthStatus = healthStatus;
        this.temperament = temperament;
        this.specialCareRequired = specialCareRequired;
        this.housingRequirement = housingRequirement;
        this.status = status;
        this.careProfile = careProfile;
    }

    public abstract String getCareDescription();

    public abstract PetType getType();

    public abstract ExperienceLevel requiredExperience();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String v) {
        breed = v;
    }

    public int getAgeMonths() {
        return ageMonths;
    }

    public void setAgeMonths(int v) {
        ageMonths = v;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String v) {
        healthStatus = v;
    }

    public String getTemperament() {
        return temperament;
    }

    public void setTemperament(String v) {
        temperament = v;
    }

    public boolean isSpecialCareRequired() {
        return specialCareRequired;
    }

    public void setSpecialCareRequired(boolean v) {
        specialCareRequired = v;
    }

    public String getHousingRequirement() {
        return housingRequirement;
    }

    public void setHousingRequirement(String v) {
        housingRequirement = v;
    }

    public PetStatus getStatus() {
        return status;
    }

    public void setStatus(PetStatus v) {
        status = v;
    }

    public CareProfile getCareProfile() {
        return careProfile;
    }

    public void setCareProfile(CareProfile v) {
        careProfile = v;
    }
}
