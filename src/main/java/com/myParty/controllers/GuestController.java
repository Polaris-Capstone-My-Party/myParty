package com.myParty.controllers;

import com.myParty.models.Guest;
import com.myParty.repositories.GuestRepository;
import com.myParty.repositories.PartyItemRepository;
import com.myParty.repositories.PartyRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class GuestController {

//    private final PartyRepository partyDAO;
//    private final GuestRepository guestDAO;
//    private final PartyItemRepository partyItemDAO;
//
//    public GuestController(PartyRepository partyDAO, GuestRepository guestDAO, PartyItemRepository partyItemDAO) {
//        this.partyDAO = partyDAO;
//        this.guestDAO = guestDAO;
//        this.partyItemDAO = partyItemDAO;
//    }
//
//    //show form for RSVPing
//    @GetMapping(path = "/rsvp/{partyID}") //TODO: change to party identifying url link when logic done
//    public String showRSVPToGuest(@PathVariable long partyID, Model model) {
//        model.addAttribute("guest", new Guest()); //create new Guest object for form
//        model.addAttribute("party", partyDAO.getById(partyID)); //gets & sets party info to view on form
//
//
//
//
//        //TODO: get & set party items
//        return "/guests/rsvp";
//    }
//
//    //create guest when submitting RSVP form
//    @PostMapping(path = "/rsvp/{partyID}")
//    public String saveGuestInfo(@ModelAttribute Guest guest, Model model) {
//            guestDAO.save(guest); //Create new guest instance
//            model.addAttribute("guest", guest);
//
//
//
//
//            //todo save party items via list?
//            //todo update partyItems quantity required list
//            //todo generate random url link mapped to guest
//        return "/guests/successRsvp";
//    }


}
