package com.myParty.controllers;

import com.myParty.models.*;
import com.myParty.repositories.*;
import com.myParty.services.EmailService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.text.ParseException;
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
            @RequestParam String zipcode,
            @RequestParam(name = "name[]") String[] names,
            @RequestParam(name = "quantity[]") String[] quantities) throws MessagingException {

        //Creates & Saves Location
        Location locationToAdd = new Location(0, addressOne, addressTwo, city, state, zipcode);
        Location locationInDb = locationDao.save(locationToAdd);

        //Gets Logged in Member
        Member loggedInMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //get UUID for urlKey
        UUID uuid = UUID.randomUUID();

        //sets & save party details plus sends email
        party.setOwner(loggedInMember);
        party.setStartTime(party.makeTimestampFromString(start_time));
        party.setEndTime(party.makeTimestampFromString(end_time));
        party.setUrlKey(uuid.toString());
        party.setLocation(locationInDb);
        Party newCreatedParty = partyDao.save(party);

        //TODO: fix location to show cleaner
        String partyDetails =
                "<h2>Your party " + party.getTitle() + " has been created.</h2>, <br><i>Here are the details: </i><br>" + "Description: " + party.getDescription() + "<br>"
                + "Start Time: " + party.getStartTime() + "<br>" + "End Time: " + party.getEndTime() + "<br>" + "Location: " + party.getLocation() + "<br>"
                + "Here is your custom party URL: " + party.getUrlKey() ;

        emailService.partyCreatedConfirmation(newCreatedParty, newCreatedParty.getTitle() + " has been created", partyDetails);

        //Creates and saves party Items
        for (int i = 0; i < names.length; i++) {
            //TODO: If item is null don't add - Error Message
            Item item = new Item(); //create new item instance //TODO: check if item already exists
            item.setName(names[i]); //set item name from name[]
            itemDao.save(item); //save item instance

            //creates & Saves party item
            PartyItem partyItem = new PartyItem();
            partyItem.setItem(item);
            partyItem.setQuantityRequired(Long.valueOf(quantities[i]));
            partyItem.setParty(newCreatedParty);
            partyItemDao.save(partyItem);
        }

        return "redirect:/parties/success/" + uuid;
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
    public String successParty(@PathVariable String urlKey, @RequestParam(name = "email[]") String[] emailAddresses) throws MessagingException {
        Party party = partyDao.getByUrlKey(urlKey);

        //TODO: fix location to be cleaner
        String partyDetails =
                "<h2>You're Invited to " + party.getTitle() + " by " + party.getOwner().getFirstName() + "</h2>, <br><i>Here are the details: </i><br>" + "Description: " + party.getDescription() + "<br>"
                        + "Start Time: " + party.getStartTime() + "<br>" + "End Time: " + party.getEndTime() + "<br>" + "Location: " + party.getLocation() + "<br>"
                        + "RSVP  " + "<a href=\"http://localhost:8080/rsvp/" + party.getUrlKey() + "\">here</a>";

        //TODO: fix link for party URL to make dynamic with new domain name

        for (int i = 0; i < emailAddresses.length; i++) {
            System.out.println(emailAddresses[i]);

            emailService.sendInvites(party.getTitle(), emailAddresses[i], partyDetails);

        }
        return "redirect:/profile";
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


}
