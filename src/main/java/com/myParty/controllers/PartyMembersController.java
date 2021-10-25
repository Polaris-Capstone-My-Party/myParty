package com.myParty.controllers;

import com.myParty.models.*;
import com.myParty.repositories.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

public class PartyMembersController {

    private final PartyRepository partyDao;
    private final GuestRepository guestDao;
    private final PartyItemRepository partyItemDao;
    private final ItemBringerRepository itemBringerDao;
    private final PartyMemberRepository partyMemberDao;

    public PartyMembersController(PartyRepository partyDao, GuestRepository guestDao, PartyItemRepository partyItemDao, ItemBringerRepository itemBringerDao, PartyMemberRepository partyMemberDao) {
        this.partyDao = partyDao;
        this.guestDao = guestDao;
        this.partyItemDao = partyItemDao;
        this.itemBringerDao = itemBringerDao;
        this.partyMemberDao = partyMemberDao;
    }

    //saves PartyMember & ItemBringer information
    //partyMember needs, rsvp status, party & Member
    @PostMapping(path = "/rsvp/{urlKey}/{memberId}")
    public String createGuest(@PathVariable String urlKey, @PathVariable String memberId, @ModelAttribute PartyMember partyMember, @RequestParam String rsvp, @RequestParam(name="partyItem[]") String[] myPartyItems, @RequestParam(name="quantity[]") String[] quantities){

        Member userInSession = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); //get logged in member

        partyMember.setRsvpStatus(RsvpStatuses.valueOf(rsvp)); //set RSVP status enum
        partyMember.setParty(partyDao.getByUrlKey(urlKey)); //sets Party linked to partyMember
        partyMember.setMember(userInSession); //sets Member to logged in member

        PartyMember partyMember1 = partyMemberDao.save(partyMember); //saves partyMember instance

        for(int i = 0; i < myPartyItems.length; i++){ //goes through partyItems guest submitted

            if(quantities[i].equals("0")){ //if quantity is 0, no need to create Item Bringer instance
                continue;
            }

            ItemBringer itemBringer = new ItemBringer(); //new instance of Item Bringer
            PartyItem partyItem = partyItemDao.getById(Long.valueOf(myPartyItems[i])); //get partyItem object by id

            itemBringer.setQuantity(Long.valueOf(quantities[i])); //sets quantity
            itemBringer.setPartyMember(partyMember1); //sets partyMember object
            itemBringer.setPartyItem(partyItem); // sets partyItem object
            itemBringerDao.save(itemBringer); // saves item bringer
            //TODO: Add error message to avoid negative values in the database (someone signs up for stuff before you submit)
        }

        return  "redirect:/member/successRsvp/" + urlKey + "/" + memberId;
    }

    //shows RSVPSuccess page to PartyMembers
    @GetMapping(path = "/member/successRsvp/{urlKey}/{memberId}")
    public String showRSVPSuccess(Model model, @PathVariable String memberId, @PathVariable String urlKey){
        model.addAttribute("urlKey", urlKey);
        model.addAttribute("memberId", memberId);
        return "partyMember/successRsvp";
    }

}
