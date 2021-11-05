package com.myParty.services;

import com.myParty.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
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
    public void partyCreatedConfirmation(Party party) throws MessagingException {

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(from);
        helper.setTo(party.getOwner().getEmail());
        helper.setSubject("Your party " + party.getTitle() + " has been created!");
        boolean html = true;
        String partyDetails =
                "<img src=\"http://localhost:8080/img/MyParty.png\" >" +
                        "<h2>Your party " + party.getTitle() + " has been created.</h2>" +
                        " <br><br><i>Here are the details: </i><br>"
                        + "Description: " + party.getDescription() + "<br>"
                        + "Start Time: " + party.getStartTime() + "<br>"
                        + "End Time: " + party.getEndTime() + "<br>"
                        + "Location: " + party.getLocation().getAddressOne() + "<br>"
                        + party.getLocation().getAddressTwo() + "<br>"
                        + party.getLocation().getCity() + " " + party.getLocation().getState() + " " + party.getLocation().getZipcode() + "<br>"
                        + "Here is your custom party URL: " + party.getUrlKey();

        helper.setText(partyDetails, html);

        try {
            this.emailSender.send(message);
        } catch (MailException ex) {
            // simply log it and go on...
            System.err.println(ex.getMessage());
        }
    }


    //Method for singular invite to be included in the larger sendInvites method
    public void sendInvite(Party party, String email) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(email);
        helper.setSubject("You're Invited to " + party.getTitle() + "!");
        boolean html = true;

        String partyDetails =
                "<img src=\"http://localhost:8080/img/MyParty.png\" >" +
                        "<h2>You're Invited to " + party.getTitle() + " by " + party.getOwner().getUsername() + "</h2> "
                        + "<br><i>Here are the details: </i><br>"
                        + "Description: " + party.getDescription() + "<br>"
                        + "Start Time: " + party.getStartTime() + "<br>"
                        + "End Time: " + party.getEndTime() + "<br>"
                        + "Location: " + party.getLocation().getAddressOne() + "<br>"
                        + party.getLocation().getAddressTwo() + "<br>"
                        + party.getLocation().getCity() + " " + party.getLocation().getState() + " " + party.getLocation().getZipcode() + "<br>"
                        + "RSVP " + "<a href=\"http://localhost:8080/rsvp/" + party.getUrlKey() + "\">here</a>";

        helper.setText(partyDetails, html);

        try {
            this.emailSender.send(message);
        } catch (MailException ex) {
            // simply log it and go on...
            System.err.println(ex.getMessage());
        }
    }

    //Send inviteS
    public void sendInvites(Party party, String[] emailAddresses) throws MessagingException {
        for (int i = 0; i < emailAddresses.length; i++) {
            sendInvite(party, emailAddresses[i]);
        }
    }

    //Reset password for MEMBERS
    public void sendResetPassword(Member member, String token) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(member.getEmail());
        helper.setSubject("Reset Your myParty Password");
        boolean html = true;

        //TODO: add domain to replace localhost
        String resetDetails =
                "<img src=\"http://localhost:8080/img/MyParty.png\"><br>"
                        + "<h2>" +  member.getUsername() + ",</h2><br>"
                        + "Click to reset your password " + "<a href=\"http://localhost:8080/member/resetpassword/" + token + "\">HERE</a>";

        helper.setText(resetDetails, html);

        try {
            this.emailSender.send(message);
        } catch (MailException ex) {
            // simply log it and go on...
            System.err.println(ex.getMessage());
        }
    }


    public void sendRSVPConfirmMember(Member member, PartyMember partyMember, Party party, String partyItemsDetails) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(member.getEmail());
        helper.setSubject("Your RSVP to " + party.getTitle());
        boolean html = true;

        String rsvpDetails =
                "<img src=\"http://localhost:8080/img/MyParty.png\">"
                        + "<h2 style=\"color: red\">You are RSVP'd to " + party.getTitle() + "!</h2> "
                        + "<br><br><i>Here are the details: </i><br>"
                        + "<br>"
                        + "Description: " + party.getDescription() + "<br>"
                        + "Start Time: " + party.getStartTime() + "<br>"
                        + "End Time: " + party.getEndTime() + "<br>"
                        + "Location: <br> " + party.getLocation().getAddressOne() + "<br>"
                        + party.getLocation().getAddressTwo() + "<br>"
                        + party.getLocation().getCity() + " " + party.getLocation().getState() + " " + party.getLocation().getZipcode() + "<br>"
                        + "<br>You have signed up to bring the following: <br>" + partyItemsDetails + "<br>"
                        + "Additional Guests: " + partyMember.getAdditionalGuests() + "<br>"
                        + "View or edit your RSVP: " + "<a href=\"http://localhost:8080/rsvp/" + party.getUrlKey() + "/" + partyMember.getPartyMemberKey() + "/view" + "\">here</a>";

        helper.setText(rsvpDetails, html);

        try {
            this.emailSender.send(message);
        } catch (MailException ex) {
            // simply log it and go on...
            System.err.println(ex.getMessage());
        }

    }


    public void sendRSVPConfirmGuest(Guest guest, Party party, String partyItemsDetails) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(guest.getEmail());
        helper.setSubject("Your RSVP to " + party.getTitle());
        boolean html = true;

        String rsvpDetails =
                "<img src=\"http://localhost:8080/img/MyParty.png\">"
                        + "<h2 style=\"color: red\">You are RSVP'd to " + party.getTitle() + "!</h2> "
                        + "<br><br><i>Here are the details: </i><br>"
                        + "<br>"
                        + "Description: " + party.getDescription() + "<br>"
                        + "Start Time: " + party.getStartTime() + "<br>"
                        + "End Time: " + party.getEndTime() + "<br>"
                        + "Location:<br>" + party.getLocation().getAddressOne() + "<br>"
                        + party.getLocation().getAddressTwo() + "<br>"
                        + party.getLocation().getCity() + " " + party.getLocation().getState() + " " + party.getLocation().getZipcode() + "<br>"
                        + "<br>You have signed up to bring the following: <br>" + partyItemsDetails + "<br>"
                        + "Additional Guests: " + guest.getAdditionalGuests() + "<br>"
                        + "View or edit your RSVP: " + "<a href=\"http://localhost:8080/rsvp/" + party.getUrlKey() + "/" + guest.getGuestKey() + "/view" + "\">here</a>";

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