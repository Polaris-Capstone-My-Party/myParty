package com.myParty.controllers;

import com.myParty.models.*;
import com.myParty.repositories.GuestRepository;
import com.myParty.repositories.ItemBringerRepository;
import com.myParty.repositories.PartyItemRepository;
import com.myParty.repositories.PartyRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@Controller
public class GuestController {

    private final PartyRepository partyDAO;
    private final GuestRepository guestDAO;
    private final PartyItemRepository partyItemDAO;
    private final ItemBringerRepository itemBringerDAO;


    public GuestController(PartyRepository partyDAO, GuestRepository guestDAO, PartyItemRepository partyItemDAO, ItemBringerRepository itemBringerDAO){
        this.partyDAO = partyDAO;
        this.guestDAO = guestDAO;
        this.partyItemDAO = partyItemDAO;
        this.itemBringerDAO = itemBringerDAO;
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
        return "guests/rsvp";
    }

    //saves Guest information
    @PostMapping(path = "/rsvp/{urlKey}")
    public String createGuest(@PathVariable String urlKey, @ModelAttribute Guest guest, @RequestParam String rsvp){
        guest.setRsvpStatus(RsvpStatuses.valueOf(rsvp)); //set RSVP status enum
        guest.setParty(partyDAO.getByUrlKey(urlKey)); //sets Party linked to guest

        UUID uuid = UUID.randomUUID(); // creates UUID unique for given guest
        guest.setGuestKey(uuid.toString()); //https://www.baeldung.com/java-uui

        guestDAO.save(guest); //save guest information

        return "redirect:/rsvp/" + urlKey + "/" + guest.getGuestKey() + "/items";
    }

    //shows Item sign up page
    @GetMapping(path = "/rsvp/{urlKey}/{guestKey}/items")
    public String showItemSignup(@PathVariable String urlKey, @PathVariable String guestKey, Model model){
        Party party = partyDAO.getByUrlKey(urlKey); // gets party info for form

        model.addAttribute("party", party); //sets party info
        model.addAttribute("guest", guestDAO.getByGuestKey(guestKey)); //sets guest info
        model.addAttribute("partyItems", partyItemDAO.getByParty(party)); //gets & sets party Items for party

        return "guests/itemSignup";
    }

    //saves Item Bringer info
    @PostMapping(path = "/rsvp/{urlKey}/{guestKey}/items")
    public String createItemBringer(@PathVariable String urlKey, @PathVariable String guestKey, @RequestParam(name="partyItem[]") String[] myPartyItems, @RequestParam(name="quantity[]") String[] quantities) {

        Guest guest = guestDAO.getByGuestKey(guestKey); //gets guest object

        for(int i = 0; i < myPartyItems.length; i++){

            if(quantities[i].equals("0")){ //if quantity is 0, no need to create Item Bringer instance
                continue;
            }

            ItemBringer itemBringer = new ItemBringer(); //new instance of Item Bringer
            PartyItem partyItem = partyItemDAO.getById(Long.valueOf(myPartyItems[i])); //get partyItem object

            itemBringer.setGuest(guest); //sets guest object
            itemBringer.setQuantity(Long.valueOf(quantities[i])); //sets quantity
            itemBringer.setPartyItem(partyItem); // sets partyItem object
            itemBringerDAO.save(itemBringer); // saves item bringer

            //TODO: Add error message to avoid negative values in the database (someone signs up for stuff before you submit)
            Long currentQuantityRequired = partyItem.getQuantityRequired(); //constant of current quantity required for partyItem
            Long updatedQuantity = currentQuantityRequired - itemBringer.getQuantity(); //constant of current quantity required - quantity guest/itemBringer signed up for
            partyItem.setQuantityRequired(updatedQuantity); //set updated quantityRequired
            partyItemDAO.save(partyItem); //save new partyItem instance
        }

        return "redirect:/guests/successRsvp";
    }

    //shows RSVPSuccess page
    @GetMapping(path = "/guests/successRsvp")
    public String showRSVPSuccess(){
        return "guests/successRsvp";
    }
}
