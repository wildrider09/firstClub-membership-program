package com.firstclub.membership.controller;

import com.firstclub.membership.dto.DtoMapper;
import com.firstclub.membership.dto.request.ChangeTierRequest;
import com.firstclub.membership.dto.request.SubscribeRequest;
import com.firstclub.membership.dto.response.SubscriptionResponse;
import com.firstclub.membership.dto.response.TierEligibilityResponse;
import com.firstclub.membership.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;

/**
 * Exposes the user-facing membership actions: subscribe, upgrade, downgrade,
 * cancel, and track current membership / eligibility.
 */
@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final Clock clock;

    public SubscriptionController(SubscriptionService subscriptionService, Clock clock) {
        this.subscriptionService = subscriptionService;
        this.clock = clock;
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> subscribe(@Valid @RequestBody SubscribeRequest request) {
        var sub = subscriptionService.subscribe(
                request.userId(), request.planId(), request.tierId(), request.idempotencyKey());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DtoMapper.toSubscriptionResponse(sub, clock));
    }

    @PostMapping("/{id}/upgrade")
    public SubscriptionResponse upgrade(@PathVariable Long id,
                                        @Valid @RequestBody ChangeTierRequest request) {
        var sub = subscriptionService.upgradeTier(id, request.targetTierId());
        return DtoMapper.toSubscriptionResponse(sub, clock);
    }

    @PostMapping("/{id}/downgrade")
    public SubscriptionResponse downgrade(@PathVariable Long id,
                                          @Valid @RequestBody ChangeTierRequest request) {
        var sub = subscriptionService.downgradeTier(id, request.targetTierId());
        return DtoMapper.toSubscriptionResponse(sub, clock);
    }

    @PostMapping("/{id}/cancel")
    public SubscriptionResponse cancel(@PathVariable Long id) {
        var sub = subscriptionService.cancel(id);
        return DtoMapper.toSubscriptionResponse(sub, clock);
    }

    @GetMapping("/users/{userId}/current")
    public ResponseEntity<SubscriptionResponse> current(@PathVariable Long userId) {
        return subscriptionService.getCurrentSubscription(userId)
                .map(sub -> ResponseEntity.ok(DtoMapper.toSubscriptionResponse(sub, clock)))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/users/{userId}/eligible-tier")
    public TierEligibilityResponse eligibleTier(@PathVariable Long userId) {
        var tier = subscriptionService.evaluateEligibleTier(userId);
        return new TierEligibilityResponse(userId, DtoMapper.toTierResponse(tier));
    }
}
