package com.firstclub.membership.repository;

import com.firstclub.membership.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface OrderRepository extends JpaRepository<Order, Long> {

    long countByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o "
            + "WHERE o.user.id = :userId AND o.placedAt >= :since")
    java.math.BigDecimal sumAmountByUserSince(@Param("userId") Long userId,
                                              @Param("since") Instant since);
}
