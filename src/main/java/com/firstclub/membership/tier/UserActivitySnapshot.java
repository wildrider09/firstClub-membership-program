package com.firstclub.membership.tier;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/**
 * An immutable snapshot of the signals used to evaluate tier eligibility for a
 * single user. Computed once per evaluation so individual strategies don't each
 * issue their own queries.
 */
@Value
@Builder
public class UserActivitySnapshot {
    long totalOrderCount;
    BigDecimal trailingMonthOrderValue;
    String cohort;
}
