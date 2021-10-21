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

    private final GuestController guestController;

    public MembersController(MemberRepository memberDao, PartyRepository partyDao, PasswordEncoder passwordEncoder, GuestRepository guestDao, PartyItemRepository partyItemDao, ItemBringerRepository itemBringerDao, GuestController guestController) {
        this.memberDao = memberDao;
        this.partyDao = partyDao;
        this.passwordEncoder = passwordEncoder;
        this.guestDao = guestDao;
        this.partyItemDao = partyItemDao;
        this.itemBringerDao = itemBringerDao;
        this.guestController = guestController;
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
        List<Long> quantityRemaining = guestController.calculateQuantity(partyItems);//gets list of quantity remaining

        HashMap<Long, PartyItem> completedPartyItems = new HashMap<>(); //Creates Hashmap that stores PartyItem objects & quantitiesRemaining

        for(int i = 0; i < partyItems.size(); i++){ //iterates through partyItems list & quantityRemaining list
            completedPartyItems.put(quantityRemaining.get(i), partyItems.get(i)); //sets quantityRemaining Long & PartyItem object for HashMap
        }

        List<Guest> guests = guestDao.getByParty(party); //gets guests associated w/ party
        HashMap<Guest, List<ItemBringer>> completedGuests = new HashMap<>(); //Creates Hashmap that stores Guest objects & list of ItemBringers (assoc. w/ guest)

        for (Guest guest : guests){ //for each guest
            List<ItemBringer> itemBringers = itemBringerDao.getByGuest(guest); //get List of itemBringer objects associated w/ guest
            completedGuests.put(guest, itemBringers); //adds guest object & ItemBringer List to HashMap
        }

        model.addAttribute("party", party); //sets party information
        model.addAttribute("guests", completedGuests); //sets guest information
        model.addAttribute("partyItems", completedPartyItems); //sets partyItem information
        return "member/hostPartyPage";
    }

    //logs out user
    @PostMapping("/logout")
    public String logout(){return "redirect:/";}

}



