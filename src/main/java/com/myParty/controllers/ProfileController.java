package com.myParty.controllers;

import com.myParty.repositories.MemberRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final MemberRepository memberService;

    public ProfileController(MemberRepository memberService) {
        this.memberService = memberService;
    }

    @RequestMapping(value="/profile")
    public String profile(Model model, Principal principal) {

        String username = principal.getName();

        model.addAttribute("member", memberService.findByUsername(username));

        return ("/profile");

    }




    }

//    @GetMapping("/member/profile")
//    @ResponseBody
//    public String landingPage(){
//        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//    }
//
//}
//
//    @GetMapping("/member/{username}/profile")
//    public String showMemberProfile(
//            @PathVariable String username,
//            Model model
//    ) {
//        Member memberToDisplay = memberDao.findByUsername(username);
//        model.addAttribute("owner", memberToDisplay);
//
//        return "member/profile";
//    }
//}