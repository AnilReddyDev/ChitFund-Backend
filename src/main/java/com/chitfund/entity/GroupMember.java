// entity/GroupMember.java
package com.chitfund.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "member_id"})
})
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Positive
    private Long groupId;

    @NotNull
    @Positive
    private Long memberId;

    private LocalDate joinDate = LocalDate.now();

    private Boolean isDeleted = false;
}
