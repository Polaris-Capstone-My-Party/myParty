package com.myParty.repositories;

import com.myParty.models.Member;
import com.myParty.models.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PartyRepository extends JpaRepository<Party, Long> {
    Party getByUrlKey(String urlKey);
    List<Party> findAllByOwner(Member owner);
}
