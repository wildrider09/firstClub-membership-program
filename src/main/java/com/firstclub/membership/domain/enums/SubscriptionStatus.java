package com.firstclub.membership.domain.enums;

/**
 * Lifecycle states of a membership subscription.
 */
public enum SubscriptionStatus {
    /** Subscription is active and within its validity window. */
    ACTIVE,
    /** Explicitly cancelled by the user. */
    CANCELLED,
    /** Validity window has elapsed. */
    EXPIRED
}
