package com.chitfund.repository;

import com.chitfund.entity.PayoutSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayoutScheduleRepository extends JpaRepository<PayoutSchedule, Long> {

    List<PayoutSchedule> findByGroupIdOrderByMonthIndexAsc(Long groupId);

}