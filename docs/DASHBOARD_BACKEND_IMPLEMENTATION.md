# Dashboard Backend Implementation

This document records the backend changes made for the dashboard redesign described in `docs/DASHBOARD_BACKEND_REQUIREMENTS.md`.

## Implemented Endpoints

### `GET /api/dashboard/summary?groupId={groupId}`

Also supports an optional selected month:

```txt
GET /api/dashboard/summary?groupId={groupId}&month={month}
```

Returns the dashboard-first payload the frontend needs without joining group, member, payment, ledger, and auction APIs manually.

Top-level response sections:

- `group`: group identity, amount settings, member counts, duration, calculated current month, and start month.
- `collection`: expected, collected, pending, rate, and total collected till now.
- `payments`: pending members for the current month and overdue members from previous months.
- `auction`: current month auction status, last auction details, and next/current auction eligibility.
- `profit`: total profit and average profit per auction.
- `health`: status, score, and reasons.
- `recommendations`: backend-authored dashboard actions for the selected month.
- `monthSummary`: compact selected-month counts and money totals for action cards.

When `month` is provided, the summary is calculated for that requested month. The requested value is clamped to the group's `1..duration` range. When `month` is omitted, the backend uses the calculated current month.

Recommendation shape:

```json
{
  "type": "COLLECT_PAYMENT",
  "priority": "HIGH",
  "title": "3 members pending payment",
  "description": "Collect 15000.0 for Month 4",
  "targetRoute": "/ledger"
}
```

Priority values are `HIGH`, `MEDIUM`, and `LOW`. The array is empty when there are no recommended actions.

Month summary shape:

```json
{
  "month": 4,
  "paidMembers": 17,
  "pendingMembers": 3,
  "overdueMembers": 2,
  "expectedAmount": 100000.0,
  "collectedAmount": 85000.0,
  "pendingAmount": 15000.0,
  "auctionStatus": "PENDING",
  "eligibleAuctionMembers": 14
}
```

### `GET /api/dashboard/trends?groupId={groupId}`

Returns chart-ready arrays:

- `collectionTrend`: month, expected amount, collected amount, and pending amount.
- `profitTrend`: month and auction profit.

### Compatibility Endpoints

The old endpoints are still available for the current frontend until it is migrated:

- `GET /api/dashboard?groupId={groupId}`
- `GET /api/dashboard/chart?groupId={groupId}`

They now derive their values from the new summary/trends implementation.

## Calculation Rules

### Current Month

`currentMonth` is calculated from `ChittGroup.startMonth` using calendar months:

- Month 1 starts at `startMonth`.
- The value is clamped to `1..duration`.
- If `startMonth` is missing, the stored `ChittGroup.currentMonth` is used as a fallback.

### Collection

Selected month collection uses the active assigned group members:

```txt
expectedThisMonth = assignedMembers * monthlyPremium
collectedThisMonth = sum(active paid payments for selected month)
pendingThisMonth = max(0, expectedThisMonth - collectedThisMonth)
collectionRate = round((collectedThisMonth / expectedThisMonth) * 100)
```

Payments are counted only when `isPaid` is true and `isDeleted` is not true.

### Pending Members

Pending members are assigned group members who do not have a paid payment record for the selected month. Each entry includes member id, name, phone, amount due, and month.

### Overdue Members

Overdue members are assigned group members who missed one or more months before the selected month. Each entry includes member id, name, phone, missed month indexes, and total amount due.

### Auction

`currentMonthAuctionStatus` is calculated for the selected month:

- `COMPLETED` when an auction exists for the selected month.
- `PENDING` otherwise.

`lastAuction` returns the latest auction up to the selected month. `nextAuction.eligibleMembers` counts assigned members who had not already won before the selected month.

`lastAuction.bidAmount` is derived from stored `Auction.profit`. The auction service now stores:

```txt
payoutAmount = group.totalAmount - bidAmount
profit = bidAmount
```

This matches the dashboard requirement where a bid of `20000` on a `1000000` group produces a payout of `980000` and profit of `20000`.

## Files Changed

- `src/main/java/com/chitfund/controller/DashboardController.java`
- `src/main/java/com/chitfund/service/DashboardService.java`
- `src/main/java/com/chitfund/service/AuctionService.java`
- `src/main/java/com/chitfund/dto/DashboardSummaryResponse.java`
- `src/main/java/com/chitfund/dto/DashboardTrendsResponse.java`

## Frontend Migration Notes

Update the dashboard page to call:

```txt
GET /dashboard/summary?groupId={groupId}
GET /dashboard/trends?groupId={groupId}
```

The frontend should no longer need separate calls to ledger, members, groups, and auctions to render the redesigned dashboard.
