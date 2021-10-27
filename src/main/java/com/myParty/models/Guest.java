package com.myParty.models;

import lombok.*;

import javax.persistence.*;

@ToString
@Entity
@Table(name="guests")
@NoArgsConstructor
@AllArgsConstructor

public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private long id;

    @Column(nullable = false)
    @Getter @Setter private String email;

    @Column(nullable = false, name="first_name")
    @Getter @Setter private String firstName;

    @Column(nullable = false, name="last_name")
    @Getter @Setter private String lastName;

    @Column(nullable = false)
    @Getter @Setter private RsvpStatuses rsvpStatus;

    @Column(nullable = false, name = "guest_key")
    @Getter @Setter private String guestKey;

    @ManyToOne
    @JoinColumn (name = "party_id")
    @Getter @Setter private Party party;

    @Column(name = "addtl_guests")
    @Getter @Setter private int additionalGuests;

}
