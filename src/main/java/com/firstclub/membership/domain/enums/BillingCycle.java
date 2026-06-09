package com.firstclub.membership.domain.enums;

/**
 * Billing cadence for a membership plan. The number of months each cycle spans
 * is used to compute a subscription's expiry date.
 */
public enum BillingCycle {
    MONTHLY(1),
    QUARTERLY(3),
    YEARLY(12);

    private final int months;

    BillingCycle(int months) {
        this.months = months;
    }

    public int getMonths() {
        return months;
    }
}
