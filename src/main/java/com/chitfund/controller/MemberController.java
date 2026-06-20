package com.chitfund.controller;

import com.chitfund.entity.Member;
import com.chitfund.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@Validated
public class MemberController {

    private final MemberService service;

    public MemberController(MemberService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, T(com.chitfund.security.Permission).MANAGE_MEMBERS)")
    public Member add(@Valid @RequestBody Member m) {
        return service.addMember(m);
    }

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, T(com.chitfund.security.Permission).VIEW_MEMBERS)")
    public List<Member> getAll() {
        return service.getAll();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission(authentication, T(com.chitfund.security.Permission).MANAGE_MEMBERS)")
    public void delete(@PathVariable @Positive Long id) {
        service.softDelete(id);
    }
}
