package com.firstclub.membership.dto;

import com.firstclub.membership.domain.MembershipPlan;
import com.firstclub.membership.domain.MembershipTier;
import com.firstclub.membership.domain.Order;
import com.firstclub.membership.domain.Subscription;
import com.firstclub.membership.domain.User;
import com.firstclub.membership.dto.response.BenefitResponse;
import com.firstclub.membership.dto.response.EligibilityRuleResponse;
import com.firstclub.membership.dto.response.OrderResponse;
import com.firstclub.membership.dto.response.PlanResponse;
import com.firstclub.membership.dto.response.SubscriptionResponse;
import com.firstclub.membership.dto.response.TierResponse;
import com.firstclub.membership.dto.response.UserResponse;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Pure mapping helpers from domain entities to API response DTOs. Kept free of
 * persistence concerns so it can be reused by any controller.
 */
public final class DtoMapper {

    private DtoMapper() {
    }

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCohort());
    }

    public static PlanResponse toPlanResponse(MembershipPlan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getBillingCycle(),
                plan.getBillingCycle().getMonths(),
                plan.getPrice(),
                plan.isActive());
    }

    public static BenefitResponse toBenefitResponse(com.firstclub.membership.domain.TierBenefit benefit) {
        return new BenefitResponse(benefit.getType(), benefit.getValue());
    }

    public static EligibilityRuleResponse toRuleResponse(
            com.firstclub.membership.domain.TierEligibilityRule rule) {
        return new EligibilityRuleResponse(rule.getType(), rule.getNumericThreshold(), rule.getStringValue());
    }

    public static TierResponse toTierResponse(MembershipTier tier) {
        List<BenefitResponse> benefits = tier.getBenefits().stream()
                .sorted(Comparator.comparing(b -> b.getType().name()))
                .map(DtoMapper::toBenefitResponse)
                .toList();
        List<EligibilityRuleResponse> rules = tier.getEligibilityRules().stream()
                .sorted(Comparator.comparing(r -> r.getType().name()))
                .map(DtoMapper::toRuleResponse)
                .toList();
        return new TierResponse(
                tier.getId(),
                tier.getName(),
                tier.getRank(),
                tier.getDescription(),
                tier.isBaseTier(),
                benefits,
                rules);
    }

    public static SubscriptionResponse toSubscriptionResponse(Subscription sub, Clock clock) {
        return new SubscriptionResponse(
                sub.getId(),
                sub.getUser().getId(),
                toPlanResponse(sub.getPlan()),
                toTierResponse(sub.getTier()),
                sub.getStatus(),
                sub.isCurrentlyActive(Instant.now(clock)),
                sub.getStartDate(),
                sub.getEndDate(),
                sub.getCancelledAt());
    }

    public static OrderResponse toOrderResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getAmount(),
                order.getPlacedAt());
    }
}
