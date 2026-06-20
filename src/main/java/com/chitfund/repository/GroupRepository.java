package com.chitfund.repository;

import com.chitfund.entity.ChittGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<ChittGroup, Long> {
    List<ChittGroup> findByIsDeletedFalse();
}
