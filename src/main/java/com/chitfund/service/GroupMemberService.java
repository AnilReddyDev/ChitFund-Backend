// service/GroupMemberService.java
package com.chitfund.service;

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

    public GroupMember addMemberToGroup(GroupMember gm) {
        return repo.save(gm);
    }

    public List<GroupMember> getMembers(Long groupId) {
        return repo.findByGroupIdAndIsDeletedFalse(groupId);
    }
}