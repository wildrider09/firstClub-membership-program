package com.firstclub.membership.dto.response;

import java.util.List;

public record TierResponse(
        Long id,
        String name,
        int rank,
        String description,
        boolean baseTier,
        List<BenefitResponse> benefits,
        List<EligibilityRuleResponse> eligibilityRules) {
}
