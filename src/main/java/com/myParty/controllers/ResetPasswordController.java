package com.myParty.controllers;

import com.myParty.models.Member;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.mail.MessagingException;
import java.util.UUID;

public class ResetPasswordController {

    @GetMapping("/member/resetpassword/{token}")
    //TODO: make sure this works and create the form
    public String showResetPWform(@PathVariable("token") String token) {
        Member member = memberDao.findByResetToken(token);
        return "member/resetpassword";
    }

    @PostMapping("/member/resetpassword")
    public String resetPassword(@RequestParam("email") String memberEmail) throws MessagingException {
        Member member = memberDao.getByEmail(memberEmail);

        String token = UUID.randomUUID().toString();
        member.setResetToken(token);
        memberDao.save(member);
        String resetDetails = "";
//TODO: add domain + the token in an anchor tag
        emailService.sendResetPassword(member, resetDetails);

        return "";
    }
}
