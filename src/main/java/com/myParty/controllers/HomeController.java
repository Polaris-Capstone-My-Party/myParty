package com.myParty.controllers;

import com.myParty.models.Member;
import com.myParty.repositories.MemberRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {

    private final MemberRepository memberDao;

    public HomeController(MemberRepository memberDao){this.memberDao = memberDao;}

    @GetMapping("/")
    public String landingPage(Model model){

        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() != "anonymousUser") {
            Member userInSession = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Member memberToDisplay = memberDao.getById(userInSession.getId());

            model.addAttribute("owner", memberToDisplay);
        }
        return "home";
    }
}
