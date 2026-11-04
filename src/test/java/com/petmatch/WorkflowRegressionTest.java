package com.petmatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.petmatch.matching.CapacityCriterion;
import com.petmatch.matching.CriterionGroup;
import com.petmatch.matching.CriterionResult;
import com.petmatch.matching.ExperienceCriterion;
import com.petmatch.matching.HouseholdCriterion;
import com.petmatch.matching.HousingCriterion;
import com.petmatch.model.CareProfile;
import com.petmatch.model.Cat;
import com.petmatch.model.Dog;
import com.petmatch.model.ExperienceLevel;
import com.petmatch.model.FosterFamily;
import com.petmatch.model.HousingType;
import com.petmatch.model.MatchOperator;
import com.petmatch.model.Pet;
import com.petmatch.model.PetStatus;
import com.petmatch.service.MatchingService;
import com.petmatch.service.PasswordHasher;

class WorkflowRegressionTest {
    Pet dog(boolean special, String housing) {
        return new Dog(
                1,
                "Buddy",
                "Mix",
                12,
                "GOOD",
                "CALM",
                special,
                housing,
                PetStatus.AVAILABLE,
                new CareProfile(0, 60, "Feed", "Check"));
    }

    FosterFamily family() {
        return new FosterFamily(
                2,
                "Home",
                "Staff",
                "0900000000",
                HousingType.HOUSE,
                ExperienceLevel.BEGINNER,
                2,
                0,
                true,
                false,
                true);
    }

    @Test
    void capacityUsesOccupancy() {
        var f = family();
        f.setActivePetCount(2);
        assertFalse(new CapacityCriterion().evaluate(dog(false, "ANY"), f).isSatisfied());
    }

    @Test
    void inactiveFamilyFailsCapacity() {
        var f = family();
        f.setActive(false);
        assertFalse(new CapacityCriterion().evaluate(dog(false, "ANY"), f).isSatisfied());
    }

    @Test
    void yardRequirementChecksYard() {
        var f = family();
        f.setHasYard(false);
        assertFalse(new HousingCriterion().evaluate(dog(false, "YARD"), f).isSatisfied());
    }

    @Test
    void farmMeetsHouseRequirement() {
        var f = family();
        f.setHousingType(HousingType.FARM);
        assertTrue(new HousingCriterion().evaluate(dog(false, "HOUSE"), f).isSatisfied());
    }

    @Test
    void apartmentIsExplicitRequirement() {
        assertFalse(new HousingCriterion().evaluate(dog(false, "APARTMENT"), family()).isSatisfied());
    }

    @Test
    void specialCareNeedsExperience() {
        assertFalse(new ExperienceCriterion().evaluate(dog(true, "ANY"), family()).isSatisfied());
    }

    @Test
    void experienceAllowsSpecialCare() {
        var f = family();
        f.setExperienceLevel(ExperienceLevel.EXPERIENCED);
        assertTrue(new ExperienceCriterion().evaluate(dog(true, "ANY"), f).isSatisfied());
    }

    @Test
    void speciesHaveDifferentCareBehavior() {
        var d = dog(false, "ANY");
        d.setTemperament("SHY");
        var c = new Cat(1, "Mochi", "Mix", 12, "GOOD", "SHY", false, "ANY", PetStatus.AVAILABLE, null);
        assertEquals(ExperienceLevel.BEGINNER, d.requiredExperience());
        assertEquals(ExperienceLevel.INTERMEDIATE, c.requiredExperience());
        assertTrue(new ExperienceCriterion().evaluate(d, family()).isSatisfied());
        assertFalse(new ExperienceCriterion().evaluate(c, family()).isSatisfied());
    }

    @Test
    void childrenAndShyPetFailHousehold() {
        var f = family();
        f.setHasChildren(true);
        var p = dog(false, "ANY");
        p.setTemperament("SHY");
        assertFalse(new HouseholdCriterion().evaluate(p, f).isSatisfied());
    }

    @Test
    void andOrChangesCompatibility() {
        var p = dog(true, "ANY");
        var f = family();
        var s = new MatchingService();
        assertFalse(s.evaluateMatch(p, f, MatchOperator.AND).isSatisfied());
        assertTrue(s.evaluateMatch(p, f, MatchOperator.OR).isSatisfied());
    }

    @ParameterizedTest
    @EnumSource(MatchOperator.class)
    void fullFamilyCannotBypassWithOr(MatchOperator op) {
        var f = family();
        f.setActivePetCount(2);
        assertFalse(new MatchingService().evaluateMatch(dog(false, "ANY"), f, op).isSatisfied());
    }

    @ParameterizedTest
    @EnumSource(MatchOperator.class)
    void committedFamilyCannotBypassWithOr(MatchOperator op) {
        var f = family();
        f.setActivePetCount(1);
        assertFalse(new MatchingService().evaluateMatch(dog(false, "ANY"), f, op).isSatisfied());
    }

    @ParameterizedTest
    @EnumSource(MatchOperator.class)
    void unavailablePetCannotBypassWithOr(MatchOperator op) {
        var p = dog(false, "ANY");
        p.setStatus(PetStatus.PLACED);
        assertFalse(new MatchingService().evaluateMatch(p, family(), op).isSatisfied());
    }

    @ParameterizedTest
    @EnumSource(MatchOperator.class)
    void noRulesFailsClosed(MatchOperator op) {
        assertFalse(new CriterionGroup(op).evaluate(dog(false, "ANY"), family()).isSatisfied());
    }

    @Test
    void nestedCriteriaCompose() {
        var alternative = new CriterionGroup(MatchOperator.OR);
        alternative.addCriterion((p, f) -> CriterionResult.fail("first"));
        alternative.addCriterion((p, f) -> CriterionResult.pass("second"));
        var root = new CriterionGroup(MatchOperator.AND);
        root.addCriterion(alternative);
        root.addCriterion(new CapacityCriterion());
        assertTrue(root.evaluate(dog(false, "ANY"), family()).isSatisfied());
        root.addCriterion((p, f) -> CriterionResult.fail("mandatory"));
        assertFalse(root.evaluate(dog(false, "ANY"), family()).isSatisfied());
    }

    @Test
    void detailsExposeBothPassAndFail() {
        var r = new MatchingService().evaluateMatch(dog(true, "ANY"), family(), MatchOperator.AND);
        assertTrue(r.getReason().contains("[PASS]"));
        assertTrue(r.getReason().contains("[FAIL]"));
    }

    @Test
    void missingInputsRejected() {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new MatchingService().evaluateMatch(null, family(), MatchOperator.AND));
        assertEquals(IllegalArgumentException.class, exception.getClass());
    }

    @Test
    void passwordsAreSaltedAndVerified() {
        var a = PasswordHasher.hash("SafePassword123!".toCharArray());
        var b = PasswordHasher.hash("SafePassword123!".toCharArray());
        assertNotEquals(a, b);
        assertFalse(a.contains("SafePassword"));
        assertTrue(PasswordHasher.verify("SafePassword123!".toCharArray(), a));
        assertFalse(PasswordHasher.verify("WrongPassword".toCharArray(), a));
    }

    @Test
    void malformedHashRejected() {
        assertFalse(PasswordHasher.verify("x".toCharArray(), "broken"));
        assertFalse(PasswordHasher.verify("x".toCharArray(), "999999999:a:b"));
    }
}
