package com.chitfund.service;

import com.chitfund.entity.Member;
import com.chitfund.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository repo;
    private final AuditService auditService;

    public MemberService(MemberRepository repo, AuditService auditService) {
        this.repo = repo;
        this.auditService = auditService;
    }

    public Member addMember(Member m) {
        Member saved = repo.save(m);
        auditService.record("MEMBER_CREATED", "Member", saved.getId(), "name=" + saved.getName() + ", phone=" + saved.getPhone());
        return saved;
    }

    public List<Member> getAll() {
        return repo.findByIsDeletedFalse();
    }

    public void softDelete(Long id) {
        Member member = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        member.setIsDeleted(true);
        repo.save(member);
        auditService.record("MEMBER_SOFT_DELETED", "Member", id, "name=" + member.getName());
    }
}
