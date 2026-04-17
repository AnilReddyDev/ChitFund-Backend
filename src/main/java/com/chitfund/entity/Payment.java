package com.chitfund.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"group_id", "member_id", "month"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 👉 column names must match DB
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "member_id")
    private Long memberId;

    // 🔥 changed to LocalDate
    private LocalDate month;

    private Double amount;

    private Boolean isPaid = true;

    private String paymentMode;
    private String transactionId;

    private LocalDateTime paymentDate = LocalDateTime.now();

    private Boolean isDeleted = false;
}