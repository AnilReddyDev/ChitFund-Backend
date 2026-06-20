package com.chitfund.service;

import com.chitfund.entity.ChittGroup;
import com.chitfund.entity.PayoutSchedule;
import com.chitfund.repository.GroupRepository;
import com.chitfund.repository.PayoutScheduleRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class GroupService {

    private final GroupRepository groupRepo;
    private final PayoutScheduleRepository payoutRepo;
    private final AuditService auditService;

    public GroupService(GroupRepository groupRepo,
                        PayoutScheduleRepository payoutRepo,
                        AuditService auditService) {
        this.groupRepo = groupRepo;
        this.payoutRepo = payoutRepo;
        this.auditService = auditService;
    }

    public ChittGroup createGroup(ChittGroup group) {
        validateGroup(group);

        // ✅ 1. Save group
        ChittGroup savedGroup = groupRepo.save(group);
        auditService.record("GROUP_CREATED", "ChittGroup", savedGroup.getId(), "name=" + savedGroup.getName());

        // ✅ 2. Generate months
        List<LocalDate> months = generateMonths(
                group.getStartMonth(),
                group.getDuration()
        );

        // ✅ 3. Store in DB
        List<PayoutSchedule> schedules = new ArrayList<>();

        for (int i = 0; i < months.size(); i++) {
            schedules.add(new PayoutSchedule(
                    null,
                    savedGroup.getId(),
                    i + 1,
                    months.get(i)
            ));
        }

        payoutRepo.saveAll(schedules);

        return savedGroup;
    }

    // 🔥 Core logic
    public List<LocalDate> generateMonths(LocalDate start, int duration) {
        List<LocalDate> months = new ArrayList<>();

        for (int i = 0; i < duration; i++) {
            months.add(start.plusMonths(i));
        }

        return months;
    }
    public List<ChittGroup> getAll() {
        return groupRepo.findByIsDeletedFalse();
    }

    public void softDelete(Long id) {
        ChittGroup group = groupRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        group.setIsDeleted(true);
        group.setIsActive(false);
        groupRepo.save(group);
        auditService.record("GROUP_SOFT_DELETED", "ChittGroup", id, "name=" + group.getName());
    }

    private void validateGroup(ChittGroup group) {
        if (group.getTotalAmount() == null || group.getTotalAmount() <= 0
                || group.getMonthlyPremium() == null || group.getMonthlyPremium() <= 0
                || group.getTotalMembers() == null || group.getTotalMembers() <= 0
                || group.getDuration() == null || group.getDuration() <= 0
                || group.getStartMonth() == null) {
            throw new IllegalArgumentException("Group amount, premium, members, duration, and start month are required");
        }
    }
}
