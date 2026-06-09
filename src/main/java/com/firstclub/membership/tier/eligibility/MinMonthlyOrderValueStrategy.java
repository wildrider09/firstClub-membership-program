package com.firstclub.membership.tier.eligibility;

import com.firstclub.membership.domain.TierEligibilityRule;
import com.firstclub.membership.domain.enums.EligibilityRuleType;
import com.firstclub.membership.tier.UserActivitySnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Passes when the user's trailing-month order value meets the configured minimum. */
@Component
public class MinMonthlyOrderValueStrategy implements TierEligibilityStrategy {

    @Override
    public EligibilityRuleType supportedType() {
        return EligibilityRuleType.MIN_MONTHLY_ORDER_VALUE;
    }

    @Override
    public boolean isSatisfied(TierEligibilityRule rule, UserActivitySnapshot snapshot) {
        BigDecimal required = BigDecimal.valueOf(
                rule.getNumericThreshold() == null ? 0 : rule.getNumericThreshold());
        BigDecimal actual = snapshot.getTrailingMonthOrderValue() == null
                ? BigDecimal.ZERO : snapshot.getTrailingMonthOrderValue();
        return actual.compareTo(required) >= 0;
    }
}
