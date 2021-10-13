package com.myParty.controllers;

import com.myParty.models.Party;
import com.sun.net.httpserver.Authenticator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
public class PartyController {

    @GetMapping("/parties/create")
    public String showCreatePartyForm(Model model) {
        model.addAttribute("party", new Party());
        return "party/create";
    }

    @PostMapping("/parties/create")
    public String createParty(@ModelAttribute Party party, Model model)  {
        model.addAttribute("party", new Party());

        return "redirect:/home";

    }

    @GetMapping("/parties/success")
    public String showSuccessPartyForm(Model model) {
        model.addAttribute("post", new Party());
        return "party/success";
    }

    @PostMapping("/parties/{urlKey}")
    public String successParty(@ModelAttribute Party party, Model model) {
        UUID uuid = UUID.randomUUID();
        return "redirect:/success";
    }

}
