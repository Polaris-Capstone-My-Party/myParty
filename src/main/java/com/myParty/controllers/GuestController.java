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
import java.util.List;
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
        List<PartyItem> partyItems = partyItemDAO.getByParty(party);

        List<Long> quantities = calculateQuantity(partyItems); //gets dynamic quantities left of each party
        for(int i =0; i < partyItems.size(); i++){
            partyItems.get(i).setQuantityRequired(quantities.get(i)); //sets partyItemQuantity on form to be whatever quantity is left
        }

        model.addAttribute("party", party); //sets party info
        model.addAttribute("guest", guestDAO.getByGuestKey(guestKey)); //sets guest info
        model.addAttribute("partyItems", partyItems); //gets & sets party Items for party
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
        }
        return "redirect:/guests/successRsvp";
    }

    //shows RSVPSuccess page
    @GetMapping(path = "/guests/successRsvp")
    public String showRSVPSuccess(){
        return "guests/successRsvp";
    }

    //Shows Guest Info & Allows to Edit
    @GetMapping(path = "/rsvp/{urlKey}/{guestKey}/edit")
    public String showEditRSVP(@PathVariable String urlKey, @PathVariable String guestKey, Model model){
        ArrayList<String> rsvpStatuses = new ArrayList<>(); //list of RSVP enum values/options
        rsvpStatuses.add("yes");
        rsvpStatuses.add("maybe");
        rsvpStatuses.add("no");

        Party party = partyDAO.getByUrlKey(urlKey);
        Guest guest = guestDAO.getByGuestKey(guestKey);
        List<PartyItem> partyItems = partyItemDAO.getByParty(party);
        List<ItemBringer> itemBringers = itemBringerDAO.getByGuest(guest); //gets & sets list of item bringers associated w/ guest

        List<Long> quantities = calculateQuantity(partyItems); //gets dynamic quantities left of each party
        for(int i =0; i < partyItems.size(); i++){
            partyItems.get(i).setQuantityRequired(quantities.get(i)); //sets partyItemQuantity on form to be whatever quantity is left
        }

        //TODO: set default RSVP status to be the one currently
        //TODO: if quantity = 0, do not show?
        model.addAttribute("party", party); //get party info
        model.addAttribute("guest", guest); //get guest info
        model.addAttribute("rsvps", rsvpStatuses); //allows access to rsvp enum in form
        model.addAttribute("itemBringers", itemBringers); //gets ItemBringer info associated with guestId
        model.addAttribute("partyItems", partyItems); //gets & sets partyItems for party
        return "guests/editRsvp";
    }

    //saves Guest edited information
    @PostMapping(path = "/rsvp/{urlKey}/{guestKey}/edit")
    public String saveEditRSVP(@ModelAttribute Guest guest, @RequestParam String rsvp, @RequestParam(name="itemBringer[]") String[] itemBringer, @RequestParam(name="quantity[]") String[] quantities,
                               @RequestParam(name="partyItem[]") String[] partyItem, @PathVariable String urlKey){
        guest.setRsvpStatus(RsvpStatuses.valueOf(rsvp));
        guest.setParty(partyDAO.getByUrlKey(urlKey));
        guestDAO.save(guest); //save guest information

        //TODO: Error message, something to check this bc if no items, then gives error
        for(int i = 0; i < itemBringer.length; i++){ //updates itemBringer quantity
            //TODO: Add error message to avoid negative values in the database (someone signs up for stuff before you submit)
            ItemBringer updatedItemBringer = itemBringerDAO.getById(Long.valueOf(itemBringer[i])); //get itemBringer object associated w/ itemBringerID
            updatedItemBringer.setQuantity((Long.valueOf(quantities[i]))); //sets updated quantity
            itemBringerDAO.save(updatedItemBringer); //saves & updates quantity for ItemBringer
        }
        return "redirect:/guests/successRsvp";
    }

    //calculates actual quantity remaining
    public List<Long> calculateQuantity(List<PartyItem> partyItems){ //takes in List of partyItems
        List<Long> totalQuantity= new ArrayList<>(); //list to store total quantity being brought of each partyItem for a party
        Long quantityGuestsBringing = Long.valueOf(0);

        for (PartyItem partyItem: partyItems){ //run through each partyItem in partyItem array
            List<ItemBringer> itemBringers = itemBringerDAO.getByPartyItem(partyItem); //gets list of item bringers associated w/ party item
            quantityGuestsBringing = Long.valueOf(0); //reset quantity to 0

            for (ItemBringer itemBringer: itemBringers) { //run though each ItemBringer associated w/ partyItem
                quantityGuestsBringing += itemBringer.getQuantity(); //tallies quantities of each itemBringer for specific partyItem
            }

            Long actualQuantity = partyItem.getQuantityRequired() - quantityGuestsBringing;
            totalQuantity.add(actualQuantity); //add tally of quantities to totalQuantities array
        }
        return totalQuantity;
    }
}
