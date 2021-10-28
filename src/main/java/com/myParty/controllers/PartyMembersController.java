package com.myParty.controllers;

import com.myParty.models.*;
import com.myParty.repositories.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


@Controller
public class PartyMembersController {

    private final PartyRepository partyDao;
    private final PartyItemRepository partyItemDao;
    private final ItemBringerRepository itemBringerDao;
    private final PartyMemberRepository partyMemberDao;
    private final MemberRepository memberDao;
    private final GuestController guestController;

    public PartyMembersController(PartyRepository partyDao, PartyItemRepository partyItemDao, ItemBringerRepository itemBringerDao, PartyMemberRepository partyMemberDao, MemberRepository memberDao, GuestController guestController) {
        this.partyDao = partyDao;
        this.partyItemDao = partyItemDao;
        this.itemBringerDao = itemBringerDao;
        this.partyMemberDao = partyMemberDao;
        this.memberDao = memberDao;
        this.guestController = guestController;
    }

    //saves PartyMember & ItemBringer information
    @PostMapping(path = "/rsvp/{urlKey}/{memberId}")
    public String createGuest(@PathVariable String urlKey, @PathVariable String memberId, @ModelAttribute PartyMember partyMember, @RequestParam String rsvp, @RequestParam(name="partyItem[]") String[] myPartyItems, @RequestParam(name="quantity[]") String[] quantities){

        Member member = memberDao.getById(Long.valueOf(memberId));

        UUID uuid = UUID.randomUUID(); // creates UUID unique for given partyMember
        partyMember.setPartyMemberKey(uuid.toString()); //https://www.baeldung.com/java-uui

        partyMember.setRsvpStatus(RsvpStatuses.valueOf(rsvp)); //set RSVP status enum
        partyMember.setParty(partyDao.getByUrlKey(urlKey)); //sets Party linked to partyMember
        partyMember.setMember(member); //sets Member to logged in member
        PartyMember partyMember1 = partyMemberDao.save(partyMember); //saves partyMember instance

        //TODO: Add error message to avoid negative values in the database (someone signs up for stuff before you submit)
        for(int i = 0; i < myPartyItems.length; i++){ //goes through partyItems guest submitted
            ItemBringer itemBringer = new ItemBringer(); //new instance of Item Bringer
            PartyItem partyItem = partyItemDao.getById(Long.valueOf(myPartyItems[i])); //get partyItem object by id

            itemBringer.setQuantity(Long.valueOf(quantities[i])); //sets quantity
            itemBringer.setPartyMember(partyMember1); //sets partyMember object
            itemBringer.setPartyItem(partyItem); // sets partyItem object
            itemBringerDao.save(itemBringer); // saves item bringer
        }

        return  "redirect:/member/successRsvp/" + urlKey + "/" + uuid;
    }

    //shows RSVPSuccess page to PartyMembers
    @GetMapping(path = "/member/successRsvp/{urlKey}/{partyMemberKey}")
    public String showRSVPSuccess(Model model, @PathVariable String partyMemberKey, @PathVariable String urlKey){
        model.addAttribute("urlKey", urlKey);
        model.addAttribute("partyMemberKey", partyMemberKey);
        return "partyMember/successRsvp";
    }


    //Shows PartyMember Info & Allows to Edit
    @GetMapping(path = "/rsvp/{urlKey}/member/{partyMemberKey}/edit")
    public String showEditRSVP(@PathVariable String urlKey, @PathVariable String partyMemberKey, Model model){
        ArrayList<String> rsvpStatuses = guestController.getRsvpStatuses(); //list of RSVP enum values/options
        ArrayList<String> additionalGuests = guestController.getAdditionalGuests(); //list of RSVP enum values/options

        Party party = partyDao.getByUrlKey(urlKey); //gets party
        PartyMember partyMember = partyMemberDao.getByPartyMemberKey(partyMemberKey); //gets partyMember

        List<PartyItem> partyItems = partyItemDao.getByParty(party); //gets partyItems associated with party
        List<ItemBringer> itemBringers = itemBringerDao.getByPartyMember(partyMember); //gets & sets list of item bringers associated w/ guest
        List<Long> quantities = guestController.calculateQuantity(partyItems); //gets dynamic quantities left of each party
        HashMap<ItemBringer, List<Long>> itemBringerActual= guestController.getItemBringerActual(itemBringers, quantities); //hashmap to store party items & list of long quantity values

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
    public String saveEditRSVP(@ModelAttribute PartyMember partyMember, @RequestParam String rsvp, @RequestParam(name="itemBringer[]") String[] itemBringer, @RequestParam(name="quantity[]") String[] quantities,
                               @RequestParam(name="partyItem[]") String[] partyItem, @PathVariable String urlKey, @PathVariable String partyMemberKey){

        partyMember.setRsvpStatus(RsvpStatuses.valueOf(rsvp));
        partyMember.setParty(partyDao.getByUrlKey(urlKey));

        Member userInSession = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member member = memberDao.getById(userInSession.getId());
        partyMember.setMember(member);
        partyMemberDao.save(partyMember); //saves partyMember information

        //TODO: Add error message to avoid negative values in the database (someone signs up for stuff before you submit)
            for(int i = 0; i < itemBringer.length; i++){ //updates itemBringer quantity
                ItemBringer updatedItemBringer = itemBringerDao.getById(Long.valueOf(itemBringer[i])); //get itemBringer object associated w/ itemBringerID
                updatedItemBringer.setQuantity((Long.valueOf(quantities[i]))); //sets updated quantity
                itemBringerDao.save(updatedItemBringer); //saves & updates quantity for ItemBringer
            }
        return "redirect:/member/successRsvp" + "/" + urlKey + "/" + partyMemberKey;
    }

}
