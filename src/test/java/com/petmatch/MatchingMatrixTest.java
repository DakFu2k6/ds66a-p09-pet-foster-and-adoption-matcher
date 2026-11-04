package com.petmatch;

import static org.junit.jupiter.api.Assertions.*;

import com.petmatch.model.*;
import com.petmatch.service.MatchingService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MatchingMatrixTest {
    @ParameterizedTest
    @CsvSource({
            "ANY,BEGINNER,false,true",
            "HOUSE,BEGINNER,false,true",
            "APARTMENT,BEGINNER,false,false",
            "YARD,BEGINNER,false,false",
            "ANY,BEGINNER,true,false",
            "ANY,EXPERIENCED,true,true",
            "APARTMENT,EXPERIENCED,true,false",
            "HOUSE,INTERMEDIATE,false,true"
    })
    void andMatrix(String housing, ExperienceLevel experience, boolean special, boolean expected) {
        var p = new Dog(1, "Buddy", "Mix", 12, "GOOD", "CALM", special, housing, PetStatus.AVAILABLE, null);
        var f = new FosterFamily(
                2,
                "Family",
                "Staff",
                "0900000000",
                HousingType.HOUSE,
                experience,
                2,
                0,
                false,
                false,
                true);
        assertEquals(
                expected, new MatchingService().evaluateMatch(p, f, MatchOperator.AND).isSatisfied());
    }
}
