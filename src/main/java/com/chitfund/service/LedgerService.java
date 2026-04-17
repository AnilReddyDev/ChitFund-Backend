package com.chitfund.service;

import com.chitfund.dto.*;
import com.chitfund.entity.*;
import com.chitfund.repository.*;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LedgerService {

    private final GroupMemberRepository gmRepo;
    private final MemberRepository memberRepo;
    private final PaymentRepository paymentRepo;
    private final PayoutScheduleRepository payoutRepo;

    public LedgerService(GroupMemberRepository gmRepo,
                         MemberRepository memberRepo,
                         PaymentRepository paymentRepo,
                         PayoutScheduleRepository payoutRepo) {
        this.gmRepo = gmRepo;
        this.memberRepo = memberRepo;
        this.paymentRepo = paymentRepo;
        this.payoutRepo = payoutRepo;
    }

    public LedgerFullResponse getFullLedger(Long groupId) {

        // ✅ 1. Get months from DB
        List<PayoutSchedule> schedules =
                payoutRepo.findByGroupIdOrderByMonthIndexAsc(groupId);
        System.out.println("Schedules: " + schedules.size());
        List<String> months = schedules.stream()
                .map(s -> s.getMonthDate().toString())
                .collect(Collectors.toList());

        // ✅ 2. Get group members
        List<GroupMember> gmList =
                gmRepo.findByGroupIdAndIsDeletedFalse(groupId);

        // ✅ 3. Get all payments
        List<Payment> payments = paymentRepo.findByGroupId(groupId);

        // Map for fast lookup
        Set<String> paidSet = new HashSet<>();
        for (Payment p : payments) {
            paidSet.add(
                    p.getMemberId() + "_" + p.getMonth().toString()
            );
        }

        // ✅ 4. Build response
        List<MemberLedgerDTO> memberDTOs = new ArrayList<>();

        for (GroupMember gm : gmList) {

            Member m = memberRepo.findById(gm.getMemberId()).orElse(null);

            List<PaymentStatusDTO> paymentList = new ArrayList<>();

            for (String month : months) {
                boolean paid = paidSet.contains(
                        gm.getMemberId() + "_" + month
                );

                paymentList.add(new PaymentStatusDTO(month, paid));
            }

            if (m != null) {
                memberDTOs.add(
                        new MemberLedgerDTO(
                                m.getId(),
                                m.getName(),
                                paymentList
                        )
                );
            }
        }

        return new LedgerFullResponse(months, memberDTOs);
    }
}