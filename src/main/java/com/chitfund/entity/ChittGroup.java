package com.chitfund.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotBlank
    private String name;

    @NotNull
    @Positive
    private Double totalAmount;

    @NotNull
    @Positive
    private Double monthlyPremium;

    @NotNull
    @Positive
    private Integer totalMembers;

    @Positive
    private Integer currentMonth = 1;

    @NotNull
    @Positive
    private Integer duration;

    @NotNull
    private LocalDate startMonth;

    private Boolean isActive = true;
    private Boolean isDeleted = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
