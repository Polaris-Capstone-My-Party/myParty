package com.myParty.controllers;
import com.myParty.models.Member;
import com.myParty.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MembersController {

    @Autowired
        private final MemberRepository memberDao;

        private final PasswordEncoder passwordEncoder;

        public MembersController(MemberRepository memberDao, PasswordEncoder passwordEncoder) {
            this.memberDao = memberDao;
            this.passwordEncoder = passwordEncoder;
        }

//    @GetMapping("/user/create")
//    public String createUserForm() {
//        return "user/create";
//    }

        @GetMapping("/sign-up")
        public String showSignupForm(Model model){
            model.addAttribute("member", new Member());
            return "member/sign-up";
        }

        @PostMapping("/sign-up")
        public String saveUser(@ModelAttribute Member member){
            String hash = passwordEncoder.encode(member.getPassword());
            member.setPassword(hash);
            memberDao.save(member);
            return "redirect:/login";
        }

        @GetMapping("/member/{username}/ads")
        public String showUserParties(
                @PathVariable String username,
                Model model
        ){
            Member memberToDisplay = memberDao.findByUsername(username);
            model.addAttribute("member", memberToDisplay);

            return "member/displayParties";
        }


        @PostMapping("/member/create")
        @ResponseBody
        public String createMember(
                @RequestParam(name = "uname") String username,
                @RequestParam(name = "psw") String password
        ) {
            System.out.println("Username" + username);
            System.out.println("Password" + password);

            return "Member created";
        }
    }

