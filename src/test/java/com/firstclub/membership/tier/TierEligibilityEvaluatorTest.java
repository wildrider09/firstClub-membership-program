package com.firstclub.membership.tier;

import com.firstclub.membership.domain.MembershipTier;
import com.firstclub.membership.domain.enums.EligibilityRuleType;
import com.firstclub.membership.tier.eligibility.MinMonthlyOrderValueStrategy;
import com.firstclub.membership.tier.eligibility.MinOrderCountStrategy;
import com.firstclub.membership.tier.eligibility.RequiredCohortStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TierEligibilityEvaluatorTest {

    private TierEligibilityEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new TierEligibilityEvaluator(List.of(
                new MinOrderCountStrategy(),
                new MinMonthlyOrderValueStrategy(),
                new RequiredCohortStrategy()));
    }

    private MembershipTier tier(String name, int rank) {
        return MembershipTier.builder().name(name).rank(rank).build();
    }

    @Test
    void baseTierWithNoRulesAlwaysQualifies() {
        MembershipTier silver = tier("SILVER", 1);
        UserActivitySnapshot snapshot = UserActivitySnapshot.builder()
                .totalOrderCount(0).trailingMonthOrderValue(BigDecimal.ZERO).build();

        assertThat(evaluator.qualifiesFor(silver, snapshot)).isTrue();
    }

    @Test
    void allRulesMustPassForQualification() {
        MembershipTier platinum = tier("PLATINUM", 3);
        platinum.addEligibilityRule(rule(EligibilityRuleType.MIN_ORDER_COUNT, 10.0, null));
        platinum.addEligibilityRule(rule(EligibilityRuleType.MIN_MONTHLY_ORDER_VALUE, 5000.0, null));

        // Meets order count but not spend -> not eligible (AND semantics).
        UserActivitySnapshot notEnoughSpend = UserActivitySnapshot.builder()
                .totalOrderCount(12).trailingMonthOrderValue(new BigDecimal("4000")).build();
        assertThat(evaluator.qualifiesFor(platinum, notEnoughSpend)).isFalse();

        // Meets both -> eligible.
        UserActivitySnapshot qualifies = UserActivitySnapshot.builder()
                .totalOrderCount(12).trailingMonthOrderValue(new BigDecimal("6000")).build();
        assertThat(evaluator.qualifiesFor(platinum, qualifies)).isTrue();
    }

    @Test
    void cohortRuleMatchesCaseInsensitively() {
        MembershipTier vipTier = tier("VIP", 4);
        vipTier.addEligibilityRule(rule(EligibilityRuleType.REQUIRED_COHORT, null, "VIP"));

        UserActivitySnapshot vip = UserActivitySnapshot.builder()
                .totalOrderCount(0).trailingMonthOrderValue(BigDecimal.ZERO).cohort("vip").build();
        assertThat(evaluator.qualifiesFor(vipTier, vip)).isTrue();

        UserActivitySnapshot regular = UserActivitySnapshot.builder()
                .totalOrderCount(0).trailingMonthOrderValue(BigDecimal.ZERO).cohort(null).build();
        assertThat(evaluator.qualifiesFor(vipTier, regular)).isFalse();
    }

    @Test
    void highestEligibleTierPicksGreatestRankSatisfied() {
        MembershipTier silver = tier("SILVER", 1);
        MembershipTier gold = tier("GOLD", 2);
        gold.addEligibilityRule(rule(EligibilityRuleType.MIN_ORDER_COUNT, 5.0, null));
        MembershipTier platinum = tier("PLATINUM", 3);
        platinum.addEligibilityRule(rule(EligibilityRuleType.MIN_ORDER_COUNT, 10.0, null));

        UserActivitySnapshot snapshot = UserActivitySnapshot.builder()
                .totalOrderCount(7).trailingMonthOrderValue(BigDecimal.ZERO).build();

        Optional<MembershipTier> best =
                evaluator.highestEligibleTier(List.of(silver, gold, platinum), snapshot);
        assertThat(best).isPresent();
        assertThat(best.get().getName()).isEqualTo("GOLD");
    }

    private com.firstclub.membership.domain.TierEligibilityRule rule(
            EligibilityRuleType type, Double numeric, String str) {
        return com.firstclub.membership.domain.TierEligibilityRule.builder()
                .type(type).numericThreshold(numeric).stringValue(str).build();
    }
}
