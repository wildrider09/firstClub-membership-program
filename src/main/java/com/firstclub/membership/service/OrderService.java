package com.firstclub.membership.service;

import com.firstclub.membership.domain.Order;
import com.firstclub.membership.domain.User;
import com.firstclub.membership.repository.OrderRepository;
import com.firstclub.membership.tier.UserActivitySnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Records orders and derives the {@link UserActivitySnapshot} used by tier
 * evaluation. A {@link Clock} is injected so time-based logic is testable.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final Clock clock;

    public OrderService(OrderRepository orderRepository, UserService userService, Clock clock) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.clock = clock;
    }

    @Transactional
    public Order placeOrder(Long userId, BigDecimal amount) {
        User user = userService.getUser(userId);
        Order order = Order.builder()
                .user(user)
                .amount(amount)
                .placedAt(Instant.now(clock))
                .build();
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public UserActivitySnapshot buildSnapshot(User user) {
        Instant monthAgo = Instant.now(clock).minus(30, ChronoUnit.DAYS);
        long count = orderRepository.countByUserId(user.getId());
        BigDecimal monthlyValue = orderRepository.sumAmountByUserSince(user.getId(), monthAgo);
        return UserActivitySnapshot.builder()
                .totalOrderCount(count)
                .trailingMonthOrderValue(monthlyValue == null ? BigDecimal.ZERO : monthlyValue)
                .cohort(user.getCohort())
                .build();
    }
}
