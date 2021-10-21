package com.myParty.controllers;

import com.myParty.models.*;
import com.myParty.repositories.LocationRepository;
import com.myParty.repositories.MemberRepository;
import com.myParty.repositories.PartyItemRepository;
import com.myParty.repositories.PartyRepository;
import com.myParty.repositories.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
public class PartyController {

    private final PartyRepository partyDao;
    private final MemberRepository memberDao;
    private final LocationRepository locationDao;
//    private final PartyItemRepository partyItemDAO;
    private final ItemRepository itemDao;
    private final PartyItemRepository partyItemDao;

    public PartyController(PartyRepository partyDao, MemberRepository memberDao, LocationRepository locationDao, ItemRepository itemDao, PartyItemRepository partyItemDao) {
        this.partyDao = partyDao;
        this.memberDao = memberDao;
        this.locationDao = locationDao;
//        this.partyItemDAO = partyItemDAO;

        this.itemDao = itemDao;
        this.partyItemDao = partyItemDao;
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
        System.out.println(locationToAdd);
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


        return "redirect:/parties/items/" + uuid;
//                "redirect:/parties/success?urlKey="+ uuid;

    }

    @GetMapping("/parties/success")
    public String showSuccessPartyForm(@RequestParam String urlKey, Model model) {
        Party party = partyDao.getByUrlKey(urlKey);
        model.addAttribute("party", party);
        return "party/success";
    }

    @PostMapping("/parties/{urlKey}")
    public String successParty(@RequestParam(name="customMessage") String customMessage, @RequestParam(name="emailAddress") String emailAddress){
        return "redirect:profile";
    }


    @GetMapping("/parties/edit/{id}")
    public String showEditPartyForm(@PathVariable long id, String urlKey, Model model) {
        Party partyToEdit = partyDao.getById(id);

        Party party = partyDao.getByUrlKey(urlKey); // gets party info for form
        model.addAttribute("id", partyToEdit.getId());
        model.addAttribute("party", partyToEdit.getUrlKey());
        model.addAttribute("title", partyToEdit.getTitle());
        model.addAttribute("description", partyToEdit.getDescription());
        model.addAttribute("startTime", partyToEdit.getStartTime());
        model.addAttribute("endTime", partyToEdit.getEndTime());
        model.addAttribute("address_one", partyToEdit.getLocation().getAddress_one());
        model.addAttribute("address_two", partyToEdit.getLocation().getAddress_two());
        model.addAttribute("city", partyToEdit.getLocation().getCity());
        model.addAttribute("state", partyToEdit.getLocation().getState());
        model.addAttribute("zipcode", partyToEdit.getLocation().getZipcode());

        return "party/edit";
    }

    @PostMapping("/parties/edit/{id}")
    public String editParty(
            @PathVariable long id,
            @RequestParam(name = "title") String title,
            @RequestParam(name = "description") String description,
            @RequestParam(name = "startTime") String startTime,
            @RequestParam(name = "endTime") String endTime,


            @RequestParam(name = "address_one") String address_one,
            @RequestParam(name ="address_two") String address_two,
            @RequestParam(name ="city") String city,
            @RequestParam(name ="state") String state,
            @RequestParam(name ="zipcode") String zipcode

            ) throws ParseException {
        Location locationToUpdate = new Location(
                0,
                address_one,
                address_two,
                city,
                state,
                zipcode);


// get existing info from post
        Party partyToUpdate = partyDao.getById(id);
        locationToUpdate.setId(partyToUpdate.getLocation().getId());
//        System.out.println(party);
        // update its contents
        partyToUpdate.setTitle(title);
        partyToUpdate.setDescription(description);


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Date parsedDate = dateFormat.parse(startTime.replace("T", " "));
        Member loggedInMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());

        partyToUpdate.setStartTime(timestamp);
         parsedDate = dateFormat.parse(endTime.replace("T", " "));

         timestamp = new java.sql.Timestamp(parsedDate.getTime());

        Location locationInDb = locationDao.save(locationToUpdate);

        partyToUpdate.setLocation(locationInDb);

        partyToUpdate.setEndTime(timestamp);
        partyToUpdate.setLocation(locationInDb);
//        partyToUpdate.setAddressOne(address_one);
//        partyToUpdate.setAddressTwo(address_two);
//        partyToUpdate.setCity( city);
//        partyToUpdate.Setstate(state);
//        partyToUpdate.setZipcode(zipcode);

        UUID uuid = UUID.randomUUID();

        partyDao.save(partyToUpdate); // save updated post
        return "redirect:/profile";
//        return "redirect:/parties/success?urlKey="+ uuid;

    }

    @GetMapping("/parties/delete/{id}")
    public String deleteParty(@PathVariable("id") long id) {
        partyDao.deleteById(id);
        return "redirect:/profile";
    }

    @GetMapping("/parties/items/{urlKey}")
    public String showItemForm(Model model, @PathVariable String urlKey){
        Party party = partyDao.getByUrlKey(urlKey); //gets party
        List<Long> test = new ArrayList<>();
        test.add(1L);
        test.add(2L);
        test.add(3L);

        model.addAttribute("party", party); //sets party
        model.addAttribute("tests", test); //sets tests
        return "/party/createItems";
    }

    @PostMapping("/parties/items/{urlKey}")
    public String addItems(@PathVariable String urlKey, @RequestParam(name="name[]") String[] names,@RequestParam(name="quantity[]") String[] quantities ) {
        Party party = partyDao.getByUrlKey(urlKey);

        for(int i = 0; i< names.length; i++){

            //TODO: If item is null don't add
            //TODO: How to make dynamic, 'add another item'

            Item item = new Item(); //create new item instance //TODO: check if item already exists
            item.setName(names[i]); //set item name from name[]
            itemDao.save(item); //save item instance

            PartyItem partyItem = new PartyItem(); //create new PartyItem instance
            partyItem.setItem(item);
            partyItem.setQuantityRequired(Long.valueOf(quantities[i]));
            partyItem.setParty(party);
            PartyItem partyItemInDB = partyItemDao.save(partyItem);
            System.out.println(partyItemInDB);
        }

        return "redirect:/profile";
    }






//    @GetMapping("/parties")
//    public String showParties(Model model) {
//        List<Party> listOfParties = partyDao.findAll();
//        System.out.println(listOfParties);
//        model.addAttribute("listOfParties", listOfParties);
//        return "/parties/party_index";
//    }
    }


