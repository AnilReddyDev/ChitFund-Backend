package com.chitfund.service;

import com.chitfund.audit.Auditable;
import com.chitfund.entity.AuditAction;
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

    public GroupService(GroupRepository groupRepo,
                        PayoutScheduleRepository payoutRepo) {
        this.groupRepo = groupRepo;
        this.payoutRepo = payoutRepo;
    }

    @Auditable(action = AuditAction.CREATE, entityType = "ChittGroup", entityClass = ChittGroup.class)
    public ChittGroup createGroup(ChittGroup group) {
        validateGroup(group);

        ChittGroup savedGroup = groupRepo.save(group);

        List<LocalDate> months = generateMonths(
                group.getStartMonth(),
                group.getDuration()
        );

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

    @Auditable(action = AuditAction.DELETE, entityType = "ChittGroup", entityClass = ChittGroup.class)
    public void softDelete(Long id) {
        ChittGroup group = groupRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        group.setIsDeleted(true);
        group.setIsActive(false);
        groupRepo.save(group);
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
