package com.myParty.repositories;

import com.myParty.models.Location;
import org.springframework.data.jpa.repository.JpaRepository;

//renamed file, changed Location to LocationRepository
public interface LocationRepository extends JpaRepository<Location, Long> {

}
