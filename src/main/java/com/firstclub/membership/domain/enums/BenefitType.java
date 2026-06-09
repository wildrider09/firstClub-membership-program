package com.firstclub.membership.domain.enums;

/**
 * The catalogue of benefit kinds a tier can grant. New benefit types can be
 * added here and configured per-tier without touching business logic, because
 * benefits are stored as data ({@code TierBenefit}) rather than hard-coded.
 *
 * <p>{@link #valued} indicates whether the benefit carries a numeric value
 * (e.g. a discount percentage) as opposed to being a simple on/off flag.</p>
 */
public enum BenefitType {
    FREE_DELIVERY(false),
    EXTRA_DISCOUNT_PERCENT(true),
    EXCLUSIVE_DEALS(false),
    EARLY_ACCESS_TO_SALES(false),
    PRIORITY_SUPPORT(false);

    private final boolean valued;

    BenefitType(boolean valued) {
        this.valued = valued;
    }

    public boolean isValued() {
        return valued;
    }
}
