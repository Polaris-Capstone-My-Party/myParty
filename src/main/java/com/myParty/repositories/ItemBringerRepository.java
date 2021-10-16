package com.myParty.repositories;

import com.myParty.models.Guest;
import com.myParty.models.ItemBringer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemBringerRepository extends JpaRepository<ItemBringer, Long> {
    List<ItemBringer> getByGuest(Guest guest);
}