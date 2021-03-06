package com.myParty.services;

import com.myParty.BaseURL;
import com.myParty.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;



@Service("mailService")
public class EmailService {

    @Autowired
    public JavaMailSender emailSender;

    @Value("${spring.mail.from}")
    private String from;

    //email confirmation of party created for host
    public void partyCreatedConfirmation(Party party, HttpServletRequest request) throws MessagingException {

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(from);
        helper.setTo(party.getOwner().getEmail());
        helper.setSubject("Your party " + party.getTitle() + " has been created!");
        boolean html = true;
        String successURL = BaseURL.getBaseURL(request);
        String partyDetails =
                "<img src=\"" + successURL + "/img/MyParty.png\" >" +
                        "<h2>Your party " + party.getTitle() + " has been created.</h2>" +
                        " <br><br><i>Here are the details: </i><br>"
                        + "Description: " + party.getDescription() + "<br>"
                        + "Start Time: " + party.convertTimestamp(party.getStartTime()) + "<br>"
                        + "End Time: " + party.convertTimestamp(party.getEndTime()) + "<br>"
                        + "Location: " + party.getLocation().getAddressOne() + "<br>"
                        + party.getLocation().getAddressTwo() + "<br>"
                        + party.getLocation().getCity() + " " + party.getLocation().getState() + " " + party.getLocation().getZipcode() + "<br>"
                        + "View your party " + "<a href=\"" + successURL + "/member/" + party.getUrlKey() + "/view" + "\"><em>here</em></a>";

//TODO: *NOTE* CHANGED TO VIEW YOUR PARTY HERE
        helper.setText(partyDetails, html);

        try {
            this.emailSender.send(message);
        } catch (MailException ex) {
            // simply log it and go on...
            System.err.println(ex.getMessage());
        }
    }


    //Method for singular invite to be included in the larger sendInvites method
    public void sendInvite(Party party, String email, HttpServletRequest request) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(email);
        helper.setSubject("You're Invited to " + party.getTitle() + "!");
        boolean html = true;
        String inviteURL = BaseURL.getBaseURL(request);
        String partyDetails =
                "<img src=\"" + inviteURL + "/img/MyParty.png\" >" +
                        "<h2>You're Invited to " + party.getTitle() + " by " + party.getOwner().getUsername() + "</h2> "
                        + "<br><i>Here are the details: </i><br>"
                        + "<br>Description: " + party.getDescription() + "<br>"
                        + "Start Time: " + party.convertTimestamp(party.getStartTime()) + "<br>"
                        + "End Time: " + party.convertTimestamp(party.getEndTime()) + "<br>"
                        + "Location: " + party.getLocation().getAddressOne() + "<br>"
                        + party.getLocation().getAddressTwo() + "<br>"
                        + party.getLocation().getCity() + " " + party.getLocation().getState() + " " + party.getLocation().getZipcode() + "<br>"
                        + "RSVP " + "<a href=\"" + inviteURL + "/rsvp/" + party.getUrlKey() + "\"><em>here</em></a>";

        helper.setText(partyDetails, html);

        try {
            this.emailSender.send(message);
        } catch (MailException ex) {
            // simply log it and go on...
            System.err.println(ex.getMessage());
        }
    }

    //Send inviteS
    public void sendInvites(Party party, String[] emailAddresses, HttpServletRequest request) throws MessagingException {

        for (int i = 0; i < emailAddresses.length; i++) {
            sendInvite(party, emailAddresses[i], request);
        }
    }

    //Reset password for MEMBERS
    public void sendResetPassword(Member member, String token, HttpServletRequest request) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(member.getEmail());
        helper.setSubject("Reset Your myParty Password");
        boolean html = true;
        String resetURL = BaseURL.getBaseURL(request);
        //TODO: add domain to replace localhost
        String resetDetails =
                "<img src=\"" + resetURL + "/img/MyParty.png\"><br>"
                        + "<h2>" +  member.getUsername() + ",</h2><br>"
                        + "Click to reset your password " + "<a href=\"" + resetURL + "/member/resetpassword/" + token + "\">HERE</a>";

        helper.setText(resetDetails, html);

        try {
            this.emailSender.send(message);
        } catch (MailException ex) {
            // simply log it and go on...
            System.err.println(ex.getMessage());
        }
    }


    public void sendRSVPConfirmMember(Member member, PartyMember partyMember, Party party, String partyItemsDetails, HttpServletRequest request) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(member.getEmail());
        helper.setSubject("Your RSVP to " + party.getTitle());
        boolean html = true;

        String rsvpURL = BaseURL.getBaseURL(request);
        String rsvpDetails =
                "<img src=\"" + rsvpURL + "/img/MyParty.png\">"
                        + "<h2 style=\"color: red\">You are RSVP'd to " + party.getTitle() + "!</h2> "
                        + "<br><br><i>Here are the details: </i><br>"
                        + "<br>"
                        + "Description: " + party.getDescription() + "<br>"
                        + "Start Time: " + party.convertTimestamp(party.getStartTime()) + "<br>"
                        + "End Time: " + party.convertTimestamp(party.getEndTime()) + "<br>"
                        + "Location: <br> " + party.getLocation().getAddressOne() + "<br>"
                        + party.getLocation().getAddressTwo() + "<br>"
                        + party.getLocation().getCity() + " " + party.getLocation().getState() + " " + party.getLocation().getZipcode() + "<br>"
                        + "<br>You have signed up to bring the following: <br>" + partyItemsDetails + "<br>"
                        + "Additional Guests: " + partyMember.getAdditionalGuests() + "<br>"
                        + "View or edit your RSVP: " + "<a href=" + rsvpURL + "/rsvp/" + party.getUrlKey() + "/member/" + partyMember.getPartyMemberKey() + "/view" + "><em>here</em></a>";

        helper.setText(rsvpDetails, html);

        try {
            this.emailSender.send(message);
        } catch (MailException ex) {
            // simply log it and go on...
            System.err.println(ex.getMessage());
        }

    }


    public void sendRSVPConfirmGuest(Guest guest, Party party, String partyItemsDetails, HttpServletRequest request) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(guest.getEmail());
        helper.setSubject("Your RSVP to " + party.getTitle());
        boolean html = true;
        String rsvpGuestURL = BaseURL.getBaseURL(request);

        String rsvpDetails =
                "<img src=\"" + rsvpGuestURL + "/img/MyParty.png\">"
                        + "<h2 style=\"color: red\">You are RSVP'd to " + party.getTitle() + "!</h2> "
                        + "<br><br><i>Here are the details: </i><br>"
                        + "<br>"
                        + "Description: " + party.getDescription() + "<br>"
                        + "Start Time: " + party.convertTimestamp(party.getStartTime()) + "<br>"
                        + "End Time: " + party.convertTimestamp(party.getEndTime()) + "<br>"
                        + "Location:<br>" + party.getLocation().getAddressOne() + "<br>"
                        + party.getLocation().getAddressTwo() + "<br>"
                        + party.getLocation().getCity() + " " + party.getLocation().getState() + " " + party.getLocation().getZipcode() + "<br>"
                        + "<br>You have signed up to bring the following: <br>" + partyItemsDetails + "<br>"
                        + "Additional Guests: " + guest.getAdditionalGuests() + "<br>"
                        + "View or edit your RSVP: " + "<a href=" + rsvpGuestURL + "/rsvp/" + party.getUrlKey() + "/" + guest.getGuestKey() + "/view" + ">here</a>";

        helper.setText(rsvpDetails, html);
        try {
            this.emailSender.send(message);
        } catch (MailException ex) {
            // simply log it and go on...
            System.err.println(ex.getMessage());
        }
    }
}
//    public void sendRSVPConfirmGuest(Guest guest, String subject, String customMessage) throws MessagingException {
//        MimeMessage message = emailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//        helper.setFrom(from);
//        helper.setTo(guest.getEmail());
//        helper.setSubject(subject);
//        boolean html = true;
//        helper.setText(customMessage, html);
//
//        try {
//            this.emailSender.send(message);
//        } catch (MailException ex) {
//            // simply log it and go on...
//            System.err.println(ex.getMessage());
//        }
//    }


//Send Invitations to guests
//    public void sendInvites(Party party, String email) throws MessagingException {
//        MimeMessage message = emailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//        helper.setFrom(from);
//        helper.setTo(email);
//        helper.setSubject("You're invited to " + party.getTitle() + "!");
//        boolean html = true;
//
//        String partyDetails =
//                "<h2>You're Invited to " + party.getTitle() + " by " + party.getOwner().getFirstName() + "</h2> " +
//                        "<br><i>Here are the details: </i><br>"
//                        + "Description: " + party.getDescription() + "<br>"
//                        + "Start Time: " + party.getStartTime() + "<br>"
//                        + "End Time: " + party.getEndTime() + "<br>"
//                        + "Location: " + party.getLocation().getAddressOne() + "<br>"
//                        + party.getLocation().getAddressTwo() + "<br>"
//                        + party.getLocation().getCity() + " " + party.getLocation().getState() + " " + party.getLocation().getZipcode() + "<br>"
//                        + "RSVP " + "<a href=\"http://localhost:8080/rsvp/" + party.getUrlKey() + "\">here</a>";
//
//        helper.setText(partyDetails, html);
//
//        try {
//            this.emailSender.send(message);
//        } catch (MailException ex) {
//            // simply log it and go on...
//            System.err.println(ex.getMessage());
//        }
//    }

//    public void sendRSVPConfirmMember(Member member, String subject, String customMessage) throws MessagingException {
//        MimeMessage message = emailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//        helper.setFrom(from);
//        helper.setTo(member.getEmail());
//        helper.setSubject(subject);
//        boolean html = true;
//        helper.setText(customMessage, html);
//
//        try {
//            this.emailSender.send(message);
//        } catch (MailException ex) {
//            // simply log it and go on...
//            System.err.println(ex.getMessage());
//        }
//    }