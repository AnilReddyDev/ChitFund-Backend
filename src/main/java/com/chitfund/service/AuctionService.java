package com.chitfund.service;

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
    public Auction createAuction(Long groupId, Integer month, Long winnerId, Double bidAmount) {

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
        double totalCollection = group.getMonthlyPremium() * group.getTotalMembers();
        double payout = bidAmount;
        double profit = totalCollection - payout;

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
        group.setCurrentMonth(group.getCurrentMonth() + 1);
        groupRepo.save(group);

        return auctionRepo.save(auction);
    }
    public List<Auction> getHistory(Long groupId) {
        return auctionRepo.findByGroupIdOrderByMonthAsc(groupId);
    }

}