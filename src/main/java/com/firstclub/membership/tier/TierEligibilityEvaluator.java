package com.firstclub.membership.tier;

import com.firstclub.membership.domain.MembershipTier;
import com.firstclub.membership.domain.TierEligibilityRule;
import com.firstclub.membership.domain.enums.EligibilityRuleType;
import com.firstclub.membership.tier.eligibility.TierEligibilityStrategy;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Aggregates the registered {@link TierEligibilityStrategy strategies} and uses
 * them to decide which tiers a user qualifies for.
 *
 * <p>A user qualifies for a tier only if EVERY one of that tier's rules is
 * satisfied (logical AND). Strategies are wired in by Spring, so the set of
 * supported criteria is fully pluggable.</p>
 */
@Component
public class TierEligibilityEvaluator {

    private final Map<EligibilityRuleType, TierEligibilityStrategy> strategies =
            new EnumMap<>(EligibilityRuleType.class);

    public TierEligibilityEvaluator(List<TierEligibilityStrategy> strategyBeans) {
        for (TierEligibilityStrategy strategy : strategyBeans) {
            this.strategies.put(strategy.supportedType(), strategy);
        }
    }

    /** @return true if the user satisfies all eligibility rules of the tier. */
    public boolean qualifiesFor(MembershipTier tier, UserActivitySnapshot snapshot) {
        for (TierEligibilityRule rule : tier.getEligibilityRules()) {
            TierEligibilityStrategy strategy = strategies.get(rule.getType());
            if (strategy == null) {
                throw new IllegalStateException(
                        "No eligibility strategy registered for rule type " + rule.getType());
            }
            if (!strategy.isSatisfied(rule, snapshot)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the highest-ranked tier the user currently qualifies for, if any.
     */
    public Optional<MembershipTier> highestEligibleTier(List<MembershipTier> tiers,
                                                        UserActivitySnapshot snapshot) {
        return tiers.stream()
                .filter(tier -> qualifiesFor(tier, snapshot))
                .max(Comparator.comparingInt(MembershipTier::getRank));
    }
}
