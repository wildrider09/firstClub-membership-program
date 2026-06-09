package com.firstclub.membership.repository;

import com.firstclub.membership.domain.MembershipPlan;
import com.firstclub.membership.domain.enums.BillingCycle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {
    List<MembershipPlan> findByActiveTrue();
    Optional<MembershipPlan> findByBillingCycle(BillingCycle billingCycle);
}
