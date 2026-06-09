package com.firstclub.membership.controller;

import com.firstclub.membership.dto.DtoMapper;
import com.firstclub.membership.dto.request.AddBenefitRequest;
import com.firstclub.membership.dto.request.AddEligibilityRuleRequest;
import com.firstclub.membership.dto.request.CreatePlanRequest;
import com.firstclub.membership.dto.request.CreateTierRequest;
import com.firstclub.membership.dto.response.PlanResponse;
import com.firstclub.membership.dto.response.TierResponse;
import com.firstclub.membership.service.MembershipCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read APIs let users browse plans and tiers (with their benefits). Write APIs
 * let operators configure the catalogue at runtime — demonstrating that tiers
 * and benefits are fully configurable, not hard-coded.
 */
@RestController
@RequestMapping("/api")
public class CatalogController {

    private final MembershipCatalogService catalogService;

    public CatalogController(MembershipCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    // ---- Plans ----

    @GetMapping("/plans")
    public List<PlanResponse> listPlans() {
        return catalogService.listActivePlans().stream().map(DtoMapper::toPlanResponse).toList();
    }

    @PostMapping("/plans")
    public ResponseEntity<PlanResponse> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        var plan = catalogService.createPlan(request.name(), request.billingCycle(), request.price());
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toPlanResponse(plan));
    }

    // ---- Tiers ----

    @GetMapping("/tiers")
    public List<TierResponse> listTiers() {
        return catalogService.listTiers().stream().map(DtoMapper::toTierResponse).toList();
    }

    @GetMapping("/tiers/{id}")
    public TierResponse getTier(@PathVariable Long id) {
        return DtoMapper.toTierResponse(catalogService.getTier(id));
    }

    @PostMapping("/tiers")
    public ResponseEntity<TierResponse> createTier(@Valid @RequestBody CreateTierRequest request) {
        var tier = catalogService.createTier(
                request.name(), request.rank(), request.description(), request.baseTier());
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toTierResponse(tier));
    }

    @PostMapping("/tiers/{id}/benefits")
    public ResponseEntity<TierResponse> addBenefit(@PathVariable Long id,
                                                   @Valid @RequestBody AddBenefitRequest request) {
        var tier = catalogService.addBenefit(id, request.type(), request.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toTierResponse(tier));
    }

    @PostMapping("/tiers/{id}/eligibility-rules")
    public ResponseEntity<TierResponse> addRule(@PathVariable Long id,
                                                @Valid @RequestBody AddEligibilityRuleRequest request) {
        var tier = catalogService.addEligibilityRule(
                id, request.type(), request.numericThreshold(), request.stringValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toTierResponse(tier));
    }
}
