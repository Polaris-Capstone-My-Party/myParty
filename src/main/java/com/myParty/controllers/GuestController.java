package com.myParty.controllers;

import com.myParty.models.Guest;
import com.myParty.models.Party;
import com.myParty.models.RsvpStatuses;
import com.myParty.repositories.GuestRepository;
import com.myParty.repositories.ItemBringerRepository;
import com.myParty.repositories.PartyItemRepository;
import com.myParty.repositories.PartyRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.UUID;

@Controller
public class GuestController {

    private final PartyRepository partyDAO;
    private final GuestRepository guestDAO;
    private final PartyItemRepository partyItemDAO;

    public GuestController(PartyRepository partyDAO, GuestRepository guestDAO, PartyItemRepository partyItemDAO){
        this.partyDAO = partyDAO;
        this.guestDAO = guestDAO;
        this.partyItemDAO = partyItemDAO;
    }

    //show RSVP form with corresponding Party Information
    @GetMapping(path = "/rsvp/{urlKey}")
    public String showRSVP(@PathVariable String urlKey, Model model){
        ArrayList<String> rsvpStatuses = new ArrayList<>(); //list of RSVP enum values/options
        rsvpStatuses.add("yes");
        rsvpStatuses.add("maybe");
        rsvpStatuses.add("no");

        Party party = partyDAO.getByUrlKey(urlKey); // gets party info for form

        model.addAttribute("party", party); //sets party info for form
        model.addAttribute("guest", new Guest()); //thing to allow form to recognize new guest
        model.addAttribute("rsvps", rsvpStatuses); //allows access to rsvp enum in form
        model.addAttribute("partyItems", partyItemDAO.getByParty(party)); //gets & sets party Items for party
        return "guests/rsvp";
    }

    //saves Guest information //check with herman about this
    @PostMapping(path = "/rsvp/{urlKey}")
    public String createGuest(@PathVariable String urlKey, @ModelAttribute Guest guest, @RequestParam String rsvp){
        guest.setRsvpStatus(RsvpStatuses.valueOf(rsvp)); //set RSVP status enum
        guest.setParty(partyDAO.getByUrlKey(urlKey)); //sets Party linked to guest

        UUID uuid = UUID.randomUUID(); // creates UUID unique for given guest
        guest.setGuestKey(uuid.toString()); //https://www.baeldung.com/java-uui

        guestDAO.save(guest); //save guest information

        return "redirect:/guests/successRsvp";
    }

    //shows RSVPSuccess page
    @GetMapping(path = "/guests/successRsvp")
    public String showRSVPSuccess(){
        return "guests/successRsvp";
    }

    //TODO: Create New instance of Item Bringer to link guest & Items they've signed up for?  //make nullable, be separate part
    //TODO: Update Party Items after guests sign up for parties

    //Shows Guest Info & Allows to Edit
    @GetMapping(path = "/rsvp/{urlKey}/{guestKey}/edit")
    public String showEditRSVP(@PathVariable String urlKey, @PathVariable String guestKey, Model model){
        model.addAttribute("party", partyDAO.getByUrlKey(urlKey)); //get party info
        model.addAttribute("guest", guestDAO.getByGuestKey(guestKey)); //get guest info
        //TODO: Should a new instance of guest be allowed or just save it?
        return "guests/editRsvp";
    }

    //saves Guest edited information
    @PostMapping(path = "/rsvp/{urlKey}/{guestKey}/edit")
    public String saveEditRSVP(@PathVariable String urlKey, @PathVariable String guestKey){
        //TODO: save updated guest info
        //TODO: Update ItemBringer information (?)
        //TODO: Update party items database
        return "guests/successRsvp";
    }
}
