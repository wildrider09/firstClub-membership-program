package com.firstclub.membership.tier.eligibility;

import com.firstclub.membership.domain.TierEligibilityRule;
import com.firstclub.membership.domain.enums.EligibilityRuleType;
import com.firstclub.membership.tier.UserActivitySnapshot;

/**
 * Strategy that evaluates whether a user satisfies one kind of eligibility rule.
 *
 * <p>This is the core extension point of the tier engine. To support a new
 * criterion you add an {@link EligibilityRuleType} constant and a Spring bean
 * implementing this interface — no existing code needs to change
 * (Open/Closed Principle).</p>
 */
public interface TierEligibilityStrategy {

    /** The rule type this strategy knows how to evaluate. */
    EligibilityRuleType supportedType();

    /**
     * @return true if the user (described by {@code snapshot}) satisfies the
     *         given {@code rule}.
     */
    boolean isSatisfied(TierEligibilityRule rule, UserActivitySnapshot snapshot);
}
