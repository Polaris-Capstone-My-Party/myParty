package com.myParty.controllers;
import com.myParty.models.Member;
import com.myParty.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
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
            model.addAttribute("user", new User());
            return "user/sign-up";
        }

        @PostMapping("/sign-up")
        public String saveUser(@ModelAttribute User user){
            String hash = passwordEncoder.encode(user.getPassword());
            user.setPassword(hash);
            userDao.save(user);
            return "redirect:/login";
        }

        @GetMapping("/user/{username}/ads")
        public String showUserPosts(
                @PathVariable String username,
                Model model
        ){
            User userToDisplay = userDao.findByUsername(username);
            model.addAttribute("user", userToDisplay);

            return "user/displayAds";
        }


        @PostMapping("/user/create")
        @ResponseBody
        public String createUser(
                @RequestParam(name = "uname") String username,
                @RequestParam(name = "psw") String password
        ) {
            System.out.println("Username" + username);
            System.out.println("Password" + password);

            return "User created";
        }
    }

}
