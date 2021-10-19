package com.myParty.controllers;

import com.myParty.models.Location;
import com.myParty.models.Member;
import com.myParty.models.Party;
import com.myParty.repositories.LocationRepository;
import com.myParty.repositories.MemberRepository;
import com.myParty.repositories.PartyRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Controller
public class PartyController {


    private final PartyRepository partyDao;

    private final MemberRepository memberDao;

    private final LocationRepository locationDao;

    public PartyController(PartyRepository partyDao, MemberRepository memberDao, LocationRepository locationDao) {
        this.partyDao = partyDao;
        this.memberDao = memberDao;
        this.locationDao = locationDao;
    }


    @GetMapping("/parties/create")
    public String showCreatePartyForm(Model model) {
        model.addAttribute("party", new Party());
        return "party/create";
    }

    @PostMapping("/parties/create")
    public String createParty(
            @ModelAttribute Party party,
            @RequestParam String start_time,
            @RequestParam String end_time,


            @RequestParam String address_one,
            @RequestParam String address_two,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String zipcode


            ) throws ParseException {

        Location locationToAdd = new Location(
                0,
                address_one,
                address_two,
                city,
                state,
                zipcode);
        // 2021-10-21T13:13
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Date parsedDate = dateFormat.parse(start_time.replace("T", " "));
        Member loggedInMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());

        party.setOwner(loggedInMember);

        party.setStartTime(timestamp);


        party.setEndTime(timestamp);


        System.out.println(timestamp);



        UUID uuid = UUID.randomUUID();

        System.out.println(uuid.toString());

        party.setUrlKey(uuid.toString());
        System.out.println(party);

        Location locationInDb = locationDao.save(locationToAdd);

        party.setLocation(locationInDb);

        partyDao.save(party);


        return "redirect:/parties/success?urlKey="+ uuid;

    }

    @GetMapping("/parties/success")
    public String showSuccessPartyForm(@RequestParam String urlKey, Model model) {
        Party party = partyDao.getByUrlKey(urlKey);
        model.addAttribute("party", party);
        return "party/success";
    }

    @PostMapping("/parties/{urlKey}")
    public String successParty(@RequestParam(name="customMessage") String customMessage,
                               @RequestParam(name="emailAddress") String emailAddress)  {

        return "redirect:/success";
    }

//    @GetMapping("/parties")
//    public String showParties(Model model) {
//        List<Party> listOfParties = partyDao.findAll();
//        System.out.println(listOfParties);
//        model.addAttribute("listOfParties", listOfParties);
//        return "/parties/party_index";
//    }
}
