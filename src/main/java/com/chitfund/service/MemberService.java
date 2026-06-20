package com.chitfund.service;

import com.chitfund.audit.Auditable;
import com.chitfund.entity.AuditAction;
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

    @Auditable(action = AuditAction.CREATE, entityType = "Member", entityClass = Member.class)
    public Member addMember(Member m) {
        return repo.save(m);
    }

    public List<Member> getAll() {
        return repo.findByIsDeletedFalse();
    }

    @Auditable(action = AuditAction.DELETE, entityType = "Member", entityClass = Member.class)
    public void softDelete(Long id) {
        Member member = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        member.setIsDeleted(true);
        repo.save(member);
    }
}
