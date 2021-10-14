package com.myParty.repositories;

import com.myParty.models.Party;
import org.springframework.data.jpa.repository.JpaRepository;

//changed from PartyRepository --> Party
public interface PartyRepository extends JpaRepository<Party, Long> {
    Party getByUrlKey(String urlKey);
}
