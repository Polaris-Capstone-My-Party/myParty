package com.myParty.controllers;

import com.myParty.models.Member;
import com.myParty.repositories.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MembersController {

    private final MemberRepository memberDao;

    private final PasswordEncoder passwordEncoder;

    public MembersController(MemberRepository memberDao, PasswordEncoder passwordEncoder) {
        this.memberDao = memberDao;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/sign-up")
    public String showSignupForm(Model model) {
        model.addAttribute("member", new Member());
        return "member/signup";
    }

    @PostMapping("/sign-up")
    public String saveMember(@ModelAttribute Member member) {
        String hash = passwordEncoder.encode(member.getPassword());
        member.setPassword(hash);
        memberDao.save(member);
        return "redirect:/login";
    }

    @GetMapping("/member/{username}/parties")
    public String showMemberParties(
            @PathVariable String username,
            Model model
    ) {
        Member memberToDisplay = memberDao.findByUsername(username);
        model.addAttribute("member", memberToDisplay);
        return "member/displayParties";
    }
}

