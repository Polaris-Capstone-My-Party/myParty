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
public class PartyMembersController {

    private final PartyRepository partyDao;
    private final PartyItemRepository partyItemDao;
    private final ItemBringerRepository itemBringerDao;
    private final PartyMemberRepository partyMemberDao;
    private final MemberRepository memberDao;
    private final GuestController guestController;
    private final EmailService emailService;

    public PartyMembersController(PartyRepository partyDao, PartyItemRepository partyItemDao, ItemBringerRepository itemBringerDao, PartyMemberRepository partyMemberDao, MemberRepository memberDao, GuestController guestController, EmailService emailService) {
        this.partyDao = partyDao;
        this.partyItemDao = partyItemDao;
        this.itemBringerDao = itemBringerDao;
        this.partyMemberDao = partyMemberDao;
        this.memberDao = memberDao;
        this.guestController = guestController;
        this.emailService = emailService;
    }

    //saves PartyMember & ItemBringer information
    @PostMapping(path = "/rsvp/{urlKey}/{memberId}")
    public String createPartyMember(@PathVariable String urlKey, @PathVariable String memberId, @ModelAttribute PartyMember partyMember, @RequestParam String rsvp, @RequestParam(name = "partyItem[]") String[] myPartyItems, @RequestParam(name = "quantity[]") String[] quantities) throws MessagingException {

        Member member = memberDao.getById(Long.valueOf(memberId));
        Party party = partyDao.getByUrlKey(urlKey);

        List<PartyItem> dbPartyItems = partyItemDao.getByParty(party); //gets most current/up-to-date partyItems associated w/ party from db
        List<Long> updatedQuantities = guestController.calculateQuantity(dbPartyItems); //gets list of updated quantity remaining for dbPartyItems

        //checks guest is still able to sign up for that quantity
        for (int i = 0; i < quantities.length; i++) {
            Long quantity = Long.valueOf(quantities[i]);

            //if quantity signing up for is greater than what is available in the db, reload page with updated info
            if (quantity > updatedQuantities.get(i)) {
                System.out.println("Error: quantity signing up for is greater than available in the database");
                return "redirect:/rsvp/" + urlKey;
            }
        }

        UUID uuid = UUID.randomUUID(); // creates UUID unique for given partyMember
        partyMember.setPartyMemberKey(uuid.toString()); //https://www.baeldung.com/java-uui

        partyMember.setRsvpStatus(RsvpStatuses.valueOf(rsvp)); //set RSVP status enum
        partyMember.setParty(party); //sets Party linked to partyMember
        partyMember.setMember(member); //sets Member to logged in member
        PartyMember partyMember1 = partyMemberDao.save(partyMember); //saves partyMember instance
        String partyItemsDetails = "";

        for (int i = 0; i < myPartyItems.length; i++) { //goes through partyItems guest submitted

            ItemBringer itemBringer = new ItemBringer(); //new instance of Item Bringer
            PartyItem partyItem = partyItemDao.getById(Long.valueOf(myPartyItems[i])); //get partyItem object by id

            itemBringer.setQuantity(Long.valueOf(quantities[i])); //sets quantity
            itemBringer.setPartyMember(partyMember1); //sets partyMember object
            itemBringer.setPartyItem(partyItem); // sets partyItem object
            itemBringerDao.save(itemBringer); // saves item bringer

            partyItemsDetails += "Item: " + partyItem.getItem().getName() + " Quantity: " + quantities[i] + "<br>";
        }
        System.out.println(partyItemsDetails);

        emailService.sendRSVPConfirmMember(member, partyMember, party, partyItemsDetails);

        return "redirect:/member/successRsvp/" + urlKey + "/" + uuid;
    }

    //shows RSVPSuccess page to PartyMembers
    @GetMapping(path = "/member/successRsvp/{urlKey}/{partyMemberKey}")
    public String showRSVPSuccess(Model model, @PathVariable String partyMemberKey, @PathVariable String urlKey) {
        model.addAttribute("urlKey", urlKey);
        model.addAttribute("partyMemberKey", partyMemberKey);

        return "partyMember/successRsvp";
    }

    @GetMapping(path = "/rsvp/{urlKey}/member/{partyMemberKey}/view")
    public String showPartyMemberRSVPInfo(@PathVariable String urlKey, @PathVariable String partyMemberKey, Model model) {

        Party party = partyDao.getByUrlKey(urlKey); //gets party
        PartyMember partyMember = partyMemberDao.getByPartyMemberKey(partyMemberKey); //gets partyMember

        //If Member is not logged in, redirect to login Page
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().equals("anonymousUser")) {
            return "redirect:/login";
        }

        Member userInSession = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); //get member in session
        Member actualMember = memberDao.getById(userInSession.getId()); //get member info associated with member in session
        PartyMember checkMember = partyMemberDao.getByMemberAndParty(actualMember, party); //gets partyMember associated with logged in member & party (can only be one)

        //If logged in Member is not the member associated with the PartyMember, redirect to profile page

        if(checkMember == null){

            return "redirect:/profile";
        }

        List<PartyItem> partyItems = partyItemDao.getByParty(party); //gets partyItems associated with party
        List<ItemBringer> itemBringers = itemBringerDao.getByPartyMember(partyMember); //gets & sets list of item bringers associated w/ guest
        List<Long> quantities = guestController.calculateQuantity(partyItems); //gets dynamic quantities left of each party
        HashMap<ItemBringer, List<Long>> itemBringerActual = guestController.getItemBringerActual(itemBringers, quantities); //hashmap to store party items & list of long quantity values

        model.addAttribute("party", party); //get party info
        model.addAttribute("partyMember", partyMember); //get guest info
        model.addAttribute("itemBringers", itemBringerActual); //gets ItemBringer info associated with guestId
        model.addAttribute("partyItems", partyItems); //gets & sets partyItems for party

        return "partyMember/viewRsvp";
    }

    //Shows PartyMember Info & Allows to Edit
    @GetMapping(path = "/rsvp/{urlKey}/member/{partyMemberKey}/edit")
    public String showEditRSVP(@PathVariable String urlKey, @PathVariable String partyMemberKey, Model model) {

        Party party = partyDao.getByUrlKey(urlKey); //gets party
        PartyMember partyMember = partyMemberDao.getByPartyMemberKey(partyMemberKey); //gets partyMember

        //If Member is not logged in, redirect to login Page
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().equals("anonymousUser")) {
            return "redirect:/login";
        }

        Member userInSession = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); //get member in session
        Member actualMember = memberDao.getById(userInSession.getId()); //get member info associated with member in session
        PartyMember checkMember = partyMemberDao.getByMemberAndParty(actualMember, party); //gets partyMember associated with logged in member & party (can only be one)

        //If logged in Member is not the member associated with the PartyMember, redirect to profile page
        if (checkMember == null || partyMember.getId() != checkMember.getId()) {
            return "redirect:/profile";
        }

        ArrayList<String> rsvpStatuses = guestController.getRsvpStatuses(); //list of RSVP enum values/options
        ArrayList<String> additionalGuests = guestController.getAdditionalGuests(); //list of RSVP enum values/options

        List<PartyItem> partyItems = partyItemDao.getByParty(party); //gets partyItems associated with party
        List<ItemBringer> itemBringers = itemBringerDao.getByPartyMember(partyMember); //gets & sets list of item bringers associated w/ guest
        List<Long> quantities = guestController.calculateQuantity(partyItems); //gets dynamic quantities left of each party
        HashMap<ItemBringer, List<Long>> itemBringerActual = guestController.getItemBringerActual(itemBringers, quantities); //hashmap to store party items & list of long quantity values

        model.addAttribute("party", party); //get party info
        model.addAttribute("partyMember", partyMember); //get guest info
        model.addAttribute("rsvps", rsvpStatuses); //allows access to rsvp enum in form
        model.addAttribute("additionalGuests", additionalGuests); //sets additional guests drop down
        model.addAttribute("itemBringers", itemBringerActual); //gets ItemBringer info associated with guestId
        model.addAttribute("partyItems", partyItems); //gets & sets partyItems for party

        return "partyMember/editRsvp";
    }

    //saves partyMember edited information
    @PostMapping(path = "/rsvp/{urlKey}/member/{partyMemberKey}/edit")
    public String saveEditRSVP(@ModelAttribute PartyMember partyMember, @RequestParam String rsvp, @RequestParam(name = "itemBringer[]") String[] itemBringer, @RequestParam(name = "quantity[]") String[] quantities,
                               @RequestParam(name = "partyItem[]") String[] partyItem, @PathVariable String urlKey, @PathVariable String partyMemberKey) {

        Party party = partyDao.getByUrlKey(urlKey);

        List<PartyItem> dbPartyItems = partyItemDao.getByParty(party); //gets most current/up-to-date partyItems associated w/ party from db
        List<Long> updatedQuantities = guestController.calculateQuantity(dbPartyItems); //gets list of updated quantity remaining for dbPartyItems

        //checks guest can still sign up for items
        for (int i = 0; i < quantities.length; i++) { //updates itemBringer quantity
            Long quantity = Long.valueOf(quantities[i]);

            //if quantity signing up for is greater than what is available in the db, reload page with updated info
            if (quantity > updatedQuantities.get(i)) {
                System.out.println("Error: quantity signing up for is greater than available in the database");
                return "redirect:/rsvp/" + urlKey + "/member/" + partyMemberKey + "/edit";
            }
        }

        Member userInSession = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member member = memberDao.getById(userInSession.getId());

        partyMember.setRsvpStatus(RsvpStatuses.valueOf(rsvp));
        partyMember.setParty(party);
        partyMember.setMember(member);
        partyMemberDao.save(partyMember); //saves partyMember information

        for (int i = 0; i < itemBringer.length; i++) { //updates itemBringer quantity
            ItemBringer updatedItemBringer = itemBringerDao.getById(Long.valueOf(itemBringer[i])); //get itemBringer object associated w/ itemBringerID
            updatedItemBringer.setQuantity((Long.valueOf(quantities[i]))); //sets updated quantity
            itemBringerDao.save(updatedItemBringer); //saves & updates quantity for ItemBringer
        }
        return "redirect:/member/successRsvp" + "/" + urlKey + "/" + partyMemberKey;
    }

}
