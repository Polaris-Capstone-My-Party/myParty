package com.myParty.repositories;

import com.myParty.models.PartyMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {
    PartyMember getByPartyMemberKey(String partyMemberKey);
}
