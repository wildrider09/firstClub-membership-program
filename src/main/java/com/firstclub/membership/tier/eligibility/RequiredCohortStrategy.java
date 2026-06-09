package com.firstclub.membership.tier.eligibility;

import com.firstclub.membership.domain.TierEligibilityRule;
import com.firstclub.membership.domain.enums.EligibilityRuleType;
import com.firstclub.membership.tier.UserActivitySnapshot;
import org.springframework.stereotype.Component;

/** Passes when the user belongs to the cohort required by the rule. */
@Component
public class RequiredCohortStrategy implements TierEligibilityStrategy {

    @Override
    public EligibilityRuleType supportedType() {
        return EligibilityRuleType.REQUIRED_COHORT;
    }

    @Override
    public boolean isSatisfied(TierEligibilityRule rule, UserActivitySnapshot snapshot) {
        String required = rule.getStringValue();
        if (required == null || required.isBlank()) {
            return true;
        }
        return required.equalsIgnoreCase(snapshot.getCohort());
    }
}
