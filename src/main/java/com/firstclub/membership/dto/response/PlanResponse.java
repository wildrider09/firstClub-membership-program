package com.firstclub.membership.dto.response;

import com.firstclub.membership.domain.enums.BillingCycle;

import java.math.BigDecimal;

public record PlanResponse(
        Long id,
        String name,
        BillingCycle billingCycle,
        int durationMonths,
        BigDecimal price,
        boolean active) {
}
