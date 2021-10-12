package com.myParty.controllers;

import com.myParty.models.Location;
import com.myParty.models.Member;
import com.myParty.models.Party;
import com.myParty.repositories.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final PartyRepository partyDAO;
    private final GuestRepository guestDAO;
    private final PartyItemRepository partyItemDAO;
    private final MemberRepository memberDAO;
    private final LocationRepository locationDAO;

    public HomeController(PartyRepository partyDAO, GuestRepository guestDAO, PartyItemRepository partyItemDAO, MemberRepository memberDAO, LocationRepository locationDAO) {
        this.partyDAO = partyDAO;
        this.guestDAO = guestDAO;
        this.partyItemDAO = partyItemDAO;
        this.memberDAO = memberDAO;
        this.locationDAO = locationDAO;
    }

    @GetMapping(path = "/createMember")
    public String createMember(){
        Member member = new Member();
        member.setUsername("aa");
        member.setEmail("aa@aa.com");
        member.setPassword("aa");
        member.setPhone(1234567890L);
        member.setFirst_name("Calais");
        member.setLast_name("Galbraith");
        memberDAO.save(member);
        return "createMember";
    }

    @GetMapping(path = "/createLocation")
    public String createLocation(){
        Location location = new Location();
        location.setAddress_one("address");
        location.setCity("city");
        location.setState("state");
        location.setState("zipcode");
        locationDAO.save(location);
        return "createLocation";
    }

    @GetMapping(path = "/createParty")
    public String createParty(){
        Party party = new Party();
        party.setTitle("title");
        party.setDescription("description");
        party.setUrl_key("url");
        party.setLocation(locationDAO.getById(1L));
        party.setOwner(memberDAO.getById(1L));
        partyDAO.save(party);
        return "createParty";
    }


}
