package com.chitfund.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "month"})
})
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long groupId;

    private Integer month; // month index (1,2,3...)

    private Long winnerMemberId;

    private Double payoutAmount;

    private Double profit;

    private LocalDateTime createdAt = LocalDateTime.now();
}