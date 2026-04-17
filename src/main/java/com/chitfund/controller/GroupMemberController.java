package com.chitfund.controller;

import com.chitfund.entity.GroupMember;
import com.chitfund.service.GroupMemberService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// controller/GroupMemberController.java
@RestController
@RequestMapping("/api/group-members")
public class GroupMemberController {

    private final GroupMemberService service;

    public GroupMemberController(GroupMemberService service) {
        this.service = service;
    }

    @PostMapping
    public GroupMember add(@RequestBody GroupMember gm) {
        return service.addMemberToGroup(gm);
    }

    @GetMapping("/{groupId}")
    public List<GroupMember> get(@PathVariable Long groupId) {
        return service.getMembers(groupId);
    }
}