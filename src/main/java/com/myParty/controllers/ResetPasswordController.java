package com.myParty.controllers;

import com.myParty.models.Member;
import com.myParty.repositories.MemberRepository;
import com.myParty.services.EmailService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Controller
public class ResetPasswordController {
    private final MemberRepository memberDao;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public ResetPasswordController(MemberRepository memberDao, EmailService emailService, PasswordEncoder passwordEncoder){
        this.memberDao = memberDao;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
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
        String hash = passwordEncoder.encode(password);
        pwToReset.setPassword(hash);
        memberDao.save(pwToReset);

        return "redirect:/login";
    }

    @PostMapping("/member/resetpassword")
    public String resetPasswordEmail(@RequestParam(name = "email") String memberEmail, HttpServletRequest request) throws MessagingException {
        Member member = memberDao.getByEmail(memberEmail);
        String token = UUID.randomUUID().toString();
        member.setResetToken(token);
        memberDao.save(member);

        emailService.sendResetPassword(member, token, request);

        return "redirect:/login";
    }


}
