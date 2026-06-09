package com.firstclub.membership.dto.response;

import com.firstclub.membership.domain.enums.SubscriptionStatus;

import java.time.Instant;

public record SubscriptionResponse(
        Long id,
        Long userId,
        PlanResponse plan,
        TierResponse tier,
        SubscriptionStatus status,
        boolean currentlyActive,
        Instant startDate,
        Instant endDate,
        Instant cancelledAt) {
}
