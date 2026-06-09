package com.firstclub.membership.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(Long id, Long userId, BigDecimal amount, Instant placedAt) {
}
