package com.firstclub.membership.service;

import com.firstclub.membership.domain.MembershipPlan;
import com.firstclub.membership.domain.Subscription;
import com.firstclub.membership.domain.User;
import com.firstclub.membership.domain.enums.SubscriptionStatus;
import com.firstclub.membership.exception.BusinessRuleException;
import com.firstclub.membership.repository.MembershipPlanRepository;
import com.firstclub.membership.repository.MembershipTierRepository;
import com.firstclub.membership.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the concurrency design: a {@link Subscription} is guarded by an
 * optimistic-lock {@code @Version}, and {@code SubscriptionService} mutators are
 * {@code @Retryable} on {@link OptimisticLockingFailureException}.
 *
 * <p>Two threads hammer the SAME subscription with interleaved upgrade/downgrade
 * operations. We assert that (a) no optimistic-lock failure escapes to the
 * caller (retries absorb every version clash), (b) the version advances once per
 * successful mutation (no lost updates), and (c) the subscription ends in a
 * consistent, valid state.</p>
 */
@SpringBootTest
class SubscriptionConcurrencyTest {

    @Autowired private SubscriptionService subscriptionService;
    @Autowired private OrderService orderService;
    @Autowired private UserService userService;
    @Autowired private MembershipPlanRepository planRepository;
    @Autowired private MembershipTierRepository tierRepository;
    @Autowired private SubscriptionRepository subscriptionRepository;

    @Test
    void concurrentTierChangesStayConsistentWithoutLostUpdates() throws Exception {
        // --- arrange: a user eligible for GOLD (>= 5 orders), starting on SILVER ---
        User user = userService.createUser("Concurrency Tester",
                "concurrency-" + System.nanoTime() + "@example.com", null);
        for (int i = 0; i < 6; i++) {
            orderService.placeOrder(user.getId(), new BigDecimal("1000"));
        }
        MembershipPlan monthly = planRepository.findAll().stream()
                .filter(p -> p.getName().contains("Monthly")).findFirst().orElseThrow();
        Long silverId = tierRepository.findByName("SILVER").orElseThrow().getId();
        Long goldId = tierRepository.findByName("GOLD").orElseThrow().getId();

        Subscription sub = subscriptionService.subscribe(
                user.getId(), monthly.getId(), silverId, null);
        Long subId = sub.getId();

        // --- act: two competitors, many interleaved mutations on the same row ---
        int threads = 2;
        int iterationsPerThread = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch startGun = new CountDownLatch(1);
        AtomicInteger optimisticFailuresEscaped = new AtomicInteger();
        AtomicInteger successfulMutations = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < threads; t++) {
            final boolean upgrader = (t % 2 == 0);
            futures.add(pool.submit(() -> {
                await(startGun);
                for (int i = 0; i < iterationsPerThread; i++) {
                    try {
                        if (upgrader) {
                            subscriptionService.upgradeTier(subId, goldId);
                        } else {
                            subscriptionService.downgradeTier(subId, silverId);
                        }
                        successfulMutations.incrementAndGet();
                    } catch (OptimisticLockingFailureException e) {
                        // Should never happen: @Retryable must absorb version clashes.
                        optimisticFailuresEscaped.incrementAndGet();
                    } catch (BusinessRuleException ignored) {
                        // Expected under interleaving: the subscription is already in
                        // the target tier (e.g. two upgrades in a row). Not a failure.
                    }
                }
            }));
        }
        startGun.countDown();
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdown();

        // --- assert: consistent final state, and the lock/retry held ---
        Subscription finalSub = subscriptionRepository.findById(subId).orElseThrow();
        assertThat(optimisticFailuresEscaped.get())
                .as("no OptimisticLockingFailure should escape — @Retryable must recover")
                .isZero();
        assertThat(finalSub.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(finalSub.getTier().getName()).isIn("SILVER", "GOLD");
        // version advances at least once per successful mutation -> no lost updates.
        assertThat(finalSub.getVersion())
                .as("@Version must advance with each persisted change")
                .isGreaterThanOrEqualTo((long) successfulMutations.get());
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
