package com.petmatch.matching;

import com.petmatch.model.FosterFamily;
import com.petmatch.model.Pet;

public class CapacityCriterion implements MatchCriterion {

    @Override
    public CriterionResult evaluate(Pet pet, FosterFamily family) {
        if (family == null) {
            return CriterionResult.fail("Chưa chọn gia đình để kiểm tra sức chứa.");
        }

        if (family.getCapacity() > 0) {
            return CriterionResult.pass("Gia đình vẫn còn chỗ chứa (Chỗ trống: " + family.getCapacity() + ").");
        } else {
            return CriterionResult.fail("Gia đình đã hết sức chứa nhận nuôi (Chỗ trống: 0).");
        }
    }
}

