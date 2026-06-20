package com.chitfund.controller;

import com.chitfund.entity.ChittGroup;
import com.chitfund.service.GroupService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@Validated
public class GroupController {

    private final GroupService service;

    public GroupController(GroupService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, T(com.chitfund.security.Permission).MANAGE_GROUPS)")
    public ChittGroup create(@Valid @RequestBody ChittGroup group) {
        return service.createGroup(group);
    }

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, T(com.chitfund.security.Permission).VIEW_GROUPS)")
    public List<ChittGroup> getAll() {
        return service.getAll();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission(authentication, T(com.chitfund.security.Permission).MANAGE_GROUPS)")
    public void delete(@PathVariable @Positive Long id) {
        service.softDelete(id);
    }
}
