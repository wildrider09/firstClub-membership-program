package com.firstclub.membership.domain.enums;

/**
 * Types of criteria that gate access to a membership tier. Each type maps to a
 * {@code TierEligibilityStrategy} implementation, so adding a new criterion is
 * a matter of adding an enum constant and a strategy bean (open/closed).
 */
public enum EligibilityRuleType {
    /** User must have placed at least N orders (all-time). */
    MIN_ORDER_COUNT,
    /** User's total order value within the trailing calendar month must be >= X. */
    MIN_MONTHLY_ORDER_VALUE,
    /** User must belong to a specific cohort (e.g. EMPLOYEE, VIP). */
    REQUIRED_COHORT
}
