package com.myParty.repositories;

import com.myParty.models.Member;
import com.myParty.models.Party;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByUsername(String username);
    Member getByUsername(String username);
    Member getByEmail(String email);
    Member getById(String id);
    Member findByResetToken(String token);
}
