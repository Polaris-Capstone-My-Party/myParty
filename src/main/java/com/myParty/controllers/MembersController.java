package com.myParty.controllers;

import com.myParty.models.*;
import com.myParty.repositories.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class MembersController {

    private final MemberRepository memberDao;
    private final PartyRepository partyDao;
    private final PasswordEncoder passwordEncoder;
    private final GuestRepository guestDao;
    private final PartyItemRepository partyItemDao;
    private final ItemBringerRepository itemBringerDao;

    public MembersController(MemberRepository memberDao, PartyRepository partyDao, PasswordEncoder passwordEncoder, GuestRepository guestDao, PartyItemRepository partyItemDao, ItemBringerRepository itemBringerDao) {
        this.memberDao = memberDao;
        this.partyDao = partyDao;
        this.passwordEncoder = passwordEncoder;
        this.guestDao = guestDao;
        this.partyItemDao = partyItemDao;
        this.itemBringerDao = itemBringerDao;
    }

    //shows signup Page
    @GetMapping("/sign-up")
    public String showSignupForm(Model model) {
        model.addAttribute("member", new Member());
        return "member/signup";
    }

    //submits signup form & saves user
    @PostMapping("/sign-up")
    public String saveMember(@ModelAttribute Member member) {
        String hash = passwordEncoder.encode(member.getPassword());
        member.setPassword(hash);
        memberDao.save(member);
        return "redirect:/login";
    }

    //shows public profile to other members
    @GetMapping("/member/{username}/profile")
    public String showMemberProfile(@PathVariable String username, Model model) {
        Member memberToDisplay = memberDao.findByUsername(username);
        model.addAttribute("owner", memberToDisplay);
        return "member/profile";
    }

    //shows member profile to themselves
    @GetMapping("/profile")
    public String memberProfile(Model model) {
        System.out.println("In profile");
        Member userInSession = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member memberToDisplay = memberDao.getById(userInSession.getId());
        model.addAttribute("owner",memberToDisplay);
        return "member/personal_profile";
    }

    //show host party page to member
    @GetMapping("/member/{urlKey}/view")
    public String showHostPartyPage(Model model, @PathVariable String urlKey){

        Party party = partyDao.getByUrlKey(urlKey); //gets party by urlKey
        List<PartyItem> partyItems = partyItemDao.getByParty(party); //gets partyItems associated w/ party
        List<Long> quantityRemaining = calculateQuantity(partyItems); //gets list of quantity remaining
        HashMap<Long, PartyItem> completedPartyItems = new HashMap<>(); //Creates Hashmap that stores PartyItem objects & quantitiesRemaining

        for(int i = 0; i < partyItems.size(); i++){ //iterates through partyItems list & quantityRemaining list
            completedPartyItems.put(quantityRemaining.get(i), partyItems.get(i)); //sets quantityRemaining Long & PartyItem object for HashMap
        }

        List<Guest> guests = guestDao.getByParty(party); //gets guests associated w/ party
        List<ItemBringer> itemBringers = new ArrayList<>(); //new arraylist of item bringers

//        for (Guest guest : guests){ //for each guest
//            List<ItemBringer> placeholders = itemBringerDao.getByGuest(guest); //get itembringers associated w/ guest
//            for (ItemBringer placeholder: placeholders) {//for each item bringer associated with guest
//                if(placeholder != null){ //if itemBringer is not null, add to masterlist of itemBringers
//                    itemBringers.add(placeholder);
//                }
//            }
//        }

        model.addAttribute("party", party); //sets party information
        model.addAttribute("guests", guests); //sets guest information
        model.addAttribute("partyItems", completedPartyItems); //sets partyItem information
        return "member/hostPartyPage";
    }

    //logs out user
    @PostMapping("/logout")
    public String logout(){return "redirect:/";}

    //calculate remaining Quantity
    public List<Long> calculateQuantity(List<PartyItem> partyItems){ //takes in List of partyItems
        List<Long> totalQuantity= new ArrayList<>(); //list to store total quantity being brought of each partyItem for a party
        Long quantityGuestsBringing = Long.valueOf(0);

        for (PartyItem partyItem: partyItems){ //run through each partyItem in partyItem array
            List<ItemBringer> itemBringers = itemBringerDao.getByPartyItem(partyItem); //gets list of item bringers associated w/ party item
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



