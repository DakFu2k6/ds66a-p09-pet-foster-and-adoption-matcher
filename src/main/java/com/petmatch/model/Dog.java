package com.petmatch.model;

public class Dog extends Pet {
    public Dog(
            long id,
            String name,
            String breed,
            int ageMonths,
            String healthStatus,
            String temperament,
            boolean specialCare,
            String housingRequirement,
            PetStatus status,
            CareProfile careProfile) {
        super(
                id,
                name,
                breed,
                ageMonths,
                healthStatus,
                temperament,
                specialCare,
                housingRequirement,
                status,
                careProfile);
    }

    @Override
    public String getCareDescription() {
        return "Dogs need daily walks, exercise and social interaction.";
    }

    @Override
    public ExperienceLevel requiredExperience() {
        return isSpecialCareRequired() || "SPECIAL_NEEDS".equals(getHealthStatus())
                ? ExperienceLevel.EXPERIENCED
                : ("ACTIVE".equals(getTemperament()) || getAgeMonths() < 6
                        ? ExperienceLevel.INTERMEDIATE
                        : ExperienceLevel.BEGINNER);
    }

    @Override
    public PetType getType() {
        return PetType.DOG;
    }
}
