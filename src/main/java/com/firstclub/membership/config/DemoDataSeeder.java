package com.firstclub.membership.config;

import com.firstclub.membership.domain.MembershipPlan;
import com.firstclub.membership.domain.MembershipTier;
import com.firstclub.membership.domain.TierBenefit;
import com.firstclub.membership.domain.TierEligibilityRule;
import com.firstclub.membership.domain.User;
import com.firstclub.membership.domain.enums.BenefitType;
import com.firstclub.membership.domain.enums.BillingCycle;
import com.firstclub.membership.domain.enums.EligibilityRuleType;
import com.firstclub.membership.repository.MembershipPlanRepository;
import com.firstclub.membership.repository.MembershipTierRepository;
import com.firstclub.membership.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds a configurable, demo-ready membership program on startup:
 * <ul>
 *   <li>Plans: Monthly / Quarterly / Yearly</li>
 *   <li>Tiers: Silver (base) / Gold / Platinum with graded benefits</li>
 *   <li>Eligibility rules driving tier promotion</li>
 *   <li>Two demo users (one regular, one VIP cohort)</li>
 * </ul>
 * All values live in data, so they can be changed at runtime through the APIs.
 */
@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final MembershipPlanRepository planRepository;
    private final MembershipTierRepository tierRepository;
    private final UserRepository userRepository;

    public DemoDataSeeder(MembershipPlanRepository planRepository,
                          MembershipTierRepository tierRepository,
                          UserRepository userRepository) {
        this.planRepository = planRepository;
        this.tierRepository = tierRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (planRepository.count() > 0 || tierRepository.count() > 0) {
            return; // already seeded
        }
        seedPlans();
        seedTiers();
        seedUsers();
        log.info("Demo data seeded: {} plans, {} tiers, {} users",
                planRepository.count(), tierRepository.count(), userRepository.count());
    }

    private void seedPlans() {
        planRepository.save(plan("FirstClub Monthly", BillingCycle.MONTHLY, "199.00"));
        planRepository.save(plan("FirstClub Quarterly", BillingCycle.QUARTERLY, "499.00"));
        planRepository.save(plan("FirstClub Yearly", BillingCycle.YEARLY, "1499.00"));
    }

    private MembershipPlan plan(String name, BillingCycle cycle, String price) {
        return MembershipPlan.builder()
                .name(name)
                .billingCycle(cycle)
                .price(new BigDecimal(price))
                .active(true)
                .build();
    }

    private void seedTiers() {
        // SILVER — base tier, everyone qualifies (no eligibility rules).
        MembershipTier silver = MembershipTier.builder()
                .name("SILVER").rank(1).baseTier(true)
                .description("Entry tier for all members")
                .build();
        silver.addBenefit(benefit(BenefitType.FREE_DELIVERY, null));
        silver.addBenefit(benefit(BenefitType.EXTRA_DISCOUNT_PERCENT, "5"));
        tierRepository.save(silver);

        // GOLD — unlocked after 5+ orders.
        MembershipTier gold = MembershipTier.builder()
                .name("GOLD").rank(2).baseTier(false)
                .description("For regular shoppers")
                .build();
        gold.addBenefit(benefit(BenefitType.FREE_DELIVERY, null));
        gold.addBenefit(benefit(BenefitType.EXTRA_DISCOUNT_PERCENT, "10"));
        gold.addBenefit(benefit(BenefitType.EXCLUSIVE_DEALS, null));
        gold.addEligibilityRule(rule(EligibilityRuleType.MIN_ORDER_COUNT, 5.0, null));
        tierRepository.save(gold);

        // PLATINUM — 10+ orders AND >= 5000 monthly spend (AND of two rules).
        MembershipTier platinum = MembershipTier.builder()
                .name("PLATINUM").rank(3).baseTier(false)
                .description("Premium tier for top spenders")
                .build();
        platinum.addBenefit(benefit(BenefitType.FREE_DELIVERY, null));
        platinum.addBenefit(benefit(BenefitType.EXTRA_DISCOUNT_PERCENT, "20"));
        platinum.addBenefit(benefit(BenefitType.EXCLUSIVE_DEALS, null));
        platinum.addBenefit(benefit(BenefitType.EARLY_ACCESS_TO_SALES, null));
        platinum.addBenefit(benefit(BenefitType.PRIORITY_SUPPORT, null));
        platinum.addEligibilityRule(rule(EligibilityRuleType.MIN_ORDER_COUNT, 10.0, null));
        platinum.addEligibilityRule(rule(EligibilityRuleType.MIN_MONTHLY_ORDER_VALUE, 5000.0, null));
        tierRepository.save(platinum);
    }

    private TierBenefit benefit(BenefitType type, String value) {
        return TierBenefit.builder()
                .type(type)
                .value(value == null ? null : new BigDecimal(value))
                .build();
    }

    private TierEligibilityRule rule(EligibilityRuleType type, Double numeric, String str) {
        return TierEligibilityRule.builder()
                .type(type)
                .numericThreshold(numeric)
                .stringValue(str)
                .build();
    }

    private void seedUsers() {
        userRepository.save(User.builder()
                .name("Alice Regular").email("alice@example.com").cohort(null).build());
        userRepository.save(User.builder()
                .name("Bob VIP").email("bob@example.com").cohort("VIP").build());
    }
}
