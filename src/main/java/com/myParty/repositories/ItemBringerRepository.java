package com.myParty.repositories;

import com.myParty.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemBringerRepository extends JpaRepository<ItemBringer, Long> {
    List<ItemBringer> getByGuest(Guest guest);
    List<ItemBringer> getByPartyItem(PartyItem partyItem);
    List<ItemBringer> getByPartyMember(PartyMember partyMember);
}