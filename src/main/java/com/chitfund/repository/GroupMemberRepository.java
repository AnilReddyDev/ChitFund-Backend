// repository/GroupMemberRepository.java
package com.chitfund.repository;

import com.chitfund.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroupIdAndIsDeletedFalse(Long groupId);
}