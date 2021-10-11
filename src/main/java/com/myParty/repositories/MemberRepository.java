package com.myParty.repositories;

import com.myParty.models.Member;
import org.springframework.data.jpa.repository.JpaRepository;

//changed MemberRepository to Member
public interface MemberRepository extends JpaRepository<Member, Long> {
}
