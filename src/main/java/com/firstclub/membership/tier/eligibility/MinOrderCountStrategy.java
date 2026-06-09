package com.firstclub.membership.tier.eligibility;

import com.firstclub.membership.domain.TierEligibilityRule;
import com.firstclub.membership.domain.enums.EligibilityRuleType;
import com.firstclub.membership.tier.UserActivitySnapshot;
import org.springframework.stereotype.Component;

/** Passes when the user's all-time order count meets the configured minimum. */
@Component
public class MinOrderCountStrategy implements TierEligibilityStrategy {

    @Override
    public EligibilityRuleType supportedType() {
        return EligibilityRuleType.MIN_ORDER_COUNT;
    }

    @Override
    public boolean isSatisfied(TierEligibilityRule rule, UserActivitySnapshot snapshot) {
        double required = rule.getNumericThreshold() == null ? 0 : rule.getNumericThreshold();
        return snapshot.getTotalOrderCount() >= required;
    }
}
