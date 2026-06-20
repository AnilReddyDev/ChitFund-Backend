// service/GroupMemberService.java
package com.chitfund.service;

import com.chitfund.audit.Auditable;
import com.chitfund.entity.AuditAction;
import com.chitfund.entity.GroupMember;
import com.chitfund.repository.GroupMemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupMemberService {

    private final GroupMemberRepository repo;

    public GroupMemberService(GroupMemberRepository repo) {
        this.repo = repo;
    }

    @Auditable(action = AuditAction.CREATE, entityType = "GroupMember", entityClass = GroupMember.class)
    public GroupMember addMemberToGroup(GroupMember gm) {
        if (gm.getGroupId() == null || gm.getGroupId() <= 0 || gm.getMemberId() == null || gm.getMemberId() <= 0) {
            throw new IllegalArgumentException("Group and member are required");
        }
        return repo.save(gm);
    }

    public List<GroupMember> getMembers(Long groupId) {
        return repo.findByGroupIdAndIsDeletedFalse(groupId);
    }

    @Auditable(action = AuditAction.DELETE, entityType = "GroupMember", entityClass = GroupMember.class)
    public void softDelete(Long id) {
        GroupMember groupMember = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Group member assignment not found"));
        groupMember.setIsDeleted(true);
        repo.save(groupMember);
    }
}
