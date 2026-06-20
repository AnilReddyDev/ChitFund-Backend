package com.chitfund.controller;

import com.chitfund.entity.GroupMember;
import com.chitfund.service.GroupMemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// controller/GroupMemberController.java
@RestController
@RequestMapping("/api/group-members")
@Validated
public class GroupMemberController {

    private final GroupMemberService service;

    public GroupMemberController(GroupMemberService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public GroupMember add(@Valid @RequestBody GroupMember gm) {
        return service.addMemberToGroup(gm);
    }

    @GetMapping("/{groupId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<GroupMember> get(@PathVariable @Positive Long groupId) {
        return service.getMembers(groupId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable @Positive Long id) {
        service.softDelete(id);
    }
}
