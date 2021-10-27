package com.myParty.controllers;

import com.myParty.models.*;
import com.myParty.repositories.*;
import com.myParty.services.EmailService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class PartyController {

    private final PartyRepository partyDao;
    private final LocationRepository locationDao;
    private final ItemRepository itemDao;
    private final PartyItemRepository partyItemDao;
    private final EmailService emailService;

    public PartyController(PartyRepository partyDao, LocationRepository locationDao, ItemRepository itemDao, PartyItemRepository partyItemDao, EmailService emailService) {
        this.partyDao = partyDao;
        this.locationDao = locationDao;
        this.itemDao = itemDao;
        this.partyItemDao = partyItemDao;
        this.emailService = emailService;
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
            @RequestParam String addressOne,
            @RequestParam String addressTwo,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String zipcode) throws MessagingException {

        //Creates & Saves Location
        Location locationToAdd = new Location(0, addressOne, addressTwo, city, state, zipcode);
        Location locationInDb = locationDao.save(locationToAdd);

        //Gets Logged in Member
        Member loggedInMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //get UUID for urlKey
        UUID uuid = UUID.randomUUID();

        //sets & save party details
        party.setOwner(loggedInMember);
        party.setStartTime(party.makeTimestampFromString(start_time));
        party.setEndTime(party.makeTimestampFromString(end_time));
        party.setUrlKey(uuid.toString());
        party.setLocation(locationInDb);
        Party newCreatedParty = partyDao.save(party);
        String partyDetails = "<h2>Your party " + party.getTitle() + " has been created.</h2>, <br>Here are the details: <br>" + party.getDescription() + "<br>"
                + party.getStartTime() + "<br>" + party.getEndTime() + "<br>" + party.getLocation() ;
//        boolean html = true;
        emailService.prepareAndSend(newCreatedParty, newCreatedParty.getTitle() + " has been created", partyDetails);

        return "redirect:/parties/items/" + uuid;
    }

    //show page when party successfully created
    @GetMapping("/parties/success/{urlKey}")
    public String showSuccessPartyForm(@PathVariable String urlKey, Model model) {
        Party party = partyDao.getByUrlKey(urlKey);
        model.addAttribute("party", party);
        return "party/success";
    }

    //redirects to profile when submit button pushed
    @PostMapping("/parties/{urlKey}")
    public String successParty(@RequestParam(name = "customMessage") String customMessage, @RequestParam(name = "emailAddress") String emailAddress) {
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
        model.addAttribute("startTime", partyToEdit.getStartTime().toLocalDateTime());
        model.addAttribute("endTime", partyToEdit.getEndTime().toLocalDateTime());
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
            @RequestParam(name = "addressTwo") String addressTwo,
            @RequestParam(name = "city") String city,
            @RequestParam(name = "state") String state,
            @RequestParam(name = "zipcode") String zipcode) throws ParseException {

        //get party object
        Party partyToUpdate = partyDao.getById(id);

        //saves location information
        Location locationToUpdate = new Location(0, addressOne, addressTwo, city, state, zipcode);
        locationToUpdate.setId(partyToUpdate.getLocation().getId());
        Location locationInDb = locationDao.save(locationToUpdate);

        //sets & saves party edited info
        partyToUpdate.setTitle(title);
        partyToUpdate.setDescription(description);
        partyToUpdate.setStartTime(partyToUpdate.makeTimestampFromString(startTime));
        partyToUpdate.setEndTime(partyToUpdate.makeTimestampFromString(endTime));
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
    public String showItemForm(Model model, @PathVariable String urlKey) {
        Party party = partyDao.getByUrlKey(urlKey); //gets party
        model.addAttribute("party", party); //sets party
        return "/party/createItems";
    }

    //saves party information
    @PostMapping("/parties/items/{urlKey}")
    public String addItems(@PathVariable String urlKey, @RequestParam(name = "name[]") String[] names, @RequestParam(name = "quantity[]") String[] quantities) {
        Party party = partyDao.getByUrlKey(urlKey);

        for (int i = 0; i < names.length; i++) {
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
        return "redirect:/parties/success/" + urlKey;
    }


}


