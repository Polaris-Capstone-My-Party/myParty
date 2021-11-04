package com.myParty.controllers;

import com.myParty.models.*;
import com.myParty.repositories.*;
import com.myParty.services.EmailService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.util.*;

@Controller
public class GuestController {

    private final PartyRepository partyDAO;
    private final GuestRepository guestDAO;
    private final PartyItemRepository partyItemDAO;
    private final ItemBringerRepository itemBringerDAO;
    private final MemberRepository memberDao;
    private final PartyMemberRepository partyMemberDao;
    private final EmailService emailService;

    public GuestController(PartyRepository partyDAO, GuestRepository guestDAO, PartyItemRepository partyItemDAO, ItemBringerRepository itemBringerDAO, MemberRepository memberDao, PartyMemberRepository partyMemberDao, EmailService emailService){
        this.partyDAO = partyDAO;
        this.guestDAO = guestDAO;
        this.partyItemDAO = partyItemDAO;
        this.itemBringerDAO = itemBringerDAO;
        this.memberDao = memberDao;
        this.partyMemberDao = partyMemberDao;
        this.emailService = emailService;
    }

    //show RSVP form with corresponding Party & Party Item Information
    @GetMapping(path = "/rsvp/{urlKey}")
    public String showRSVP(@PathVariable String urlKey, Model model){
        ArrayList<String> rsvpStatuses = getRsvpStatuses(); //list of RSVP enum values/options
        ArrayList<String> additionalGuests = getAdditionalGuests(); //list of RSVP enum values/options

        Party party = partyDAO.getByUrlKey(urlKey); // gets party info for form

        List<PartyItem> partyItems = partyItemDAO.getByParty(party); //gets item associated with party
        List<Long> quantities = calculateQuantity(partyItems); //gets dynamic quantities left of each party

        HashMap<PartyItem, List<Long>> partyItemsActual= new HashMap<>(); //hashmap to store party items & list of long quantiity values
        for(int i = 0; i < partyItems.size(); i++){

            Long quantityDigit = quantities.get(i);
            partyItems.get(i).setQuantityRequired(quantityDigit);
            List<Long> quantityList = new ArrayList<>();

            for(long j = 0; j <= quantityDigit; j++){ //creates List of Longs up until quantity value
                quantityList.add(j);
            }

            partyItemsActual.put(partyItems.get(i), quantityList); //adds party item & associated quantity list to hashmap
            partyItems.get(i).setQuantityRequired(quantities.get(i)); //sets partyItemQuantity on form to be whatever quantity is left
        }

        model.addAttribute("party", party); //sets party info for form
        model.addAttribute("rsvps", rsvpStatuses); //allows access to rsvp enum in form
        model.addAttribute("additionalGuests", additionalGuests); //sets additional guests drop down
        model.addAttribute("partyItems", partyItemsActual); //sets partyItem info form
        model.addAttribute("guest", new Guest()); //thing to allow form to recognize new guest

        //Checks if Member is logged in or not
        if(!SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().equals("anonymousUser")){
            Member userInSession = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); //get member in session
            Member actualMember = memberDao.getById(userInSession.getId()); //get member info associated with member in session

            PartyMember checkMember = partyMemberDao.getByMember(actualMember);

            //check if member already has partyMember associated with account
            if(checkMember != null){
                return "redirect:/rsvp/" + urlKey + "/member/" + checkMember.getPartyMemberKey() + "/view";
            }

            //checks if member is the host of the party, redirect to view party page not RSVP
            if(actualMember == party.getOwner()){
                return "redirect:/member/" + urlKey + "/view";
            }

            model.addAttribute("member", actualMember); //sets member info for prefilled in stuff
            model.addAttribute("partyMember", new PartyMember()); //allows form to recognize new guest
        }
        return "guests/rsvp";
    }

    //saves Guest & ItemBringer information
    @PostMapping(path = "/rsvp/{urlKey}")
    public String createGuest(@PathVariable String urlKey, @ModelAttribute Guest guest, @RequestParam String rsvp,
                              @RequestParam(name="partyItem[]") String[] myPartyItems, @RequestParam(name="quantity[]") String[] quantities) throws MessagingException {

        Party party = partyDAO.getByUrlKey(urlKey); //gets party

        List<PartyItem> dbPartyItems = partyItemDAO.getByParty(party); //gets most current/up-to-date partyItems associated w/ party from db
        List<Long> updatedQuantities = calculateQuantity(dbPartyItems); //gets list of updated quantity remaining for dbPartyItems

        //checks guest is still able to sign up for that quantity
        for(int i = 0; i < quantities.length; i++){
            Long quantity = Long.valueOf(quantities[i]);

            //if quantity signing up for is greater than what is available in the db, reload page with updated info
            if(quantity > updatedQuantities.get(i)){
                System.out.println("Error: quantity signing up for is greater than available in the database");
                return "redirect:/rsvp/" + urlKey;
            }
        }

        UUID uuid = UUID.randomUUID(); // creates UUID unique for given guest

        guest.setRsvpStatus(RsvpStatuses.valueOf(rsvp)); //set RSVP status enum
        guest.setParty(party); //sets Party linked to guest
        guest.setGuestKey(uuid.toString()); //https://www.baeldung.com/java-uui
        Guest guest1 =  guestDAO.save(guest); //save guest information & creates item to reference
        String partyItemsDetails = "";

        for(int i = 0; i < myPartyItems.length; i++){ //goes through partyItems guest submitted
            ItemBringer itemBringer = new ItemBringer(); //new instance of Item Bringer
            PartyItem partyItem = partyItemDAO.getById(Long.valueOf(myPartyItems[i])); //get partyItem object by id

            itemBringer.setQuantity(Long.valueOf(quantities[i])); //sets quantity
            itemBringer.setGuest(guest1); //sets guest object
            itemBringer.setPartyItem(partyItem); // sets partyItem object
            itemBringerDAO.save(itemBringer); // saves item bringer

            partyItemsDetails += "\"Item: " + partyItem.getItem().getName() + "      Quantity: " + quantities[i] + "<br>";
        }
        String rsvpDetails =
                "<h2 style=\"color: red\">You are RSVP'd to " + party.getTitle() + "!</h2>, " +
                        "<img src=\"http://localhost:8080/img/MyParty.png\" >" +
                        "<br><br><i>Here are the details: </i><br>"
                        + "Description: " + party.getDescription() + "<br>"
                        + "Start Time: " + party.getStartTime() + "<br>"
                        + "End Time: " + party.getEndTime() + "<br>"
                        + "Location:<br>" + party.getLocation().getAddressOne() + "<br>"
                        + party.getLocation().getAddressTwo() + "<br>"
                        + party.getLocation().getCity() + " " + party.getLocation().getState() + " " + party.getLocation().getZipcode() + "<br>"
                        + "<br>You have signed up to bring the following: <br>" + partyItemsDetails + "<br>"
                        + "Additional Guests: " + guest.getAdditionalGuests() + "<br>"
                        + "View or edit your RSVP: " + "<a href=\"http://localhost:8080/rsvp/" + party.getUrlKey() + "/" + guest1.getGuestKey() + "/view" + "\">here</a>";

        emailService.sendRSVPConfirmGuest(guest, "Your RSVP to " + party.getTitle(), rsvpDetails);

        return  "redirect:/guests/successRsvp/" + urlKey + "/" + uuid;
    }

    //shows RSVPSuccess page
    @GetMapping(path = "/guests/successRsvp/{urlKey}/{guestKey}")
    public String showRSVPSuccess(Model model, @PathVariable String guestKey, @PathVariable String urlKey){
        model.addAttribute("urlKey", urlKey);
        model.addAttribute("guestKey", guestKey);
        return "guests/successRsvp";
    }

    //shows RSVP Information to guests
    @GetMapping(path = "/rsvp/{urlKey}/{guestKey}/view")
    public String showRSVPInfo(Model model, @PathVariable String urlKey, @PathVariable String guestKey){

        //gets RSVP info
        Party party = partyDAO.getByUrlKey(urlKey);
        Guest guest = guestDAO.getByGuestKey(guestKey);

        List<PartyItem> partyItems = partyItemDAO.getByParty(party); //gets list of party Items w/ party
        List<ItemBringer> itemBringers = itemBringerDAO.getByGuest(guest); //gets & sets list of item bringers associated w/ guest
        List<Long> quantities = calculateQuantity(partyItems); //gets dynamic quantities left of each party
        HashMap<ItemBringer, List<Long>> itemBringerActual= getItemBringerActual(itemBringers, quantities); //hashmap to store party items & list of long quantity values

        model.addAttribute("party", party);
        model.addAttribute("guest", guest);
        model.addAttribute("itemBringers", itemBringerActual); //gets ItemBringer info associated with guestId

        return "guests/viewRsvp";
    }

    //Shows Guest Info & Allows to Edit
    @GetMapping(path = "/rsvp/{urlKey}/{guestKey}/edit")
    public String showEditRSVP(@PathVariable String urlKey, @PathVariable String guestKey, Model model){
        ArrayList<String> rsvpStatuses = getRsvpStatuses(); //list of RSVP enum values/options
        ArrayList<String> additionalGuests = getAdditionalGuests(); //list of RSVP enum values/options

        Party party = partyDAO.getByUrlKey(urlKey);
        Guest guest = guestDAO.getByGuestKey(guestKey);

        List<PartyItem> partyItems = partyItemDAO.getByParty(party); //gets list of party Items w/ party
        List<ItemBringer> itemBringers = itemBringerDAO.getByGuest(guest); //gets & sets list of item bringers associated w/ guest
        List<Long> quantities = calculateQuantity(partyItems); //gets dynamic quantities left of each party
        HashMap<ItemBringer, List<Long>> itemBringerActual= getItemBringerActual(itemBringers, quantities); //hashmap to store party items & list of long quantity values

        model.addAttribute("party", party); //get party info
        model.addAttribute("guest", guest); //get guest info
        model.addAttribute("rsvps", rsvpStatuses); //allows access to rsvp enum in form
        model.addAttribute("additionalGuests", additionalGuests); //sets additional guests drop down
        model.addAttribute("itemBringers", itemBringerActual); //gets ItemBringer info associated with guestId
        return "guests/editRsvp";
    }

    //saves Guest edited information
    @PostMapping(path = "/rsvp/{urlKey}/{guestKey}/edit")
    public String saveEditRSVP(@ModelAttribute Guest guest, @RequestParam String rsvp, @RequestParam(name="itemBringer[]") String[] itemBringer,
                               @RequestParam(name="quantity[]") String[] quantities, @RequestParam(name="partyItem[]") String[] partyItem,
                               @PathVariable String urlKey, @PathVariable String guestKey){

        Party party = partyDAO.getByUrlKey(urlKey);

        List<PartyItem> dbPartyItems = partyItemDAO.getByParty(party); //gets most current/up-to-date partyItems associated w/ party from db
        List<Long> updatedQuantities = calculateQuantity(dbPartyItems); //gets list of updated quantity remaining for dbPartyItems

        //checks guest can still sign up for items
        for(int i = 0; i < quantities.length; i++) { //updates itemBringer quantity
            Long quantity = Long.valueOf(quantities[i]);

            //if quantity signing up for is greater than what is available in the db, reload page with updated info
            if (quantity > updatedQuantities.get(i)) {
                System.out.println("Error: quantity signing up for is greater than available in the database");
                return "redirect:/rsvp/" + urlKey + "/" + guestKey + "/edit";
            }
        }

        //saves guest information
        guest.setRsvpStatus(RsvpStatuses.valueOf(rsvp));
        guest.setParty(party);
        guestDAO.save(guest);

        for(int i = 0; i < itemBringer.length; i++){ //updates itemBringer quantity
            ItemBringer updatedItemBringer = itemBringerDAO.getById(Long.valueOf(itemBringer[i])); //get itemBringer object associated w/ itemBringerID
            updatedItemBringer.setQuantity(Long.valueOf(quantities[i])); //sets updated quantity
            itemBringerDAO.save(updatedItemBringer); //saves & updates quantity for ItemBringer
        }

        return "redirect:/guests/successRsvp/" + urlKey + "/" + guestKey;
    }


    //calculates actual quantity remaining
    public List<Long> calculateQuantity(List<PartyItem> partyItems){ //takes in List of partyItems
        List<Long> totalQuantity= new ArrayList<>(); //list to store total quantity being brought of each partyItem for a party
        Long quantityGuestsBringing;

        for (PartyItem partyItem: partyItems){ //run through each partyItem in partyItem array
            List<ItemBringer> itemBringers = itemBringerDAO.getByPartyItem(partyItem); //gets list of item bringers associated w/ party item
            quantityGuestsBringing = 0L; //reset quantity to 0

            for (ItemBringer itemBringer: itemBringers) { //run though each ItemBringer associated w/ partyItem
                quantityGuestsBringing += itemBringer.getQuantity(); //tallies quantities of each itemBringer for specific partyItem
            }

            Long actualQuantity = partyItem.getQuantityRequired() - quantityGuestsBringing;
            totalQuantity.add(actualQuantity); //add tally of quantities to totalQuantities array
        }
        return totalQuantity;
    }

    //returns list of rsvp values
    public ArrayList<String> getRsvpStatuses(){
        ArrayList<String> rsvpStatuses = new ArrayList<>(); //list of RSVP enum values/options
        rsvpStatuses.add("yes");
        rsvpStatuses.add("maybe");
        rsvpStatuses.add("no");
        return rsvpStatuses;
    }

    //returns list of additional guest values
    public ArrayList<String> getAdditionalGuests(){
        ArrayList<String> additionalGuests = new ArrayList<>(); //list of RSVP enum values/options
        additionalGuests.add("0");
        additionalGuests.add("1");
        additionalGuests.add("2");
        additionalGuests.add("3");
        additionalGuests.add("4");
        additionalGuests.add("5");
        additionalGuests.add("5+");
        return additionalGuests;
    }

    //returns hashMap of ItemBringers & Updated Quantities
    public HashMap<ItemBringer, List<Long>> getItemBringerActual(List<ItemBringer> itemBringers, List<Long> quantities){

        HashMap<ItemBringer, List<Long>> itemBringerActual= new HashMap<>(); //hashmap to store party items & list of long quantity values
        for(int i = 0; i < itemBringers.size(); i++){ //loop through item bringer instances

            itemBringers.get(i).getPartyItem().setQuantityRequired(quantities.get(i)); //sets partyItemQuantity on form to be whatever quantity is left

            Long quantityDigit = quantities.get(i) + itemBringers.get(i).getQuantity(); //holds quantity remaining + quantity signed up form
            List<Long> quantityList = new ArrayList<>();

            for(long j = 0; j <= quantityDigit; j++){ //creates List of Longs up until quantity value
                quantityList.add(j);
            }

            itemBringerActual.put(itemBringers.get(i), quantityList); //adds party item & associated quantity list to hashmap
        }

        return itemBringerActual;
    }

}
