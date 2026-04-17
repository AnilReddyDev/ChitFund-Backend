package com.chitfund.service;

import com.chitfund.dto.ChartDataDTO;
import com.chitfund.dto.DashboardResponse;
import com.chitfund.entity.*;
import com.chitfund.repository.*;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private final PaymentRepository paymentRepo;
    private final AuctionRepository auctionRepo;
    private final GroupRepository groupRepo;
    private final MemberRepository memberRepo;

    public DashboardService(PaymentRepository paymentRepo,
                            AuctionRepository auctionRepo,
                            GroupRepository groupRepo,
                            MemberRepository memberRepo) {
        this.paymentRepo = paymentRepo;
        this.auctionRepo = auctionRepo;
        this.groupRepo = groupRepo;
        this.memberRepo = memberRepo;
    }

    public DashboardResponse getDashboard(Long groupId) {

        ChittGroup group = groupRepo.findById(groupId).orElseThrow();

        List<Payment> payments = paymentRepo.findByGroupId(groupId);
        List<Auction> auctions = auctionRepo.findByGroupIdOrderByMonthAsc(groupId);

        // 💰 total collection
        double totalCollection = payments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();

        // 📉 total profit
        double totalProfit = auctions.stream()
                .mapToDouble(Auction::getProfit)
                .sum();

        // 👥 members
        int totalMembers = group.getTotalMembers();

        // ⚠️ pending
        int expected = group.getCurrentMonth() * totalMembers;
        int pending = expected - payments.size();

        // 🏆 last winner
        String lastWinner = "N/A";
        if (!auctions.isEmpty()) {
            Auction last = auctions.get(auctions.size() - 1);
            Member m = memberRepo.findById(last.getWinnerMemberId()).orElse(null);
            if (m != null) lastWinner = m.getName();
        }

        return new DashboardResponse(
                totalCollection,
                totalProfit,
                totalMembers,
                pending,
                group.getCurrentMonth(),
                lastWinner
        );
    }

    public List<ChartDataDTO> getChartData(Long groupId) {

        List<Auction> auctions = auctionRepo.findByGroupIdOrderByMonthAsc(groupId);
        List<Payment> payments = paymentRepo.findByGroupId(groupId);

        List<ChartDataDTO> result = new ArrayList<>();

        for (Auction a : auctions) {

            double collection = payments.stream()
                    .filter(p -> p.getMonth().getMonthValue() == a.getMonth())
                    .mapToDouble(Payment::getAmount)
                    .sum();

            result.add(new ChartDataDTO(
                    "M" + a.getMonth(),
                    collection,
                    a.getProfit()
            ));
        }

        return result;
    }
}