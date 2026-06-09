package com.firstclub.membership.dto.response;

public record TierEligibilityResponse(
        Long userId,
        TierResponse highestEligibleTier) {
}
