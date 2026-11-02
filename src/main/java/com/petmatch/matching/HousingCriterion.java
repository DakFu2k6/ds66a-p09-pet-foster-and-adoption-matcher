package com.petmatch.matching;

import com.petmatch.model.FosterFamily;
import com.petmatch.model.Pet;

public class HousingCriterion implements MatchCriterion {

    @Override
    public CriterionResult evaluate(Pet pet, FosterFamily family) {
        if (family == null) {
            return CriterionResult.fail("Thiếu thông tin điều kiện nhà ở của gia đình.");
        }

        if (family.getHousingType() != null) {
            return CriterionResult.pass("Điều kiện nhà ở phù hợp: " + family.getHousingType());
        }

        return CriterionResult.fail("Gia đình chưa đăng ký loại hình nhà ở.");
    }
}