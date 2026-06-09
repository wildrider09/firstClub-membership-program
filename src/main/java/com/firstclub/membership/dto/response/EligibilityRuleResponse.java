package com.firstclub.membership.dto.response;

import com.firstclub.membership.domain.enums.EligibilityRuleType;

public record EligibilityRuleResponse(
        EligibilityRuleType type,
        Double numericThreshold,
        String stringValue) {
}
