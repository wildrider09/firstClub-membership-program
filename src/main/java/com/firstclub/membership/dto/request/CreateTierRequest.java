package com.firstclub.membership.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateTierRequest(
        @NotBlank String name,
        @Positive int rank,
        String description,
        boolean baseTier) {
}
