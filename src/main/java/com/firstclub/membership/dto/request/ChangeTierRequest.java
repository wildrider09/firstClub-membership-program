package com.firstclub.membership.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChangeTierRequest(
        @NotNull Long targetTierId) {
}
