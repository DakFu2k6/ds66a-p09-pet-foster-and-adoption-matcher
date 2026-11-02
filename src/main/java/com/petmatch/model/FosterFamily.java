package com.petmatch.model;

public class FosterFamily {
    private long id;
    private String familyName, contactName, phone;
    private HousingType housingType;
    private ExperienceLevel experienceLevel;
    private int capacity, activePetCount;
    private boolean hasYard, hasChildren, active;

    public FosterFamily(long id, String familyName, String contactName, String phone,
            HousingType housingType, ExperienceLevel experienceLevel, int capacity,
            int activePetCount, boolean hasYard, boolean hasChildren, boolean active) {
        this.id = id;
        this.familyName = familyName;
        this.contactName = contactName;
        this.phone = phone;
        this.housingType = housingType;
        this.experienceLevel = experienceLevel;
        this.capacity = capacity;
        this.activePetCount = activePetCount;
        this.hasYard = hasYard;
        this.hasChildren = hasChildren;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getContactName() {
        return contactName;
    }

    public String getPhone() {
        return phone;
    }

    public HousingType getHousingType() {
        return housingType;
    }

    public ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getActivePetCount() {
        return activePetCount;
    }

    public boolean isHasYard() {
        return hasYard;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public boolean isActive() {
        return active;
    }

    public boolean hasCapacity() {
        return active && activePetCount < capacity;
    }
}
