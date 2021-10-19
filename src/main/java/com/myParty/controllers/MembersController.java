package com.myParty.controllers;

import com.myParty.models.Member;
import com.myParty.repositories.MemberRepository;
import com.myParty.repositories.PartyRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MembersController {

//    @Autowired
       private final MemberRepository memberDao;

    private final PartyRepository partyDao;


    private final PasswordEncoder passwordEncoder;

        public MembersController(MemberRepository memberDao, PartyRepository partyDao, PasswordEncoder passwordEncoder) {
            this.memberDao = memberDao;
            this.partyDao = partyDao;
            this.passwordEncoder = passwordEncoder;
        }

    @GetMapping("/user/create")
    public String createUserForm() {
        return "user/create";
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

//    @GetMapping("/party/{member_id}")
//
//    public String showPostById(@PathVariable long id, Model model) {
//
//        Party party = partyDao.getById(id);
//        model.addAttribute("party", party);
//
//        return "/party/view_party";
//    }

    @GetMapping("/member/{username}/profile")
    public String showMemberProfile(
            @PathVariable String username,
            Model model
    ) {
        Member memberToDisplay = memberDao.findByUsername(username);
        model.addAttribute("owner", memberToDisplay);

        return "member/profile";
    }
    @GetMapping("/profile")
    public String memberProfile(Model model) {
        Member userInSession = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member memberToDisplay = memberDao.getById(userInSession.getId());
        model.addAttribute("owner",memberToDisplay);
        return "member/personal_profile";
    }


}


