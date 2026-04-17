package com.chitfund.repository;

import com.chitfund.entity.ChittGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<ChittGroup, Long> {
}