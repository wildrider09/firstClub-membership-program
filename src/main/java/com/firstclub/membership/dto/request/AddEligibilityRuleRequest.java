package com.firstclub.membership.dto.request;

import com.firstclub.membership.domain.enums.EligibilityRuleType;
import jakarta.validation.constraints.NotNull;

public record AddEligibilityRuleRequest(
        @NotNull EligibilityRuleType type,
        Double numericThreshold,
        String stringValue) {
}
