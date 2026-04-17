package com.chitfund.controller;

import com.chitfund.entity.Member;
import com.chitfund.service.MemberService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService service;

    public MemberController(MemberService service) {
        this.service = service;
    }

    @PostMapping
    public Member add(@RequestBody Member m) {
        return service.addMember(m);
    }

    @GetMapping
    public List<Member> getAll() {
        return service.getAll();
    }
}