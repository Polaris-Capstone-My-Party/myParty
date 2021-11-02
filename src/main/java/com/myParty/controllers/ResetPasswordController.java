package com.myParty.controllers;

import com.myParty.models.Member;
import com.myParty.repositories.MemberRepository;
import com.myParty.services.EmailService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.mail.MessagingException;
import java.util.UUID;

@Controller
public class ResetPasswordController {
    private final MemberRepository memberDao;
    private final EmailService emailService;

    public ResetPasswordController(MemberRepository memberDao, EmailService emailService){
        this.memberDao = memberDao;
        this.emailService = emailService;
    }

    @GetMapping("/member/forgotpassword")
    public String forgotPW(){
        return "member/forgotpassword";
    }

    @GetMapping("/member/resetpassword/{token}")
    //TODO: make sure this works and create the form
    public String showResetPWform(@PathVariable("token") String token) {
        Member member = memberDao.findByResetToken(token);
        return "member/resetpassword";
    }

    @PostMapping("/member/resetpassword/{token}")
    public String resetPassword(@PathVariable String token, @RequestParam(name = "password") String password){
        Member pwToReset = memberDao.findByResetToken(token);
        pwToReset.setPassword(password);
        memberDao.save(pwToReset);

        return "redirect:/login";
    }

    @PostMapping("/member/resetpassword")
    public String resetPasswordEmail(@RequestParam(name = "email") String memberEmail) throws MessagingException {
        Member member = memberDao.getByEmail(memberEmail);

        String token = UUID.randomUUID().toString();
        member.setResetToken(token);
        memberDao.save(member);
        String resetDetails = "Click to reset your password " + "<a href=\"http://localhost:8080/member/resetpassword/" + token + "\">here</a>";
//TODO: add domain + the token in an anchor tag
        emailService.sendResetPassword(member, resetDetails);

        return "redirect:/login";
    }


}
