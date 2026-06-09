package com.firstclub.membership.repository;

import com.firstclub.membership.domain.Subscription;
import com.firstclub.membership.domain.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByIdempotencyKey(String idempotencyKey);

    List<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);

    Optional<Subscription> findFirstByUserIdAndStatusOrderByStartDateDesc(Long userId, SubscriptionStatus status);
}
