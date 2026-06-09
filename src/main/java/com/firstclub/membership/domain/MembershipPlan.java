package com.firstclub.membership.domain;

import com.firstclub.membership.domain.enums.BillingCycle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * A membership plan defines the billing cadence (Monthly / Quarterly / Yearly)
 * and its price. Plans are independent of tiers: a user subscribes to a plan
 * and is assigned a tier.
 */
@Entity
@Table(name = "membership_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private BillingCycle billingCycle;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** Whether the plan can currently be subscribed to. */
    @Column(nullable = false)
    private boolean active;
}
