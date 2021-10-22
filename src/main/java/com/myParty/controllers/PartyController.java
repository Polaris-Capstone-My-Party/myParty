package com.myParty.controllers;

import com.myParty.models.*;
import com.myParty.repositories.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class PartyController {

    private final PartyRepository partyDao;
    private final MemberRepository memberDao;
    private final LocationRepository locationDao;
    private final ItemRepository itemDao;
    private final PartyItemRepository partyItemDao;

    public PartyController(PartyRepository partyDao, MemberRepository memberDao, LocationRepository locationDao, ItemRepository itemDao, PartyItemRepository partyItemDao) {
        this.partyDao = partyDao;
        this.memberDao = memberDao;
        this.locationDao = locationDao;
        this.itemDao = itemDao;
        this.partyItemDao = partyItemDao;
    }

    //show form for creating a party
    @GetMapping("/parties/create")
    public String showCreatePartyForm(Model model) {
        model.addAttribute("party", new Party());
        return "party/create";
    }

    //saves party info & creates new party
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
    }

    //show page when party successfully created
    @GetMapping("/parties/success")
    public String showSuccessPartyForm(@RequestParam String urlKey, Model model) {
        Party party = partyDao.getByUrlKey(urlKey);
        model.addAttribute("party", party);
        return "party/success";
    }

    //redirects to profile when submit button pushed
    @PostMapping("/parties/{urlKey}")
    public String successParty(@RequestParam(name="customMessage") String customMessage, @RequestParam(name="emailAddress") String emailAddress){
        return "redirect:profile";
    }

    //show form for editing party
    @GetMapping("/parties/edit/{id}")
    public String showEditPartyForm(@PathVariable long id, String urlKey, Model model) {
        Party partyToEdit = partyDao.getById(id);
        //TODO: Refactor later
        model.addAttribute("id", partyToEdit.getId());
        model.addAttribute("party", partyToEdit.getUrlKey());
        model.addAttribute("title", partyToEdit.getTitle());
        model.addAttribute("description", partyToEdit.getDescription());
        model.addAttribute("startTime", partyToEdit.getStartTime());
        model.addAttribute("endTime", partyToEdit.getEndTime());
        model.addAttribute("addressOne", partyToEdit.getLocation().getAddressOne());
        model.addAttribute("addressTwo", partyToEdit.getLocation().getAddressTwo());
        model.addAttribute("city", partyToEdit.getLocation().getCity());
        model.addAttribute("state", partyToEdit.getLocation().getState());
        model.addAttribute("zipcode", partyToEdit.getLocation().getZipcode());

        return "party/edit";
    }

    //saves edited party information
    @PostMapping("/parties/edit/{id}")
    public String editParty(
            @PathVariable long id,
            @RequestParam(name = "title") String title,
            @RequestParam(name = "description") String description,
            @RequestParam(name = "startTime") String startTime,
            @RequestParam(name = "endTime") String endTime,
            @RequestParam(name = "addressOne") String addressOne,
            @RequestParam(name ="addressTwo") String addressTwo,
            @RequestParam(name ="city") String city,
            @RequestParam(name ="state") String state,
            @RequestParam(name ="zipcode") String zipcode) throws ParseException {

        //get party object
        Party partyToUpdate = partyDao.getById(id);

        //saves location information
        Location locationToUpdate = new Location(0, addressOne, addressTwo, city, state, zipcode);
        locationToUpdate.setId(partyToUpdate.getLocation().getId());
        Location locationInDb = locationDao.save(locationToUpdate);

        //Date & Time stuff
        //TODO ASK Herman
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Date parsedDate = dateFormat.parse(startTime.replace("T", " "));
        Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
        parsedDate = dateFormat.parse(endTime.replace("T", " "));
        timestamp = new java.sql.Timestamp(parsedDate.getTime());

        // update & save party contents
        partyToUpdate.setTitle(title);
        partyToUpdate.setDescription(description);
        partyToUpdate.setStartTime(timestamp);
        partyToUpdate.setEndTime(timestamp);
        partyToUpdate.setLocation(locationInDb);
        partyDao.save(partyToUpdate);

        return "redirect:/profile";
    }

    //deletes party
    @GetMapping("/parties/delete/{id}")
    public String deleteParty(@PathVariable("id") long id) {
        partyDao.deleteById(id);
        return "redirect:/profile";
    }

    //show form for adding partyItems
    //TODO: Check in on
    @GetMapping("/parties/items/{urlKey}")
    public String showItemForm(Model model, @PathVariable String urlKey){
        Party party = partyDao.getByUrlKey(urlKey); //gets party
        model.addAttribute("party", party); //sets party
        return "/party/createItems";
    }

    //saves party information
    @PostMapping("/parties/items/{urlKey}")
    public String addItems(@PathVariable String urlKey, @RequestParam(name="name[]") String[] names,@RequestParam(name="quantity[]") String[] quantities ) {
        Party party = partyDao.getByUrlKey(urlKey);

        for(int i = 0; i< names.length; i++){
            //TODO: If item is null don't add
            //TODO: How to make dynamic, 'add another item'

            Item item = new Item(); //create new item instance //TODO: check if item already exists
            item.setName(names[i]); //set item name from name[]
            itemDao.save(item); //save item instance

            //creates & Saves party item
            PartyItem partyItem = new PartyItem();
            partyItem.setItem(item);
            partyItem.setQuantityRequired(Long.valueOf(quantities[i]));
            partyItem.setParty(party);
            partyItemDao.save(partyItem);
        }
        return "redirect:/profile";
    }
}

