package com.myParty.controllers;

import com.myParty.models.*;
import com.myParty.repositories.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
        List<Guest> guests = guestDao.getByParty(party); //gets guests associated w/ party
        List<PartyItem> partyItems = partyItemDao.getByParty(party); //gets partyItems associated w/ party
        List<ItemBringer> itemBringers = new ArrayList<>(); //new arraylist of item bringers

        for (Guest guest : guests){ //for each guest
            List<ItemBringer> placeholders = itemBringerDao.getByGuest(guest); //get itembringers associated w/ guest
            for (ItemBringer placeholder: placeholders) {//for each item bringer associated with guest
                if(placeholder != null){ //if itemBringer is not null, add to masterlist of itemBringers
                    itemBringers.add(placeholder);
                }
            }
        }

        model.addAttribute("party", party); //sets party information
        model.addAttribute("guests", guests); //sets guest information
        model.addAttribute("partyItems", partyItems); //sets partyItem information
        model.addAttribute("itemBringers", itemBringers);
        return "member/hostPartyPage";
    }

    //logs out user
    @PostMapping("/logout")
    public String logout(){return "redirect:/";}
}



