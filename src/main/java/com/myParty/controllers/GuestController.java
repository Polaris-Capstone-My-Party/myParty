package com.myParty.controllers;

import com.myParty.models.*;
import com.myParty.repositories.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;


@Controller
public class GuestController {

    private final PartyRepository partyDAO;
    private final GuestRepository guestDAO;
    private final PartyItemRepository partyItemDAO;
    private final ItemBringerRepository itemBringerDAO;
    private final MemberRepository memberDao;

    public GuestController(PartyRepository partyDAO, GuestRepository guestDAO, PartyItemRepository partyItemDAO, ItemBringerRepository itemBringerDAO, MemberRepository memberDao){
        this.partyDAO = partyDAO;
        this.guestDAO = guestDAO;
        this.partyItemDAO = partyItemDAO;
        this.itemBringerDAO = itemBringerDAO;
        this.memberDao = memberDao;
    }

    //show RSVP form with corresponding Party & Party Item Information
    @GetMapping(path = "/rsvp/{urlKey}")
    public String showRSVP(@PathVariable String urlKey, Model model){
        ArrayList<String> rsvpStatuses = new ArrayList<>(); //list of RSVP enum values/options
        rsvpStatuses.add("yes");
        rsvpStatuses.add("maybe");
        rsvpStatuses.add("no");

        Party party = partyDAO.getByUrlKey(urlKey); // gets party info for form

        List<PartyItem> partyItems = partyItemDAO.getByParty(party); //gets item associated with party
        List<Long> quantities = calculateQuantity(partyItems); //gets dynamic quantities left of each party
        for(int i =0; i < partyItems.size(); i++){
            partyItems.get(i).setQuantityRequired(quantities.get(i)); //sets partyItemQuantity on form to be whatever quantity is left
        }

        model.addAttribute("party", party); //sets party info for form
        model.addAttribute("rsvps", rsvpStatuses); //allows access to rsvp enum in form
        model.addAttribute("partyItems", partyItems); //sets partyItem info form

        //Checks if Member is logged in or not
        if(!SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().equals("anonymousUser")){
            Member userInSession = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); //get member in session
            Member actualMember = memberDao.getById(userInSession.getId()); //get member info associated with member in session
            model.addAttribute("member", actualMember); //sets member info for prefilled in stuff
            model.addAttribute("partyMember", new PartyMember()); //allows form to recognize new guest
        }

        model.addAttribute("guest", new Guest()); //thing to allow form to recognize new guest
        return "guests/rsvp";
    }

    //saves Guest & ItemBringer information
    @PostMapping(path = "/rsvp/{urlKey}")
    public String createGuest(@PathVariable String urlKey, @ModelAttribute Guest guest, @RequestParam String rsvp,  @RequestParam(name="partyItem[]") String[] myPartyItems, @RequestParam(name="quantity[]") String[] quantities){
         //TODO: Double Check RSVP works
        guest.setRsvpStatus(RsvpStatuses.valueOf(rsvp)); //set RSVP status enum
        guest.setParty(partyDAO.getByUrlKey(urlKey)); //sets Party linked to guest

        UUID uuid = UUID.randomUUID(); // creates UUID unique for given guest
        guest.setGuestKey(uuid.toString()); //https://www.baeldung.com/java-uui
        Guest guest1 =  guestDAO.save(guest); //save guest information & creates item to reference

        for(int i = 0; i < myPartyItems.length; i++){ //goes through partyItems guest submitted

            if(quantities[i].equals("0")){ //if quantity is 0, no need to create Item Bringer instance
                continue;
            }

            ItemBringer itemBringer = new ItemBringer(); //new instance of Item Bringer
            PartyItem partyItem = partyItemDAO.getById(Long.valueOf(myPartyItems[i])); //get partyItem object by id

            itemBringer.setQuantity(Long.valueOf(quantities[i])); //sets quantity
            itemBringer.setGuest(guest1); //sets guest object
            itemBringer.setPartyItem(partyItem); // sets partyItem object
            itemBringerDAO.save(itemBringer); // saves item bringer
            //TODO: Add error message to avoid negative values in the database (someone signs up for stuff before you submit)
        }

        return  "redirect:/guests/successRsvp/" + urlKey + "/" + uuid;
    }

    //shows RSVPSuccess page
    @GetMapping(path = "/guests/successRsvp/{urlKey}/{guestKey}")
    public String showRSVPSuccess(Model model, @PathVariable String guestKey, @PathVariable String urlKey){
        model.addAttribute("urlKey", urlKey);
        model.addAttribute("guestKey", guestKey);
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
        //TODO: Add error message to avoid negative values in the database (someone signs up for stuff before you submit)
        if(itemBringer != null){
            for(int i = 0; i < itemBringer.length; i++){ //updates itemBringer quantity
                ItemBringer updatedItemBringer = itemBringerDAO.getById(Long.valueOf(itemBringer[i])); //get itemBringer object associated w/ itemBringerID
                updatedItemBringer.setQuantity((Long.valueOf(quantities[i]))); //sets updated quantity
                itemBringerDAO.save(updatedItemBringer); //saves & updates quantity for ItemBringer
            }
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
