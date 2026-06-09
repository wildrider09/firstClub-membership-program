package com.firstclub.membership.service;

import com.firstclub.membership.domain.MembershipPlan;
import com.firstclub.membership.domain.MembershipTier;
import com.firstclub.membership.domain.Subscription;
import com.firstclub.membership.domain.User;
import com.firstclub.membership.domain.enums.SubscriptionStatus;
import com.firstclub.membership.exception.BusinessRuleException;
import com.firstclub.membership.repository.MembershipPlanRepository;
import com.firstclub.membership.repository.MembershipTierRepository;
import com.firstclub.membership.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class SubscriptionServiceIntegrationTest {

    @Autowired private SubscriptionService subscriptionService;
    @Autowired private OrderService orderService;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private MembershipPlanRepository planRepository;
    @Autowired private MembershipTierRepository tierRepository;

    private MembershipPlan monthlyPlan() {
        return planRepository.findAll().stream()
                .filter(p -> p.getName().contains("Monthly")).findFirst().orElseThrow();
    }

    private MembershipTier tier(String name) {
        return tierRepository.findByName(name).orElseThrow();
    }

    private User freshUser(String email) {
        return userService.createUser("Test " + email, email, null);
    }

    @Test
    void subscribeDefaultsToBaseTierForNewUser() {
        User user = freshUser("new-" + System.nanoTime() + "@example.com");

        Subscription sub = subscriptionService.subscribe(
                user.getId(), monthlyPlan().getId(), null, null);

        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(sub.getTier().getName()).isEqualTo("SILVER");
        assertThat(sub.getEndDate()).isAfter(sub.getStartDate());
    }

    @Test
    void cannotSubscribeTwiceWhileActive() {
        User user = freshUser("dup-" + System.nanoTime() + "@example.com");
        subscriptionService.subscribe(user.getId(), monthlyPlan().getId(), null, null);

        assertThatThrownBy(() ->
                subscriptionService.subscribe(user.getId(), monthlyPlan().getId(), null, null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void subscribeIsIdempotentWithKey() {
        User user = freshUser("idem-" + System.nanoTime() + "@example.com");
        String key = "key-" + System.nanoTime();

        Subscription first = subscriptionService.subscribe(
                user.getId(), monthlyPlan().getId(), null, key);
        Subscription second = subscriptionService.subscribe(
                user.getId(), monthlyPlan().getId(), null, key);

        assertThat(second.getId()).isEqualTo(first.getId());
    }

    @Test
    void cannotSubscribeToTierUserIsNotEligibleFor() {
        User user = freshUser("inelig-" + System.nanoTime() + "@example.com");

        assertThatThrownBy(() -> subscriptionService.subscribe(
                user.getId(), monthlyPlan().getId(), tier("GOLD").getId(), null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void upgradeAfterPlacingEnoughOrders() {
        User user = freshUser("upg-" + System.nanoTime() + "@example.com");
        Subscription sub = subscriptionService.subscribe(
                user.getId(), monthlyPlan().getId(), null, null);

        // GOLD requires 5+ orders; place 6.
        for (int i = 0; i < 6; i++) {
            orderService.placeOrder(user.getId(), new BigDecimal("100"));
        }

        Subscription upgraded = subscriptionService.upgradeTier(sub.getId(), tier("GOLD").getId());
        assertThat(upgraded.getTier().getName()).isEqualTo("GOLD");
    }

    @Test
    void downgradeAndCancelLifecycle() {
        User user = freshUser("life-" + System.nanoTime() + "@example.com");
        Subscription sub = subscriptionService.subscribe(
                user.getId(), monthlyPlan().getId(), null, null);
        for (int i = 0; i < 6; i++) {
            orderService.placeOrder(user.getId(), new BigDecimal("100"));
        }
        subscriptionService.upgradeTier(sub.getId(), tier("GOLD").getId());

        Subscription downgraded = subscriptionService.downgradeTier(sub.getId(), tier("SILVER").getId());
        assertThat(downgraded.getTier().getName()).isEqualTo("SILVER");

        Subscription cancelled = subscriptionService.cancel(sub.getId());
        assertThat(cancelled.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(subscriptionService.getCurrentSubscription(user.getId())).isEmpty();
    }
}
