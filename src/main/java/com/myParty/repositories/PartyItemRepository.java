package com.myParty.repositories;

import com.myParty.models.PartyItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyItemRepository extends JpaRepository<PartyItem, Long> {
//    PartyItem findByPartyID(Long id);
}
