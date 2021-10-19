package com.myParty.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
    public class AuthenticationController {
        @GetMapping("/login")
        public String showLoginForm() {return "member/login";}

//    @GetMapping("/member/{username}/profile")
//    public Object authentication(@CurrentSecurityContext(expression = "authentication")
//                                         Authentication authentication, @PathVariable String username) {
//        return authentication.getDetails();
//    }

    }


