package com.firstclub.membership.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * A membership tier (e.g. SILVER, GOLD, PLATINUM).
 *
 * <p>{@code rank} establishes the ordering used for upgrade/downgrade and for
 * resolving the highest tier a user qualifies for. Higher rank == better tier.
 * Both the granted {@link TierBenefit benefits} and the
 * {@link TierEligibilityRule eligibility rules} are stored as data, making the
 * whole tier system configurable without code changes.</p>
 */
@Entity
@Table(name = "membership_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /** Ordering of the tier; higher is better (SILVER=1, GOLD=2, PLATINUM=3). */
    @Column(name = "tier_rank", nullable = false, unique = true)
    private int rank;

    @Column(length = 500)
    private String description;

    /** Whether this is the default tier granted on a fresh subscription. */
    @Column(nullable = false)
    private boolean baseTier;

    @OneToMany(mappedBy = "tier", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<TierBenefit> benefits = new ArrayList<>();

    @OneToMany(mappedBy = "tier", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<TierEligibilityRule> eligibilityRules = new ArrayList<>();

    public void addBenefit(TierBenefit benefit) {
        benefit.setTier(this);
        this.benefits.add(benefit);
    }

    public void addEligibilityRule(TierEligibilityRule rule) {
        rule.setTier(this);
        this.eligibilityRules.add(rule);
    }
}
