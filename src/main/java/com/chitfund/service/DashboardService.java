package com.chitfund.service;

import com.chitfund.dto.ChartDataDTO;
import com.chitfund.dto.DashboardResponse;
import com.chitfund.dto.DashboardSummaryResponse;
import com.chitfund.dto.DashboardTrendsResponse;
import com.chitfund.entity.*;
import com.chitfund.repository.*;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final PaymentRepository paymentRepo;
    private final AuctionRepository auctionRepo;
    private final GroupRepository groupRepo;
    private final MemberRepository memberRepo;
    private final GroupMemberRepository groupMemberRepo;

    public DashboardService(PaymentRepository paymentRepo,
                            AuctionRepository auctionRepo,
                            GroupRepository groupRepo,
                            MemberRepository memberRepo,
                            GroupMemberRepository groupMemberRepo) {
        this.paymentRepo = paymentRepo;
        this.auctionRepo = auctionRepo;
        this.groupRepo = groupRepo;
        this.memberRepo = memberRepo;
        this.groupMemberRepo = groupMemberRepo;
    }

    public DashboardResponse getDashboard(Long groupId) {

        DashboardSummaryResponse summary = getSummary(groupId);

        return new DashboardResponse(
                summary.getCollection().getTotalCollectedTillNow(),
                summary.getProfit().getTotalProfit(),
                summary.getGroup().getTotalMembers(),
                summary.getPayments().getPendingMembers().size(),
                summary.getGroup().getCurrentMonth(),
                summary.getAuction().getLastAuction() == null
                        ? "N/A"
                        : summary.getAuction().getLastAuction().getWinnerName()
        );
    }

    public List<ChartDataDTO> getChartData(Long groupId) {

        DashboardTrendsResponse trends = getTrends(groupId);
        Map<Integer, Double> profitByMonth = trends.getProfitTrend().stream()
                .collect(Collectors.toMap(
                        DashboardTrendsResponse.ProfitTrendPoint::getMonth,
                        DashboardTrendsResponse.ProfitTrendPoint::getProfit
                ));

        List<ChartDataDTO> result = new ArrayList<>();

        for (DashboardTrendsResponse.CollectionTrendPoint point : trends.getCollectionTrend()) {
            result.add(new ChartDataDTO(
                    "M" + point.getMonth(),
                    point.getCollected(),
                    profitByMonth.getOrDefault(point.getMonth(), 0.0)
            ));
        }

        return result;
    }

    public DashboardSummaryResponse getSummary(Long groupId) {
        return getSummary(groupId, null);
    }

    public DashboardSummaryResponse getSummary(Long groupId, Integer month) {

        ChittGroup group = groupRepo.findById(groupId).orElseThrow();
        int dashboardMonth = resolveDashboardMonth(group, month);
        LocalDate dashboardMonthDate = monthDate(group, dashboardMonth);

        List<GroupMember> groupMembers = groupMemberRepo.findByGroupIdAndIsDeletedFalse(groupId);
        List<Long> assignedMemberIds = groupMembers.stream()
                .map(GroupMember::getMemberId)
                .toList();
        Map<Long, Member> membersById = memberRepo.findAllById(assignedMemberIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        List<Payment> payments = paymentRepo.findByGroupId(groupId).stream()
                .filter(this::isActivePaidPayment)
                .toList();
        List<Auction> auctions = auctionRepo.findByGroupIdOrderByMonthAsc(groupId);

        double monthlyPremium = defaultDouble(group.getMonthlyPremium());
        int assignedMembers = groupMembers.size();
        double expectedThisMonth = assignedMembers * monthlyPremium;
        double collectedThisMonth = payments.stream()
                .filter(payment -> dashboardMonthDate.equals(payment.getMonth()))
                .mapToDouble(payment -> defaultDouble(payment.getAmount()))
                .sum();
        double pendingThisMonth = Math.max(0.0, expectedThisMonth - collectedThisMonth);
        int collectionRate = expectedThisMonth == 0.0
                ? 0
                : (int) Math.round((collectedThisMonth / expectedThisMonth) * 100);
        double totalCollectedTillNow = payments.stream()
                .filter(payment -> monthIndex(group, payment.getMonth()) <= dashboardMonth)
                .mapToDouble(payment -> defaultDouble(payment.getAmount()))
                .sum();

        Set<Long> dashboardMonthPaidMembers = paidMemberIdsForMonth(payments, dashboardMonthDate);
        List<DashboardSummaryResponse.PendingMember> pendingMembers = groupMembers.stream()
                .filter(groupMember -> !dashboardMonthPaidMembers.contains(groupMember.getMemberId()))
                .map(groupMember -> pendingMember(groupMember.getMemberId(), membersById, monthlyPremium, dashboardMonth))
                .toList();

        List<DashboardSummaryResponse.OverdueMember> overdueMembers = buildOverdueMembers(
                group,
                groupMembers,
                membersById,
                payments,
                dashboardMonth,
                monthlyPremium
        );

        String currentMonthAuctionStatus = auctions.stream()
                .anyMatch(auction -> dashboardMonth == defaultInt(auction.getMonth(), 0))
                ? "COMPLETED"
                : "PENDING";

        Auction latestAuctionTillDashboardMonth = auctions.stream()
                .filter(auction -> defaultInt(auction.getMonth(), 0) <= dashboardMonth)
                .reduce((previous, current) -> current)
                .orElse(null);
        DashboardSummaryResponse.LastAuction lastAuction = latestAuctionTillDashboardMonth == null
                ? null
                : lastAuction(latestAuctionTillDashboardMonth, membersById);

        Set<Long> winners = auctions.stream()
                .filter(auction -> defaultInt(auction.getMonth(), 0) < dashboardMonth)
                .map(Auction::getWinnerMemberId)
                .collect(Collectors.toSet());
        int eligibleMembers = (int) groupMembers.stream()
                .filter(groupMember -> !winners.contains(groupMember.getMemberId()))
                .count();

        double totalProfit = auctions.stream()
                .filter(auction -> defaultInt(auction.getMonth(), 0) <= dashboardMonth)
                .mapToDouble(auction -> defaultDouble(auction.getProfit()))
                .sum();
        long completedAuctionsTillDashboardMonth = auctions.stream()
                .filter(auction -> defaultInt(auction.getMonth(), 0) <= dashboardMonth)
                .count();
        double averageProfit = completedAuctionsTillDashboardMonth == 0
                ? 0.0
                : totalProfit / completedAuctionsTillDashboardMonth;

        DashboardSummaryResponse.HealthSummary health = buildHealth(
                collectionRate,
                overdueMembers.size(),
                "PENDING".equals(currentMonthAuctionStatus),
                assignedMembers,
                defaultInt(group.getTotalMembers(), 0)
        );
        int paidMembers = assignedMembers - pendingMembers.size();
        DashboardSummaryResponse.MonthSummary monthSummary = new DashboardSummaryResponse.MonthSummary(
                dashboardMonth,
                paidMembers,
                pendingMembers.size(),
                overdueMembers.size(),
                expectedThisMonth,
                collectedThisMonth,
                pendingThisMonth,
                currentMonthAuctionStatus,
                eligibleMembers
        );
        List<DashboardSummaryResponse.Recommendation> recommendations = buildRecommendations(
                dashboardMonth,
                pendingMembers.size(),
                overdueMembers.size(),
                pendingThisMonth,
                currentMonthAuctionStatus,
                assignedMembers,
                defaultInt(group.getTotalMembers(), 0)
        );

        return new DashboardSummaryResponse(
                new DashboardSummaryResponse.GroupSummary(
                        group.getId(),
                        group.getName(),
                        defaultDouble(group.getTotalAmount()),
                        monthlyPremium,
                        defaultInt(group.getTotalMembers(), 0),
                        assignedMembers,
                        defaultInt(group.getDuration(), 1),
                        dashboardMonth,
                        group.getStartMonth()
                ),
                new DashboardSummaryResponse.CollectionSummary(
                        expectedThisMonth,
                        collectedThisMonth,
                        pendingThisMonth,
                        collectionRate,
                        totalCollectedTillNow
                ),
                new DashboardSummaryResponse.PaymentsSummary(pendingMembers, overdueMembers),
                new DashboardSummaryResponse.AuctionSummary(
                        currentMonthAuctionStatus,
                        lastAuction,
                        new DashboardSummaryResponse.NextAuction(dashboardMonth, eligibleMembers)
                ),
                new DashboardSummaryResponse.ProfitSummary(totalProfit, averageProfit),
                health,
                recommendations,
                monthSummary
        );
    }

    public DashboardTrendsResponse getTrends(Long groupId) {

        ChittGroup group = groupRepo.findById(groupId).orElseThrow();
        int currentMonth = calculateCurrentMonth(group);
        int assignedMembers = groupMemberRepo.findByGroupIdAndIsDeletedFalse(groupId).size();
        double expected = assignedMembers * defaultDouble(group.getMonthlyPremium());

        List<Payment> payments = paymentRepo.findByGroupId(groupId).stream()
                .filter(this::isActivePaidPayment)
                .toList();
        Map<Integer, Double> collectionByMonth = new HashMap<>();
        for (Payment payment : payments) {
            int monthIndex = monthIndex(group, payment.getMonth());
            if (monthIndex >= 1 && monthIndex <= currentMonth) {
                collectionByMonth.merge(monthIndex, defaultDouble(payment.getAmount()), Double::sum);
            }
        }

        List<Auction> auctions = auctionRepo.findByGroupIdOrderByMonthAsc(groupId);
        Map<Integer, Double> profitByMonth = new HashMap<>();
        for (Auction auction : auctions) {
            profitByMonth.merge(defaultInt(auction.getMonth(), 0), defaultDouble(auction.getProfit()), Double::sum);
        }

        List<DashboardTrendsResponse.CollectionTrendPoint> collectionTrend = new ArrayList<>();
        List<DashboardTrendsResponse.ProfitTrendPoint> profitTrend = new ArrayList<>();
        for (int month = 1; month <= currentMonth; month++) {
            double collected = collectionByMonth.getOrDefault(month, 0.0);
            collectionTrend.add(new DashboardTrendsResponse.CollectionTrendPoint(
                    month,
                    expected,
                    collected,
                    Math.max(0.0, expected - collected)
            ));
            profitTrend.add(new DashboardTrendsResponse.ProfitTrendPoint(
                    month,
                    profitByMonth.getOrDefault(month, 0.0)
            ));
        }

        return new DashboardTrendsResponse(collectionTrend, profitTrend);
    }

    private List<DashboardSummaryResponse.OverdueMember> buildOverdueMembers(ChittGroup group,
                                                                             List<GroupMember> groupMembers,
                                                                             Map<Long, Member> membersById,
                                                                             List<Payment> payments,
                                                                             int currentMonth,
                                                                             double monthlyPremium) {
        Map<Integer, Set<Long>> paidMembersByMonth = new HashMap<>();
        for (Payment payment : payments) {
            int monthIndex = monthIndex(group, payment.getMonth());
            paidMembersByMonth.computeIfAbsent(monthIndex, ignored -> new HashSet<>()).add(payment.getMemberId());
        }

        List<DashboardSummaryResponse.OverdueMember> overdueMembers = new ArrayList<>();
        for (GroupMember groupMember : groupMembers) {
            List<Integer> missedMonths = new ArrayList<>();
            for (int month = 1; month < currentMonth; month++) {
                if (!paidMembersByMonth.getOrDefault(month, Set.of()).contains(groupMember.getMemberId())) {
                    missedMonths.add(month);
                }
            }

            if (!missedMonths.isEmpty()) {
                Member member = membersById.get(groupMember.getMemberId());
                overdueMembers.add(new DashboardSummaryResponse.OverdueMember(
                        groupMember.getMemberId(),
                        member == null ? "Unknown Member" : member.getName(),
                        member == null ? null : member.getPhone(),
                        missedMonths,
                        missedMonths.size() * monthlyPremium
                ));
            }
        }
        return overdueMembers;
    }

    private DashboardSummaryResponse.PendingMember pendingMember(Long memberId,
                                                                 Map<Long, Member> membersById,
                                                                 double monthlyPremium,
                                                                 int currentMonth) {
        Member member = membersById.get(memberId);
        return new DashboardSummaryResponse.PendingMember(
                memberId,
                member == null ? "Unknown Member" : member.getName(),
                member == null ? null : member.getPhone(),
                monthlyPremium,
                currentMonth
        );
    }

    private DashboardSummaryResponse.LastAuction lastAuction(Auction auction, Map<Long, Member> membersById) {
        Member winner = membersById.get(auction.getWinnerMemberId());
        double payoutAmount = defaultDouble(auction.getPayoutAmount());
        double profit = defaultDouble(auction.getProfit());

        return new DashboardSummaryResponse.LastAuction(
                auction.getId(),
                auction.getMonth(),
                auction.getWinnerMemberId(),
                winner == null ? "Unknown Member" : winner.getName(),
                profit,
                payoutAmount,
                profit
        );
    }

    private DashboardSummaryResponse.HealthSummary buildHealth(int collectionRate,
                                                               int overdueCount,
                                                               boolean auctionPending,
                                                               int assignedMembers,
                                                               int totalMembers) {
        int score = 100;
        List<String> reasons = new ArrayList<>();

        if (collectionRate < 60) {
            score -= 25;
        } else if (collectionRate < 80) {
            score -= 15;
        }

        if (overdueCount > 0) {
            score -= 20;
        }
        if (auctionPending) {
            score -= 10;
        }
        if (assignedMembers < totalMembers) {
            score -= 10;
        }

        reasons.add(collectionRate + "% collection completed this month");
        reasons.add(overdueCount + " overdue members");
        reasons.add(auctionPending ? "Auction pending for current month" : "Auction completed for current month");
        reasons.add(assignedMembers + " of " + totalMembers + " members assigned");

        String status;
        if (score >= 80) {
            status = "GOOD";
        } else if (score >= 50) {
            status = "NEEDS_ATTENTION";
        } else {
            status = "RISKY";
        }

        return new DashboardSummaryResponse.HealthSummary(status, Math.max(0, score), reasons);
    }

    private List<DashboardSummaryResponse.Recommendation> buildRecommendations(int dashboardMonth,
                                                                               int pendingMemberCount,
                                                                               int overdueMemberCount,
                                                                               double pendingAmount,
                                                                               String auctionStatus,
                                                                               int assignedMembers,
                                                                               int totalMembers) {
        List<DashboardSummaryResponse.Recommendation> recommendations = new ArrayList<>();

        if (pendingMemberCount > 0) {
            recommendations.add(new DashboardSummaryResponse.Recommendation(
                    "COLLECT_PAYMENT",
                    "HIGH",
                    pluralize(pendingMemberCount, "member") + " pending payment",
                    "Collect " + pendingAmount + " for Month " + dashboardMonth,
                    "/ledger"
            ));
        }

        if (overdueMemberCount > 0) {
            recommendations.add(new DashboardSummaryResponse.Recommendation(
                    "FOLLOW_UP_OVERDUE",
                    "HIGH",
                    pluralize(overdueMemberCount, "member") + " overdue",
                    "Follow up previous missed payments before closing Month " + dashboardMonth,
                    "/ledger"
            ));
        }

        if ("PENDING".equals(auctionStatus)) {
            recommendations.add(new DashboardSummaryResponse.Recommendation(
                    "RUN_AUCTION",
                    "MEDIUM",
                    "Auction pending",
                    "Month " + dashboardMonth + " auction is not completed",
                    "/auction"
            ));
        }

        if (assignedMembers < totalMembers) {
            recommendations.add(new DashboardSummaryResponse.Recommendation(
                    "ASSIGN_MEMBERS",
                    "LOW",
                    "Only " + assignedMembers + " of " + totalMembers + " members assigned",
                    "Assign remaining members to keep the group complete",
                    "/members"
            ));
        }

        return recommendations;
    }

    private String pluralize(int count, String singular) {
        return count + " " + singular + (count == 1 ? "" : "s");
    }

    private Set<Long> paidMemberIdsForMonth(List<Payment> payments, LocalDate monthDate) {
        return payments.stream()
                .filter(payment -> monthDate.equals(payment.getMonth()))
                .map(Payment::getMemberId)
                .collect(Collectors.toSet());
    }

    private boolean isActivePaidPayment(Payment payment) {
        return Boolean.TRUE.equals(payment.getIsPaid()) && !Boolean.TRUE.equals(payment.getIsDeleted());
    }

    private int calculateCurrentMonth(ChittGroup group) {
        int duration = Math.max(1, defaultInt(group.getDuration(), 1));
        if (group.getStartMonth() == null) {
            return clamp(defaultInt(group.getCurrentMonth(), 1), 1, duration);
        }

        LocalDate startMonth = group.getStartMonth().withDayOfMonth(1);
        LocalDate thisMonth = LocalDate.now().withDayOfMonth(1);
        int calculatedMonth = (int) ChronoUnit.MONTHS.between(startMonth, thisMonth) + 1;
        return clamp(calculatedMonth, 1, duration);
    }

    private int resolveDashboardMonth(ChittGroup group, Integer requestedMonth) {
        int duration = Math.max(1, defaultInt(group.getDuration(), 1));
        if (requestedMonth == null) {
            return calculateCurrentMonth(group);
        }
        return clamp(requestedMonth, 1, duration);
    }

    private LocalDate monthDate(ChittGroup group, int monthIndex) {
        LocalDate startMonth = group.getStartMonth() == null
                ? LocalDate.now().withDayOfMonth(1)
                : group.getStartMonth().withDayOfMonth(1);
        return startMonth.plusMonths(monthIndex - 1L);
    }

    private int monthIndex(ChittGroup group, LocalDate monthDate) {
        if (monthDate == null || group.getStartMonth() == null) {
            return 0;
        }
        LocalDate startMonth = group.getStartMonth().withDayOfMonth(1);
        return (int) ChronoUnit.MONTHS.between(startMonth, monthDate.withDayOfMonth(1)) + 1;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private double defaultDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private int defaultInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }
}
