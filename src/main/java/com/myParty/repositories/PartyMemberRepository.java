package com.myParty.repositories;

import com.myParty.models.Party;
import com.myParty.models.PartyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {
    List<PartyMember> getByParty(Party party);
}
