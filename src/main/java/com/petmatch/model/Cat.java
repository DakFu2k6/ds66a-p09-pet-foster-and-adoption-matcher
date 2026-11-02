package main.java.com.petmatch.model;

public class Cat extends Pet {
    public Cat(long id, String name, String breed, int ageMonths, String healthStatus,
            String temperament, boolean specialCare, String housingRequirement,
            PetStatus status, CareProfile careProfile) {
        super(id, name, breed, ageMonths, healthStatus, temperament, specialCare, housingRequirement, status,
                careProfile);
    }

    @Override
    public String getCareDescription() {
        return "Cats need a safe indoor environment, enrichment and a clean litter area.";
    }

    @Override
    public PetType getType() {
        return PetType.CAT;
    }
}
