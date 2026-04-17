package com.chitfund.service;

import com.chitfund.entity.Member;
import com.chitfund.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository repo;

    public MemberService(MemberRepository repo) {
        this.repo = repo;
    }

    public Member addMember(Member m) {
        return repo.save(m);
    }

    public List<Member> getAll() {
        return repo.findAll();
    }
}