package com.firstclub.membership.dto.request;

import jakarta.validation.constraints.NotNull;

public record SubscribeRequest(
        @NotNull Long userId,
        @NotNull Long planId,
        /** Optional: if null, the highest eligible tier (or base tier) is assigned. */
        Long tierId,
        /** Optional client token making the subscribe call idempotent. */
        String idempotencyKey) {
}
