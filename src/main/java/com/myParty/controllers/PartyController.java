package com.myParty.controllers;

import com.myParty.BaseURL;
import com.myParty.models.*;
import com.myParty.repositories.*;
import com.myParty.services.EmailService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class PartyController {

    private final PartyRepository partyDao;
    private final LocationRepository locationDao;
    private final ItemRepository itemDao;
    private final PartyItemRepository partyItemDao;
    private final MemberRepository memberDao;
    private final EmailService emailService;
    private final GuestController guestController;

    public PartyController(PartyRepository partyDao, LocationRepository locationDao, ItemRepository itemDao, PartyItemRepository partyItemDao, MemberRepository memberDao, EmailService emailService, GuestController guestController) {
        this.partyDao = partyDao;
        this.locationDao = locationDao;
        this.itemDao = itemDao;
        this.partyItemDao = partyItemDao;
        this.memberDao = memberDao;
        this.emailService = emailService;
        this.guestController = guestController;
    }

    //show form for creating a party
    @GetMapping("/parties/create")
    public String showCreatePartyForm(Model model) {
        Member userInSession = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member memberToDisplay = memberDao.getById(userInSession.getId());

        model.addAttribute("states", generateStates());
        model.addAttribute("party", new Party());
        model.addAttribute("owner", memberToDisplay);
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
            @RequestParam(name = "quantity[]") String[] quantities, HttpServletRequest request) throws MessagingException {

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

        emailService.partyCreatedConfirmation(party, request);

        //Calls method to create & save new items
        if(names != null){
            createItems(names, quantities, newCreatedParty);
        }

        return "redirect:/parties/success/" + uuid;
    }

    //show page when party successfully created
    @GetMapping("/parties/success/{urlKey}")
    public String showSuccessPartyForm(@PathVariable String urlKey, Model model, HttpServletRequest request) {
        Member userInSession = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member memberToDisplay = memberDao.getById(userInSession.getId());

        Party party = partyDao.getByUrlKey(urlKey);
        String url = BaseURL.getBaseURL(request) + "/rsvp/" + party.getUrlKey();

        model.addAttribute("party", party);
        model.addAttribute("url", url);
        model.addAttribute("owner", memberToDisplay);
        return "party/success";
    }

    //redirects to profile when submit button pushed
    @PostMapping("/parties/{urlKey}")
    public String successParty(@PathVariable String urlKey, @RequestParam(name = "email[]") String[] emailAddresses, Model model, HttpServletRequest request) throws MessagingException {
        Member userInSession = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member memberToDisplay = memberDao.getById(userInSession.getId());

        Party party = partyDao.getByUrlKey(urlKey);
        emailService.sendInvites(party, emailAddresses, request);

        model.addAttribute("owner", memberToDisplay);
        return "redirect:/profile";
    }

    //show form for editing party
    @GetMapping("/parties/edit/{id}")
    public String showEditPartyForm(@PathVariable long id, String urlKey, Model model) {

        Member userInSession = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member memberToDisplay = memberDao.getById(userInSession.getId());
        //Checks if party Exists
        if(!guestController.checkIfPartyExists(urlKey)){
            return "redirect:/parties/notFound";
        }


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
        model.addAttribute("stateOptions", generateStates());
        model.addAttribute("owner", memberToDisplay);
        List<PartyItem> partyItems = partyItemDao.getByParty(partyToEdit); //get partyItems associated with party

        model.addAttribute("partyItems", partyItems);
        model.addAttribute("party", partyToEdit);
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
            @RequestParam(name = "zipcode") String zipcode,
            @RequestParam(name = "name[]",  required = false) String[] names,
            @RequestParam(name = "quantity[]",  required = false) String[] quantities,
            @RequestParam(name = "delete[]",  required = false) String[] deletes, Model model){

        Member userInSession = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member memberToDisplay = memberDao.getById(userInSession.getId());

        model.addAttribute("owner", memberToDisplay);
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
        Party partyUpdated = partyDao.save(partyToUpdate);

        //Calls method to create & save new items
        if(names != null){
            createItems(names, quantities, partyUpdated);
        }

        //deletes items that are marked to be deleted
        if(deletes != null){
            for (String delete: deletes) {
                PartyItem partyItem = partyItemDao.getById(Long.valueOf(delete)); //gets partyItem to be deleted
                partyItemDao.delete(partyItem); //deletes PartyItem
            }
        }

        return "redirect:/member/" + partyUpdated.getUrlKey() + "/view";
    }

    //deletes party
    @GetMapping("/parties/delete/{id}")
    public String deleteParty(@PathVariable("id") long id, Model model) {
        Member userInSession = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member memberToDisplay = memberDao.getById(userInSession.getId());

        partyDao.deleteById(id);
        model.addAttribute("owner", memberToDisplay);
        return "redirect:/profile";
    }

    //Shows Error Page if Party is Not Found
    @GetMapping("/parties/notFound")
    public String partyNotFound(Model model){
        Member userInSession = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member memberToDisplay = memberDao.getById(userInSession.getId());

        model.addAttribute("owner", memberToDisplay);
        return "error/partyNotFound";
    }

    //Creates & saves new Items
    public void createItems(String[] names, String[] quantities, Party party){
        for (int i = 0; i < names.length; i++) {
            //TODO: If item is null don't add - Error Message
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
    }

    public List<String> generateStates(){
        List <String> states = new ArrayList<>();

        states.add("AL");
        states.add("AK");
        states.add("AZ");
        states.add("AR");
        states.add("CA");
        states.add("CO");
        states.add("CT");
        states.add("DE");
        states.add("FL");
        states.add("GA");
        states.add("HI");
        states.add("ID");
        states.add("IL");
        states.add("IN");
        states.add("IA");
        states.add("KS");
        states.add("KY");
        states.add("LA");
        states.add("ME");
        states.add("MD");
        states.add("MA");
        states.add("MI");
        states.add("MN");
        states.add("MS");
        states.add("MO");
        states.add("MT");
        states.add("NE");
        states.add("NV");
        states.add("NH");
        states.add("NJ");
        states.add("NM");
        states.add("NY");
        states.add("NC");
        states.add("ND");
        states.add("OH");
        states.add("OK");
        states.add("OR");
        states.add("PA");
        states.add("RI");
        states.add("SC");
        states.add("SD");
        states.add("TN");
        states.add("TX");
        states.add("UT");
        states.add("VT");
        states.add("VA");
        states.add("WA");
        states.add("WV");
        states.add("WI");
        states.add("WY");

        return states;
    }
}
