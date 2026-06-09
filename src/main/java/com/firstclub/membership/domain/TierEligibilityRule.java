package com.firstclub.membership.domain;

import com.firstclub.membership.domain.enums.EligibilityRuleType;
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

/**
 * A configurable rule that a user must satisfy to qualify for a tier. A tier
 * may have several rules; a user qualifies only when ALL of them pass (logical
 * AND), evaluated by the matching {@code TierEligibilityStrategy}.
 */
@Entity
@Table(name = "tier_eligibility_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierEligibilityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EligibilityRuleType type;

    /** Numeric threshold (e.g. min order count or min monthly value). */
    @Column
    private Double numericThreshold;

    /** String parameter (e.g. required cohort name). */
    @Column
    private String stringValue;
}
