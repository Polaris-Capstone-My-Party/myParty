package com.myParty.repositories;

import com.myParty.models.Party;
import com.myParty.models.PartyItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PartyItemRepository extends JpaRepository<PartyItem, Long> {
    List<PartyItem> getByParty(Party party);
}
