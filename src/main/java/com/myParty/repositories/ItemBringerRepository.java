package com.myParty.repositories;

import com.myParty.models.Guest;
import com.myParty.models.ItemBringer;
import com.myParty.models.Party;
import com.myParty.models.PartyItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemBringerRepository extends JpaRepository<ItemBringer, Long> {
    List<ItemBringer> getByGuest(Guest guest);
    List<ItemBringer> getByPartyItem(PartyItem partyItem);
}