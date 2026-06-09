package com.firstclub.membership.service;

import com.firstclub.membership.domain.MembershipPlan;
import com.firstclub.membership.domain.MembershipTier;
import com.firstclub.membership.domain.Subscription;
import com.firstclub.membership.domain.User;
import com.firstclub.membership.domain.enums.SubscriptionStatus;
import com.firstclub.membership.exception.BusinessRuleException;
import com.firstclub.membership.exception.ResourceNotFoundException;
import com.firstclub.membership.repository.SubscriptionRepository;
import com.firstclub.membership.tier.TierEligibilityEvaluator;
import com.firstclub.membership.tier.UserActivitySnapshot;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates the membership subscription lifecycle: subscribe, upgrade,
 * downgrade, cancel and tracking, plus tier-eligibility evaluation.
 *
 * <h2>Concurrency</h2>
 * <ul>
 *   <li><b>Optimistic locking</b>: {@link Subscription} carries a {@code @Version};
 *       concurrent mutations that would clobber each other fail and are retried
 *       via {@link Retryable} (see {@link #upgradeTier} / {@link #downgradeTier} /
 *       {@link #cancel}).</li>
 *   <li><b>Idempotency</b>: {@link #subscribe} keys on a client-supplied token so
 *       a retried request returns the original subscription instead of creating
 *       a duplicate.</li>
 * </ul>
 */
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserService userService;
    private final MembershipCatalogService catalogService;
    private final OrderService orderService;
    private final TierEligibilityEvaluator eligibilityEvaluator;
    private final Clock clock;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               UserService userService,
                               MembershipCatalogService catalogService,
                               OrderService orderService,
                               TierEligibilityEvaluator eligibilityEvaluator,
                               Clock clock) {
        this.subscriptionRepository = subscriptionRepository;
        this.userService = userService;
        this.catalogService = catalogService;
        this.orderService = orderService;
        this.eligibilityEvaluator = eligibilityEvaluator;
        this.clock = clock;
    }

    /**
     * Subscribe a user to a plan and (optionally) a requested tier.
     *
     * @param requestedTierId if null, the user is placed on the highest tier
     *                        they currently qualify for (falling back to the
     *                        base tier); if provided, eligibility is enforced.
     * @param idempotencyKey  optional client token; a repeat call with the same
     *                        key returns the original subscription.
     */
    @Transactional
    public Subscription subscribe(Long userId, Long planId, Long requestedTierId, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Subscription> existing = subscriptionRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        User user = userService.getUser(userId);
        expireIfElapsed(getRawCurrent(userId).orElse(null));

        if (getActiveSubscription(userId).isPresent()) {
            throw new BusinessRuleException(
                    "User already has an active subscription; upgrade, downgrade or cancel it instead");
        }

        MembershipPlan plan = catalogService.getPlan(planId);
        if (!plan.isActive()) {
            throw new BusinessRuleException("Plan is not active: " + planId);
        }

        MembershipTier tier = resolveTierForSubscribe(user, requestedTierId);

        Instant start = Instant.now(clock);
        Instant end = plusCycle(start, plan.getBillingCycle().getMonths());

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .tier(tier)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(start)
                .endDate(end)
                .idempotencyKey(idempotencyKey)
                .build();
        return subscriptionRepository.save(subscription);
    }

    private MembershipTier resolveTierForSubscribe(User user, Long requestedTierId) {
        if (requestedTierId == null) {
            UserActivitySnapshot snapshot = orderService.buildSnapshot(user);
            return eligibilityEvaluator
                    .highestEligibleTier(catalogService.listTiers(), snapshot)
                    .orElseGet(catalogService::getBaseTier);
        }
        MembershipTier requested = catalogService.getTier(requestedTierId);
        assertEligible(user, requested);
        return requested;
    }

    /** Move to a strictly higher-ranked tier (eligibility enforced). */
    @Retryable(retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3, backoff = @Backoff(delay = 50))
    @Transactional
    public Subscription upgradeTier(Long subscriptionId, Long targetTierId) {
        Subscription sub = getActiveById(subscriptionId);
        MembershipTier target = catalogService.getTier(targetTierId);
        if (target.getRank() <= sub.getTier().getRank()) {
            throw new BusinessRuleException(
                    "Target tier '" + target.getName() + "' is not higher than current tier '"
                            + sub.getTier().getName() + "'; use downgrade instead");
        }
        assertEligible(sub.getUser(), target);
        sub.setTier(target);
        return subscriptionRepository.save(sub);
    }

    /** Move to a strictly lower-ranked tier. Always allowed if it exists. */
    @Retryable(retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3, backoff = @Backoff(delay = 50))
    @Transactional
    public Subscription downgradeTier(Long subscriptionId, Long targetTierId) {
        Subscription sub = getActiveById(subscriptionId);
        MembershipTier target = catalogService.getTier(targetTierId);
        if (target.getRank() >= sub.getTier().getRank()) {
            throw new BusinessRuleException(
                    "Target tier '" + target.getName() + "' is not lower than current tier '"
                            + sub.getTier().getName() + "'; use upgrade instead");
        }
        sub.setTier(target);
        return subscriptionRepository.save(sub);
    }

    @Retryable(retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3, backoff = @Backoff(delay = 50))
    @Transactional
    public Subscription cancel(Long subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + subscriptionId));
        if (sub.getStatus() == SubscriptionStatus.CANCELLED) {
            return sub;
        }
        sub.setStatus(SubscriptionStatus.CANCELLED);
        sub.setCancelledAt(Instant.now(clock));
        return subscriptionRepository.save(sub);
    }

    /** The user's current (active, non-expired) subscription, if any. */
    @Transactional
    public Optional<Subscription> getCurrentSubscription(Long userId) {
        userService.getUser(userId);
        Subscription current = getRawCurrent(userId).orElse(null);
        expireIfElapsed(current);
        return getActiveSubscription(userId);
    }

    /** The highest tier the user currently qualifies for. */
    @Transactional(readOnly = true)
    public MembershipTier evaluateEligibleTier(Long userId) {
        User user = userService.getUser(userId);
        UserActivitySnapshot snapshot = orderService.buildSnapshot(user);
        return eligibilityEvaluator
                .highestEligibleTier(catalogService.listTiers(), snapshot)
                .orElseGet(catalogService::getBaseTier);
    }

    // ---- helpers ----

    private void assertEligible(User user, MembershipTier tier) {
        UserActivitySnapshot snapshot = orderService.buildSnapshot(user);
        if (!eligibilityEvaluator.qualifiesFor(tier, snapshot)) {
            throw new BusinessRuleException(
                    "User " + user.getId() + " is not eligible for tier '" + tier.getName() + "'");
        }
    }

    private Subscription getActiveById(Long subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + subscriptionId));
        expireIfElapsed(sub);
        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "Subscription " + subscriptionId + " is not active (status=" + sub.getStatus() + ")");
        }
        return sub;
    }

    private Optional<Subscription> getActiveSubscription(Long userId) {
        List<Subscription> active =
                subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
        return active.stream().findFirst();
    }

    private Optional<Subscription> getRawCurrent(Long userId) {
        return subscriptionRepository
                .findFirstByUserIdAndStatusOrderByStartDateDesc(userId, SubscriptionStatus.ACTIVE);
    }

    /** Lazily flips an ACTIVE subscription to EXPIRED once its window elapses. */
    private void expireIfElapsed(Subscription sub) {
        if (sub == null) {
            return;
        }
        if (sub.getStatus() == SubscriptionStatus.ACTIVE
                && Instant.now(clock).isAfter(sub.getEndDate())) {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(sub);
        }
    }

    private Instant plusCycle(Instant start, int months) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(start, ZoneId.of("UTC"));
        return zdt.plusMonths(months).toInstant();
    }
}
