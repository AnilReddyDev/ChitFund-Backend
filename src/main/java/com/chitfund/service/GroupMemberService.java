// service/GroupMemberService.java
package com.chitfund.service;

import com.chitfund.entity.GroupMember;
import com.chitfund.repository.GroupMemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupMemberService {

    private final GroupMemberRepository repo;
    private final AuditService auditService;

    public GroupMemberService(GroupMemberRepository repo, AuditService auditService) {
        this.repo = repo;
        this.auditService = auditService;
    }

    public GroupMember addMemberToGroup(GroupMember gm) {
        if (gm.getGroupId() == null || gm.getGroupId() <= 0 || gm.getMemberId() == null || gm.getMemberId() <= 0) {
            throw new IllegalArgumentException("Group and member are required");
        }
        GroupMember saved = repo.save(gm);
        auditService.record(
                "MEMBER_ASSIGNED",
                "GroupMember",
                saved.getId(),
                "groupId=" + saved.getGroupId() + ", memberId=" + saved.getMemberId()
        );
        return saved;
    }

    public List<GroupMember> getMembers(Long groupId) {
        return repo.findByGroupIdAndIsDeletedFalse(groupId);
    }

    public void softDelete(Long id) {
        GroupMember groupMember = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Group member assignment not found"));
        groupMember.setIsDeleted(true);
        repo.save(groupMember);
        auditService.record(
                "MEMBER_UNASSIGNED",
                "GroupMember",
                id,
                "groupId=" + groupMember.getGroupId() + ", memberId=" + groupMember.getMemberId()
        );
    }
}
