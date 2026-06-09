package com.firstclub.membership.repository;

import com.firstclub.membership.domain.MembershipTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipTierRepository extends JpaRepository<MembershipTier, Long> {
    List<MembershipTier> findAllByOrderByRankAsc();
    Optional<MembershipTier> findByName(String name);
    Optional<MembershipTier> findFirstByBaseTierTrueOrderByRankAsc();
}
