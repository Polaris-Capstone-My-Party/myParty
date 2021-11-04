package com.myParty.repositories;

import com.myParty.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    Guest getByGuestKey(String guestKey);
    List<Guest> getByParty(Party party);

}
