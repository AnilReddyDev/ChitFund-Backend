package com.chitfund.service;

import com.chitfund.audit.Auditable;
import com.chitfund.entity.AuditAction;
import com.chitfund.entity.*;
import com.chitfund.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuctionService {

    private final AuctionRepository auctionRepo;
    private final PaymentRepository paymentRepo;
    private final GroupRepository groupRepo;

    public AuctionService(AuctionRepository auctionRepo,
                          PaymentRepository paymentRepo,
                          GroupRepository groupRepo) {
        this.auctionRepo = auctionRepo;
        this.paymentRepo = paymentRepo;
        this.groupRepo = groupRepo;
    }

    @Auditable(action = AuditAction.CREATE, entityType = "Auction", entityClass = Auction.class)
    public Auction createAuction(Long groupId, Integer month, Long winnerId, Double bidAmount) {
        if (groupId == null || groupId <= 0 || month == null || month <= 0 || winnerId == null || winnerId <= 0) {
            throw new IllegalArgumentException("Group, month, and winner are required");
        }
        if (bidAmount == null || bidAmount <= 0) {
            throw new IllegalArgumentException("Bid amount must be positive");
        }

        // ❌ prevent duplicate auction for same month
        if (auctionRepo.findByGroupIdAndMonth(groupId, month).isPresent()) {
            throw new RuntimeException("Auction already done for this month");
        }

        // ❌ prevent same winner again
        boolean alreadyWon = auctionRepo.existsByGroupIdAndWinnerMemberId(groupId, winnerId);
        if (alreadyWon) {
            throw new RuntimeException("This member has already won an auction");
        }

        ChittGroup group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // 💰 calculations
        double bid = bidAmount == null ? 0.0 : bidAmount;
        double totalAmount = group.getTotalAmount() == null ? 0.0 : group.getTotalAmount();
        double payout = Math.max(0.0, totalAmount - bid);
        double profit = bid;

        Auction auction = new Auction(
                null,
                groupId,
                month,
                winnerId,
                payout,
                profit,
                null
        );

        // 👉 move to next month
        group.setCurrentMonth((group.getCurrentMonth() == null ? 1 : group.getCurrentMonth()) + 1);
        groupRepo.save(group);

        return auctionRepo.save(auction);
    }
    public List<Auction> getHistory(Long groupId) {
        return auctionRepo.findByGroupIdOrderByMonthAsc(groupId);
    }

}
