package com.firstclub.membership.service;

import com.firstclub.membership.domain.MembershipPlan;
import com.firstclub.membership.domain.MembershipTier;
import com.firstclub.membership.domain.TierBenefit;
import com.firstclub.membership.domain.TierEligibilityRule;
import com.firstclub.membership.domain.enums.BenefitType;
import com.firstclub.membership.domain.enums.BillingCycle;
import com.firstclub.membership.domain.enums.EligibilityRuleType;
import com.firstclub.membership.exception.BusinessRuleException;
import com.firstclub.membership.exception.ResourceNotFoundException;
import com.firstclub.membership.repository.MembershipPlanRepository;
import com.firstclub.membership.repository.MembershipTierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Owns the membership catalogue: plans, tiers and their configurable benefits
 * and eligibility rules. Everything here is data-driven, so operators can shape
 * the program (prices, perks, promotion thresholds) without code changes.
 */
@Service
public class MembershipCatalogService {

    private final MembershipPlanRepository planRepository;
    private final MembershipTierRepository tierRepository;

    public MembershipCatalogService(MembershipPlanRepository planRepository,
                                    MembershipTierRepository tierRepository) {
        this.planRepository = planRepository;
        this.tierRepository = tierRepository;
    }

    // ---- Plans ----

    @Transactional(readOnly = true)
    public List<MembershipPlan> listActivePlans() {
        return planRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public MembershipPlan getPlan(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + id));
    }

    @Transactional
    public MembershipPlan createPlan(String name, BillingCycle cycle, BigDecimal price) {
        planRepository.findByBillingCycle(cycle).ifPresent(p -> {
            throw new BusinessRuleException("A plan already exists for billing cycle " + cycle);
        });
        MembershipPlan plan = MembershipPlan.builder()
                .name(name)
                .billingCycle(cycle)
                .price(price)
                .active(true)
                .build();
        return planRepository.save(plan);
    }

    // ---- Tiers ----

    @Transactional(readOnly = true)
    public List<MembershipTier> listTiers() {
        return tierRepository.findAllByOrderByRankAsc();
    }

    @Transactional(readOnly = true)
    public MembershipTier getTier(Long id) {
        return tierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tier not found: " + id));
    }

    @Transactional(readOnly = true)
    public MembershipTier getBaseTier() {
        return tierRepository.findFirstByBaseTierTrueOrderByRankAsc()
                .orElseThrow(() -> new BusinessRuleException(
                        "No base tier configured; cannot start a subscription"));
    }

    @Transactional
    public MembershipTier createTier(String name, int rank, String description, boolean baseTier) {
        MembershipTier tier = MembershipTier.builder()
                .name(name)
                .rank(rank)
                .description(description)
                .baseTier(baseTier)
                .build();
        return tierRepository.save(tier);
    }

    @Transactional
    public MembershipTier addBenefit(Long tierId, BenefitType type, BigDecimal value) {
        MembershipTier tier = getTier(tierId);
        TierBenefit benefit = TierBenefit.builder().type(type).value(value).build();
        tier.addBenefit(benefit);
        return tierRepository.save(tier);
    }

    @Transactional
    public MembershipTier addEligibilityRule(Long tierId, EligibilityRuleType type,
                                             Double numericThreshold, String stringValue) {
        MembershipTier tier = getTier(tierId);
        TierEligibilityRule rule = TierEligibilityRule.builder()
                .type(type)
                .numericThreshold(numericThreshold)
                .stringValue(stringValue)
                .build();
        tier.addEligibilityRule(rule);
        return tierRepository.save(tier);
    }
}
