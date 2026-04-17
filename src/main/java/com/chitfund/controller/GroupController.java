package com.chitfund.controller;

import com.chitfund.entity.ChittGroup;
import com.chitfund.service.GroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService service;

    public GroupController(GroupService service) {
        this.service = service;
    }

    @PostMapping
    public ChittGroup create(@RequestBody ChittGroup group) {
        return service.createGroup(group);
    }

    @GetMapping
    public List<ChittGroup> getAll() {
        return service.getAll();
    }
}