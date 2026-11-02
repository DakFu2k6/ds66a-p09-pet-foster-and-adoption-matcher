package com.petmatch.matching;

import com.petmatch.model.FosterFamily;
import com.petmatch.model.Pet;

public class ExperienceCriterion implements MatchCriterion {

    @Override
    public CriterionResult evaluate(Pet pet, FosterFamily family) {
        if (pet == null || family == null) {
            return CriterionResult.fail("Thiếu thông tin thú cưng hoặc gia đình.");
        }

        if (family.getExperienceLevel() != null) {
            return CriterionResult.pass("Gia đình có mức độ kinh nghiệm: " + family.getExperienceLevel());
        }

        return CriterionResult.fail("Gia đình chưa cập nhật thông tin kinh nghiệm chăm sóc.");
    }
}