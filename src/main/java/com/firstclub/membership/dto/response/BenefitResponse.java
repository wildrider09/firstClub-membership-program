package com.firstclub.membership.dto.response;

import com.firstclub.membership.domain.enums.BenefitType;

import java.math.BigDecimal;

public record BenefitResponse(BenefitType type, BigDecimal value) {
}
