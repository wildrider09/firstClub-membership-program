package com.firstclub.membership.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PlaceOrderRequest(
        @NotNull Long userId,
        @NotNull @Positive BigDecimal amount) {
}
