package com.chitfund.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChittGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double totalAmount;
    private Double monthlyPremium;
    private Integer totalMembers;
    private Integer currentMonth = 1;
    private Integer duration;
    private LocalDate startMonth;

    private Boolean isActive = true;
    private Boolean isDeleted = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}