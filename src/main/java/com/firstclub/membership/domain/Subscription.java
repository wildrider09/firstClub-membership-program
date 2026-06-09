package com.firstclub.membership.domain;

import com.firstclub.membership.domain.enums.SubscriptionStatus;
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
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A user's membership subscription, binding a {@link User} to a
 * {@link MembershipPlan} and a {@link MembershipTier} for a validity window.
 *
 * <p>Concurrency: the {@link Version} field enables optimistic locking so two
 * concurrent mutations (e.g. simultaneous upgrade + cancel) cannot silently
 * clobber each other. The {@code idempotencyKey} makes the subscribe operation
 * safe to retry without creating duplicate subscriptions.</p>
 */
@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan plan;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(nullable = false)
    private Instant startDate;

    @Column(nullable = false)
    private Instant endDate;

    @Column
    private Instant cancelledAt;

    /** Unique per successful subscribe call; enables safe client retries. */
    @Column(unique = true)
    private String idempotencyKey;

    @Version
    private Long version;

    public boolean isCurrentlyActive(Instant now) {
        return status == SubscriptionStatus.ACTIVE
                && !now.isAfter(endDate);
    }
}
