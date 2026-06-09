package com.firstclub.membership.domain;

import com.firstclub.membership.domain.enums.BenefitType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * A single configurable benefit granted by a tier. Storing benefits as rows
 * (rather than columns/flags) lets operators add, remove or re-value perks per
 * tier at runtime via the admin APIs.
 */
@Entity
@Table(name = "tier_benefits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BenefitType type;

    /**
     * Numeric value for "valued" benefits (e.g. discount percent). Null/ignored
     * for flag-style benefits such as FREE_DELIVERY.
     */
    @Column(name = "benefit_value", precision = 6, scale = 2)
    private BigDecimal value;
}
