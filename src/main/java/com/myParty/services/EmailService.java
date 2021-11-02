package com.myParty.services;

import com.myParty.models.Member;
import com.myParty.models.Party;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service("mailService")
public class EmailService {

    @Autowired
    public JavaMailSender emailSender;

    @Value("${spring.mail.from}")
    private String from;

    //email confirmation of party created for host
    public void prepareAndSend(Party party, String subject, String body) throws MessagingException {

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(from);
        helper.setTo(party.getOwner().getEmail());
        helper.setSubject(subject);
        boolean html = true;
        helper.setText(body, html);

        try{
            this.emailSender.send(message);
        }
        catch (MailException ex) {
            // simply log it and go on...
            System.err.println(ex.getMessage());
        }
    }

    //Send Invitations to guests
    public void sendInvites(String subject,  String email, String customMessage) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(from);
        helper.setTo(email);
        helper.setSubject(subject);
        boolean html = true;
        helper.setText(customMessage, html);

        try{
            this.emailSender.send(message);
        }
        catch (MailException ex) {
            // simply log it and go on...
            System.err.println(ex.getMessage());
        }
    }

    public void sendResetPassword(Member member, String customMessage) throws MessagingException {
    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(member.getEmail());
        helper.setSubject("Reset Your myParty Password");
        boolean html = true;
        helper.setText(customMessage, html);

        try{
        this.emailSender.send(message);
    }
        catch (MailException ex) {
        // simply log it and go on...
        System.err.println(ex.getMessage());
    }
}



}