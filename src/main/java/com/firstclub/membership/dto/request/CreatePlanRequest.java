package com.firstclub.membership.dto.request;

import com.firstclub.membership.domain.enums.BillingCycle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreatePlanRequest(
        @NotBlank String name,
        @NotNull BillingCycle billingCycle,
        @NotNull @Positive BigDecimal price) {
}
