package com.myParty.repositories;

import com.myParty.models.Member;
import com.myParty.models.Party;
import com.myParty.models.PartyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {
    PartyMember getByPartyMemberKey(String partyMemberKey);
    List<PartyMember> getByParty(Party party);
    PartyMember getByMember(Member member);

}
