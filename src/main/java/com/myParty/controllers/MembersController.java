package com.myParty.controllers;

import com.myParty.BaseURL;
import com.myParty.models.*;
import com.myParty.repositories.*;
import com.myParty.services.EmailService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.*;


@Controller
public class MembersController {

    private final MemberRepository memberDao;
    private final PartyRepository partyDao;
    private final PasswordEncoder passwordEncoder;
    private final GuestRepository guestDao;
    private final PartyItemRepository partyItemDao;
    private final ItemBringerRepository itemBringerDao;
    private final GuestController guestControllerDao;
    private final PartyMemberRepository partyMemberDao;
    private final EmailService emailService;

    public MembersController(MemberRepository memberDao, PartyRepository partyDao, PasswordEncoder passwordEncoder, GuestRepository guestDao, PartyItemRepository partyItemDao, ItemBringerRepository itemBringerDao, GuestController guestControllerDao, PartyMemberRepository partyMemberDao, EmailService emailService) {
        this.memberDao = memberDao;
        this.partyDao = partyDao;
        this.passwordEncoder = passwordEncoder;
        this.guestDao = guestDao;
        this.partyItemDao = partyItemDao;
        this.itemBringerDao = itemBringerDao;
        this.partyMemberDao = partyMemberDao;
        this.guestControllerDao = guestControllerDao;
        this.emailService = emailService;
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
        return "member/publicProfile";
    }

    //shows member profile to themselves
    @GetMapping("/profile")
    public String memberProfile(Model model) {
        Member userInSession = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member memberToDisplay = memberDao.getById(userInSession.getId());

        List<PartyMember> partyMembers = memberToDisplay.getPartyMembers();
        List<PartyMember> upcomingParties = new ArrayList<>();
        List<PartyMember> pastParties = new ArrayList<>();

        //checks & orders parties if they are upcoming or past parties
        Date date = new Date();
        Timestamp currentTime = new Timestamp(date.getTime());
        for (PartyMember partyMember: partyMembers) {
            Timestamp partyTime = partyMember.getParty().getStartTime();  //gets partyTime

            //if partyTime is before currentTime --> party is in the past
            if(partyTime.before(currentTime)){
                pastParties.add(partyMember);
            }
            else{ //if partyTime is after currentTime --> party is upcoming
                upcomingParties.add(partyMember);
            }
        }

        model.addAttribute("owner", memberToDisplay);
        model.addAttribute("upcomingParties", upcomingParties);
        model.addAttribute("pastParties", pastParties); //TODO: Add logic to HTML form 
        return "member/personalProfile";
    }

    //show host party page to member
    @GetMapping("/member/{urlKey}/view")
    public String showHostPartyPage(Model model, @PathVariable String urlKey, HttpServletRequest request) {

        Party party = partyDao.getByUrlKey(urlKey); //gets party by urlKey

        //Display Party Items Logic
        List<PartyItem> partyItems = partyItemDao.getByParty(party); //gets partyItems associated w/ party
        List<Long> quantityRemaining = guestControllerDao.calculateQuantity(partyItems);//gets list of quantity remaining
        HashMap<PartyItem, Long> completedPartyItems = new HashMap<>(); //Creates Hashmap that stores PartyItem objects & quantitiesRemaining

        //TODO Works But Keep an Eye on - CG
        for (int i = 0; i < partyItems.size(); i++) { //iterates through partyItems list & quantityRemaining list
            completedPartyItems.put(partyItems.get(i), quantityRemaining.get(i)); //sets quantityRemaining Long & PartyItem object for HashMap
        }

        //Display Guest logic
        List<Guest> guests = guestDao.getByParty(party); //gets guests associated w/ party
        HashMap<Guest, List<ItemBringer>> completedGuests = new HashMap<>(); //Creates Hashmap that stores Guest objects & list of ItemBringers (assoc. w/ guest)

        for (Guest guest : guests) { //for each guest
            List<ItemBringer> itemBringers = itemBringerDao.getByGuest(guest); //get List of itemBringer objects associated w/ guest
            completedGuests.put(guest, itemBringers); //adds guest object & ItemBringer List to HashMap
        }

        //Display Party Members Logic
        List<PartyMember> partyMembers = partyMemberDao.getByParty(party); //gets partyMembers associated with party
        HashMap<PartyMember, List<ItemBringer>> completedPartyMembers = new HashMap<>(); //Creates Hashmap that stores PartyMember objects & list of ItemBringers (assoc. w/ partyMember)

        for (PartyMember partyMember : partyMembers) { //for each partyMember
            List<ItemBringer> itemBringers = itemBringerDao.getByPartyMember(partyMember); //get List of itemBringer objects associated w/ guest
            completedPartyMembers.put(partyMember, itemBringers); //adds guest object & ItemBringer List to HashMap
        }
        String url = BaseURL.getBaseURL(request) + "/rsvp/" + party.getUrlKey();

        model.addAttribute("party", party); //sets party information
        model.addAttribute("guests", completedGuests); //sets guest information
        model.addAttribute("partyMembers", completedPartyMembers); //sets partyMember information
        model.addAttribute("partyItems", completedPartyItems); //sets partyItem information
        model.addAttribute("url", url);

        return "member/hostPartyPage";
    }

    //logs out user
    @PostMapping("/logout")
    public String logout() {
        return "redirect:/";
    }

    //show form for editing member
    @GetMapping("/members/editProfile/{id}")
    public String showEditMemberForm(@PathVariable long id, String username, Model model) {
        Member memberToAdd = memberDao.getById(id);
        //TODO: Refactor later
        model.addAttribute("email", memberToAdd.getEmail());
        model.addAttribute("firstName", memberToAdd.getFirstName());
        model.addAttribute("lastName", memberToAdd.getLastName());
        model.addAttribute("phone", memberToAdd.getPhone());
        model.addAttribute("username", memberToAdd.getUsername());
        model.addAttribute("password", memberToAdd.getPassword());

        return "member/editProfile";
    }

    //saves edited member information
    @PostMapping("/members/editProfile/{id}")
    public String editProfile(
            @PathVariable long id,
            @RequestParam(name = "email") String email,
            @RequestParam(name = "firstName") String firstName,
            @RequestParam(name = "lastName") String lastName,
            @RequestParam(name = "phone") Long phone,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "password") String password) {

        //get member object
        Member memberToUpdate = memberDao.getById(id);

        //sets & saves member edited info
        memberToUpdate.setEmail(email);
        memberToUpdate.setFirstName(firstName);
        memberToUpdate.setLastName(lastName);
        memberToUpdate.setPhone(phone);
        memberToUpdate.setUsername(username);
        memberToUpdate.setPassword(password);
        memberDao.save(memberToUpdate);

        return "redirect:/profile";
    }

    @GetMapping("/members/delete/{id}")
    public String deleteMember(@PathVariable("id") long id, HttpSession httpSession) {
        httpSession.invalidate();
        memberDao.deleteById(id);
        return "redirect:/";
    }
}



