package com.chitfund.repository;

import com.chitfund.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

    Optional<Auction> findByGroupIdAndMonth(Long groupId, Integer month);
    boolean existsByGroupIdAndWinnerMemberId(Long groupId, Long winnerMemberId);
    List<Auction> findByGroupIdOrderByMonthAsc(Long groupId);
}