package com.firstclub.membership.dto.request;

import com.firstclub.membership.domain.enums.BenefitType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AddBenefitRequest(
        @NotNull BenefitType type,
        BigDecimal value) {
}
