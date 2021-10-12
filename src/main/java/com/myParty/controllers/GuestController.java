package com.myParty.controllers;

import com.myParty.models.Guest;
import com.myParty.repositories.GuestRepository;
import com.myParty.repositories.MemberRepository;
import com.myParty.repositories.PartyRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class GuestController {

    private final PartyRepository partyDAO;
    private final GuestRepository guestDAO;

    public GuestController(PartyRepository partyDAO, GuestRepository guestDAO){
        this.partyDAO = partyDAO;
        this.guestDAO = guestDAO;
    }

    //show RSVP form with corresponding Party Information
    @GetMapping(path = "/rsvp/{url_key}")
    public String showRSVP(@PathVariable String url_key, Model model){
        model.addAttribute("party", partyDAO.getByUrl_Key(url_key));
        model.addAttribute("guest", new Guest());
        return "guests/rsvp";
    }

    //saves Guest information
    @PostMapping(path = "/rsvp/{url_key}")
    public String createGuest(@ModelAttribute Guest guest){
        guestDAO.save(guest);
        return "guests/successRsvp";
    }






}
