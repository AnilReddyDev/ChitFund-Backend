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

    public GroupService(GroupRepository groupRepo,
                        PayoutScheduleRepository payoutRepo) {
        this.groupRepo = groupRepo;
        this.payoutRepo = payoutRepo;
    }

    public ChittGroup createGroup(ChittGroup group) {

        // ✅ 1. Save group
        ChittGroup savedGroup = groupRepo.save(group);

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
        return groupRepo.findAll();
    }
}